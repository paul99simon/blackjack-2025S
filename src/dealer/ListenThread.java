package dealer;

import common.Config;
import common.GameProtocoll;
import common.Message;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;

import org.javatuples.Pair;

public class ListenThread extends Thread
{
    private Dealer dealer;

    public ListenThread(Dealer dealer)
    {
        this.dealer = dealer;
    }

    public void run()
    {
        byte[] buffer = new byte[Config.BUFFER_LENGTH];

        while(true)
        {
            try
            {
                DatagramPacket packet = new DatagramPacket(buffer, Config.BUFFER_LENGTH);
                dealer.socket.receive(packet);
                Message message = new Message(packet);
                Message ackMessage = message.acknowledgeMessage(dealer.id, GameProtocoll.DEALER);
                DatagramPacket ackPacket = new DatagramPacket(ackMessage.getBytes(), ackMessage.message_length);
                dealer.socket.send(ackPacket);

                switch (message.message_type) {
                    case GameProtocoll.ACKNOWLEDGE:
                        if(dealer.acknowledge.get(message.message_id) != null)
                        {
                            dealer.acknowledge.replace(message.message_id, true);
                        }
                        break;
                    case GameProtocoll.REGISTRATION:
                        InetAddress addr = packet.getAddress();
                        int port = packet.getPort();
                        switch (message.sender_type)
                        {
                            case GameProtocoll.COUNTER:
                                dealer.counter = new Pair<InetAddress,Integer>(addr, port);
                                break;
                            case GameProtocoll.PLAYER:
                                dealer.players.put(message.sender_id, new Pair<InetAddress, Integer>(addr, port));
                                break;
                        }
                        break;
                    case GameProtocoll.DISCONNECT:
                        break;
                    case GameProtocoll.BET:
                        break;
                    default:
                        break;
                }
            }
            catch(IOException e)
            {
                System.out.println(e.getMessage());
            }    
        }
    }
}

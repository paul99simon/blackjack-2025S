package counter;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.javatuples.Pair;

import common.Card;
import common.Config;
import common.Config.Game;
import common.Hand;
import common.Id;
import common.Message;
import common.Protocoll;
import common.UDP_Endpoint;
import common.StatisticsEntry;

public class Counter extends UDP_Endpoint
{

    public Pair<InetAddress, Integer> dealer;
    public Map<Id, Pair<InetAddress, Integer>> players;

    public List<StatisticsEntry> statistics;

    public Counter()
    {
        super(Protocoll.Header.Role.COUNTER);
    }

    @Override
    public void listen()
    {
        byte[] buffer = new byte[Config.Network.BUFFER_LENGTH];

        while(true)
        {
            try
            {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                Message message = new Message(packet);
                System.out.println("received:" + message);
                InetAddress addr = packet.getAddress();
                int port = packet.getPort();
                acknowledge(addr, port, message);

                switch (message.message_type) {
                    case Protocoll.Header.Type.ACK:
                        synchronized(acknowledge)
                        {
                            acknowledge.put(message.message_id, message);
                        }
                        break;
                    case Protocoll.Header.Type.SYN:
                        if(message.sender_type == Protocoll.Header.Role.COUNTER) break;

                        switch (message.sender_type)
                        {
                            case Protocoll.Header.Role.DEALER:
                                dealer = new Pair<InetAddress,Integer>(addr, port);
                                break;
                            case Protocoll.Header.Role.PLAYER:
                                players.put(message.sender_id, new Pair<InetAddress, Integer>(addr, port));
                                break;
                            default:
                                break;
                        }

                        Message synMessage = Message.synMessage(id, Protocoll.Header.Role.COUNTER);
                        Thread synThread = new Thread(() ->
                        {
                            try
                            {
                                send(addr, port, synMessage);
                            }
                            catch (IOException e)
                            {
                                System.out.println(e.getMessage());
                            }
                        });
                        synThread.start();
                        break;
                    case Protocoll.Header.Type.FIN:
                        if(message.message_type == Protocoll.Header.Role.DEALER) break;
                        break;
                    case Protocoll.Header.Type.AMOUNT:
                        if(message.sender_type != Protocoll.Header.Role.PLAYER) break;
                        if(game.currentPhase != Game.BETTING_PHASE) break;

                        game.placeBet(message.sender_id, message.amount);
                        System.out.println(game.allBetsPlaced());
                        if(game.allBetsPlaced())
                        {
                            synchronized(game.lock)
                            {
                                gameThread.waiting=true;
                                game.lock.notify();
                            }
                        }
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

    @Override
    public void processInput() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'processInput'");
    }

    @Override
    protected void printHelp() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'printHelp'");
    }

    private int bestAction(Card upCard, Stack<Card> draw_stack, Hand hand)
    {
        
    }
}

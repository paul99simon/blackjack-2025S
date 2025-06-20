package player;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.LinkedList;
import java.util.List;

import org.javatuples.Triplet;

import common.Card;
import common.Config;
import common.Hand;
import common.Id;
import common.Message;
import common.Protocoll;
import common.UDP_Endpoint;

public class Player extends UDP_Endpoint
{
    public Triplet<Id, InetAddress, Integer> dealer;
    public Triplet<Id, InetAddress, Integer> counter;

    public List<Hand> hands;
    public int bankroll;

    public Player()
    {
        super(Protocoll.Header.Role.PLAYER);
        this.hands = new LinkedList<>();
        this.bankroll = Config.Game.START_BANKROLL;
    }

    @Override
    public void listen() {
        
        byte[] buffer = new byte[Config.Network.BUFFER_LENGTH];

        while(true)
        {
            try
            {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                Message message = new Message(packet);
                InetAddress addr = packet.getAddress();
                int port = packet.getPort();

                System.out.println("received:" + message);
                acknowledge(addr, port, message);

                switch (message.message_type) {
                    case Protocoll.Header.Type.ACK:
                        synchronized(acknowledge)
                        {
                            acknowledge.put(message.message_id, message);
                        }
                        break;
                    case Protocoll.Header.Type.SYN:
                        if(message.sender_type == Protocoll.Header.Role.PLAYER) break;
                        switch (message.sender_type)
                        {
                            case Protocoll.Header.Role.COUNTER:
                                counter = new Triplet<Id, InetAddress,Integer>(message.sender_id, addr, port);
                                break;
                            case Protocoll.Header.Role.DEALER:
                                dealer = new Triplet<Id, InetAddress, Integer>(message.sender_id, addr, port);
                                break;
                            default:
                                break;
                        }
                        break;
                    case Protocoll.Header.Type.FIN:
                        break;
                    case Protocoll.Header.Type.AMOUNT:
                        int wager = getBestWager();
                        hands.add(new Hand(id, wager));
                        Message betMessage = Message.amountMessage(id, role, wager);
                        Thread send = new Thread( () -> 
                        {
                            try
                            {
                                send(dealer.getValue1(), dealer.getValue2(), betMessage);
                            }
                            catch(IOException e)
                            {
                                System.out.println(e.getMessage());
                            }
                        });
                        send.start();
                        break;
                    case Protocoll.Header.Type.CARD:
                        Card card = new Card(message.payload[0]);
                        Hand hand = hands.get(0);
                        hand.cards.add(card);
                        if(hand.size() == 2)
                        {
                            System.out.println(hand.toString());
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
        try
        {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

            while(true)
            {
                String input = reader.readLine();

                if(input.startsWith("register"))
                {
                    String[] command = input.split(" ", 3);
                    if(command.length != 3)
                    {
                        printHelp();
                    }
                    
                    InetAddress addr = InetAddress.getByName(command[1]);
                    int port = Integer.valueOf(command[2]);

                    Message synMessage = Message.synMessage(id, Protocoll.Header.Role.PLAYER);
                    send(addr, port, synMessage);
                    if(acknowledge.get(synMessage.message_id) != null)
                    {
                        acknowledge.remove(synMessage.message_id);
                    }
                }
                else
                {
                    printHelp();
                }
            }
        }
        catch (IOException e)
        {
            System.out.printf("Exception: %s\n",e.getMessage());
        }
    }

    @Override
    protected void printHelp()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("usage:\n");
        builder.append("    register");
        System.out.println(builder.toString());
    } 

    public int getBestAction()
    {
        return 0;
    }

    public int getBestWager()
    {
        return 1;        
    }

}

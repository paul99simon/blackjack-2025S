package player;

import java.io.IOException;
import java.net.InetAddress;
import java.util.List;

import org.javatuples.Triplet;

import common.Card;
import common.Context;
import common.Message;
import common.MessageIdentifier;
import common.Protocoll;
import common.endpoint.InstanceId;
import common.endpoint.UDP_Endpoint;

public class Player extends UDP_Endpoint
{
    public Triplet<InstanceId, InetAddress, Integer> dealer;
    public Triplet<InstanceId, InetAddress, Integer> counter;

    public List<Card> hand;
    public int bankroll;

    public Player()
    {
        super(Protocoll.Header.Role.PLAYER);
        this.bankroll = Context.Game.START_BANKROLL;
    }

    @Override
    public void processMessage(InetAddress sender_addr, int sender_port, Message message)
    {
        switch (message.message_type)
        {
            case Protocoll.Header.Type.SYN:
                if(message.sender_role == Protocoll.Header.Role.PLAYER) break;
                switch (message.sender_role)
                {
                    case Protocoll.Header.Role.COUNTER:
                        counter = new Triplet<InstanceId, InetAddress,Integer>(message.sender_id, addr, port);
                        break;
                    case Protocoll.Header.Role.DEALER:
                        dealer = new Triplet<InstanceId, InetAddress, Integer>(message.sender_id, addr, port);
                        break;
                    default:
                        break;
                }
                break;
            case Protocoll.Header.Type.FIN:
                break;
            case Protocoll.Header.Type.BET:
                int wager = getBestWager();
                Message betMessage = Message.betMessage(id, role, wager);
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
            case Protocoll.Header.Type.CARDS:

                break;
            default:
                break;
        }
    }

    @Override
    public void processInput(String input)
    {
        try
        {
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
                MessageIdentifier messageIdentifier = synMessage.getIdentifier();
                send(addr, port, synMessage);
                synchronized(acknowledged_Messages)
                {
                    if(acknowledged_Messages.contains(messageIdentifier))
                    {
                        acknowledged_Messages.remove(messageIdentifier);
                    }
                }

            }
            else
            {
                printHelp();
            }
        }
        catch(IOException e)
        {
            System.out.println(e.getMessage());
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

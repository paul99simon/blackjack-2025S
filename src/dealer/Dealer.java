package dealer;

import common.Config;
import common.Id;
import common.Message;
import common.Protocoll;
import common.UDP_Endpoint;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.javatuples.Pair;

public class Dealer extends UDP_Endpoint
{
    public Pair<InetAddress, Integer> counter;
    public Map<Id, Pair<InetAddress, Integer>> players;
        
    public Game game;
    public GameThread gameThread;

    public Dealer()
    {
        this(1);
    }

    public Dealer(int deckCount) throws IllegalArgumentException
    {   
        super(Protocoll.Header.Role.DEALER);
        this.counter = null;
        this.players = Collections.synchronizedMap(new HashMap<>());
        game = new Game(this);
        game.setDeckCount(deckCount);
        gameThread = new GameThread(this, game);
        gameThread.start();
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
                        if(message.sender_type == Protocoll.Header.Role.DEALER) break;

                        switch (message.sender_type)
                        {
                            case Protocoll.Header.Role.COUNTER:
                                counter = new Pair<InetAddress,Integer>(addr, port);
                                break;
                            case Protocoll.Header.Role.PLAYER:
                                players.put(message.sender_id, new Pair<InetAddress, Integer>(addr, port));
                                break;
                            default:
                                break;
                        }

                        Message synMessage = Message.synMessage(id, Protocoll.Header.Role.DEALER);
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
                        if(game.allBetsPlaced())
                        {
                            synchronized(game.lock)
                            {
                                gameThread.ready=true;
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
    public void processInput()
    {
        try
        {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

            while(true)
            {
                String input = reader.readLine();
                if(input.equals("start"))
                {
                    game.paused = false;
                    synchronized(game.lock) {game.lock.notify();}
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
        builder.append("    start");
        System.out.println(builder.toString());
    }
    
    //------------------------------------------------------------------------------------------------------------------------------------
    // Card related stuff
    //------------------------------------------------------------------------------------------------------------------------------------

    
}

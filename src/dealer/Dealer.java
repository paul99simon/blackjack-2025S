package dealer;

import common.Card;
import common.Config;
import common.Hand;
import common.Id;
import common.MaxRetriesExceededException;
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
import java.util.Stack;

import org.javatuples.Pair;

public class Dealer extends UDP_Endpoint
{
    public Pair<InetAddress, Integer> counter;
    public Map<Id, Pair<InetAddress, Integer>> players;
        
    public int cut;
    public int deckCount;
    public boolean deckCountChanged;
    public Stack<Card> draw_stack;
    public Stack<Card> disc_stack;
    
    public Game game;

    public Dealer()
    {
        this(1);
    }

    public Dealer(int deckCount) throws IllegalArgumentException
    {   
        super(Protocoll.Header.Role.DEALER);
        this.counter = null;
        this.players = Collections.synchronizedMap(new HashMap<>());

        setDeckCount(deckCount);;
        this.draw_stack = new Stack<>();
        this.disc_stack = new Stack<>();
        game = new Game(this);
    } 

    @Override
    public void listen()
    {
        byte[] buffer = new byte[Config.BUFFER_LENGTH];

        while(true)
        {
            try
            {
                DatagramPacket packet = new DatagramPacket(buffer, Config.BUFFER_LENGTH);
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
                            Thread drawThread = new Thread(() -> game.startDrawPhase());
                            drawThread.start();
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
                    game.startBettinPhase();
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
    
    public void sendBettingRequest()
    {
        for(Hand hand : game.hands)
        {
            InetAddress addr = players.get(hand.owner).getValue0();
            int port = players.get(hand.owner).getValue1();
            Message bettingRequest = Message.amountMessage(id, Protocoll.Header.Role.DEALER, 0);
            try
            {
                send(addr, port,  bettingRequest);
            }
            catch(IOException e)
            {
                System.out.println(e.getMessage());
            }
            catch(MaxRetriesExceededException e)
            {
                players.remove(hand.owner);
                game.hands.remove(hand);
            }
        }
    }

    public void sendInitialCards()
    {
        for(Hand hand : game.hands)
        {
            Card card1 = top();
            Card card2 = top();
            Message message1 = Message.cardMessage(id, Protocoll.Header.Role.DEALER, true, card1);
            Message message2 = Message.cardMessage(id, Protocoll.Header.Role.DEALER, true, card2);
            
            InetAddress addr = players.get(hand.owner).getValue0();
            int port = players.get(hand.owner).getValue1();
            try
            {
                send(addr, port, message1);
                send(addr, port, message2);
                hand.cards.add(card1);
                hand.cards.add(card2);
            }
            catch(IOException e)
            {
                System.out.println(e.getMessage());
            }
            catch(MaxRetriesExceededException e)
            {
                draw_stack.push(card1);
                draw_stack.push(card2);
                game.hands.remove(hand);
                players.remove(hand.owner);
            }
        }
    }

    //------------------------------------------------------------------------------------------------------------------------------------
    // Card related stuff
    //------------------------------------------------------------------------------------------------------------------------------------

    public Card top()
    {
        if(cut == 0)
        {
            shuffle();
        }
        cut--;
        return draw_stack.pop();
    }

    public void setDeckCount(int deckCount) throws IllegalArgumentException
    {
        if(1 > deckCount || 8 < deckCount) throw new IllegalArgumentException("deckount must be between 1 and 8");
        this.deckCount = deckCount;
        this.deckCountChanged = true;
    }

    public void shuffle()
    {
        int[] arr;
        
        if(deckCountChanged)
        {
            arr = new int[deckCount * 52];
            for(int j = 0; j < deckCount ; j++)
            {
                for(int i = 0; i < 52; i++)
                {
                    arr[j * 52 + i] = i + 1;
                }
            }
        }
        else
        {
            arr = new int[draw_stack.size() + disc_stack.size()];
            int index = 0;
            while(!draw_stack.empty())
            {
                arr[index++] = draw_stack.pop().value;
            }

            while(!disc_stack.empty())
            {
                arr[index++] = disc_stack.pop().value;
            }
        }

        int maxCut = (int) (0.75 * arr.length);
        int minCut = (int) (0.50 * arr.length);
        cut = (int) (Math.random() * (maxCut - minCut) + minCut);

        //Shuffle
        draw_stack.clear();
        disc_stack.clear();

        for(int i = arr.length-1; i >= 0 ; i--)
        {
            int swap = (int) (Math.random() * i); 
            int temp = arr[i];
            arr[i] = arr[swap];
            arr[swap] = temp;
        }

        for(int i = 0; i < arr.length; i++)
        {
            draw_stack.add(new Card((byte) arr[i]));
        }
    }
}

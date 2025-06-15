package dealer;

import common.Card;
import common.Id;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Queue;
import java.util.Stack;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.javatuples.Pair;

public class Dealer implements Runnable
{
    private final Id id;
    
    public DatagramSocket socket;
    public Pair<InetAddress, Integer> counter;
    public Map<Id, Pair<InetAddress, Integer>> players;

    private int deckCount;
    private int cut;
    private Stack<Card> draw_stack;
    private Stack<Card> disc_stack;
    private Game game;

    public Dealer()
    {
        this(1);
    }

    public Dealer(int deckCount) throws IllegalArgumentException
    {   
        this.id = new Id();
        changeDeckCount(deckCount);
        
        try
        {
            socket = new DatagramSocket();
            socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
            socket.disconnect();
            System.out.printf("udp endpoint is (%s %d)\n", socket.getLocalAddress().getHostAddress(), socket.getLocalPort());
        }
        catch(UnknownHostException e)
        {
            System.out.println(e.getMessage());
        }
        catch(SocketException e)
        {
            System.out.println(e.getMessage());
        }

        stack = new Stack<Card>();
        prepareStack();
    }

    public void changeDeckCount(int deckCount) throws IllegalArgumentException
    {
        if(8 < deckCount || 1 > deckCount ) throw new IllegalArgumentException("deck count must be between 1 and 8");
        this.deckCount = deckCount;   
    }

    private void prepareStack()
    {
        stack.clear();

        //prepare new decks
        int[] arr = new int[deckCount * 52];

        for(int j = 0; j < deckCount ; j++)
        {
            for(int i = 0; i < 52; i++)
            {
                arr[j * 52 + i] = i + 1;
            }
        }

        //Shuffle
        for(int i = arr.length-1; i >= 0 ; i--)
        {
            int swap = (int) (Math.random() * i); 
            int temp = arr[i];
            arr[i] = arr[swap];
            arr[swap] = temp;
        }

        for(int i = 0; i < arr.length; i++)
        {
            stack.add(new Card(arr[i]));
        }
    } 

    public void run()
    {

    }
}

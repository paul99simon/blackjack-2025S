package dealer;

import common.Card;
import common.Id;
import common.UDP_Endpoint;

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
        super();
        this.counter = null;
        this.players = Collections.synchronizedMap(new HashMap<>());

        setDeckCount(deckCount);;
        this.draw_stack = new Stack<>();
        this.disc_stack = new Stack<>();
        this.listenThread = new ListenThread(this);
        this.inputThread = new InputThread(this);
        game = new Game(this);
    } 

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
            draw_stack.add(new Card(arr[i]));
        }
    }
}

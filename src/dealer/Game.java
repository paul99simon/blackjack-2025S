package dealer;

import java.io.IOException;
import java.net.InetAddress;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.javatuples.Pair;
import org.javatuples.Triplet;

import common.Card;
import common.GameProtocoll;
import common.Id;
import common.Message;

public class Game
{

    public static final int BETTING_PHASE = 1;
    public static final int DRAW_PHASE = 2;
    public static final int PLAY_PHASE = 3;

    private Dealer dealer;

    public int currentPhase;

    public boolean inProgress;
    public int deckCount;

    public Queue<Triplet<Id, List<Card>, Integer>> hands;
    public Triplet<Id, List<Card>, Integer> currentPlayer;    

    public Game(Dealer dealer)
    {
        this.inProgress = false;
        this.hands = new ConcurrentLinkedQueue<>();
    }

    public void reStart()
    {
        this.inProgress = true;

        this.currentPhase = Game.BETTING_PHASE;
        for(Id id : this.dealer.players.keySet())
        {
            hands.add( new Triplet<Id, List<Card>, Integer>(id, new LinkedList<Card>(), 0) );
        }
        sendBettingRequest();
        currentPlayer = hands.poll();
    }

    private void sendBettingRequest()
    {
        for(Entry<Id, Pair<InetAddress, Integer>> player : dealer.players.entrySet())
        {
            Message bettingRequest = new Message(dealer.id, GameProtocoll.DEALER, GameProtocoll.BET, "" );
            InetAddress addr = player.getValue().getValue0();
            int port = player.getValue().getValue1();
            try
            {
                this.dealer.send(addr, port,  bettingRequest);
                if(!dealer.acknowledge.get(bettingRequest.message_id))
                {
                    dealer.players.remove(player.getKey());
                }
                dealer.acknowledge.remove(bettingRequest.message_id);
            }
            catch(IOException e)
            {
                System.out.println(e.getMessage());
            }
        }
    }

}

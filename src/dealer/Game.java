package dealer;

import java.net.InetAddress;
import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;

import common.Hand;
import common.Id;
import common.Message;
import common.Protocoll;

public class Game
{

    public static final int WAITING_PHASE = 0;
    public static final int BETTING_PHASE = 1;
    public static final int DRAW_PHASE = 2;
    public static final int PLAYER_PHASE = 3;
    public static final int DEALER_PHASE = 4; 


    private Dealer dealer;

    public int currentPhase;

    public int deckCount;

    public Deque<Hand> hands;
    public Hand currentPlayer;    
   
    public Game(Dealer dealer)
    {
        this.dealer = dealer;
        this.currentPhase = WAITING_PHASE;
        this.hands = new ConcurrentLinkedDeque<>();
    }

    public boolean inProgress()
    {
        return currentPhase != WAITING_PHASE;
    }

    public void startBettinPhase()
    {
        if(currentPhase != WAITING_PHASE) throw new GameException("WAITING_PHASE must preceed BETTING_PHASE");
        this.currentPhase = BETTING_PHASE;
        for(Id id : dealer.players.keySet())
        {
            hands.add( new Hand(id));
        }
        dealer.sendBettingRequest();
    }

    public void startDrawPhase()
    {
        if(currentPhase != BETTING_PHASE) throw new GameException("BETTING_PHASE must preceed DRAW_PHASE");
        currentPhase = DRAW_PHASE;
        dealer.sendInitialCards(); 
        startPlayerPhase();
    }

    public void startPlayerPhase()
    {
        if(currentPhase != DRAW_PHASE) throw new GameException("BETTING_PHASE must preceed DRAW_PHASE");
        currentPhase = PLAYER_PHASE;
        while(!hands.isEmpty())
        {
            currentPlayer = hands.removeFirst();
            Message actionRequest = Message.actionRequest(dealer.id, Protocoll.Header.Role.DEALER);
            InetAddress addr = dealer.players.get(currentPlayer.owner).getValue0();
            int port = dealer.players.get(currentPlayer.owner).getValue1();
            dealer.send(addr, port, actionRequest);
        }
        startDealerPhase();
    }

    public void startDealerPhase()
    {

    }

    public void placeBet(Id owner, int amount)
    {
        for(Hand hand : hands)
        {
            if(hand.owner.equals(owner))
            {
                hand.wager = amount;
            }
        }
    }

    public boolean allBetsPlaced()
    {
        long missingBets = hands.stream().filter(h -> h.wager == 0).count();
        return missingBets == 0;
    }

   

}

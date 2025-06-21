package dealer;

import java.io.IOException;
import java.net.InetAddress;
import java.util.LinkedList;
import java.util.List;

import common.Card;
import common.Message;
import common.Protocoll;
import common.endpoint.InstanceId;
import common.endpoint.MaxRetriesExceededException;
import common.Context;

public class GameThread extends Thread
{
    public volatile boolean ready = false;
    public Dealer dealer;
    public Game game;

    public GameThread(Dealer dealer, Game game)
    {
        this.dealer = dealer;
        this.game = game;
    }

    @Override
    public void run()
    {
        while(true)
        {
            try
            {
                if(game.paused)
                {
                    synchronized(game.lock)
                    {
                        game.lock.wait();
                    }   
                }
                game.round_id++;
                bettinPhase();
                synchronized(game.lock)
                {
                    while(!ready)
                    {
                        game.lock.wait(Context.Game.BETTING_PHASE_DURATION);
                    }
                    ready = false;
                }

                synchronized(game.active_hands)
                {
                    for(Hand hand : game.active_hands)
                    {
                        if(hand.wager == 0)
                        {
                            game.active_hands.remove(hand);
                            synchronized(dealer.players)
                            {
                                dealer.players.remove(hand.owner_id);
                            }
                        }
                    }
                }
                drawPhase();
                playerPhase();
                dealerPhase();
                game.currentPhase = Game.WAITING_PHASE;
                
            }
            catch(InterruptedException e)
            {
                System.out.println(e.getMessage());
            }
        }
    }

    public void bettinPhase()
    {
        if(game.currentPhase != Game.WAITING_PHASE) throw new GameException("WAITING_PHASE must preceed BETTING_PHASE");
        game.currentPhase = Game.BETTING_PHASE;
        
        synchronized(dealer.players)
        {
            for(InstanceId id : dealer.players.keySet())
            {
                Hand hand = new Hand(game.round_id, id);
                game.active_hands.add(hand);
                InetAddress addr = dealer.players.get(id).getValue0();
                int port = dealer.players.get(id).getValue1();
                Message bettingRequest = Message.betMessage(id, Protocoll.Header.Role.DEALER, 0);
                try
                {
                    dealer.send(addr, port, bettingRequest);
                }
                catch(IOException e)
                {
                    System.out.println(e.getMessage());
                }
                catch(MaxRetriesExceededException e)
                {
                    dealer.players.remove(id);
                    game.active_hands.remove(hand);
                }
            }
        }
    }

    public void drawPhase()
    {
        if(game.currentPhase != Game.BETTING_PHASE) throw new GameException("BETTING_PHASE must preceed DRAW_PHASE");
        game.currentPhase = Game.DRAW_PHASE;

        InetAddress counter_addr;
        int counter_port;
        synchronized(dealer.counter)
        {
            counter_addr = dealer.counter.getValue0();
            counter_port = dealer.counter.getValue1();
        }

        for(Hand hand : game.active_hands)
        {
            List<Card> cards = new LinkedList<>();
            cards.add(game.drawCard());
            cards.add(game.drawCard());
            Message message = Message.cardsMessage(dealer.id, Protocoll.Header.Role.DEALER, cards);
            
            InetAddress player_addr;
            int player_port;

            synchronized(dealer.players)
            {
                player_addr = dealer.players.get(hand.owner_id).getValue0();
                player_port = dealer.players.get(hand.owner_id).getValue1();
            }

            try
            {
                dealer.send(counter_addr, counter_port, message);
                dealer.send(player_addr, player_port, message);
                hand.cards.addAll(cards);
            }
            catch(IOException e)
            {
                System.out.println(e.getMessage());
            }
            catch(MaxRetriesExceededException e)
            {
                game.draw_stack.addAll(cards);
                game.active_hands.remove(hand);
                dealer.players.remove(hand.owner_id);
            }
        }
        game.dealer_hand.add(game.drawCard());
    }

    public void playerPhase()
    {
        if(game.currentPhase != Game.DRAW_PHASE) throw new GameException("DRAW_PHASE must preceed PLAYER_PHASE");
        game.currentPhase = Game.PLAYER_PHASE;
        
        while(!game.active_hands.isEmpty())
        {
            game.currentHand = game.active_hands.removeFirst();                
            Message actionRequest = Message.actionRequest(dealer.id, Protocoll.Header.Role.DEALER, game.currentHand);
            InetAddress addr;
            int port;
            synchronized(dealer.players)
            {
                addr = dealer.players.get(game.currentHand.owner_id).getValue0();
                port = dealer.players.get(game.currentHand.owner_id).getValue1();
            }
            while(game.currentHand.active)
            {
                try
                {
                    dealer.send(addr, port, actionRequest);
                    synchronized(game.lock){game.lock.wait(Context.Game.PLAYER_MOVE_DURATION);}
                    if(! game.currentHand.actionPerformed)
                    {
                        synchronized(dealer.players)
                        {
                            dealer.players.remove(game.currentHand.owner_id);
                            game.disc_stack.addAll(game.currentHand.cards);
                        }
                    }
                }
                catch(IOException e)
                {
                    System.out.println(e.getMessage());
                }
                catch(InterruptedException e)
                {
                    System.out.println(e.getMessage());
                }
                catch(MaxRetriesExceededException e)
                {

                }
            }
        }
    }

    public void dealerPhase()
    {
        if(game.currentPhase != Game.PLAYER_PHASE) throw new GameException("PLAYER_PHASE must preceed DEALER_PHASE");
        game.currentPhase = Game.DEALER_PHASE;
    }

}
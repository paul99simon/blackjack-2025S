package dealer;

import java.io.IOException;
import java.net.InetAddress;

import common.Card;
import common.Hand;
import common.Id;
import common.MaxRetriesExceededException;
import common.Message;
import common.Protocoll;
import common.Config;

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
                bettinPhase();
                synchronized(game.lock)
                {
                    while(!ready)
                    {
                        game.lock.wait(Config.Game.BETTING_PHASE_DURATION);
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
                                dealer.players.remove(hand.owner);
                            }
                        }
                    }
                }
                drawPhase();
                playerPhase();
                startDealerPhase();
                
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
            for(Id id : dealer.players.keySet())
            {
                Hand hand = new Hand(id);
                game.active_hands.add( hand);
                InetAddress addr = dealer.players.get(id).getValue0();
                int port = dealer.players.get(id).getValue1();
                Message bettingRequest = Message.amountMessage(id, Protocoll.Header.Role.DEALER, 0);
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
        
        for(Hand hand : game.active_hands)
        {
            Card card1 = game.drawCard();
            Card card2 = game.drawCard();
            Message message1 = Message.cardMessage(dealer.id, Protocoll.Header.Role.DEALER, hand.id, card1);
            Message message2 = Message.cardMessage(dealer.id, Protocoll.Header.Role.DEALER, hand.id, card2);
            
            InetAddress addr;
            int port;

            synchronized(dealer.players)
            {
                addr = dealer.players.get(hand.owner).getValue0();
                port = dealer.players.get(hand.owner).getValue1();
            }
            try
            {
                dealer.send(addr, port, message1);
                dealer.send(addr, port, message2);
                hand.cards.add(card1);
                hand.cards.add(card2);
            }
            catch(IOException e)
            {
                System.out.println(e.getMessage());
            }
            catch(MaxRetriesExceededException e)
            {
                game.draw_stack.push(card1);
                game.draw_stack.push(card2);
                game.active_hands.remove(hand);
                dealer.players.remove(hand.owner);
            }
        }
        game.dealer_hand.add(game.drawCard());
    }

    public void playerPhase()
    {
        if(game.currentPhase != Game.DRAW_PHASE) throw new GameException("BETTING_PHASE must preceed DRAW_PHASE");
        game.currentPhase = Game.PLAYER_PHASE;
        
        while(!game.active_hands.isEmpty())
        {
            game.currentHand = game.active_hands.removeFirst();                
            Message actionRequest = Message.actionRequest(dealer.id, Protocoll.Header.Role.DEALER, game.currentHand.id);
            InetAddress addr;
            int port;
            synchronized(dealer.players)
            {
                addr = dealer.players.get(game.currentHand.owner).getValue0();
                port = dealer.players.get(game.currentHand.owner).getValue1();
            }
            while(game.currentHand.active)
            {
                try
                {
                    dealer.send(addr, port, actionRequest);
                    synchronized(game.lock){game.lock.wait(Config.Game.PLAYER_MOVE_DURATION);}
                    if(! game.currentHand.actionPerformed)
                    {
                        synchronized(dealer.players)
                        {
                            dealer.players.remove(game.currentHand.owner);
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
        startDealerPhase();
    }

    public void startDealerPhase()
    {

    }

}
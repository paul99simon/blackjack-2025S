package common;

import java.util.List;

public class Hand
{
    public final Id owner;
    public List<Card> cards;
    public final int wager;

    public Hand(Id owner, int wager)
    {
        this.owner = owner;
        this.cards = new LinkedList<Card>();
        this.wager = wager;
    }



    public int value()
    
    
}

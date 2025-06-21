package dealer;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import common.Card;
import common.Protocoll;
import common.endpoint.InstanceId;

public class Hand
{   
    public final int round_id;
    public final InstanceId owner_id;

    private static int id_counter = 0;
    public final int hand_id;

    public List<Card> cards;
    public int wager;
    public boolean active;
    public boolean actionPerformed;
    public boolean split;
    
    private Hand(int round_id, InstanceId owner_id, int hand_id, int wager, boolean split)
    {
        this.round_id = round_id;
        this.hand_id = hand_id;
        this.owner_id = owner_id;
        this.cards = new LinkedList<Card>();
        this.wager = wager;
        this.split = split;
        this.active = true;
        this.actionPerformed = false;
    }


    public Hand(int round_id, InstanceId owner_id)
    {
        this(round_id, owner_id, id_counter++, 0, false);
    }

    public Hand(int round_id, InstanceId owner_id, int wager)
    {
        this(round_id, owner_id, id_counter++, wager, false);
    }

    public Hand(Hand split)
    {
        this(split.round_id, split.owner_id, id_counter++, split.wager, true);
        this.cards.add(split.cards.remove(0));
        split.split = true;
    }

    public void add(Card card)
    {
        cards.add(card);
        active = values().stream().filter(value -> value < 22).count() != 0;
    }

    public int size()
    {
        return cards.size();
    }

    public Set<Integer> values()
    {

        Set<Integer> result = new HashSet<>();
        List<Integer> values = new LinkedList<>(); 

        if(cards.isEmpty())
        {
            result.add(0);
            return result;
        }
        else
        {   if(cards.get(0).getValue() == 1)
            {
                values.add(1);
                values.add(11);
            }
            else
            {
                values.add(cards.get(0).getValue());
            }
        }

        for(int i = 1; i < size() ; i++)
        {
            if(cards.get(i).getValue() == 1)
            {
                int size = values.size();
                for(int j = 0; j < size; j++)
                {
                    values.add(values.get(j) + 11);
                    values.set(j,values.get(j) + 1);
                }
            }
            else
            {
                for(int j = 0; j < values.size(); j++)
                {
                    values.set(j, values.get(j) + cards.get(i).getValue());
                }
            }
        }
        result.addAll(values);
        return result;
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        for(int i = 0; i < cards.size(); i++)
        {
            builder.append(cards.get(i)).append(Protocoll.SEPERATOR);
        }

        for(Integer value : values())
        {
            builder.append(value).append(Protocoll.SEPERATOR);
        }
        builder.deleteCharAt(builder.length()-1);
        return builder.toString();
    }

    byte[] cardsToBytes()
    {        
        byte[] arr = new byte[cards.size()];
        
        for(int i = 0; i < cards.size(); i++)
        {
            arr[i] = cards.get(i).value;
        }

        return arr;
    }

}

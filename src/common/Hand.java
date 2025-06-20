package common;

import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class Hand
{
    public final Id owner;
    //TODO: you know what to do...
    public final int round_id;

    private static int id_counter = 0;
    public final int id;

    public List<Card> cards;
    public int wager;
    public boolean active;
    public boolean actionPerformed;
    public boolean split;

    
    public Hand(int id, Id owner, int wager, boolean split)
    {
        this.id = id;
        this.owner = owner;
        this.cards = new LinkedList<Card>();
        this.wager = wager;
        this.active = true;
        this.actionPerformed = false;
        this.split = split;
    }

    public Hand(Id owner)
    {
        this(id_counter++, owner, 0, false);
    }

    public Hand(Id owner, int wager)
    {
        this(id_counter++, owner, wager, false);
    }

    public Hand(Hand split)
    {
        this(id_counter++, split.owner, split.wager, true);
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

    byte[] toByte()
    {        
        byte[] arr = new byte[cards.size() + 5];
        
        ByteBuffer buffer = ByteBuffer.allocate(4).order(Protocoll.BYTE_ORDER).putInt(wager);
        arr[0] = buffer.get(0);
        arr[1] = buffer.get(1);
        arr[2] = buffer.get(2);
        arr[3] = buffer.get(3);
        arr[4] = (byte) (split ? 1 : 0);
        
        for(int i = 0; i < cards.size(); i++)
        {
            arr[i+5] = cards.get(i).value;
        }

        return arr;
    }

}

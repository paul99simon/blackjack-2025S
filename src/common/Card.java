package common;

public class Card {

    public final int value;

    public Card(int value) throws IllegalArgumentException
    {
        if(1 > value || 52 < value) throw new IllegalArgumentException("value must be between 1 and 52");
        this.value = value;
    }

    public int getSuit()
    {
        return ((value -1)  % 4) + 1;
    }

    public int getRank()
    {
        return value % 4 == 0 ? (value / 4) : (value / 4) + 1;
    }

    @Override
    public String toString()
    {
        int suit = getSuit();
        int rank = getRank();

        String suit_s = "";

        switch (suit) {
            case 1:
                suit_s = "C";
                break;
            case 2:
                suit_s = "D";
                break;
            case 3:
                suit_s = "H";
                break;
            case 4:
                suit_s = "S";
                break;
        }

        String rank_s = "";

        switch (rank)
        {
            case 1:
                rank_s = "A";
                break;
            case 10:
                rank_s = "T";
                break;
            case 11:
                rank_s = "J";
                break;
            case 12:
                rank_s = "Q";
                break;
            case 13:
                rank_s = "K";
                break;
            default:
                rank_s = Integer.toString(rank);
                
        }

        return rank_s + suit_s;
    }

    public byte toByte()
    {
        return (byte) value;
    }

    public static void main(String[] args)
    {
        for(int i = 1; i < 53; i++)
        {
            System.out.println(new Card(i));
        }
    }

}

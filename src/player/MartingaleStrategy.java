package player;

public class MartingaleStrategy extends BettingStrategy
{

    public MartingaleStrategy(Player player)
    {
        super(player);
    }

    @Override
    public int getWager()
    {
        return 0;
    }
}
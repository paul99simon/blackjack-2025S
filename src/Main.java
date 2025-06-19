import counter.Counter;
import dealer.Dealer;
import player.Player;

public class Main
{

   
    private static void printHelp()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("usage: Main --role=[option] where [option] equals\n");
        builder.append("    dealer \n");
        builder.append("    player \n");
        builder.append("    counter\n");
        System.out.println(builder.toString());
    }

    public static void main(String[] args)
    {
        if(args.length != 1)
        {
            printHelp();
            return;
        }

        if(args[0].equals("--role=dealer"))
        {
            Dealer dealer = new Dealer();
            dealer.run();            
        }
        else if(args[0].equals("--role=player"))
        {
            Player player = new Player();
            player.run();
        }
        else if(args[0].equals("--role=counter"))
        {
            Counter counter = new Counter();
            counter.run();
        }
        else
        {
            printHelp();
        }        
    }
}

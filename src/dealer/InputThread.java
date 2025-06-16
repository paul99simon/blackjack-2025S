package dealer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class InputThread extends Thread
{
    Dealer dealer;

    public InputThread(Dealer dealer)
    {
        this.dealer = dealer;
    }

    public void run()
    {
        try
        {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

            while(true)
            {
                String input = reader.readLine();
                if(input.equals("start"))
                {
                    dealer.game.reStart();
                }
                else
                {
                    printHelp();
                }
            }
        }
        catch (IOException e)
        {
            System.out.printf("Exception: %s\n",e.getMessage());
        }
    }

    private void printHelp()
    {

    }
}

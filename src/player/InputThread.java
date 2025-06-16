package player;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;

import common.GameProtocoll;
import common.Message;

public class InputThread extends Thread
{

    public Player player;

    public InputThread(Player player)
    {
        this.player = player;
    }

    public void run()
    {
        try
        {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

            while(true)
            {
                String input = reader.readLine();

                if(input.startsWith("register"))
                {
                    String[] command = input.split(" ", 3);
                    if(command.length != 3)
                    {
                        printHelp();
                    }
                    
                    InetAddress addr = InetAddress.getByName(command[1]);
                    int port = Integer.valueOf(command[2]);

                    Message registerMessage = new Message(player.id, GameProtocoll.PLAYER, GameProtocoll.REGISTRATION, "");
                    player.send(addr, port, registerMessage);
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

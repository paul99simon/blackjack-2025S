package dealer;

import java.util.Queue;
import java.util.Stack;

import common.Card;

public class Game
{
    private boolean running = true;
    private boolean finished = false;
    public Queue<String> players;


    public Game(Queue<String> players)
    {

        this.players = players;
    }


}

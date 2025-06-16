package player;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.LinkedList;
import java.util.List;

import org.javatuples.Triplet;

import common.Card;
import common.Config;
import common.Id;
import common.UDP_Endpoint;

public class Player extends UDP_Endpoint
{
    public Triplet<Id, InetAddress, Integer> dealer;
    public Triplet<Id, Inet4Address, Integer> counter;

    public List<Card> hand;
    public int bankroll;

    public Player()
    {
        hand = new LinkedList<>();
        this.bankroll = Config.START_BANKROLL;

        listenThread = new ListenThread(this);
        inputThread = new InputThread(this);

        listenThread.start();
        inputThread.start();
    }



}

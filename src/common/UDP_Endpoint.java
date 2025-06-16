package common;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public abstract class UDP_Endpoint implements Runnable
{ 
    public final Id id;

    public DatagramSocket socket;
    public Thread listenThread;  
    public Thread inputThread;
    
    public Map<Integer, Boolean> acknowledge;
   
    public UDP_Endpoint()
    {
        id = new Id();
        acknowledge = Collections.synchronizedMap(new HashMap<>());

        try
        {
            socket = new DatagramSocket();
            socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
            InetAddress addr = socket.getLocalAddress();
            int port = socket.getLocalPort();
            socket.disconnect();
            System.out.printf("udp endpoint is (%s %d)\n", addr.getHostAddress(), port);
        }
        catch(UnknownHostException e)
        {
            System.out.println(e.getMessage());
        }
        catch(SocketException e)
        {
            System.out.println(e.getMessage());
        }
    }

    public void run()
    {
        listenThread.start();
        inputThread.start();
    }

    public void send(InetAddress addr, int port, Message message) throws IOException, MaxRetriesExceededExeption
    {
        int message_id = message.message_id;
        synchronized(acknowledge)
        {
            acknowledge.put(message_id, false);
        }
        byte[] data = message.getBytes();
        DatagramPacket packet = new DatagramPacket(data, data.length, addr, port);
        
        synchronized(acknowledge)
        {
            int tries = 0;
            while(!acknowledge.get(message_id))
            {
                if(tries > Config.MAX_TRIES) break;
                socket.send(packet);
                tries++;
            }
        }
    }

}
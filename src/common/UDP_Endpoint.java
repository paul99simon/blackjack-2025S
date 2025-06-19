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
    public final byte role;

    public InetAddress addr;
    public int port;

    public DatagramSocket socket;
    public Thread listenThread;  
    public Thread inputThread;
    
    public Map<Integer, Message> acknowledge;
   
    public UDP_Endpoint(byte role)
    {
        this.role = role;
        acknowledge = Collections.synchronizedMap(new HashMap<>());
        initIpAndPort();
        this.id = new Id(this.port);

        listenThread = new Thread(() -> this.listen());
        inputThread = new Thread(() -> this.processInput());
    }

    private void initIpAndPort()
    {
        try
        {
            socket = new DatagramSocket();
            socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
            addr = socket.getLocalAddress();
            port = socket.getLocalPort();
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

    public abstract void listen();
    public abstract void processInput();
    protected abstract void printHelp();

    public void run()
    {
        listenThread.start();
        inputThread.start();
    }

    public void acknowledge(InetAddress addr, int port, Message message)
    {
        if(message.message_type != Protocoll.Header.Type.ACK)
        {
            Message ackMessage = Message.ackMessage(id, role, message);
            DatagramPacket ackPacket = new DatagramPacket(ackMessage.getBytes(), ackMessage.message_length, addr, port);
            try
            {
                socket.send(ackPacket);
            }
            catch (IOException e)
            {
                System.out.println(e.getMessage());
            }
            System.out.println("acknowledge:"+ackMessage);
        }
    }

    public void send(InetAddress addr, int port, Message message) throws IOException, MaxRetriesExceededException
    {
        int message_id = message.message_id;
        byte[] data = message.getBytes();
        DatagramPacket packet = new DatagramPacket(data, data.length, addr, port);
        
        int tries = 0;
        while(tries < Config.MAX_TRIES)
        {
            synchronized(acknowledge)
            {
                if(acknowledge.get(message_id) != null)
                {   
                    acknowledge.remove(message_id);
                    break;
                }
            }
            if(tries > Config.MAX_TRIES) throw new MaxRetriesExceededException("maximum tries exceeded");
            socket.send(packet);
            System.out.println("sending:" + message);
            tries++;
            try
            {
                Thread.sleep((long) Math.pow(2,tries) * Config.TIMEOUT_MS);
            }
            catch (InterruptedException e)
            {
                System.out.println(e.getMessage());
            }
        }
    }

}
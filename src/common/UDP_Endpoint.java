package common;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public abstract class UDP_Endpoint implements Runnable
{
    public DatagramSocket socket;

    public abstract void run();

    public void send(InetAddress addr, int port, Message message) throws IOException
    {
        byte[] data = message.getBytes();
        int id = message.message_id;

        DatagramPacket packet = new DatagramPacket(data, data.length, addr, port);
        


        socket.send(packet);
    }

}
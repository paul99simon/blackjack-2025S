package common;

import java.net.DatagramPacket;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class Message {
    
    private static int id_counter = 1;
    
    public int    message_length;
    public int    message_id;
    public Id     sender_id;
    public byte   sender_type;
    public byte   message_type;
    public String payload;
    
    public Message(Id sender_id, int sender_type, int message_type, String payload)
    {
        this.message_length = payload.getBytes(GameProtocoll.CHARSET).length + 5 ;
        this.sender_id = sender_id;
        this.sender_type = (byte) sender_type;
        this.message_type = (byte) message_type;
        this.message_id = id_counter++;
        this.payload = payload;
    }

    public Message(int id)
    {
        this.message_type = GameProtocoll.ACKNOWLEDGE;
        this.message_length = 5;
        this.message_id = id;
        this.payload = "";
    }

    public Message (DatagramPacket packet)
    {
        byte[] data = packet.getData();

        byte[] length = Arrays.copyOfRange(data, 0, 4);
        this.message_length = ByteBuffer.wrap(length).order(GameProtocoll.BYTE_ORDER).getInt();

        byte[] id = Arrays.copyOfRange(data, 4, 8);
        this.message_id = ByteBuffer.wrap(id).order(GameProtocoll.BYTE_ORDER).getInt();
        this.message_type = data[8];
        this.payload = new String(Arrays.copyOfRange(data, 9, this.message_length +1));
    }

    public Message(int type, String[] payload)
    {
        StringBuilder builder = new StringBuilder();

        for(int i = 0; i < payload.length; i++)
        {
            builder.append(payload[i]).append(GameProtocoll.SEPERATOR);
        }

        builder.deleteCharAt(builder.length()-1);
        this.message_type = type;
        this.payload = builder.toString();
        this.message_id = id_counter++;
        this.message_length = this.payload.getBytes(GameProtocoll.CHARSET).length + 5;

    }

    public byte[] getBytes()
    {
        byte[] arr = new byte[message_length + 9];
        
        ByteBuffer buffer = ByteBuffer.allocate(4).order(GameProtocoll.BYTE_ORDER).putInt(message_length);
        arr[0] = buffer.get(0);
        arr[1] = buffer.get(1);
        arr[2] = buffer.get(2);
        arr[3] = buffer.get(3);

        buffer = ByteBuffer.allocate(4).order(GameProtocoll.BYTE_ORDER).putInt(message_id);
        arr[4] = buffer.get(0);
        arr[5] = buffer.get(1);
        arr[6] = buffer.get(2);
        arr[7] = buffer.get(3);

        arr[8] = (byte) message_type;

        byte[] messageBytes = payload.getBytes(GameProtocoll.CHARSET);
        int j = 9;
        for(int i = 0; i < messageBytes.length; i++)
        {
            arr[j++] = messageBytes[i];
        }
        return arr;
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append(message_length).append(GameProtocoll.SEPERATOR);
        builder.append(message_id).append(GameProtocoll.SEPERATOR);
        builder.append(message_type).append(GameProtocoll.SEPERATOR);
        builder.append(payload);
        return builder.toString();
    }
}
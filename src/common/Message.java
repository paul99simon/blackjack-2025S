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
        this(sender_id, sender_type, id_counter++,  message_type, payload);
    }

    public Message(Id sender_id, int sender_type, int message_id, int message_type, String payload)
    {
        this.message_length = payload.getBytes(GameProtocoll.CHARSET).length + GameProtocoll.HEADER_LENGTH ;
        this.sender_id = sender_id;
        this.sender_type = (byte) sender_type;
        this.message_type = (byte) message_type;
        this.message_id = id_counter++;
        this.payload = payload;
    }

    public Message (DatagramPacket packet)
    {
        byte[] data = packet.getData();

        byte[] length = Arrays.copyOfRange(data, 0, 4);
        this.message_length = ByteBuffer.wrap(length).order(GameProtocoll.BYTE_ORDER).getInt();

        byte[] message_id = Arrays.copyOfRange(data, 4, 8);
        this.message_id = ByteBuffer.wrap(message_id).order(GameProtocoll.BYTE_ORDER).getInt();

        this.sender_id = new Id(Arrays.copyOfRange(data, 8 , 16));

        this.message_type = data[16];
        this.sender_type = data[17];

        this.payload = new String(Arrays.copyOfRange(data, GameProtocoll.HEADER_LENGTH, this.message_length));
    }

    public Message acknowledgeMessage(Id sender_id, int sender_type)
    {
        return new Message(sender_id, sender_type, this.message_id, GameProtocoll.ACKNOWLEDGE, "");
    }

    public byte[] getBytes()
    {
        byte[] arr = new byte[GameProtocoll.HEADER_LENGTH + message_length];
        
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

        arr[8]  = this.sender_id.id[0];
        arr[9]  = this.sender_id.id[1];
        arr[10] = this.sender_id.id[2];
        arr[11] = this.sender_id.id[3];
        arr[12] = this.sender_id.id[4];
        arr[13] = this.sender_id.id[5];
        arr[14] = this.sender_id.id[6];
        arr[15] = this.sender_id.id[7];
        
        arr[16] = (byte) message_type;
        arr[17] = (byte) sender_type;

        byte[] payloadBytes = payload.getBytes(GameProtocoll.CHARSET);
        
        for(int i = 0; i < payloadBytes.length; i++)
        {
            arr[GameProtocoll.HEADER_LENGTH + i] = payloadBytes[i];
        }
        return arr;
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append(message_length).append(GameProtocoll.SEPERATOR);
        builder.append(message_id).append(GameProtocoll.SEPERATOR);
        builder.append(sender_id).append(GameProtocoll.SEPERATOR);
        builder.append(message_type).append(GameProtocoll.SEPERATOR);
        builder.append(sender_type).append(GameProtocoll.SEPERATOR);
        builder.append(payload);
        return builder.toString();
    }
}
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
    public int    amount; //only relevant when message type is AMOUNT
    public int    hand_id; //only relevant when message type is action or card;
    public byte[] payload;

    private Message(Id sender_id, int sender_type, int message_id, int message_type, int amount, int hand_id, byte[] payload)
    {
        if(payload == null)
        {
            this.message_length =  Protocoll.Header.LENGTH ;
        }
        else
        {
            this.message_length = payload.length + Protocoll.Header.LENGTH ;
        }
        this.sender_id = sender_id;
        this.sender_type = (byte) sender_type;
        this.message_type = (byte) message_type;
        this.message_id = message_id;
        this.amount = amount;
        this.hand_id = hand_id;
        this.payload = payload;
    }

    public Message (DatagramPacket packet)
    {
        byte[] data = packet.getData();

        byte[] length = Arrays.copyOfRange(data, 0, 4);
        this.message_length = ByteBuffer.wrap(length).order(Protocoll.BYTE_ORDER).getInt();

        byte[] message_id = Arrays.copyOfRange(data, 4, 8);
        this.message_id = ByteBuffer.wrap(message_id).order(Protocoll.BYTE_ORDER).getInt();

        this.sender_id = new Id(Arrays.copyOfRange(data, 8 , 16));

        byte[] amount = Arrays.copyOfRange(data, 16, 20);
        this.amount = ByteBuffer.wrap(amount).order(Protocoll.BYTE_ORDER).getInt();

        byte[] hand_id = Arrays.copyOfRange(data, 20, 24);
        this.hand_id = ByteBuffer.wrap(hand_id).order(Protocoll.BYTE_ORDER).getInt();

        this.message_type = data[24];
        this.sender_type = data[25];
        this.payload = Arrays.copyOfRange(data, Protocoll.Header.LENGTH, this.message_length);
    }

    public static Message ackMessage(Id sender_id, byte sender_type, Message message)
    {
        return new Message(sender_id, sender_type, message.message_id, Protocoll.Header.Type.ACK, 0, 0, null);
    }

    public static Message synMessage(Id sender_id, byte sender_type)
    {
        return new Message(sender_id, sender_type, id_counter++, Protocoll.Header.Type.SYN, 0,0, null);
    }

    public static Message amountMessage(Id sender_id, byte sender_type, int amount)
    {
        return new Message(sender_id, sender_type, id_counter++, Protocoll.Header.Type.AMOUNT, amount,0, null);
    }

    public static Message cardMessage(Id sender_id, byte sender_type, int hand_id, Card card)
    {
        byte[] payload = new byte[1];
        payload[0] = card.value;
        return new Message(sender_id, sender_type, id_counter++, Protocoll.Header.Type.CARD,0, hand_id, payload);
    }

    public static Message actionRequest(Id sender_id, byte sender_type, int hand_id)
    {
        return new Message(sender_id, sender_type, id_counter++, Protocoll.Header.Type.ACTION, 0, hand_id, null);
    }

    public byte[] getBytes()
    {
        byte[] arr = new byte[Protocoll.Header.LENGTH + message_length];
        
        ByteBuffer buffer = ByteBuffer.allocate(4).order(Protocoll.BYTE_ORDER).putInt(message_length);
        arr[0] = buffer.get(0);
        arr[1] = buffer.get(1);
        arr[2] = buffer.get(2);
        arr[3] = buffer.get(3);

        buffer = ByteBuffer.allocate(4).order(Protocoll.BYTE_ORDER).putInt(message_id);
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

        buffer = ByteBuffer.allocate(4).order(Protocoll.BYTE_ORDER).putInt(amount);
        arr[16] = buffer.get(0);
        arr[17] = buffer.get(1);
        arr[18] = buffer.get(2);
        arr[19] = buffer.get(3);

        buffer = ByteBuffer.allocate(4).order(Protocoll.BYTE_ORDER).putInt(hand_id);
        arr[20] = buffer.get(0);
        arr[21] = buffer.get(1);
        arr[22] = buffer.get(2);
        arr[23] = buffer.get(3);


        arr[24] = (byte) message_type;
        arr[25] = (byte) sender_type;

        if(payload != null)
        {
            for(int i = 0; i < payload.length; i++)
            {
                arr[Protocoll.Header.LENGTH + i] = payload[i];
            }
        } 

        return arr;
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("{length: ").append(message_length).append("}").append(Protocoll.SEPERATOR);
        builder.append("{message_id: ").append(message_id).append("}").append(Protocoll.SEPERATOR);
        builder.append("{sender_id: ").append(sender_id).append("}").append(Protocoll.SEPERATOR);
        builder.append("{amount: ").append(amount).append("}").append(Protocoll.SEPERATOR);
        builder.append("{hand_id: ").append(hand_id).append("}").append(Protocoll.SEPERATOR);
        builder.append("{message_type: ").append(message_type).append("}").append(Protocoll.SEPERATOR);
        builder.append("{sender_type: ").append(sender_type).append("}").append(Protocoll.SEPERATOR);
        if(payload != null)
        {
            builder.append("{payload: ");
            for(int i = 0; i < payload.length; i++)
            {
                builder.append(String.format("%02X", payload[i]));
            }
            builder.append("}");
        }
        return builder.toString();
    }
}
package common;

import java.nio.ByteBuffer;
import java.util.Arrays;

public record StatisticsEntry(int round_id, Id player_id, int hand_id, boolean win, boolean blackJack, Hand hand)
{

    public static StatisticsEntry fromByte(byte[] arr)
    {
        int round_id = ByteBuffer.wrap(Arrays.copyOfRange(arr, 0, 4)).order(Protocoll.BYTE_ORDER).getInt();
        Id player_id = new Id(Arrays.copyOfRange(arr, 4, 12));
        int hand_id = ByteBuffer.wrap(Arrays.copyOfRange(arr, 12, 16)).order(Protocoll.BYTE_ORDER).getInt();
        boolean win = arr[16] == 0 ? false : true;
        boolean blackJack = arr[17] == 0 ? false : true;
        
        int amount = ByteBuffer.wrap(Arrays.copyOfRange(arr, 16, 20)).getInt();
        boolean split = arr[20] == 0 ? false : true;
        Hand hand = new Hand(hand_id, player_id, amount, split);
        for(int i = 21 ; i < arr.length; i++)
        {
            Card card = new Card(arr[i]);
            hand.add(card);
        }
        return new StatisticsEntry(round_id, player_id, hand_id, win, blackJack, hand);
    }


    public byte[] toByte()
    {
        byte[] hand_bytes = hand.toByte();
        byte[] arr = new byte[18 + hand_bytes.length];

        ByteBuffer buffer = ByteBuffer.allocate(4).order(Protocoll.BYTE_ORDER).putInt(round_id);
        arr[0] = buffer.get(0);
        arr[1] = buffer.get(1);
        arr[2] = buffer.get(2);
        arr[3] = buffer.get(3);
        
        arr[4] = player_id.id[0];
        arr[5] = player_id.id[1];
        arr[6] = player_id.id[2];
        arr[7] = player_id.id[3];
        arr[8] = player_id.id[4];
        arr[9] = player_id.id[5];
        arr[10] = player_id.id[6];
        arr[11] = player_id.id[7];

        buffer = ByteBuffer.allocate(4).order(Protocoll.BYTE_ORDER).putInt(hand_id);
        arr[12] = buffer.get(0);
        arr[13] = buffer.get(1);
        arr[14] = buffer.get(2);
        arr[15] = buffer.get(3);

        arr[16] = (byte) (win ? 1 : 0);
        arr[17] = (byte) (blackJack ? 1 : 0);

        int i = 18;
        for(Byte b : hand_bytes)
        {
            arr[i] = b;
        }

        return arr;
    }
}
package common;

import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public final class Protocoll {
    
    private Protocoll(){}

    public static final Charset CHARSET = StandardCharsets.UTF_8;
    public static final ByteOrder BYTE_ORDER = ByteOrder.BIG_ENDIAN;
    public static final String SEPERATOR = ":";

    public static final class Header
    {
        public static final int LENGTH = 32;  

        public static final class Type
        {
            public static final int ACK = 0x01;
            public static final int SYN = 0x02;
            public static final int FIN = 0x03;
            public static final int ACTION = 0x04;
            public static final int HIT = 0x05;
            public static final int STAND = 0x06;
            public static final int SPLIT = 0x07;
            public static final int DOUBLE_DOWN = 0x08;
            public static final int SURRENDER = 0x09;
            public static final int AMOUNT = 0x0A;
            public static final int DECKCOUNT = 0x0B;
            public static final int SHUFFLED = 0x0C;
            public static final int CARD = 0x0D;
        }

        public static final class Role
        {
            public static final byte DEALER  = 0x01;
            public static final byte PLAYER  = 0x02;
            public static final byte COUNTER = 0x03;
        }
    }
}

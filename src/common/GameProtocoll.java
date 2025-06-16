package common;

import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class GameProtocoll {
    
    private GameProtocoll(){}

    public static final int HEADER_LENGTH = 24;  
    public static final Charset CHARSET = StandardCharsets.UTF_8;
    public static final ByteOrder BYTE_ORDER = ByteOrder.BIG_ENDIAN;
    public static final String SEPERATOR = ":";

    public static final int DEALER  = 0x01;
    public static final int PLAYER  = 0x02;
    public static final int COUNTER = 0x03;

    public static final int ACKNOWLEDGE = 0x01;
    public static final int REGISTRATION = 0x02;
    public static final int DISCONNECT = 0x03;
    public static final int HIT = 0x04;
    public static final int STAND = 0x05;
    public static final int SPLIT = 0x06;
    public static final int DOUBLE_DOWN = 0x07;
    public static final int SURRENDER = 0x08;
    public static final int BET = 0x09;
    public static final int DECKCOUNT = 0x0A;
    public static final int SHUFFLED = 0x0B;
    public static final int INITIAL_CARD = 0x0C;
    public static final int CARD = 0x0D;

}

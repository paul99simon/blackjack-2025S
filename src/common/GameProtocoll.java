package common;

import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class GameProtocoll {
    
    private GameProtocoll(){}

    public static final Charset CHARSET = StandardCharsets.UTF_8;
    public static final ByteOrder BYTE_ORDER = ByteOrder.BIG_ENDIAN;
    public static final String SEPERATOR = ":";

    public static final int ACKNOWLEDGE = 0x01;
    
    public static final int REGISTRATION = 0x02;
        public static final int DEALER  = 0x01;
        public static final int PLAYER  = 0x02;
        public static final int COUNTER = 0x03;

    public static final int ACTION = 0x03;
    public static final int BET = 0x04;
}

package common;

public final class Config
{

    private Config(){}

    public static final class Network
    {
        public static final int BUFFER_LENGTH = 4096;
        public static final int TRIES_LINEAR = 5;
        public static final int TRIES_NON_LINEAR = 5;
        public static final int TIMEOUT_MS = 1000;
    }

    public static final class Game
    {
        public static final int MAX_PLAYERS = 7;
        public static final int BETTING_PHASE_DURATION = 30000;
        public static final int PLAYER_MOVE_DURATION = 20000;
        public static final int START_BANKROLL = 4000;
    }

    

}
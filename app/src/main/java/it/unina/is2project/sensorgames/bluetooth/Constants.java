package it.unina.is2project.sensorgames.bluetooth;

public interface Constants {

    // Message types sent from the BluetoothService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    // Message Types
    public static final int MSG_TYPE_COORDS = 6;
    public static final int MSG_TYPE_FIRST_START = 7;
    public static final int MSG_TYPE_SYNC = 8;
    public static final int MSG_TYPE_FAIL = 9;
    public static final int MSG_TYPE_PAUSE = 10;
    public static final int MSG_TYPE_RESUME = 11;
    public static final int MSG_TYPE_ALERT = 12;
    public static final int MSG_TYPE_NOREADY = 13;
    @Deprecated
    public static final int MSG_TYPE_RESUME_NOREADY = 14;
    public static final int MSG_TYPE_POINT_UP = 15;
    public static final int MSG_TYPE_GAME_OVER = 16;

    // Message Bonus Types
    public static final int MSG_TYPE_BONUS_SPEEDX2 = 17;
    public static final int MSG_TYPE_BONUS_SPEEDX3 = 18;
    public static final int MSG_TYPE_BONUS_SPEEDX4 = 19;
    public static final int MSG_TYPE_BONUS_LOCKFIELD = 20;
    public static final int MSG_TYPE_BONUS_CUTBAR30 = 21;
    public static final int MSG_TYPE_BONUS_CUTBAR50 = 22;
    public static final int MSG_TYPE_BONUS_REVERTEDBAR = 23;
    public static final int MSG_TYPE_BONUS_RUSHHOUR = 24;

    // Key names received from the BluetoothService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    // Keys for shared preferences
    public static final String PREF_NICKNAME = "prefNickname";
}

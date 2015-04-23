package it.unina.is2project.sensorgames.bluetooth;

public interface Constants {

    // Message types sent from the BluetoothService Handler
    int MESSAGE_STATE_CHANGE = 1;
    int MESSAGE_READ = 2;
    int MESSAGE_WRITE = 3;
    int MESSAGE_DEVICE_NAME = 4;
    int MESSAGE_TOAST = 5;

    // Message Types
    int MSG_TYPE_COORDS = 6;
    int MSG_TYPE_FIRST_START = 7;
    int MSG_TYPE_SYNC = 8;
    int MSG_TYPE_FAIL = 9;
    int MSG_TYPE_PAUSE = 10;
    int MSG_TYPE_RESUME = 11;
    int MSG_TYPE_ALERT = 12;
    int MSG_TYPE_NOREADY = 13;
    @Deprecated
    int MSG_TYPE_RESUME_NOREADY = 14;
    int MSG_TYPE_POINT_UP = 15;
    int MSG_TYPE_GAME_OVER = 16;
    int MSG_TYPE_STOP_REQUEST = 25;
    int MSG_TYPE_SUSPEND_REQUEST = 26;
    int MSG_TYPE_RESUME_AFTER_SUSPEND = 27;
    int MSG_TYPE_BALL_REQUEST = 28;
    int MSG_TYPE_BALL_ACK = 29;

    // Message Bonus Types
    int MSG_TYPE_BONUS_SPEEDX2 = 17;
    int MSG_TYPE_BONUS_SPEEDX3 = 18;
    int MSG_TYPE_BONUS_SPEEDX4 = 19;
    int MSG_TYPE_BONUS_LOCKFIELD = 20;
    int MSG_TYPE_BONUS_CUTBAR30 = 21;
    int MSG_TYPE_BONUS_CUTBAR50 = 22;
    int MSG_TYPE_BONUS_REVERTEDBAR = 23;
    int MSG_TYPE_BONUS_RUSHHOUR = 24;

    // Key names received from the BluetoothService Handler
    String DEVICE_NAME = "device_name";
    String TOAST = "toast";

    // Keys for shared preferences
    String PREF_NICKNAME = "prefNickname";
}

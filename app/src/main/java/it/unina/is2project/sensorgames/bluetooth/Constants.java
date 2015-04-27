package it.unina.is2project.sensorgames.bluetooth;

public interface Constants {

    //======================================================
    // Message types sent from the BluetoothService Handler
    //======================================================
    int MESSAGE_STATE_CHANGE = 1;
    int MESSAGE_READ = 2;
    int MESSAGE_WRITE = 3;
    int MESSAGE_DEVICE_NAME = 4;
    int MESSAGE_TOAST = 5;

    //======================================================
    // Message types sent between games activity of 2P
    //======================================================
    int MSG_TYPE_COORDS = 6;
    int MSG_TYPE_FIRST_START = 7;
    int MSG_TYPE_SYNC = 8;
    int MSG_TYPE_FAIL = 9;
    int MSG_TYPE_PAUSE = 10;
    int MSG_TYPE_RESUME = 11;
    int MSG_TYPE_ALERT = 12;
    int MSG_TYPE_NO_READY = 13;
    @Deprecated
    int MSG_TYPE_RESUME_NO_READY = 14;
    int MSG_TYPE_POINT_UP = 15;
    int MSG_TYPE_GAME_OVER = 16;
    int MSG_TYPE_STOP_REQUEST = 25;
    int MSG_TYPE_SUSPEND_REQUEST = 26;
    int MSG_TYPE_RESUME_AFTER_SUSPEND = 27;
    int MSG_TYPE_GAME_TIMEOUT = 28;

    //======================================================
    // Message bonus types
    //======================================================
    int MSG_TYPE_BONUS_SPEED_X2 = 17;
    int MSG_TYPE_BONUS_SPEED_X3 = 18;
    int MSG_TYPE_BONUS_SPEED_X4 = 19;
    int MSG_TYPE_BONUS_LOCK_FIELD = 20;
    int MSG_TYPE_BONUS_CUT_BAR_30 = 21;
    int MSG_TYPE_BONUS_CUT_BAR_50 = 22;
    int MSG_TYPE_BONUS_REVERTED_BAR = 23;
    int MSG_TYPE_BONUS_RUSH_HOUR = 24;

    //======================================================
    // Bonus IDs
    //======================================================
    int NO_EVENT        = 0;    // 1P  //     // ID for no bonus
    int NO_BONUS        = 1;    //     // 2P  // ID for no bonus
    int FIRST_ENEMY     = 1;    // 1P  //     // ID for first_enemy
    int BUBBLE_BONUS    = 2;    // 1P  //     // ID for bubble bonus
    int SPEED_X2        = 2;    //     // 2P  // ID for bonus speed x2
    int CUT_BAR_30      = 3;    // 1P  // 2P  // ID for bonus cut bar 30%
    int SPEED_X3        = 4;    //     // 2P  // ID for bonus speed x3
    int LIFE_BONUS      = 4;    // 1P  //     // ID for bonus life
    int CUT_BAR_50      = 5;    // 1P  // 2P  // ID for bonus cut bar 50%
    int BIG_BAR         = 6;    // 1P  //     // ID for bonus big bar
    int SPEED_X4        = 6;    //     // 2P  // ID for bonus speed x4
    int REVERSE         = 7;    // 1P  // 2P  // ID for bonus reverted bar
    int FREEZE          = 8;    // 1P  //     // ID for bonus freeze
    int LOCK_FIELD      = 8;    //     // 2P  // ID for bonus lock field
    int RUSH_HOUR       = 9;    // 1P  // 2P  // ID for bonus rush hour

    //======================================================
    // Bonus utils
    //======================================================
    long BONUS_REPEATING_TIME_MILLIS = 7000;    // Time between two selectable bonus
    long TIMEOUT_FOR_GAME_OVER = 120000;  // Time to wait to win the game if opponent pause game for too much time

    // Key names received from the BluetoothService Handler
    String DEVICE_NAME = "device_name";
    String TOAST = "toast";

    // Keys for shared preferences
    String PREF_NICKNAME = "prefNickname";
}

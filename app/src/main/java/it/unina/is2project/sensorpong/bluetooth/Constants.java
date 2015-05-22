package it.unina.is2project.sensorpong.bluetooth;

public interface Constants {

    //======================================================
    // Message types sent from the BluetoothService Handler
    //======================================================
    int MESSAGE_STATE_CHANGE    = 1;   // Bluetooth service message for State Change
    int MESSAGE_READ            = 2;   // Bluetooth service message for message read
    int MESSAGE_WRITE           = 3;   // Bluetooth service message for message write
    int MESSAGE_DEVICE_NAME     = 4;   // Bluetooth service message for device name
    int MESSAGE_TOAST           = 5;   // Bluetooth service message for toast to show

    //======================================================
    // Message types sent between games activity of 2P
    //======================================================
    int MSG_TYPE_COORDS         = 6;    // Message to communicate position and angulation of ball
    int MSG_TYPE_FIRST_START    = 7;    // Message to communicate ownership of ball
    int MSG_TYPE_SYNC           = 8;    // Message to communicate that non-master is ready to play
    int MSG_TYPE_FAIL           = 9;    // Message to communicate that someone left the game
    int MSG_TYPE_PAUSE          = 10;   // Message to communicate puase state
    int MSG_TYPE_RESUME         = 11;   // Message to communicate resume from pause or stop
    int MSG_TYPE_ALERT          = 12;   // Message to communicate that master want to play
    int MSG_TYPE_NO_READY       = 13;   // Message to communicate that non-master is not ready to play
    int MSG_TYPE_POINT_UP               = 15;   // Message to communicate the goal
    int MSG_TYPE_GAME_OVER              = 16;   // Message to communicate that player lose the game
    int MSG_TYPE_STOP_REQUEST           = 25;   // Message to communicate that someone stop the game
    int MSG_TYPE_SUSPEND_REQUEST        = 26;   // Message to communicate that both player are engaged
    int MSG_TYPE_RESUME_AFTER_SUSPEND   = 27;   // Message to cummunicate that someone want to resume the game from pause
    int MSG_TYPE_GAME_TIMEOUT           = 28;   // Message to communicate that someone stop the game for too much time

    //======================================================
    // Message bonus types
    //======================================================
    int MSG_TYPE_BONUS_SPEED_X2         = 17;   // Message type for the activation of bonus speed x2
    int MSG_TYPE_BONUS_SPEED_X3         = 18;   // Message type for the activation of bonus speed x3
    int MSG_TYPE_BONUS_SPEED_X4         = 19;   // Message type for the activation of bonus speed x4
    int MSG_TYPE_BONUS_LOCK_FIELD       = 20;   // Message type for the activation of bonus lock field
    int MSG_TYPE_BONUS_CUT_BAR_30       = 21;   // Message type for the activation of bonus cutbar 30
    int MSG_TYPE_BONUS_CUT_BAR_50       = 22;   // Message type for the activation of bonus cutbar 50
    int MSG_TYPE_BONUS_REVERSE_BAR      = 23;   // Message type for the activation of bonus reverse bar
    int MSG_TYPE_BONUS_RUSH_HOUR        = 24;   // Message type for the activation of bonus rush hour

    //======================================================
    // Bonus IDs for Two Player
    //======================================================
    int NO_BONUS    = 1;    // ID for no bonus
    int SPEED_X2    = 2;    // ID for bonus speed x2
    int SPEED_X3    = 3;    // ID for bonus speed x3
    int SPEED_X4    = 4;    // ID for bonus speed x4
    int CUT_BAR_30  = 5;    // ID for bonus cut bar 30%
    int CUT_BAR_50  = 6;    // ID for bonus cut bar 50%
    int REVERSE     = 7;    // ID for bonus reverted bar
    int LOCK_FIELD  = 8;    // ID for bonus lock field
    int RUSH_HOUR   = 9;    // ID for bonus rush hour

    //======================================================
    // Bonus utils
    //======================================================
    long BONUS_REPEATING_TIME_MILLIS = 7000;    // Time between two selectable bonus
    long TIMEOUT_FOR_GAME_OVER = 120000;        // Time to wait to win the game if opponent pause game for too much time

    //======================================================
    // 2 Players game utils
    //======================================================
    int LOWER_BOUND_POINT = 8;  // Lower bound for point's score
    int UPPER_BOUND_POINT = 14; // Upper bound for point's score

    // Key names received from the BluetoothService Handler
    String DEVICE_NAME = "device_name";
    String TOAST = "toast";
}

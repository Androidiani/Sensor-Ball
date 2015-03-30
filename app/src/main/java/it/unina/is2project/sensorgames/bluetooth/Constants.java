package it.unina.is2project.sensorgames.bluetooth;

public interface Constants {

    // Message types sent from the BluetoothService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    // Message types
    public static final int MSG_TYPE_COORDS = 6;
    public static final int MSG_TYPE_INTEGER = 7;
    public static final int MSG_TYPE_SYNC = 8;
    public static final int MSG_TYPE_FAIL = 9;
    public static final int MSG_TYPE_PAUSE = 10;
    public static final int MSG_TYPE_RESUME = 11;
    public static final int MSG_TYPE_ALERT = 12;
    public static final int MSG_TYPE_NOREADY = 13;
    public static final int MSG_TYPE_RESUME_NOREADY = 14;

    // Key names received from the BluetoothService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";
}

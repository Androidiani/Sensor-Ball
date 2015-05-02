package it.unina.is2project.sensorgames.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Random;
import java.util.Set;

import it.unina.is2project.sensorgames.R;
import it.unina.is2project.sensorgames.bluetooth.BluetoothService;
import it.unina.is2project.sensorgames.bluetooth.Constants;
import it.unina.is2project.sensorgames.bluetooth.FSMGame;
import it.unina.is2project.sensorgames.bluetooth.Serializer;
import it.unina.is2project.sensorgames.bluetooth.messages.AppMessage;
import it.unina.is2project.sensorgames.game.pong.GamePongTwoPlayer;

public class TwoPlayerActivity extends Activity {

    //===========================================
    // DEBUG
    //===========================================
    private static final String TAG         = "TwoPlayerActivity";
    private static final String LIFE_CYCLE  = "CicloDiVita2PActivity";

    //===========================================
    // BLUETOOTH SECTION
    //===========================================
    private ArrayAdapter<String> stringArrayAdapter;                // Scanned devices
    private String mConnectedDeviceName                 = null;     // Connected device name
    private BluetoothAdapter mBluetoothAdapter          = null;     // Bluetooth adapter
    private BluetoothService mBluetoothService          = null;     // Bluetooth service
    private Integer privateNumber                       = null;     // Synchronization's number
    private Integer points                              = null;     // Points to reach
    private boolean isMaster                            = false;    // Peer role
    private boolean mStatus                             = false;    // Adapter state


    //===========================================
    // INTENT EXTRAS
    //===========================================
    public static final int REQUEST_ENABLE_BT       = 1;            // Request for bluetooth enabling
    private final int GAME_START                    = 200;          // Request Code
    public static final String EXTRA_POINTS         = "points";     // Intent extra for points
    public static final String EXTRA_BALL           = "ball";       // Intent extra for ball
    public static final String EXTRA_MASTER         = "master";     // Intent extra for master
    public static final String EXTRA_DEVICE_NAME    = "deviceName"; // Intent extra for device name

    //===========================================
    // LAYOUT CHILDS
    //===========================================
    private Typeface typeFace;      // Font typeface
    private TextView lblBluetooth;  // TextView for constant string "Bluetooth"
    private TextView lblEnemy;      // TextView for constant string "Play With"
    private TextView txtEnemy;      // TextView for current paired device
    private Button btnPlay;         // Button for start game
    private Button btnScan;         // Button for start scan
    private Button btnPaired;       // Button for show paired device
    private Switch switchBluetooth; // Switch for bluetooth activation
    private ListView listDevice;    // ListView for show devices

    //===========================================
    // FSM
    //===========================================
    private FSMGame fsmGame = null;

    //----------------------------------------------
    // LIFECYCLE METHODS
    //----------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(LIFE_CYCLE, "OnCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_two_player);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Load the font
        typeFace = Typeface.createFromAsset(getAssets(), "font/secrcode.ttf");

        // Find Activity's View
        findViews();

        // Set "secrcode.ttf" font
        setFont();

        // Load default adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (mBluetoothAdapter == null) {
            // If device doesn't support bluetooth, disable all buttons
            disableAllButtons();
            Toast.makeText(getApplicationContext(), getApplicationContext().getString(R.string.toast_bluetoothNotSupported),
                    Toast.LENGTH_LONG).show();
        } else {
            mStatus = mBluetoothAdapter.isEnabled();

            // Adapter set
            stringArrayAdapter = new ArrayAdapter<>(this, R.layout.bluetooth_list_element);

            listDevice.setAdapter(stringArrayAdapter);

            switchBluetooth.setChecked(mStatus);

            IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            this.registerReceiver(bReceiver, filter);

            filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
            this.registerReceiver(bReceiver, filter);

            setListners();
        }

    }

    @Override
    protected void onStart() {
        Log.i(LIFE_CYCLE, "OnStart()");
        // Avvio del servizio per bluetooth
        if (mBluetoothAdapter != null && mBluetoothService == null) {
            Log.d(TAG, "setupService()");
            mBluetoothService = BluetoothService.getBluetoothService(getApplicationContext(), mHandler);
        }

        // FSM STATE CHANGE
        fsmGame = FSMGame.getFsmInstance(fsmHandler);
        if (fsmGame.getState() == FSMGame.STATE_NOT_READY) {
            fsmGame.setState(FSMGame.STATE_DISCONNECTED);
        }

        super.onStart();
    }

    @Override
    protected void onResume() {
        Log.i(LIFE_CYCLE, "OnResume()");
        if (mBluetoothService != null && mBluetoothAdapter != null) {
            mStatus = mBluetoothAdapter.isEnabled();
            switchBluetooth.setChecked(mStatus);
            if (mStatus) {
                if (mBluetoothService.getState() == BluetoothService.STATE_NONE) {
                    mBluetoothService.start();
                }
            }
        } else {
            Log.d(TAG, "setupService()");
            mBluetoothService = BluetoothService.getBluetoothService(getApplicationContext(), mHandler);
            mBluetoothService.start();
        }


        // FSM STATE CHANGE
        fsmGame = FSMGame.getFsmInstance(fsmHandler);
        if (fsmGame.getState() == FSMGame.STATE_DISCONNECTED) {
            fsmGame.setState(FSMGame.STATE_DISCONNECTED);
        }
//        if(mBluetoothService.getState() == BluetoothService.STATE_CONNECTED){
//
//        }

        super.onResume();
    }

    @Override
    protected void onPause() {
        Log.i(LIFE_CYCLE, "OnPause()");
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        Log.i(LIFE_CYCLE, "OnDestroy()");
        super.onDestroy();
        if (mBluetoothService != null) {
            mBluetoothService.stop();
            mBluetoothService = null;
        }
        this.unregisterReceiver(bReceiver);
    }

    //----------------------------------------------
    // ACTIONBAR ACTIVITY METHODS
    //----------------------------------------------
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_2player_bluetooth, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.option_discoverable:
                ensureDiscoverable();
                return true;
            case R.id.option_disconnect:
                disconnect();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                if (resultCode == Activity.RESULT_OK) {
                    if (mBluetoothService != null) {
                        mBluetoothService = BluetoothService.getBluetoothService(getApplicationContext(), mHandler);
                        mBluetoothService.stop();
                        mBluetoothService.start();
                    }
//                    mBluetoothService.stop();
//                    mBluetoothService.start();
                    if (!mStatus) {
                        mStatus = true;
                        switchBluetooth.setChecked(true);
                        Toast.makeText(getApplicationContext(), getApplicationContext().getString(R.string.toast_bluetoothActived), Toast.LENGTH_LONG).show();
                    }
                } else {
                    mStatus = false;
                    switchBluetooth.setChecked(false);
                }
                break;
            case GAME_START:
                mBluetoothService = BluetoothService.getBluetoothService(getApplicationContext(), mHandler);
                fsmGame = FSMGame.getFsmInstance(fsmHandler);
                isMaster = data.getExtras().getBoolean(GamePongTwoPlayer.EXTRA_MASTER);
                boolean isConnected = data.getExtras().getBoolean(GamePongTwoPlayer.EXTRA_CONNECTION_STATE);
                if (resultCode == Activity.RESULT_CANCELED) {
                    Log.d(TAG, "2 Players Game Was Canceled");
                    if (isConnected) {
                        fsmGame.setState(FSMGame.STATE_GAME_ABORTED);
                        txtEnemy.setText(data.getStringExtra(GamePongTwoPlayer.EXTRA_DEVICE));
                    } else {
                        fsmGame.setState(FSMGame.STATE_DISCONNECTED);
                    }
                }
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if (fsmGame.getState() == FSMGame.STATE_CONNECTED) {
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle(getResources().getString(R.string.text_disconnect));
            alert.setMessage(getResources().getString(R.string.text_msg_twoplayer_leave));
            alert.setPositiveButton(getResources().getString(R.string.text_yes), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    mBluetoothService.stop();
                    finish();
                }
            });
            alert.setNegativeButton(getResources().getString(R.string.text_no), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // do nothing
                }
            });
            alert.show();
        } else {
            mBluetoothService.stop();
            super.onBackPressed();
        }
    }

    //----------------------------------------------
    // SUPPORT METHODS
    //----------------------------------------------

    /**
     * Find views on activity layout
     */
    private void findViews() {
        lblBluetooth = (TextView) findViewById(R.id.lblBluetooth);
        lblEnemy = (TextView) findViewById(R.id.lblEnemy);
        txtEnemy = (TextView) findViewById(R.id.txtEnemy);
        btnPlay = (Button) findViewById(R.id.btnPlay);
        btnPaired = (Button) findViewById(R.id.btnPaired);
        btnScan = (Button) findViewById(R.id.btnScan);
        listDevice = (ListView) findViewById(R.id.listDevice);
        switchBluetooth = (Switch) findViewById(R.id.switchBluetooth);
    }

    /**
     * Set listners on buttons
     */
    private void setListners() {

        // BUTTON PAIRED
        btnPaired.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnPairedClick();
            }
        });

        // BUTTON SCAN
        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnScanClick();
            }
        });

        // BUTTON PLAY
        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnPlayClick();
            }
        });

        // LIST DEVICE
        listDevice.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                onSelectedItem(view);
            }
        });

        // SWITCH BUTTON
        switchBluetooth.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                onSwitchClicked(isChecked);
            }
        });
    }


    /**
     * Set fonts on activity layout
     */
    private void setFont() {
        lblBluetooth.setTypeface(typeFace);
        lblEnemy.setTypeface(typeFace);
        txtEnemy.setTypeface(typeFace);
        btnPlay.setTypeface(typeFace);
        btnPaired.setTypeface(typeFace);
        btnScan.setTypeface(typeFace);
    }

    /**
     * Disable all buttons on activity layout
     */
    private void disableAllButtons() {
        btnPaired.setEnabled(false);
        btnPlay.setEnabled(false);
        btnScan.setEnabled(false);
    }

    //----------------------------------------------
    // BUTTON CLICK METHODS
    //----------------------------------------------

    /**
     * Manage click on button paired
     */
    private void btnPairedClick() {
        Log.d(TAG, "btnPaired()");
        Set<BluetoothDevice> pairedDevice = mBluetoothAdapter.getBondedDevices();
        stringArrayAdapter.clear();
        for (BluetoothDevice device : pairedDevice)
            stringArrayAdapter.add(device.getName() + "\n" + device.getAddress());

        stringArrayAdapter.notifyDataSetChanged();
    }

    /**
     * Manage click on button scan
     */
    private void btnScanClick() {
        Log.d(TAG, "btnScan()");

        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        } else {
            stringArrayAdapter.clear();
            mBluetoothAdapter.startDiscovery();

            registerReceiver(bReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        }
    }

    /**
     * Manage click on button play
     */
    private void btnPlayClick() {
        Log.d(TAG, "btnPlay()");
        if (privateNumber == null) {
            // Crea un numero casuale per la scelta di dove far apparire la palla.
            Random rand = new Random();
            privateNumber = rand.nextInt(1000);
            privateNumber = privateNumber % 2;
            // Creo un numero casuale per definire i punti da raggiungere.
            points = rand.nextInt((Constants.UPPER_BOUND_POINT - Constants.LOWER_BOUND_POINT) + 1) +
                    Constants.LOWER_BOUND_POINT;
            // Invio messaggio
            AppMessage ballChoise = new AppMessage(Constants.MSG_TYPE_FIRST_START, privateNumber, points);
            sendBluetoothMessage(ballChoise);
        }
        Intent mIntent = new Intent(TwoPlayerActivity.this, GamePongTwoPlayer.class);
        mIntent.putExtra(EXTRA_POINTS, points);
        mIntent.putExtra(EXTRA_BALL, privateNumber);
        mIntent.putExtra(EXTRA_MASTER, isMaster);
        mIntent.putExtra(EXTRA_DEVICE_NAME, mConnectedDeviceName);
        startActivityForResult(mIntent, GAME_START);
    }

    /**
     * Manage click on listview items
     *
     * @param view Indicates this view
     */
    private void onSelectedItem(View view) {
        mBluetoothAdapter.cancelDiscovery();
        String info = ((TextView) view).getText().toString();
        String address = info.substring(info.length() - 17);
        connectDevice(address);
    }

    /**
     * Manage click on bluetooth switch
     *
     * @param isChecked Represent the state of the bluetooth switch
     */
    private void onSwitchClicked(boolean isChecked) {
        if (isChecked) {
            // If buttons is ON
            if (!mStatus) {
                //If bluetooth is not enable, it enables.
                Intent turnOnIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(turnOnIntent, REQUEST_ENABLE_BT);
            } else {
                // Not covered case
                mStatus = true;
            }
        } else {
            // If buttons is OFF
            if (mStatus) {
                mStatus = false;
                // Stopping service
                mBluetoothService.stop();

                //Clear bluetooth activity's data
                txtEnemy.setText(getResources().getString(R.string.text_none));
                stringArrayAdapter.clear();
                stringArrayAdapter.notifyDataSetChanged();

                Toast.makeText(getApplicationContext(), getApplicationContext().getString(R.string.toast_bluetoothDeactived),
                        Toast.LENGTH_LONG).show();

                // Turn Off bluetooth
                mBluetoothAdapter.disable();
            } else {
                // Not covered case
                mStatus = false;
            }
        }
    }

    //----------------------------------------------
    // MISCELLANEA
    //----------------------------------------------

    /**
     * Makes device discoverable
     */
    private void ensureDiscoverable() {
        Log.d(TAG, "ensureDiscoverable()");
        if (!mStatus) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.alert_bluetooth_activator)
                    .setPositiveButton(R.string.text_yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent turnOnIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                            startActivityForResult(turnOnIntent, REQUEST_ENABLE_BT);
                        }
                    })
                    .setNegativeButton(R.string.text_no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    })
                    .setCancelable(false)
                    .show();

        } else {
            if (mBluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
                Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
                startActivity(discoverableIntent);
            }
        }
    }

    /**
     * Start a bluetooth connection with device
     *
     * @param address MAC of the device to connect
     */
    private void connectDevice(String address) {
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        // Attempt to connect
        mBluetoothService.connect(device, true);
        // Who starts connection, began master
        isMaster = true;
    }

    /**
     * Disconnect current connection (if it is active)
     */
    private void disconnect() {
        mBluetoothService.stop();
        fsmGame.setState(FSMGame.STATE_DISCONNECTED);
        mBluetoothService.start();
    }

    /**
     * Send a bluetooth message
     *
     * @param ballChoise message that contains first informations to play game
     */
    private void sendBluetoothMessage(AppMessage ballChoise) {
        if (mBluetoothService.getState() != BluetoothService.STATE_CONNECTED) {
            Toast.makeText(getApplicationContext(), getApplicationContext().getString(R.string.toast_notConnected), Toast.LENGTH_SHORT).show();
            return;
        }

        byte[] send = Serializer.serializeObject(ballChoise);
        mBluetoothService.write(send);
    }

    //----------------------------------------------
    // BROADCAST RECEIVERS
    //----------------------------------------------
    final BroadcastReceiver bReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // Quando si è scoperto un nuovo device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Ricevo l'oggetto bluetooth device dall'intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // Aggiungo il nome del dispositivo ed il MAC address.
                stringArrayAdapter.add(device.getName() + "\n" + device.getAddress());
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                if (stringArrayAdapter.getCount() == 0) {
                    String noDevices = getResources().getText(R.string.none_found).toString();
                    stringArrayAdapter.add(noDevices);
                }
            }
            stringArrayAdapter.notifyDataSetChanged();
        }
    };

    //----------------------------------------------
    // HANDLERS
    //----------------------------------------------
    @SuppressWarnings("all")
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Log.i(TAG, "Handler Called");
            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothService.STATE_CONNECTED:
                            txtEnemy.setText(mConnectedDeviceName);
                            Log.d(TAG, "mHandler, Connected: Size is " + txtEnemy.getTextSize());
                            // FSM STATE CHANGE
                            fsmGame.setState(FSMGame.STATE_CONNECTED);
                            break;
                        case BluetoothService.STATE_CONNECTING:
                            fsmGame.setState(FSMGame.STATE_CONNECTING);
                            Log.i(TAG, "mHandler, Connecting...");
                            break;
                        case BluetoothService.STATE_LISTEN:
                            Log.i(TAG, "mHandler, Listening...");
                            break;
                        case BluetoothService.STATE_NONE:
                            Log.i(TAG, "mHandler, State None");
                            fsmGame.setState(FSMGame.STATE_DISCONNECTED);
                            break;
                    }
                    break;
                case Constants.MESSAGE_WRITE:
                    break;
                case Constants.MESSAGE_READ:
                    if (!isMaster) {
                        byte[] readBuf = (byte[]) msg.obj;
                        AppMessage recMsg = (AppMessage) Serializer.deserializeObject(readBuf);
                        if (recMsg != null) {
                            switch (recMsg.TYPE) {
                                case Constants.MSG_TYPE_FIRST_START:
                                    privateNumber = recMsg.OP1 == 0 ? new Integer(1) : new Integer(0);
                                    points = recMsg.OP5;
                                    break;
                                case Constants.MSG_TYPE_SYNC:
                                    AppMessage notReadyMessage = new AppMessage(Constants.MSG_TYPE_NO_READY);
                                    sendBluetoothMessage(notReadyMessage);
                                    break;
                                case Constants.MSG_TYPE_FAIL:
                                    btnPlay.setEnabled(false);
                                    break;
                                case Constants.MSG_TYPE_ALERT:
                                    btnPlay.setEnabled(true);
                                    break;
                                case Constants.MSG_TYPE_NO_READY:
                                    btnPlay.setEnabled(false);
                                default:
                                    Log.e(TAG, "Ricevuto messaggio non idoneo - Type is " + recMsg.TYPE);
                                    break;
                            }
                        } else {
                            Log.e(TAG, "Ricevuto messaggio nullo.");
                        }
                        Log.i(TAG, "Numero ricevuto " + recMsg.OP1);
                    }
                    break;
                case Constants.MESSAGE_DEVICE_NAME:
                    // Salvo il nome del dispositivo connesso
                    mConnectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
                    Log.d(TAG, "Device - Enemy is " + mConnectedDeviceName);
//                    txtEnemy.setText(mConnectedDeviceName);
                    break;
                case Constants.MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(), msg.getData().getString(Constants.TOAST),
                            Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    @SuppressWarnings("all")
    private final Handler fsmHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case FSMGame.STATE_NOT_READY:
                            break;
                        case FSMGame.STATE_CONNECTED:
                            if (isMaster) btnPlay.setEnabled(true);
                            stringArrayAdapter.clear();
                            stringArrayAdapter.notifyDataSetChanged();
                            break;
                        case FSMGame.STATE_IN_GAME:
                            break;
                        case FSMGame.STATE_IN_GAME_WAITING:
                            break;
                        case FSMGame.STATE_DISCONNECTED:
                            stringArrayAdapter.clear();
                            stringArrayAdapter.notifyDataSetChanged();
                            txtEnemy.setText(getResources().getString(R.string.text_none));
                            btnPlay.setEnabled(false);
                            mConnectedDeviceName = null;
                            privateNumber = null;
                            isMaster = false;
                            break;
                        case FSMGame.STATE_OPPONENT_LEFT:
                            break;
                        case FSMGame.STATE_GAME_ABORTED:
                            privateNumber = null;
                            if (!isMaster) {
                                btnPlay.setEnabled(false);
                            }
                            stringArrayAdapter.clear();
                            stringArrayAdapter.notifyDataSetChanged();
                            break;
                        default:
                    }
                default:
            }
        }
    };
}

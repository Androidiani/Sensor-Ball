package it.unina.is2project.sensorgames;

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
import android.support.v7.app.ActionBarActivity;
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

import it.unina.is2project.sensorgames.bluetooth.BluetoothService;
import it.unina.is2project.sensorgames.bluetooth.Constants;
import it.unina.is2project.sensorgames.bluetooth.Serializer;
import it.unina.is2project.sensorgames.bluetooth.messages.AppMessage;
import it.unina.is2project.sensorgames.pong.GamePongTwoPlayer;

public class TwoPlayerActivity extends ActionBarActivity {

    //----------------------------------------------
    // DEBUG
    //----------------------------------------------
    private static final String TAG = "TwoPlayerActivity";
    private static final String LIFE_CYCLE = "CicloDiVita2PActivity";

    //----------------------------------------------
    // BLUETOOTH SECTION
    //----------------------------------------------

    // Connected device name
    private String mConnectedDeviceName = null;

    // Bluetooth adapter
    private BluetoothAdapter mBluetoothAdapter = null;

    // Bluetooth service
    private BluetoothService mBluetoothService = null;

    // Adapter state
    private boolean mStatus = false;

    // Paired devices
    private Set<BluetoothDevice> pairedDevice;

    // Scanned devices
    private ArrayAdapter<String> stringArrayAdapter;

    // Peer role
    private boolean isMaster = false;

    // Synchronization's number
    private Integer privateNumber = null;

    // Intents code
    public static final int REQUEST_ENABLE_BT = 1;

    //----------------------------------------------
    // ACTIVITY ELEMENTS
    //----------------------------------------------

    // Font typeface
    private Typeface typeFace;

    // TextView
    private TextView lblBluetooth;
    private TextView lblEnemy;
    private TextView txtEnemy;

    // Button
    private Button btnPlay;
    private Button btnScan;
    private Button btnPaired;

    // Switch
    private Switch switchBluetooth;

    // ListView
    private ListView listDevice;

    // Request Code
    private final int GAME_START = 200;

    //----------------------------------------------
    // FSM ELEMENTS
    //----------------------------------------------

    private FSMGame fsmGame = null;

    //----------------------------------------------
    // LIFECYCLE METHODS
    //----------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(LIFE_CYCLE, "OnCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_2p);

        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Load the font
        typeFace = Typeface.createFromAsset(getAssets(),"font/secrcode.ttf");

        // Find Activity's View
        findViews();

        // Set "secrcode.ttf" font
        setFont();

        // Load default adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if(mBluetoothAdapter == null){
            // If device doesn't support bluetooth, disable all buttons
            disableAllButtons();
            Toast.makeText(getApplicationContext(), getApplicationContext().getString(R.string.toast_bluetoothNotSupported),
                    Toast.LENGTH_LONG).show();
        }else{
            mStatus = mBluetoothAdapter.isEnabled();

            // Adapter set
            stringArrayAdapter = new ArrayAdapter<String>(this, R.layout.bluetooth_list_element);

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
        if (mBluetoothAdapter != null && mBluetoothService == null){
            Log.d(TAG, "setupService()");
            mBluetoothService = BluetoothService.getBluetoothService(getApplicationContext(), mHandler);
        }

        // FSM STATE CHANGE
        fsmGame = FSMGame.getFsmInstance(fsmHandler);
        if(fsmGame.getState() == FSMGame.STATE_NOT_READY) {
            fsmGame.setState(FSMGame.STATE_DISCONNECTED);
        }

        super.onStart();
    }

    @Override
    protected void onResume() {
        Log.i(LIFE_CYCLE, "OnResume()");
        if(mBluetoothService != null){
            if(mStatus) {
                if (mBluetoothService.getState() == BluetoothService.STATE_NONE) {
                    mBluetoothService.start();
                }
            }
        }else{
            Log.d(TAG, "setupService()");
            mBluetoothService = BluetoothService.getBluetoothService(getApplicationContext(), mHandler);
            mBluetoothService.start();
        }
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
        if(mBluetoothService != null){
            mBluetoothService.stop();
            mBluetoothService = null;
        }
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

        switch (id){
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
        switch (requestCode){
            case REQUEST_ENABLE_BT:
                if(resultCode == Activity.RESULT_OK) {
                    if (mBluetoothService != null) {
                        mBluetoothService = BluetoothService.getBluetoothService(getApplicationContext(), mHandler);
                    }
                    mBluetoothService.stop();
                    mBluetoothService.start();
                    if(!mStatus){
                        mStatus = true;
                        switchBluetooth.setChecked(true);
                        Toast.makeText(getApplicationContext(), getApplicationContext().getString(R.string.toast_bluetoothActived) ,
                                Toast.LENGTH_LONG).show();
                    }
                }else{
                    mStatus = false;
                    switchBluetooth.setChecked(false);
                }
                break;
            case GAME_START:
                mBluetoothService = BluetoothService.getBluetoothService(getApplicationContext(), mHandler);
                fsmGame = FSMGame.getFsmInstance(fsmHandler);
                isMaster = data.getExtras().getBoolean(GamePongTwoPlayer.EXTRA_MASTER);
                boolean isConnected = data.getExtras().getBoolean(GamePongTwoPlayer.EXTRA_CONNECTION_STATE);
                if(resultCode == Activity.RESULT_CANCELED){
                    Log.d(TAG, "2 Players Game Was Canceled");
                    if(isConnected)
                        fsmGame.setState(FSMGame.STATE_GAME_ABORTED);
                    else
                        fsmGame.setState(FSMGame.STATE_DISCONNECTED);
                }
                break;
        }
    }

    //----------------------------------------------
    // SUPPORT METHODS
    //----------------------------------------------

    /**
     * Find views on activity layout
     */
    private void findViews(){
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
                onSelectedItem(parent, view, position, id);
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
    private void setFont(){
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
        pairedDevice = mBluetoothAdapter.getBondedDevices();
        stringArrayAdapter.clear();
        for(BluetoothDevice device : pairedDevice)
            stringArrayAdapter.add(device.getName()+ "\n" + device.getAddress());

        stringArrayAdapter.notifyDataSetChanged();
    }

    /**
     * Manage click on button scan
     */
    private void btnScanClick() {
        Log.d(TAG, "btnScan()");

        if(mBluetoothAdapter.isDiscovering()){
            mBluetoothAdapter.cancelDiscovery();
        }else{
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
        int intMaster;
        intMaster = isMaster? 1 : 0;
        if(privateNumber == null) {
            // Crea un numero casuale per la scelta di dove far apparire la palla.
            Random rand = new Random();
            privateNumber = rand.nextInt(1000);
            privateNumber = privateNumber % 2;
            AppMessage ballChoise = new AppMessage(Constants.MSG_TYPE_INTEGER, privateNumber);
            sendBluetoothMessage(ballChoise);
        }
        Intent mIntent = new Intent(TwoPlayerActivity.this, GamePongTwoPlayer.class);
        mIntent.putExtra("ball", privateNumber);
        mIntent.putExtra("master", intMaster);
        startActivityForResult(mIntent, GAME_START);
    }

    /**
     * Manage click on listview items
     * @param parent Indicates parent's view
     * @param view Indicates this view
     * @param position Indicates position
     * @param id Indicates id
     */
    private void onSelectedItem(AdapterView<?> parent, View view, int position, long id) {
        mBluetoothAdapter.cancelDiscovery();
        String info = ((TextView)view).getText().toString();
        String address = info.substring(info.length() - 17);
        connectDevice(address);
    }

    private void onSwitchClicked(boolean isChecked) {
        if (isChecked){
            // If buttons is ON
            if(!mStatus){
                //If bluetooth is not enable, it enables.
                Intent turnOnIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(turnOnIntent, REQUEST_ENABLE_BT);
            }else{
                // Not covered case
                mStatus = true;
            }
        }else{
            // If buttons is OFF
            if(mStatus){
                mStatus = false;
                // Stopping service
                mBluetoothService.stop();

                //Clear bluetooth activity's data
                txtEnemy.setText("");
                stringArrayAdapter.clear();
                stringArrayAdapter.notifyDataSetChanged();

                Toast.makeText(getApplicationContext(),getApplicationContext().getString(R.string.toast_bluetoothDeactived),
                        Toast.LENGTH_LONG).show();

                // Turn Off bluetooth
                mBluetoothAdapter.disable();
            }else{
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
        if(!mStatus){
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

        }else{
            if (mBluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
                Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
                startActivity(discoverableIntent);
            }
        }
    }

    private void connectDevice(String address) {
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        // Attempt to connect
        mBluetoothService.connect(device, true);
        // Who starts connection, began master
        isMaster = true;
    }

    private void disconnect() {
        mBluetoothService.stop();
        fsmGame.setState(FSMGame.STATE_DISCONNECTED);
        mBluetoothService.start();
    }

    private void sendBluetoothMessage(AppMessage ballChoise) {
        if (mBluetoothService.getState() != mBluetoothService.STATE_CONNECTED) {
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
            }else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
                if(stringArrayAdapter.getCount()==0){
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
    private final Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            Log.i(TAG, "Handler Called");
            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothService.STATE_CONNECTED:
                            Log.i(TAG, "Connected To " + mConnectedDeviceName);
                            // FSM STATE CHANGE
                            fsmGame.setState(FSMGame.STATE_CONNECTED);
                            break;
                        case BluetoothService.STATE_CONNECTING:
                            Log.i(TAG, "Connecting...");
                            break;
                        case BluetoothService.STATE_LISTEN:
                            Log.i(TAG, "Listening...");
                            break;
                        case BluetoothService.STATE_NONE:
                            Log.i(TAG, "State None");
                            fsmGame.setState(FSMGame.STATE_DISCONNECTED);
                            break;
                    }
                    break;
                case Constants.MESSAGE_WRITE:
                    break;
                case Constants.MESSAGE_READ:
                    if(!isMaster) {
                        byte[] readBuf = (byte[]) msg.obj;
                        AppMessage recMsg = (AppMessage) Serializer.deserializeObject(readBuf);
                        if (recMsg != null) {
                            switch (recMsg.TYPE){
                                case Constants.MSG_TYPE_INTEGER:
                                    privateNumber = recMsg.OP1 == 0 ? new Integer(1) : new Integer(0);
                                    btnPlay.setEnabled(true);
                                    break;
                                case Constants.MSG_TYPE_SYNC:
                                    //TODO C'è qualcosa di sbagliato qui
                                    //TODO Quando il master esce e rientra, l'altro ha il bottone abilitato
                                    AppMessage notReadyMessage = new AppMessage(Constants.MSG_TYPE_NOREADY);
                                    sendBluetoothMessage(notReadyMessage);
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
                    txtEnemy.setText(mConnectedDeviceName);
                    break;
                case Constants.MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(), msg.getData().getString(Constants.TOAST),
                            Toast.LENGTH_LONG).show();
                    break;
            }
        }
    };

    private final Handler fsmHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case FSMGame.STATE_NOT_READY:
                            break;
                        case FSMGame.STATE_CONNECTED:
                            if(isMaster) btnPlay.setEnabled(true);
                            break;
                        case FSMGame.STATE_IN_GAME:
                            break;
                        case FSMGame.STATE_IN_GAME_WAITING:
                            break;
                        case FSMGame.STATE_DISCONNECTED:
                            stringArrayAdapter.clear();
                            stringArrayAdapter.notifyDataSetChanged();
                            txtEnemy.setText("");
                            btnPlay.setEnabled(false);
                            mConnectedDeviceName = null;
                            privateNumber = null;
                            isMaster = false;
                            break;
                        case FSMGame.STATE_OPPONENT_LEFT:
                            break;
                        case FSMGame.STATE_GAME_ABORTED:
                            privateNumber = null;
                            if(!isMaster){
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

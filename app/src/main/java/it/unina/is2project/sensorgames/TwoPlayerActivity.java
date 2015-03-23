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
import it.unina.is2project.sensorgames.bluetooth.messages.IntegerMessage;
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

    //----------------------------------------------
    // LIFECYCLE METHODS
    //----------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(LIFE_CYCLE, "OnCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_2p);

        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Set the fullscreen window
/*        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();*/

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
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case REQUEST_ENABLE_BT:
                if(resultCode == Activity.RESULT_OK){
                    if(mBluetoothService != null){
                        mBluetoothService = BluetoothService.getBluetoothService(getApplicationContext(), mHandler);
                    }
                    mBluetoothService.start();
                    if(!mStatus){
                        mStatus = true;
                        switchBluetooth.setChecked(true);
                    }
                }
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
            IntegerMessage ballChoise = new IntegerMessage(Constants.MSG_TYPE_SYNC, privateNumber);
            sendMessage(ballChoise);

            Intent mIntent = new Intent(TwoPlayerActivity.this, GamePongTwoPlayer.class);
            mIntent.putExtra("ball", privateNumber);
            mIntent.putExtra("master", intMaster);
            startActivity(mIntent);
        }else{
            Intent mIntent = new Intent(TwoPlayerActivity.this, GamePongTwoPlayer.class);
            mIntent.putExtra("ball", privateNumber);
            mIntent.putExtra("master", intMaster);
            startActivity(mIntent);
        }
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
                //mStatus = true;

                Toast.makeText(getApplicationContext(), getApplicationContext().getString(R.string.toast_bluetoothActived) ,
                        Toast.LENGTH_LONG).show();
            }else{
                // Not covered case
                mStatus = true;
            }
        }else{
            // If buttons is OFF
            if(mStatus){
                // Turn Off bluetooth
                mBluetoothAdapter.disable();
                mStatus = false;
                // Stopping service
                mBluetoothService.stop();

                //Clear bluetooth activity's data
                txtEnemy.setText("");
                stringArrayAdapter.clear();
                stringArrayAdapter.notifyDataSetChanged();

                Toast.makeText(getApplicationContext(),getApplicationContext().getString(R.string.toast_bluetoothDeactived),
                        Toast.LENGTH_LONG).show();
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

    private void sendMessage(IntegerMessage ballChoise) {
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
                            if(isMaster) btnPlay.setEnabled(true);
                            break;
                        case BluetoothService.STATE_CONNECTING:
                            Log.i(TAG, "Connecting...");
                            break;
                        case BluetoothService.STATE_LISTEN:
                            Log.i(TAG, "Listening...");
                            break;
                        case BluetoothService.STATE_NONE:
                            btnPlay.setEnabled(false);
                            mConnectedDeviceName = null;
                            privateNumber = null;
                            txtEnemy.setText("");
                            break;
                    }
                    break;
                case Constants.MESSAGE_WRITE:
                    break;
                case Constants.MESSAGE_READ:
                    if(!isMaster) {
                        byte[] readBuf = (byte[]) msg.obj;
                        IntegerMessage recMsg = (IntegerMessage) Serializer.deserializeObject(readBuf);
                        if (recMsg != null) {
                            if (recMsg.TYPE == Constants.MSG_TYPE_SYNC) {
                                if (recMsg.MESSAGE == 0) {
                                    privateNumber = new Integer(1);
                                } else {
                                    privateNumber = new Integer(0);
                                }
                                btnPlay.setEnabled(true);
                            } else {
                                Log.e(TAG, "Ricevuto messaggio non idoneo - Type is " + recMsg.TYPE);
                            }
                        } else {
                            Log.e(TAG, "Ricevuto messaggio nullo.");
                        }
                        Log.i(TAG, "Numero ricevuto " + recMsg.MESSAGE);
                    }
                    break;
                case Constants.MESSAGE_DEVICE_NAME:
                    // salvo il nome del dispositivo connesso
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
}

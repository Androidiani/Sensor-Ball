package it.unina.is2project.sensorgames.bluetooth;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class BluetoothService implements Cloneable{

    //-----------------------------------------------------------------
    // DICHIARAZIONE VARIABILI E COSTANTI
    //-----------------------------------------------------------------

    // Debugging
    private static final String TAG = "BluetoothService";
    public static int counterThread = 0;

    // Nomi delle connessioni sicure e insicure.
    private static final String NAME_SECURE = "BluetoothSecure";
    private static final String NAME_INSECURE = "BluetoothInsecure";

    // UUID per l'applicazione
    private static final UUID MY_UUID_SECURE =
            UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
    private static final UUID MY_UUID_INSECURE =
            UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");

    // Membri della classe
    private final BluetoothAdapter mAdapter;
    private Handler mHandler;
    private AcceptThread mSecureAcceptThread;
//    private AcceptThread mInsecureAcceptThread;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    private int mState;

    // Sezione costanti
    public static final int STATE_NONE = 0;         // Non stiamo facendo niente
    public static final int STATE_LISTEN = 1;       // Siamo in ascolto per delle connessioni in ingresso
    public static final int STATE_CONNECTING = 2;   // Ci stiamo connettendo
    public static final int STATE_CONNECTED = 3;    // Siamo connessi ad un device

    // Singleton
    private static BluetoothService serviceInstance = null;

    /**
     * Costruttore
     *
     * @param context Il contesto dell'activity chiamante
     * @param handler Un handler per inviare un messaggio all'activity
     */
    private BluetoothService(Context context, Handler handler){
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mHandler = handler;
        setState(STATE_NONE);
    }

    public static BluetoothService getBluetoothService(Context context, Handler handler){
        if(serviceInstance == null){
            serviceInstance = new BluetoothService(context,handler);
        }
        serviceInstance.setmHandler(handler);
        return serviceInstance;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    //-----------------------------------------------------------------
    // FUNZIONI DI STATO
    //-----------------------------------------------------------------

    /**
     * Modifica lo stato corrente della connessione
     *
     * @param state Un intero che identifica un particolare stato di connessione
     */
    private synchronized void setState(int state){
        if(mState == STATE_NONE)
            Log.d(TAG, " setState() " + "First state is " + state);
        else
            Log.d(TAG, " setState() " + mState + " -> " + state);
        mState = state;
        mHandler.obtainMessage(Constants.MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
    }

    /**
     * Return the current connection state.
     */
    public synchronized int getState() {

        return mState;
    }

    /**
     * Set dell'handler
     * @param mHandler
     */
    public void setmHandler(Handler mHandler) {
        this.mHandler = mHandler;
    }

    //-----------------------------------------------------------------
    // FUNZIONI PRINCIPALI:
    // START - CONNECT - CONNECTED - STOP - WRITE - CONNECTION FAILED - CONNECTION LOST
    //-----------------------------------------------------------------

    /**
     * Fa partire il servizio.
     * Istanzia un thread per iniziare l'ascolto di eventuali connessioni.
     * Chiamato da onResume() dell'activity.
     */
    public synchronized void start(){
        Log.d(TAG, " start()");

        // Ferma il thread che sta tentando di stabilire una connessione
        if(mConnectThread != null){
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // Ferma il thread che han stabilito una connessione
        if(mConnectedThread != null){
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // Imposta stato "In Ascolto"
        setState(STATE_LISTEN);

        // Avvia il thread in ascolto per eventuali connessioni (sia in modalità sicura che insicura)
        if(mSecureAcceptThread == null){
            mSecureAcceptThread = new AcceptThread(true);
            mSecureAcceptThread.start();
        }

/*        if(mInsecureAcceptThread == null){
            mInsecureAcceptThread = new AcceptThread(false);
            mInsecureAcceptThread.start();
        }
*/
    }

    /**
     * Avvia una Thread di connessione (ConnectThread) per un device remoto.
     *
     * @param device Il device bluetooth al quale ci vogliamo connettere
     * @param secure Il tipo di socket da aprire
     *               true - Secure
     *               false - Insecure
     */
    public synchronized void connect(BluetoothDevice device, boolean secure){
        Log.d(TAG, " connect to: " + device);

        // Ferma il thread che tenta di stabilire una connessione
        if(mState == STATE_CONNECTING){
            if(mConnectThread != null){
                mConnectThread.cancel();
                mConnectThread = null;
            }
        }

        // Ferma il thread che sta gestendo una connessione
        if(mConnectedThread != null){
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // Avvia il thread per connettere ad un determinato device
        mConnectThread = new ConnectThread(device, secure);
        mConnectThread.start();

        // Imposta lo stato "In Connessione"
        setState(STATE_CONNECTING);

    }

    /**
     * Avvia il thread per la connessione creata (ConnectedThread) per iniziare a gestire una connessione bluetooth
     *
     * @param socket La socket bluetooth dove la connessione è stata stabilita
     * @param device Il device con il quale si è connessi
     */
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device, final String socketType){
        Log.d(TAG, " connected, Socket Type:" + socketType);

        // Ferma il thread che sta stabilendo una connessione
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // Ferma il thread che ha sta gestendo una connessione
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // Ferma il thread che accetta connessioni poiché vogliamo connetterci ad un determinato device
        if (mSecureAcceptThread != null) {
            mSecureAcceptThread.cancel();
            mSecureAcceptThread = null;
        }
/*        if (mInsecureAcceptThread != null) {
            mInsecureAcceptThread.cancel();
            mInsecureAcceptThread = null;
        }
*/

        // Avvia il thread per gestire la connessione
        mConnectedThread = new ConnectedThread(socket, socketType);
        mConnectedThread.start();

        // Invio il nome del device a cui sono connesso alla mainActivity tramite Handler
        Message msg = mHandler.obtainMessage(Constants.MESSAGE_DEVICE_NAME);
        Bundle bundle = new Bundle();
        bundle.putString(Constants.DEVICE_NAME, device.getName());
        msg.setData(bundle);
        mHandler.sendMessage(msg);

        // Imposta lo stato in "Connesso"
        setState(STATE_CONNECTED);
    }

    /**
     * Ferma tutti i threads
     */
    public synchronized void stop() {
        Log.d(TAG, "stop");

        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        if (mSecureAcceptThread != null) {
            mSecureAcceptThread.cancel();
            mSecureAcceptThread = null;
        }

/*        if (mInsecureAcceptThread != null) {
            mInsecureAcceptThread.cancel();
            mInsecureAcceptThread = null;
        }
*/

        // Imposta lo stato a "Nulla"
        setState(STATE_NONE);
    }

    /**
     * Scrive sul buffer di comunicazione in modo asincrono
     *
     * @param out I bytes da scrivere
     */
    public void write(byte[] out){
        // Creo oggetto temporaneo
        ConnectedThread r;
        // Sincronizza una copia del thread di connessione
        synchronized (this){
            if(mState != STATE_CONNECTED) return;
            r = mConnectedThread;
        }
        // Scrivo su buffer
        r.write(out);
        Log.i(TAG,"Message Written By Service");
    }

    /**
     * Indica che il tentativo di connessione è fallito e notifica la main activity
     */
    private void connectionFailed(){
        // Creo un messaggio di fallimento
        Message msg = mHandler.obtainMessage(Constants.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(Constants.TOAST, "Unable to connect device");
        msg.setData(bundle);
        mHandler.sendMessage(msg);

        // Avvio di nuovo il servizio
        BluetoothService.this.stop();
        if(mAdapter.isEnabled()) {
            BluetoothService.this.start();
        }
    }

    /**
     * Indica che la connessione è stata persa e notifica la main activity
     */
    private void connectionLost() {
        // Send a failure message back to the Activity
        Message msg = mHandler.obtainMessage(Constants.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(Constants.TOAST, "Device connection was lost");
        msg.setData(bundle);
        mHandler.sendMessage(msg);

        // Start the service over to restart listening mode
        BluetoothService.this.stop();
        if(mAdapter.isEnabled()) {
            BluetoothService.this.start();
        }
    }

    //-----------------------------------------------------------------
    // THREAD:
    // ACCETTAZIONE RICHIESTE - CONNESSIONE - GESTIONE CONNESSIONE
    //-----------------------------------------------------------------
    /**
     * Questo thread resta in ascolto di eventuali connessioni in ingresso.
     * Esso resterà attivo fin quando un'eccezione viene lanciata o una connessione creata.
     */
    private class AcceptThread extends Thread{
        // Server socket localE
        private final BluetoothServerSocket mmServerSocket;
        private String mSocketType;

        public AcceptThread(boolean secure){
            // Creiamo un oggetto temporaneo di tipo BluetootkServerSocket poichè quello dichiarato è final.
            BluetoothServerSocket tmp = null;
            // Stabiliamo tipo di connessione sicura o non
            mSocketType = secure ? "Secure" : "Insecure";
            try{
                if(secure){
                    tmp = mAdapter.listenUsingRfcommWithServiceRecord(NAME_SECURE, MY_UUID_SECURE);
                }else{
                    tmp = mAdapter.listenUsingInsecureRfcommWithServiceRecord(NAME_INSECURE, MY_UUID_INSECURE);
                }
            }catch (IOException e){
                Log.e(TAG, "Socket Type: " + mSocketType + " listen() failed (" + counterThread + " Thread)", e);
            }
            mmServerSocket = tmp;
            Log.i(TAG, "Socket secure:" + secure + " - Created");

            synchronized (BluetoothService.this){
                counterThread++;
            }
            Log.d(TAG, "Thread Accept(Secure: " + secure + ") Created # " + counterThread);
        }

        @Override
        public void run() {
            Log.d(TAG, "Socket Type: " + mSocketType + " - BEGIN mAcceptThread" + this);
            setName("AcceptThread" + mSocketType);

            BluetoothSocket socket = null;

            while(mState != STATE_CONNECTED){
                try{
                    // Chiamata bloccante per accettare richieste.
                    socket = mmServerSocket.accept();
                }catch(IOException e){
                    Log.e(TAG, " Socket Type: " + mSocketType + " - accept() failed (" + counterThread + " Thread)", e);
                    try {
                        mmServerSocket.close();
                    } catch (IOException e1) {
                        Log.e(TAG, " Socket Type: " + mSocketType + " - close() failed", e);
                    }
                    break;
                }

                // Se una connessione è stata stabilita
                if(socket != null){
                    synchronized (BluetoothService.this){
                        switch (mState){
                            case STATE_LISTEN:
                            case STATE_CONNECTING:
                                // Avvia il thread per la connessione
                                connected(socket, socket.getRemoteDevice(), mSocketType);
                                break;
                            case STATE_NONE:
                            case STATE_CONNECTED:
                                // Non si è pronto o già connessi con la socket. Termina socket
                                try {
                                    socket.close();
                                } catch (IOException e) {
                                    Log.e(TAG, "Could not close unwanted socket (" + counterThread + " Thread)", e);
                                }
                                break;
                        }
                    }
                }
            }
            Log.i(TAG, "END mAcceptThread, socket Type: " + mSocketType);
        }

        public void cancel() {
            Log.d(TAG, "Socket Type:" + mSocketType + " cancel " + this);

            try {
                mmServerSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Socket Type:" + mSocketType + " close() of server failed (" + counterThread + " Thread)", e);
            }
            synchronized (BluetoothService.this){
                counterThread--;
            }
        }
    }

    /**
     * Questo thread resterà attivo tentando di stabilire una connessione in uscita.
     * Verrà interrotto a seguito di una connessione stabilita oppure di un'eccezione lanciata
     */
    private class ConnectThread extends Thread{
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;
        private String mSocketType;

        public ConnectThread(BluetoothDevice device, boolean secure) {
            mmDevice = device;

            mSocketType = secure ? "Secure" : "Insecure";

            // Creiamo un oggetto temporaneo di tipo BluetootkSocket poichè quello dichiarato è final.
            BluetoothSocket tmp = null;

            // Ottieni una socket per la connessione con un dato dispositivo.
            try{
                if(secure){
                    tmp = device.createRfcommSocketToServiceRecord(MY_UUID_SECURE);
                }else{
                    tmp = device.createInsecureRfcommSocketToServiceRecord(MY_UUID_INSECURE);
                }
            }catch (IOException e){
                Log.e(TAG, "Socket Type: " + mSocketType + " create() failed (" + counterThread + " Thread)", e);
            }
            mmSocket = tmp;

            synchronized (BluetoothService.this){
                counterThread++;
            }
            Log.d(TAG, "Thread Connect Created # " + counterThread);
        }

        @Override
        public void run() {
            Log.i(TAG, " BEGIN mConnectThread SocketType:" + mSocketType);
            setName("ConnectThread" + mSocketType);

            // Fermo la scoperta poiché impiega molte risorse
            mAdapter.cancelDiscovery();

            // Tento una connessione alla socket
            try {
                // Chiamata bloccante
                mmSocket.connect();
            }catch (IOException e){
                // Chiudi la socket
                try {
                    mmSocket.close();
                } catch (IOException e1) {
                    Log.e(TAG, "unable to close() " + mSocketType + " socket during connection failure (" + counterThread + " Thread)", e1);
                }
                connectionFailed();
                return;
            }

            // Reset del thread di connessione poichè abbiamo finito
            synchronized (BluetoothService.this){
                mConnectThread = null;
            }

            // Avvio il thread per la gestione della connessione
            connected(mmSocket, mmDevice, mSocketType);
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, " close() of connect " + mSocketType + " socket failed (" + counterThread + " Thread)", e);
            }
            synchronized (BluetoothService.this){
                counterThread--;
            }
        }
    }

    /**
     * Questo è il thread che gestisce la connessione bluetooth avvenuta con un dispositivo
     * Gestisce connessioni in ingresso ed uscita
     */

    private class ConnectedThread extends Thread{

        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket, String socketType) {
            Log.d(TAG, "create ConnectedThread: " + socketType);
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try{
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            }catch (IOException e){
                Log.e(TAG, " temp sockets not created", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;

            synchronized (BluetoothService.this){
                counterThread++;
            }
            Log.d(TAG, "Thread Connected Created # " + counterThread);
        }

        @Override
        public void run() {
            Log.i(TAG, " BEGIN mConnectedThread");
            // Buffer per scambio dati
            byte[] buffer = new byte[1024];
            // Numero di bytes letti
            int bytes;

            // Lettura continua dell'inputStream fin quando siamo connessi
            while (true){
                try{
                    // Lettura dall'imput stream
                    bytes = mmInStream.read(buffer);

                    // Mando il messaggio alla UI
                    mHandler.obtainMessage(Constants.MESSAGE_READ, bytes, -1, buffer).sendToTarget();
                }catch (IOException e){
                    Log.e(TAG, "disconnected while reading", e);
                    connectionLost();
                    break;
                }
            }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, " close() of connect socket failed", e);
            }
            synchronized (BluetoothService.this){
                counterThread--;
            }
        }

        /**
         * Scrive sul buffer di connessione in uscita.
         *
         * @param buffer Stream di byte del messaggio da spedire
         */
        public void write(byte[] buffer) {
            try {
                mmOutStream.write(buffer);
                mHandler.obtainMessage(Constants.MESSAGE_WRITE, -1, -1, buffer).sendToTarget();
            } catch (IOException e) {
                Log.e(TAG, " Exception during write", e);
            }
        }
    }
}

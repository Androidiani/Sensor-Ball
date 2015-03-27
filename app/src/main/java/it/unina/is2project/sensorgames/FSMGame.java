package it.unina.is2project.sensorgames;

import android.os.Handler;
import android.util.Log;

import it.unina.is2project.sensorgames.bluetooth.Constants;

public class FSMGame implements Cloneable {

    // Debug
    private final String TAG = "FSMGame";

    // States
    public final static int STATE_NOT_READY = 111;
    public final static int STATE_CONNECTED = 222;
    public final static int STATE_IN_GAME_WAITING = 333;
    public final static int STATE_IN_GAME = 444;
    public final static int STATE_OPPONENT_LEFT = 555;
    public final static int STATE_DISCONNECTED = 666;
    public final static int STATE_GAME_ABORTED = 777;


    // Private fields
    private static FSMGame fsmInstance = null;
    private Handler handler;
    private int state;

    private FSMGame(Handler handler) {
        this.handler = handler;
        this.state = STATE_NOT_READY;
    }

    public static FSMGame getFsmInstance(Handler handler){
        if(fsmInstance == null){
            fsmInstance = new FSMGame(handler);
        }
        fsmInstance.setHandler(handler);
        return fsmInstance;
    }

    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    public void setState(int state) {
        Log.d(TAG, "Set State From " + this.state + " to " + state);
        this.state = state;
        handler.obtainMessage(Constants.MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
    }

    public int getState() {
        return state;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }
}

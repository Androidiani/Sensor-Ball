package it.unina.is2project.sensorgames;

import android.os.Handler;
import android.util.Log;

import it.unina.is2project.sensorgames.bluetooth.Constants;

public class FSMGame implements Cloneable {

    // Debug
    private final String TAG = "FSMGame";

    // States
    public final static int STATE_NOT_READY = 500;
    public final static int STATE_READY = 501;
    public final static int STATE_CONNECTED = 502;
    public final static int STATE_IN_GAME_WAITING = 503;
    public final static int STATE_IN_GAME = 504;
    public final static int STATE_OPPONENT_LEFT = 505;
    public final static int STATE_DISCONNECTED = 506;

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

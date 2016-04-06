package it.unina.is2project.sensorball.bluetooth;

import android.os.Handler;
import android.util.Log;

public class FSMGame implements Cloneable {

    // Debug
    private final String TAG = "FSMGame";

    // States
    public final static int STATE_NOT_READY = 111;
    public final static int STATE_CONNECTING = 200;
    public final static int STATE_CONNECTED = 222;
    public final static int STATE_IN_GAME_WAITING = 333;
    public final static int STATE_IN_GAME = 444;
    public final static int STATE_OPPONENT_LEFT = 555;
    public final static int STATE_OPPONENT_NOT_READY = 505;
    public final static int STATE_DISCONNECTED = 666;
    public final static int STATE_GAME_ABORTED = 777;
    public final static int STATE_GAME_PAUSED = 800;
    public final static int STATE_GAME_PAUSE_STOP = 404;
    public final static int STATE_GAME_OPPONENT_PAUSED = 880;
    public final static int STATE_GAME_SUSPENDED = 900;
    public final static int STATE_GAME_WINNER = 489;
    public final static int STATE_GAME_LOSER = 490;

    // Private fields
    private static FSMGame fsmInstance = null;
    private Handler handler;
    private int state;

    private FSMGame(Handler handler) {
        Log.d(TAG, "Private Constructor");
        this.handler = handler;
        this.state = STATE_NOT_READY;
    }

    @Override
    public String toString() {
        String result;
        switch (state) {
            case STATE_CONNECTED:
                result = "STATE_CONNECTED";
                break;
            case STATE_CONNECTING:
                result = "STATE_CONNECTING";
                break;
            case STATE_DISCONNECTED:
                result = "STATE_DISCONNECTED";
                break;
            case STATE_GAME_ABORTED:
                result = "STATE_GAME_ABORTED";
                break;
            case STATE_GAME_OPPONENT_PAUSED:
                result = "STATE_GAME_OPPONENT_PAUSED";
                break;
            case STATE_GAME_PAUSED:
                result = "STATE_GAME_PAUSED";
                break;
            case STATE_IN_GAME:
                result = "STATE_IN_GAME";
                break;
            case STATE_IN_GAME_WAITING:
                result = "STATE_IN_GAME_WAITING";
                break;
            case STATE_OPPONENT_LEFT:
                result = "STATE_OPPONENT_LEFTo";
                break;
            case STATE_NOT_READY:
                result = "STATE_NOT_READY";
                break;
            case STATE_GAME_WINNER:
                result = "STATE_GAME_WINNER";
                break;
            case STATE_GAME_LOSER:
                result = "STATE_GAME_LOSER";
                break;
            case STATE_GAME_PAUSE_STOP:
                result = "STATE_GAME_PAUSE_STOP";
                break;
            case STATE_GAME_SUSPENDED:
                result = "STATE_GAME_SUSPENDED";
                break;
            default:
                result = "default";
                break;
        }
        return result;
    }

    public static FSMGame getFsmInstance(Handler handler) {
        if (fsmInstance == null) {
            fsmInstance = new FSMGame(handler);
        }
        fsmInstance.setHandler(handler);
        return fsmInstance;
    }

    private void setHandler(Handler handler) {
        this.handler = handler;
    }

    public void setState(int state) {
        int previousState = this.state;
        this.state = state;
        Log.d(TAG, "Set State to " + this.toString());
        handler.obtainMessage(Constants.MESSAGE_STATE_CHANGE, state, previousState).sendToTarget();
    }

    public int getState() {
        return state;
    }

    public static String toStringDebug(int theState) {
        String result;
        switch (theState) {
            case STATE_CONNECTED:
                result = "STATE_CONNECTED";
                break;
            case STATE_CONNECTING:
                result = "STATE_CONNECTING";
                break;
            case STATE_DISCONNECTED:
                result = "STATE_DISCONNECTED";
                break;
            case STATE_GAME_ABORTED:
                result = "STATE_GAME_ABORTED";
                break;
            case STATE_GAME_OPPONENT_PAUSED:
                result = "STATE_GAME_OPPONENT_PAUSED";
                break;
            case STATE_GAME_PAUSED:
                result = "STATE_GAME_PAUSED";
                break;
            case STATE_IN_GAME:
                result = "STATE_IN_GAME";
                break;
            case STATE_IN_GAME_WAITING:
                result = "STATE_IN_GAME_WAITING";
                break;
            case STATE_OPPONENT_LEFT:
                result = "STATE_OPPONENT_LEFTo";
                break;
            case STATE_NOT_READY:
                result = "STATE_NOT_READY";
                break;
            case STATE_GAME_WINNER:
                result = "STATE_GAME_WINNER";
                break;
            case STATE_GAME_LOSER:
                result = "STATE_GAME_LOSER";
                break;
            case STATE_GAME_PAUSE_STOP:
                result = "STATE_GAME_PAUSE_STOP";
                break;
            case STATE_GAME_SUSPENDED:
                result = "STATE_GAME_SUSPENDED";
                break;
            default:
                result = "default";
                break;
        }
        return result;
    }

    @SuppressWarnings("CloneDoesntCallSuperClone")
    @Override
    protected Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }
}

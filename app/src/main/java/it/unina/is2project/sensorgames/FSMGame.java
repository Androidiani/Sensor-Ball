package it.unina.is2project.sensorgames;

import android.os.Handler;
import android.util.Log;

import it.unina.is2project.sensorgames.bluetooth.Constants;

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
    @Deprecated
    public final static int STATE_GAME_EXIT_PAUSE = 888;
    public final static int STATE_GAME_WINNER = 489;
    public final static int STATE_GAME_LOSER = 490;



    // Private fields
    private static FSMGame fsmInstance = null;
    private Handler handler;
    private int state;

    private FSMGame(Handler handler) {
        this.handler = handler;
        this.state = STATE_NOT_READY;
    }

    @Override
    public String toString() {
        String result;
        switch (state){
            case STATE_CONNECTED:
                result = "Connesso";
                break;
            case STATE_CONNECTING:
                result = "In Connessione";
                break;
            case STATE_DISCONNECTED:
                result = "Disconnesso";
                break;
            case STATE_GAME_ABORTED:
                result = "Gioco abortito";
                break;
            case STATE_GAME_EXIT_PAUSE:
                result = "Uscita da pausa";
                break;
            case STATE_GAME_OPPONENT_PAUSED:
                result = "Pausa avversario";
                break;
            case STATE_GAME_PAUSED:
                result = "Pausa";
                break;
            case STATE_IN_GAME:
                result = "In gioco";
                break;
            case STATE_IN_GAME_WAITING:
                result = "In attesa di gioco";
                break;
            case STATE_OPPONENT_LEFT:
                result = "Avversario ritirato";
                break;
            case STATE_NOT_READY:
                result = "Non pronto";
                break;
            case STATE_GAME_WINNER:
                result = "Vincitore";
                break;
            case STATE_GAME_LOSER:
                result = "Sconfitto";
                break;
            case STATE_GAME_PAUSE_STOP:
                result = "Pausa OnStop";
                break;
            case STATE_GAME_SUSPENDED:
                result = "Gioco Sospeso";
                break;
            default:
                result = "default";
                break;
        }
        return result;
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
        this.state = state;
        Log.d(TAG, "Set State to " + this.toString());
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

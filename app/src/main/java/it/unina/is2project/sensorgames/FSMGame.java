package it.unina.is2project.sensorgames;

import android.os.Handler;

import it.unina.is2project.sensorgames.bluetooth.Constants;

public class FSMGame implements Cloneable {

    private static FSMGame fsmInstance = null;
    private Handler handler;
    private int state;

    private FSMGame(Handler handler) {
        this.handler = handler;
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

package it.unina.is2project.sensorgames.bluetooth.messages;

import java.io.Serializable;

public class IntegerMessage implements Serializable {

    public int TYPE;
    public int MESSAGE;

    public IntegerMessage(int type, Integer message){
        this.TYPE = type;
        this.MESSAGE = message;
    }

}

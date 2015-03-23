package it.unina.is2project.sensorgames.bluetooth.messages;

import java.io.Serializable;

public class CoordsMessage implements Serializable{

    public int TYPE;
    public float VELOCITY_X;
    public float VELOCITY_Y;
    public float X_RATIO;

    public CoordsMessage(int type, float velocityX, float velocityY, float xRatio) {
        this.TYPE = type;
        this.VELOCITY_X = velocityX;
        this.VELOCITY_Y = velocityY;
        this.X_RATIO = xRatio;
    }
}

package it.unina.is2project.sensorgames.bluetooth.messages;

import java.io.Serializable;

public class AppMessage implements Serializable {
    public int TYPE;    // Type
    public int OP1;     // Integer
    public float OP2;   // VelocityX
    public float OP3;   // Angolo
    public float OP4;   // X Ratio

    public AppMessage(int TYPE) {
        this.TYPE = TYPE;
    }

    public AppMessage(int TYPE, int OP1) {
        this.TYPE = TYPE;
        this.OP1 = OP1;
    }

    public AppMessage(int TYPE, float OP2, float OP3, float OP4) {
        this.TYPE = TYPE;
        this.OP2 = OP2;
        this.OP3 = OP3;
        this.OP4 = OP4;
    }
}

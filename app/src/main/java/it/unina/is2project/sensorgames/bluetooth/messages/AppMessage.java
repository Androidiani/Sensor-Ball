package it.unina.is2project.sensorgames.bluetooth.messages;

import java.io.Serializable;

public class AppMessage implements Serializable {
    public int TYPE;    // Type         // Type         // Type
    public int OP1;     //              // Integer      // Sign(VelocityX)
    public float OP2;   //              //              // COS_X
    public float OP3;   //              //              // SIN_X
    public float OP4;   //              //              // X Ratio

    public AppMessage(int TYPE) {
        this.TYPE = TYPE;
    }

    public AppMessage(int TYPE, int OP1) {
        this.TYPE = TYPE;
        this.OP1 = OP1;
    }

    /**
     * Generally a coords message
     * @param TYPE Type of sent message
     * @param OP1
     * @param OP2
     * @param OP3
     * @param OP4
     */
    public AppMessage(int TYPE, float OP1, float OP2, float OP3, float OP4) {
        this.TYPE = TYPE;
        this.OP1 = (int)OP1;
        this.OP2 = OP2;
        this.OP3 = OP3;
        this.OP4 = OP4;
    }
}

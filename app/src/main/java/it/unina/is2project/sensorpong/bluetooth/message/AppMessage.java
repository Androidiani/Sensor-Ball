package it.unina.is2project.sensorpong.bluetooth.message;

import java.io.Serializable;

public class AppMessage implements Serializable {
    //Types:              // Single Type  // Integer Type // Bonus Actived        // Coords Message
    public int TYPE;      // Type         // Type         // Type                 // Type
    public int OP1;       //              // Integer      // Bonus Reach Count    // Sign(VelocityX)
    public float OP2;     //              //              //                      // COS_X
    public float OP3;     //              //              //                      // SIN_X
    public float OP4;     //              //              //                      // X Ratio
    public int OP5;       //              // Points       //                      //

    public AppMessage(int TYPE) {
        this.TYPE = TYPE;
    }


    public AppMessage(int TYPE, int OP1) {
        this.TYPE = TYPE;
        this.OP1 = OP1;
    }

    public AppMessage(int TYPE, int OP1, int OP5) {
        this.TYPE = TYPE;
        this.OP1 = OP1;
        this.OP5 = OP5;
    }

    /**
     * Generally a coords message
     *
     * @param TYPE Type of sent message
     * @param OP1  Sign of velocityX
     * @param OP2  Cosine(angle)
     * @param OP3  Sine(angle)
     * @param OP4  X Ratio (adapted by screen)
     */
    public AppMessage(int TYPE, float OP1, float OP2, float OP3, float OP4) {
        this.TYPE = TYPE;
        this.OP1 = (int) OP1;
        this.OP2 = OP2;
        this.OP3 = OP3;
        this.OP4 = OP4;
    }
}

package it.unina.is2project.sensorgames.bluetooth;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class Serializer {

    public Serializer() {
    }

    public static byte[] serializeObject(Object o) {

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream os = null;
        try {
            os = new ObjectOutputStream(out);
            os.writeObject(o);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return out.toByteArray();
    }

    public static Object deserializeObject(byte[] b) {

        ByteArrayInputStream in = new ByteArrayInputStream(b);
        ObjectInputStream is = null;
        Object object = null;
        try {
            is = new ObjectInputStream(in);
            object = is.readObject();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return object;
    }

}

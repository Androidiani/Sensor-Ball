package it.unina.is2project.sensorball.bluetooth.message;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class Serializer {

    public static byte[] serializeObject(Object o) {

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream os;
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
        ObjectInputStream is;
        Object object = null;
        try {
            is = new ObjectInputStream(in);
            object = is.readObject();

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return object;
    }

}

package cereal.examples;

import jaci.openrio.toast.core.Toast;
import jssc.SerialPortException;

import java.util.Arrays;

public class SerialExample extends SerialListener {

    SerialPortWrapper sPort;

    public void register() throws SerialPortException {                 // Call this from your main module
        sPort = Cereal.getPort("PORTID", 9600, 8, 1, 0);
        sPort.registerListener(this);

        expect(10);
    }

    public void send(byte[] data) throws SerialPortException {          // Write some data
        sPort.writeBytes(data);
    }

    @Override
    public void onSerialData(byte[] data) {
        Toast.log().info("I got some bytes, 10 of them! " + Arrays.toString(data));
    }
}

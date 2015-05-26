package cereal.examples;

import jaci.openrio.toast.core.Toast;
import jssc.SerialPortException;

import java.util.Arrays;

public class DynamicSerialExample extends SerialListener {

    SerialPortWrapper sPort;
    boolean request_cycle;

    public void register() throws SerialPortException {                 // Call this from your main module
        sPort = Cereal.getPort("PORTID", 9600, 8, 1, 0);
        sPort.registerListener(this);

        request_cycle = true;
        expect(1);              // Set the initial expecting bytes to '1'
    }

    // What does this do? It takes the first byte and uses it as the 'length' of the next set of incoming bytes. When those bytes are received, we
    // take the next byte as the length, and run the cycle all over again. This allows us to 'dynamically' change the amount of bytes we're expecting
    // from the Serial Port

    @Override
    public void onSerialData(byte[] data) {
        if (request_cycle) {
            expect(data[0]);
            request_cycle = false;
        } else {
            Toast.log().info("I got some bytes! " + Arrays.toString(data));
            request_cycle = true;
        }
    }
}

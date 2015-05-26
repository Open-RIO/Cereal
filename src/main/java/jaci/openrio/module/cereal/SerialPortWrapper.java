package jaci.openrio.module.cereal;

import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides an implementation to allow multiple modules to use the same Serial Port. This is where all port logic is done
 * after the port is assigned.
 *
 * @author Jaci
 */
public class SerialPortWrapper extends SerialPort {

    int baud, data, stop, parity;
    final Object lock = new Object();
    List<SerialListener> listeners = new ArrayList<SerialListener>();

    protected SerialPortWrapper(String portName) {
        super(portName);
    }

    protected void setEvents() throws SerialPortException {
        int mask = SerialPort.MASK_RXCHAR + SerialPort.MASK_CTS + SerialPort.MASK_DSR;
        setEventsMask(mask);
        addEventListener(new SerialPortEventListener() {
            @Override
            public void serialEvent(SerialPortEvent serialPortEvent) {
                if (serialPortEvent.isRXCHAR()) {
                    try {
                        synchronized (lock) {
                            byte[] b = readBytes(1);
                            for (SerialListener listener : listeners)
                                listener.serial_push_bytes(b[0]);
                        }
                    } catch (SerialPortException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    /**
     * Set the parameters for the Serial Port.
     * @param baudRate The baud rate (bits per second). This is usually 9600 for things like Arduino, or
     *                 115200 for things like the Raspberry Pi. Options can be found in {@link jssc.SerialPort}
     * @param dataBits The data bits for the port. This is usually 8 {@link jssc.SerialPort#DATABITS_8}. Options can be found in {@link jssc.SerialPort}
     * @param stopBits The stop bits for the port. This is usually 1 {@link jssc.SerialPort#STOPBITS_1}. Options can be found in {@link jssc.SerialPort}
     * @param parity The parity for the port. This is usually 0 {@link jssc.SerialPort#PARITY_NONE}. Options can be found in {@link jssc.SerialPort}
     * @return Whether the parameter setting was successful
     * @throws SerialPortException If the port is closed or other port access errors
     */
    public boolean setParams(int baudRate, int dataBits, int stopBits, int parity) throws SerialPortException {
        baud = baudRate; data = dataBits; stop = stopBits; this.parity = parity;
        return super.setParams(baudRate, dataBits, stopBits, parity);
    }

    /**
     * Returns true if the given parameters meet those that were set in {@link #setParams(int, int, int, int)}
     */
    public boolean check(int baudRate, int dataBits, int stopBits, int parity) {
        return this.baud == baudRate && this.data == dataBits && this.stop == stopBits && this.parity == parity;
    }

    /**
     * Register a SerialListener to the port. This is where all Serial Port listening is done.
     * @param listener The {@link jaci.openrio.module.cereal.SerialListener} to listen on
     * @return The same serial listener passed into the params for easy chaining
     */
    public SerialListener registerListener(SerialListener listener) {
        synchronized (lock) {
            listeners.add(listener);
            return listener;
        }
    }
}

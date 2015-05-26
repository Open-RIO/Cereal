package jaci.openrio.module.cereal;

/**
 * The base class for an object that wishes to listen for input on a Serial Port.
 */
public abstract class SerialListener {

    int serial_expecting_bytes = 0;
    byte[] serial_byte_cache;
    int serial_byte_index = 0;

    /**
     * Sets the amount of bytes to 'expect' from the Serial Port. When the buffer reaches this size, {@link #onSerialData(byte[])}
     * is called and the byte buffer flushed. This value can be changed each time {@link #onSerialData(byte[])} is called, allowing
     * you to expect a different amount of bytes after each 'packet'
     *
     * @param bytes The amount of bytes to expect from the Serial Port
     */
    public void expect(int bytes) {
        this.serial_expecting_bytes = bytes;
        this.serial_byte_cache = new byte[serial_expecting_bytes];
    }

    protected void serial_push_bytes(byte b) {
        if (serial_byte_cache != null) {
            serial_byte_cache[serial_byte_index] = b;
            serial_byte_index++;
            if (serial_byte_index == serial_expecting_bytes) {
                onSerialData(serial_byte_cache);
                serial_byte_cache = new byte[serial_expecting_bytes];
                serial_byte_index = 0;
            }
        }
    }

    /**
     * Called when the expected amount of bytes is received and stored in the buffer. After this method is called, the
     * buffer is flushed.
     * @param data The data stored in the buffer.
     */
    public abstract void onSerialData(byte[] data);

    /**
     * Get the data stored in the byte buffer. Keep in mind that the data in the buffer will be flushed when
     * {@link #onSerialData(byte[])} is called, so there is no reliable way of knowing what is stored in the buffer.
     */
    public byte[] getSerialData() {
        return serial_byte_cache;
    }
}

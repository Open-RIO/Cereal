package jaci.openrio.module.cereal;

import jaci.openrio.toast.lib.crash.CrashHandler;
import jaci.openrio.toast.lib.log.Logger;
import jaci.openrio.toast.lib.module.ToastModule;
import jssc.SerialPortException;
import jssc.SerialPortList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * The Cereal Serial module. When using Toast, it's recommended this module is used instead of WPILib's Serial Port implementation for the following reasons.
 * 1) Simulation. In simulated environments, WPILib's Serial Port implementation will not work due to the system Toast is running on can vary. In these cases,
 * the OS is not the same as the RoboRIO, meaning WPILib's implementation fails to work.
 * 2) Port Count. In WPILib's Serial Port implementation, only one (1) Serial Port on each interface can be used. Since the RoboRIO has 2 USB ports, this means only
 * 1 device can be used on the USB ports. Not to mention more devices can be added via a USB hub. Cereal can utilize any amount of Serial Port devices.
 * 3) Independence. In WPILib's implementation, if multiple modules try to use the same serial port, we run into a 'race-condition' like scenario. Basically, the first
 * module to call .read() will get the byte, while the other module will not. To fix this, each listener on Cereal has their own set of bytes. When a byte it received on the
 * Serial Port, it is duplicated into the buffer of all the listeners. Listeners can call .expect() and specify a byte count. When this byte count is reached, the onSerialData()
 * method is called and the buffer flushed. This makes sure all modules receive all the bytes being send over the Serial port.
 *
 * @author Jaci
 */
public class Cereal extends ToastModule {

    static List<String> ports;
    static Logger logger;
    static HashMap<String, SerialPortWrapper> portWrappers;
    public static boolean preinit;
    public static Cereal instance;

    @Override
    public String getModuleName() {
        return "Cereal";
    }

    @Override
    public String getModuleVersion() {
        return "0.1.0";
    }

    @Override
    public void prestart() {

    }

    @Override
    public void start() { }

    public static void s_preinit() {
        try {
            if (!preinit) {
                preinit = true;
                logger = new Logger("Cereal", Logger.ATTR_DEFAULT);
                portWrappers = new HashMap<String, SerialPortWrapper>();
                CrashHandler.registerProvider(new CerealCrashInfo());
                refresh();
            }
        } catch (Exception e) {
            logger.error("Could not initialize Serial Ports: " + e);
            logger.exception(e);
        }
    }

    public void preinit() {
        s_preinit();
    }

    /**
     * Refresh the list of available ports and remove all the ports that are no longer
     * connected to the system.
     */
    public static void refresh() {
        List<String> portsOld = ports;
        ports = new ArrayList(Arrays.asList(SerialPortList.getPortNames()));
        if (portsOld != null) {
            portsOld.removeAll(ports);
            for (String port : portsOld)
                portWrappers.remove(port);
        }
    }

    /**
     * Get a list of all the available serial ports connected to the system.
     */
    public static List<String> getAvailablePorts() {
        return ports;
    }

    /**
     * Get a {@link jaci.openrio.module.cereal.SerialPortWrapper} with the given name and data. If the serial port is already registered,
     * it will be returned. If not, the port will be created and opened.
     *
     * @param port_name The port name to use. This is usually given by {@link #getAvailablePorts()}
     * @param baudRate The baud rate (bits per second). This is usually 9600 for things like Arduino, or
     *                 115200 for things like the Raspberry Pi. Options can be found in {@link jssc.SerialPort}
     * @param dataBits The data bits for the port. This is usually 8 {@link jssc.SerialPort#DATABITS_8}. Options can be found in {@link jssc.SerialPort}
     * @param stopBits The stop bits for the port. This is usually 1 {@link jssc.SerialPort#STOPBITS_1}. Options can be found in {@link jssc.SerialPort}
     * @param parity The parity for the port. This is usually 0 {@link jssc.SerialPort#PARITY_NONE}. Options can be found in {@link jssc.SerialPort}
     * @return The Serial Port. This will return an existing Port if it exists in the map, or a new one if not. Keep in mind this opens the Serial Port.
     * @throws SerialPortException Either the port does not exist, or the baudRate, dataBits, stopBits and parity are not equal to a port already existing in the map
     */
    public static SerialPortWrapper getPort(String port_name, int baudRate, int dataBits, int stopBits, int parity) throws SerialPortException {
        if (!ports.contains(port_name))
            throw new SerialPortException(port_name, "getPort()", "Port does not exist");
        if (portWrappers.containsKey(port_name)) {
            SerialPortWrapper wrapper = portWrappers.get(port_name);
            if (!wrapper.check(baudRate, dataBits, stopBits, parity))
                throw new SerialPortException(port_name, "getPort()", "Port already exists, but does not match the given parameters. Call destory() to clear this port.");
            return wrapper;
        }
        SerialPortWrapper wrapper = new SerialPortWrapper(port_name);
        wrapper.openPort();
        wrapper.setParams(baudRate, dataBits, stopBits, parity);
        wrapper.setEvents();
        portWrappers.put(port_name, wrapper);
        return wrapper;
    }

    /**
     * Get a {@link jaci.openrio.module.cereal.SerialPortWrapper} with the given name. If the port does not exist in the map, an exception will be thrown. Use
     * {@link #getPort(String, int, int, int, int)} if you wish to create a new port if it does not exist.
     *
     * @param port_name The port name to use. This is usually given by {@link #getAvailablePorts()}
     * @return The Serial Port
     * @throws SerialPortException The port is not yet registered. Call {@link #getPort(String, int, int, int, int)} to create it.
     */
    public static SerialPortWrapper getPort(String port_name) throws SerialPortException {
        if (!portWrappers.containsKey(port_name))
            throw new SerialPortException(port_name, "getPort()", "Port is not registered");
        else
            return portWrappers.get(port_name);
    }

    /**
     * Remove a registered port from the map. Use this if you are certain the port is not being used by anything, or if you want to change the baud rate,
     * stop bits, data bits or parity of an existing port. This will also close the port if it is open
     *
     * @param port_name The port to destroy
     * @throws SerialPortException If there was an error while closing the port.
     */
    public static void destroy(String port_name) throws SerialPortException {
        if (portWrappers.containsKey(port_name)) {
            SerialPortWrapper wrapper = portWrappers.get(port_name);
            if (wrapper.isOpened()) wrapper.closePort();
            portWrappers.remove(port_name);
        }
    }

}

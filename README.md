# Cereal
A Serial Port implementation that doesn't suck.

# Wait, doesn't WPILib have Serial Ports?
Yes, but it's not done well. Here's a list of reasons why.

### Simulation
WPILib's Serial Port implementation doesn't work in simulation. Plain and simple. WPILib's serial port implementation is based around the RoboRIO's Linux OS with a patch, so trying to run Serial Ports on Windows, Mac and most Linux platforms in simulation just plain won't work, and will probably crash. A lot. To try and get the most rock-solid support for serial ports possible, we're using the 'jssc' library to ensure we get native support on all platforms.

### Port Count
WPILib's implementation was designed to use one (1) Serial Port per interface (onboard, usb, mxp). This means that if you have multiple serial port devices, you end up crashing and burning, not knowing which device you're sending to, if any at all. Needless to say, this is an issue.

### Independence
Toast is modular. WPILib is not. See the problem yet? WPILib's Serial Port implementation, while straight forward, just plain won't work for modules. If 2 modules try to grab the same serial port, it turns into a race condition of 'Who can read from the port first gets the data'. This ends up in a mangled stream of data that can cause issues with multiple serial ports acting at once. To fix this, we give each module it's own instance of a Serial Port if it wants one. When data is received on the Serial Port, it is duplicated into each module's listener. This allows each module to get the FULL stream of data from the Serial Port. Everyone's happy.  

# How do I get it?
Modify your ``` build.gradle ``` to represent the following. Note that the ``` dependencies { } ``` block probably already exists.
```groovy
dependencies {
  compile group: 'jaci.openrio.cereal', name: 'Cereal', version: '+'
}
```

# Okay, how steep's the learning curve?
About as steep as a nicely paved road. Let's get started.  
First, register your Serial Port.
```java
SerialPortWrapper serial_port = Cereal.getPort("port_id", baud, data, stop, parity);
```
Usually your baud is something like 9600 (arduino, 115200 for the Raspberry Pi), data 8, stop 1 and parity 0. If you don't know your port_id, call ```Cereal.getAvailablePorts()```, usually it's COM# on Windows, and tty.something on Mac/Linux.  

Now, get started!
```java
serial_port.registerListener(this);           // Make sure the class you're calling this from extends 'SerialListener'
```
Now, from your SerialListener class...
```java
expect(num_bytes);

public void onSerialData(byte[] data) {
  //Handle your data here
}
```

``` expect ``` will define how many bytes to read before calling ``` onSerialData ```. If you want to read one byte at a time, just set this to 1.

** That's it. ** You can go home now

# Nice Pun
Thanks, I try my hardest to make my own fun whilst getting angry at java

# Horrible Pun
<3 you too

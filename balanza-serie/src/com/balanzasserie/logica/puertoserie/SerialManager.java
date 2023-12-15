package com.balanzasserie.logica.puertoserie;

import com.balanzasserie.logica.PropertiesManager;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.TooManyListenersException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.comm.CommDriver;
import javax.comm.CommPortIdentifier;

public class SerialManager {

    private static HashMap<String, SerialManager> lstInsRW = new HashMap<String, SerialManager>();
    private SerialConnection sCon;
    private boolean isInitialized = false;

    public static SerialManager getInstance(SerialParameters parameters) throws Exception {
        SerialManager inst = lstInsRW.get(parameters.getPortName());

        if (inst == null) {
            synchronized (lstInsRW) {
                inst = lstInsRW.get(parameters.getPortName());

                if (inst == null) {
                    inst = new SerialManager(parameters);
                    lstInsRW.put(parameters.getPortName(), inst);
                }
            }
        }

        // deferring initialization since it is opens the port and may block
        // till port is ready and thus if we can't keep it inside the above
        // synch block.
        inst.initialize();

        return inst;
    }


    /**
     * Connects to the serial port provided in the 'comPort' parameter and
     * listen for commands and can be used to write commands. only one instance
     * of this class can be created for each port at the same time unless we
     * call 'close()'
     *
     * @param args command line arguments used when program was invoked.
     * @throws Exception
     */
    private SerialManager(SerialParameters parameters) {
        SerialPortProcessor proc = new SerialPortProcessor(parameters.getPortName());
        sCon = new SerialConnection(parameters, proc);
    }

    private synchronized void initialize() throws Exception {
        if (!isInitialized) {
            sCon.openConnection();
            isInitialized = true;
        }
    }


    /**
     * Low level commands can be sent using this method. if commands are sent
     * back to PLM and aren't recognized by 'IsteonSerialPortProcessor then
     * commands are added to the 'ignoredInsteonCmdQueue'
     *
     * @param b
     */
    public synchronized void writeBytes(byte[] b) {
        sCon.writeBytes(b);

    }

    public void close() {
        sCon.closeConnection();
    }

    public void addDataListener(final SerialEventProcessor eventProcessor) throws TooManyListenersException {
        sCon.addDataListener(eventProcessor);
    }

    public void removeDataListener() {
        sCon.removeDataListener();
    }

    public static List<String> listarPuertosDisponibles() {
        List<String> ports = new ArrayList<String>();
        try {
            CommDriver commdriver = (CommDriver) Class.forName("com.sun.comm.Win32Driver").newInstance();
            commdriver.initialize();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Enumeration<?> e = CommPortIdentifier.getPortIdentifiers();
        System.out.println("Puertos series disponibles: ");
        while (e.hasMoreElements()) {
            CommPortIdentifier cpId = (CommPortIdentifier) e.nextElement();
            if (cpId.getPortType() == CommPortIdentifier.PORT_SERIAL) {
                System.out.println(cpId.getName());
                ports.add(cpId.getName());
            }
        }

        return ports;
    }
}

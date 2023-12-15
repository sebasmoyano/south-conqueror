package com.balanzasserie.logica.puertoserie;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.TooManyListenersException;

import javax.comm.CommPortIdentifier;
import javax.comm.CommPortOwnershipListener;
import javax.comm.NoSuchPortException;
import javax.comm.SerialPort;
import javax.comm.SerialPortEvent;
import javax.comm.SerialPortEventListener;
import javax.comm.UnsupportedCommOperationException;

/**
 * A class that handles the details of a serial connection. Reads from one TextArea and writes to a second TextArea. Holds the state of the connection.
 */
public class SerialConnection implements CommPortOwnershipListener {

    private SerialParameters parameters;
    private OutputStream os;
    private InputStream is;
    private CommPortIdentifier portId;
    private SerialPort sPort;
    private boolean open;
    private SerialEventProcessor evProcessor;

    /**
     * Creates a SerialConnection object and initializes variables passed in as params.
     */
    public SerialConnection(SerialParameters parameters, SerialEventProcessor evProcessor) {
        this.parameters = parameters;
        open = false;
        this.evProcessor = evProcessor;
    }

    /**
     * Attempts to open a serial connection and streams using the parameters in the SerialParameters object. If it is unsuccesfull at any step it returns the port to a closed state, throws a <code>SerialConnectionException</code>, and returns.
     * <p>
     * Gives a timeout of 30 seconds on the portOpen to allow other applications to reliquish the port if have it open and no longer need it.
     *
     * @throws NoSuchPortException
     */
    public void openConnection() throws Exception {

        // Obtain a CommPortIdentifier object for the port you want to open.

        portId = CommPortIdentifier.getPortIdentifier(parameters.getPortName());

        // Open the port represented by the CommPortIdentifier object. Give
        // the open call a relatively long timeout of 30 seconds to allow
        // a different application to reliquish the port if the user
        // wants to.

        sPort = (SerialPort) portId.open("Serial Connection", 10000);

        // Set the parameters of the connection. If they won't set, close the
        // port before throwing an exception.

        setConnectionParameters();

        // Open the input and output streams for the connection. If they won't
        // open, close the port before throwing an exception.

        os = sPort.getOutputStream();
        is = sPort.getInputStream();

        // Create a new KeyHandler to respond to key strokes in the
        // messageAreaOut. Add the KeyHandler as a keyListener to the
        // messageAreaOut.

        // Add this object as an event listener for the serial port.

        SerialPortEventListener ev = new SerialPortEventListener() {

            public void serialEvent(SerialPortEvent e) {
                if (evProcessor != null) {
                    evProcessor.serialEvent(e, is, os);
                } else {
                }
            }
        };
        sPort.addEventListener(ev);

        // Set notifyOnDataAvailable to true to allow event driven input.
        sPort.notifyOnDataAvailable(true);

        // Set notifyOnBreakInterrup to allow event driven break handling.
        sPort.notifyOnBreakInterrupt(true);

        // Set receive timeout to allow breaking out of polling loop during
        // input handling.

        sPort.enableReceiveTimeout(30);

        // Add ownership listener to allow ownership event handling.
        portId.addPortOwnershipListener(this);

        open = true;
    }

    public void addDataListener(final SerialEventProcessor eventProcessor) throws TooManyListenersException {

        SerialPortEventListener ev = new SerialPortEventListener() {

            public void serialEvent(SerialPortEvent e) {

                eventProcessor.serialEvent(e, is, os);

            }
        };
        sPort.addEventListener(ev);
    }

    public void removeDataListener() {
        sPort.removeEventListener();
    }

    /**
     * Sets the connection parameters to the setting in the parameters object. If set fails return the parameters object to origional settings and throw exception.
     */
    public void setConnectionParameters() throws SerialConnectionException {

        // Save state of parameters before trying a set.
        int oldBaudRate = sPort.getBaudRate();
        int oldDatabits = sPort.getDataBits();
        int oldStopbits = sPort.getStopBits();
        int oldParity = sPort.getParity();
        // int oldFlowControl = sPort.getFlowControlMode();

        // Set connection parameters, if set fails return parameters object
        // to original state.
        try {
            sPort.setSerialPortParams(parameters.getBaudRate(), parameters.getDatabits(), parameters.getStopbits(), parameters.getParity());
        } catch (UnsupportedCommOperationException e) {
            parameters.setBaudRate(oldBaudRate);
            parameters.setDatabits(oldDatabits);
            parameters.setStopbits(oldStopbits);
            parameters.setParity(oldParity);
            throw new SerialConnectionException("Unsupported parameter");
        }

        // Set flow control.
        try {
            sPort.setFlowControlMode(parameters.getFlowControlIn() | parameters.getFlowControlOut());
        } catch (UnsupportedCommOperationException e) {
            throw new SerialConnectionException("Unsupported flow control");
        }
    }

    /**
     * Close the port and clean up associated elements.
     */
    public void closeConnection() {
        // If port is alread closed just return.
        if (!open) {
            return;
        }

        // Check to make sure sPort has reference to avoid a NPE.
        if (sPort != null) {
            try {
                // close the i/o streams.
                os.close();
                is.close();
            } catch (IOException e) {
                System.err.println(e);
            }

            // Close the port.
            sPort.close();

            // Remove the ownership listener.
            portId.removePortOwnershipListener(this);
        }

        open = false;
    }

    /**
     * Send a one second break signal.
     */
    public void sendBreak() {
        sPort.sendBreak(1000);
    }

    /**
     * Reports the open status of the port.
     *
     * @return true if port is open, false if port is closed.
     */
    public boolean isOpen() {
        return open;
    }

    /**
     * Handles ownership events. If a PORT_OWNERSHIP_REQUESTED event is received a dialog box is created asking the user if they are willing to give up the port. No action is taken on other types of ownership events.
     */
    public void ownershipChange(int type) {
        System.out.println("Port Ownership change request received:" + type);
    }

    public void writeBytes(byte[] lstBytes) {
        try {
            System.out.println("Writing bytes in " + sPort.getName());
            os.write(lstBytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}

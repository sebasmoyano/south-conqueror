package com.balanzasserie.logica.puertoserie;

import java.io.InputStream;
import java.io.OutputStream;

import javax.comm.SerialPortEvent;

public interface SerialEventProcessor {

    /**
     * Called when a serial event occured. it passes the InputStream and OutputStream for processing.
     *
     * @param e
     * @param in
     */
    public void serialEvent(SerialPortEvent e, InputStream in, OutputStream out);
}

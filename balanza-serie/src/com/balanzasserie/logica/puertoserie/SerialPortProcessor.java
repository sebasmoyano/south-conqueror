package com.balanzasserie.logica.puertoserie;

import com.balanzasserie.logica.BalanzasManager;
import com.balanzasserie.logica.Medicion;
import com.balanzasserie.logica.PropertiesManager;
import com.balanzasserie.logica.utils.NumeroUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.comm.SerialPortEvent;

public class SerialPortProcessor implements SerialEventProcessor {

    private String portName;

    SerialPortProcessor(String portName) {
        this.portName = portName;
        if (PropertiesManager.isTestMode()) {
            System.out.println("In test mode....");
            new TestThread().start();
        }
    }

    /**
     * Handles SerialPortEvents. The two types of SerialPortEvents that this
     * program is registered to listen for are DATA_AVAILABLE and BI. During
     * DATA_AVAILABLE the port buffer is read until it is drained, when no more
     * data is available and 30ms has passed the method returns. When a BI event
     * occurs the words BREAK RECEIVED are written to the messageAreaIn.
     */
    public void serialEvent(SerialPortEvent e, InputStream is, OutputStream os) {
        int newData = 0;
        LinkedList<Byte> bytesQueue = new LinkedList<Byte>();

        // Determine type of event.
        switch (e.getEventType()) {

            // Read data until -1 is returned.
            case SerialPortEvent.DATA_AVAILABLE:
                try {
                    while ((newData = is.read()) != -1) {
                        bytesQueue.add((byte) newData);
                    }
                } catch (IOException ex) {
                    System.out.println(ex);
                    return;
                }

                break;

            // If break event append BREAK RECEIVED message.
            case SerialPortEvent.BI:
                System.out.println("\n--- BREAK RECEIVED ---\n");
                break;

            default:
                System.out.println("Event received:" + e.getEventType());
        }

        byte[] rawBytes = NumeroUtil.tobytes(bytesQueue.toArray(new Byte[0]));
        BalanzasManager.getInstance().nuevaMedicionRecibida(new Medicion(portName, rawBytes));


    }


    private class TestThread extends Thread {
        //44 30 30 31 30 34 30 0D
        @Override
        public void run() {
            String medicionesStr[] = {"000000", "000020", "000040", "001740", "003420", "005140", "006800", "006820", "006820", "006860", "006840", "006460", "006400", "010340", "014180", "018160", "021920", "022140", "022300", "022420", "022600", "022540", "022600", "022680", "022720", "022800", "023040", "023020", "023240", "023260", "023320", "023280", "023220", "025660", "027900", "020000", "020000", "020000", "020000", "020000"};
            for (int i = 0; i < medicionesStr.length; i++) {
                byte[] rawBytes = new byte[8];
                rawBytes[0] = 0x44;
                for (int j = 0; j < medicionesStr[i].getBytes().length; j++) {
                    rawBytes[j + 1] = medicionesStr[i].getBytes()[j];
                }

                rawBytes[7] = 0x0D;

                BalanzasManager.getInstance().nuevaMedicionRecibida(new Medicion(portName, rawBytes));

                try {
                    sleep(1000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(SerialPortProcessor.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            System.out.println("Resultado final " + BalanzasManager.getInstance().getMedicionesBalanza1());


        }

        private byte getRandomByte() {
            byte[] bytesRan = new byte[1024];
            Random r = new Random();
            r.nextBytes(bytesRan);

            return bytesRan[0];
        }
    }

}

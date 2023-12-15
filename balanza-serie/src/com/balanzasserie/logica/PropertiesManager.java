/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.balanzasserie.logica;

import com.balanzasserie.logica.puertoserie.SerialParameters;
import com.balanzasserie.logica.utils.PropertiesUtil;

/**
 * @author root
 */
public class PropertiesManager {

    private static String CONFIGURATION_FILE = "conf/configuration.properties";

    public static SerialParameters getBalanza1SerialParameters() {
        SerialParameters params = new SerialParameters();
        String portName = PropertiesUtil.getInstance().getProperty(CONFIGURATION_FILE, "b1.portName");
        String baudRate = PropertiesUtil.getInstance().getProperty(CONFIGURATION_FILE, "b1.baudRate"); // 9600, 19200;
        String flowControlIn = PropertiesUtil.getInstance().getProperty(CONFIGURATION_FILE, "b1.flowControlIn");
        String databits = PropertiesUtil.getInstance().getProperty(CONFIGURATION_FILE, "b1.databits");
        String stopbits = PropertiesUtil.getInstance().getProperty(CONFIGURATION_FILE, "b1.stopbits");
        String parity = PropertiesUtil.getInstance().getProperty(CONFIGURATION_FILE, "b1.parity");

        params.setPortName(portName);
        params.setBaudRate(baudRate);
        params.setFlowControlIn(flowControlIn);
        params.setDatabits(databits);
        params.setStopbits(stopbits);
        params.setParity(parity);

        return params;
    }

    public static SerialParameters getBalanza2SerialParameters() {
        SerialParameters params = new SerialParameters();
        String portName = PropertiesUtil.getInstance().getProperty(CONFIGURATION_FILE, "b2.portName");
        String baudRate = PropertiesUtil.getInstance().getProperty(CONFIGURATION_FILE, "b2.baudRate"); // 9600, 19200;
        String flowControlIn = PropertiesUtil.getInstance().getProperty(CONFIGURATION_FILE, "b2.flowControlIn");
        String databits = PropertiesUtil.getInstance().getProperty(CONFIGURATION_FILE, "b2.databits");
        String stopbits = PropertiesUtil.getInstance().getProperty(CONFIGURATION_FILE, "b2.stopbits");
        String parity = PropertiesUtil.getInstance().getProperty(CONFIGURATION_FILE, "b2.parity");

        params.setPortName(portName);
        params.setBaudRate(baudRate);
        params.setFlowControlIn(flowControlIn);
        params.setDatabits(databits);
        params.setStopbits(stopbits);
        params.setParity(parity);

        return params;
    }

    public static void setBalanza1SerialParameters(SerialParameters params) {
        PropertiesUtil.getInstance().setProperty(CONFIGURATION_FILE, "b1.portName", params.getPortName());
        PropertiesUtil.getInstance().setProperty(CONFIGURATION_FILE, "b1.baudRate", params.getBaudRateString());
        PropertiesUtil.getInstance().setProperty(CONFIGURATION_FILE, "b1.flowControlIn", params.getFlowControlInString());
        PropertiesUtil.getInstance().setProperty(CONFIGURATION_FILE, "b1.stopbits", params.getStopbitsString());
        PropertiesUtil.getInstance().setProperty(CONFIGURATION_FILE, "b1.databits", params.getDatabitsString());
        PropertiesUtil.getInstance().setProperty(CONFIGURATION_FILE, "b1.parity", params.getParityString());
    }

    public static void setBalanza2SerialParameters(SerialParameters params) {
        PropertiesUtil.getInstance().setProperty(CONFIGURATION_FILE, "b2.portName", params.getPortName());
        PropertiesUtil.getInstance().setProperty(CONFIGURATION_FILE, "b2.baudRate", params.getBaudRateString());
        PropertiesUtil.getInstance().setProperty(CONFIGURATION_FILE, "b2.flowControlIn", params.getFlowControlInString());
        PropertiesUtil.getInstance().setProperty(CONFIGURATION_FILE, "b2.stopbits", params.getStopbitsString());
        PropertiesUtil.getInstance().setProperty(CONFIGURATION_FILE, "b2.databits", params.getDatabitsString());
        PropertiesUtil.getInstance().setProperty(CONFIGURATION_FILE, "b2.parity", params.getParityString());
    }

    public static boolean isTestMode() {
        boolean rta = false;
        String testmode = PropertiesUtil.getInstance().getProperty(CONFIGURATION_FILE, "testmode");
        if (testmode != null) {
            rta = Boolean.parseBoolean(testmode);
        }

        return rta;
    }

}

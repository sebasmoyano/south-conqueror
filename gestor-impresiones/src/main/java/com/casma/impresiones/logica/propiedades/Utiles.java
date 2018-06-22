/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.casma.impresiones.logica.propiedades;

/**
 *
 * @author sebastián
 */
public class Utiles {

    public static String parseString(String s) {
        if (s == null) {
            return "";
        }
        return s.trim();
    }

    public static Double parseDoubleString(String s) {
        Double d = new Double(0);
        try {
            d = Double.parseDouble(s);
        } catch (Exception e) {
        }
        return d;
    }
}

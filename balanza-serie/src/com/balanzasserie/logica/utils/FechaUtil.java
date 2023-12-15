/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.balanzasserie.logica.utils;

import java.util.Date;

/**
 * @author sebastián
 */
public class FechaUtil {
    public static long diferenciaEnMinutos(Date date1, Date date2) {
        long milis1 = date1.getTime();
        long milis2 = date2.getTime();
        long diff = Math.abs(milis2 - milis1);

        long diffMinutes = diff / (60 * 1000);

        return diffMinutes;
    }
}

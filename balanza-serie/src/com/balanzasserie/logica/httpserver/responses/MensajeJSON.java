/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.balanzasserie.logica.httpserver.responses;

/**
 * @author sebastián
 */
public class MensajeJSON {
    private String mensaje;

    public String getError() {
        return mensaje;
    }

    public void setError(String error) {
        this.mensaje = error;
    }

}

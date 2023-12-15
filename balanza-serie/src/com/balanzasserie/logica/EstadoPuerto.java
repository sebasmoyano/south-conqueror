/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.balanzasserie.logica;

/**
 * @author smoyano
 */
public enum EstadoPuerto {
    DESCONECTADO("Desconectado"),
    RECIBIENDO("Recibiendo Datos"),
    CONECTADO("Conectado");

    String label;

    EstadoPuerto(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}

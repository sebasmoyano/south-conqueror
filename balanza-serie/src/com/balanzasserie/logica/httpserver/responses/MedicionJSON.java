/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.balanzasserie.logica.httpserver.responses;

/**
 * @author sebastián
 */
public class MedicionJSON {
    private String fecha;
    private String balanza;
    private int peso;

    public String getBalanza() {
        return balanza;
    }

    public void setBalanza(String balanza) {
        this.balanza = balanza;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public int getPeso() {
        return peso;
    }

    public void setPeso(int peso) {
        this.peso = peso;
    }
}

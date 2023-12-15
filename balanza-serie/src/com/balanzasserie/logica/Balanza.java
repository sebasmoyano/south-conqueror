/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.balanzasserie.logica;

/**
 * @author smoyano
 */
public enum Balanza {
    BALANZA1("Balanza 1"),
    BALANZA2("Balanza 2");

    private String label;

    Balanza(String label) {
        this.label = label;
    }

    String getLabel() {
        return label;
    }
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.balanzasserie.vista;

import com.balanzasserie.logica.Medicion;

/**
 * @author smoyano
 */
public interface IVistaListener {
    void nuevaMedicionRecibida(Medicion medicion);
}

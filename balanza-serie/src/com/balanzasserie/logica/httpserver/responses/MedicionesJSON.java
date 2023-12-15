/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.balanzasserie.logica.httpserver.responses;

import java.util.ArrayList;
import java.util.List;

/**
 * @author sebastián
 */
public class MedicionesJSON {
    private long id = System.currentTimeMillis();

    private List<MedicionJSON> ultimasMediciones = new ArrayList<MedicionJSON>();

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public List<MedicionJSON> getUltimasMediciones() {
        return ultimasMediciones;
    }

    public void setUltimasMediciones(List<MedicionJSON> ultimasMediciones) {
        this.ultimasMediciones = ultimasMediciones;
    }

}

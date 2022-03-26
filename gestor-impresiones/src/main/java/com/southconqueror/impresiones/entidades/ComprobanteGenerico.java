package com.southconqueror.impresiones.entidades;

import java.util.List;

/**
 * Created by smoyano on 31/3/17.
 */
public class ComprobanteGenerico {

    private String titulo;
    private List<ComprobanteLinea> filas;

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getTitulo() {
        return titulo;
    }

    public List<ComprobanteLinea> getFilas() {
        return filas;
    }

    public void setFilas(List<ComprobanteLinea> filas) {
        this.filas = filas;
    }

}

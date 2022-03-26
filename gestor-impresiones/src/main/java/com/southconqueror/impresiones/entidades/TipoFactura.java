package com.southconqueror.impresiones.entidades;

/**
 * Created by smoyano on 02/01/17.
 */
public enum  TipoFactura {
    A,
    B;

    public static TipoFactura fromString(String tipoFactura) {
        try {
            return valueOf(tipoFactura);
        } catch (Exception e) {
            return null;
        }
    }
}

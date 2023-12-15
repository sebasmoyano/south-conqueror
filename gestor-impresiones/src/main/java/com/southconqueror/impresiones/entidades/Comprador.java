package com.southconqueror.impresiones.entidades;

import com.southconqueror.impresiones.logica.propiedades.Utiles;
import org.apache.commons.lang.StringUtils;

/**
 * Datos comprador factura
 * - Nombre (max 30 bytes)
 * - CUIT / Nro documento (max 11 bytes)
 * - Responsabilidad frente al IVA {INEACBMST}
 * - Tipo de documento {CL1234}
 * - Domicilio comercial (max 40 bytes)
 * <p>
 * Created by smoyano on 02/01/17.
 */
public class Comprador {

    private String nombre;
    private String cuit;
    private String responsabilidadIva;
    private String tipoDocumento = "C";
    private String domicilioComercial;

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = StringUtils.substring(Utiles.parseString(nombre), 0, 30);
    }

    public String getCuit() {
        return cuit;
    }

    public void setCuit(String cuit) {
        this.cuit = Utiles.parseString(cuit);
    }

    public String getResponsabilidadIva() {
        return responsabilidadIva;
    }

    public void setResponsabilidadIva(String responsabilidadIva) {
        this.responsabilidadIva = Utiles.parseString(responsabilidadIva);
    }

    public String getTipoDocumento() {
        return tipoDocumento;
    }

    public void setTipoDocumento(String tipoDocumento) {
        this.tipoDocumento = Utiles.parseString(tipoDocumento);
    }

    public String getDomicilioComercial() {
        return domicilioComercial;
    }

    public void setDomicilioComercial(String domicilioComercial) {
        this.domicilioComercial = StringUtils.substring(Utiles.parseString(domicilioComercial), 0, 40);
    }

}

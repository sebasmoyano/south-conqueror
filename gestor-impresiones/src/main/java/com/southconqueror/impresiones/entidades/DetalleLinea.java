package com.southconqueror.impresiones.entidades;

import com.southconqueror.impresiones.logica.propiedades.Utiles;
import org.apache.commons.lang.StringUtils;

/**
 * - Texto descripcion del item (max 20 bytes)
 * - Cantidad (nnnn.nnnnnnnnnn)
 * - Precio unitario (nnnnnn.nn)
 * - Porcentaje IVA (nn.nn)/(**.**) (max 5 bytes)
 * - Calificador de la operacion {Mm}
 * - Impuestos internos
 * - Parametro display: 0, 1 o 2 {012}
 * - T: precio total; otro caracter: precio base {TBO}
 *
 * Created by smoyano on 02/01/17.
 */
public class DetalleLinea {

    private String descripcion;
    private double cantidad;
    private double precioUnitario;
    private String porcentajeIva = "";
    private String calificadorOperacion = "M";
    private String impuestosInternos = "0.0";
    private String parametroDisplay = "0";
    private String precioBase = "B";
    private double subtotal;

    public String getDescripcion() {
        return getDescripcion(false);
    }

    public String getDescripcion(boolean cortar) {
        if (cortar) {
            return StringUtils.substring(Utiles.parseString(descripcion), 0, 20);
        }
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public double getCantidad() {
        return cantidad;
    }

    public void setCantidad(double cantidad) {
        this.cantidad = cantidad;
    }

    public double getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(double precioUnitario) {
        this.precioUnitario = precioUnitario;
    }

    public String getPorcentajeIva() {
        return porcentajeIva;
    }

    public void setPorcentajeIva(String porcentajeIva) {
        this.porcentajeIva = Utiles.parseString(porcentajeIva);
    }

    public String getCalificadorOperacion() {
        return calificadorOperacion;
    }

    public void setCalificadorOperacion(String calificadorOperacion) {
        this.calificadorOperacion = calificadorOperacion;
    }

    public String getImpuestosInternos() {
        return impuestosInternos;
    }

    public void setImpuestosInternos(String impuestosInternos) {
        this.impuestosInternos = impuestosInternos;
    }

    public String getParametroDisplay() {
        return parametroDisplay;
    }

    public void setParametroDisplay(String parametroDisplay) {
        this.parametroDisplay = parametroDisplay;
    }

    public String getPrecioBase() {
        return precioBase;
    }

    public void setPrecioBase(String precioBase) {
        this.precioBase = precioBase;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }
}

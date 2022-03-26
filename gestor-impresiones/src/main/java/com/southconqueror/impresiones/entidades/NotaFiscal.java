package com.southconqueror.impresiones.entidades;

/**
 * Created by smoyano on 19/06/17.
 */
public class NotaFiscal {

    private TipoNota tipo;
    private String numero;
    private String fecha;
    private Factura factura;
    private DetalleLinea detalle;
    private double total;
    private double iva;
    private String urlCallback;

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public Factura getFactura() {
        return factura;
    }

    public void setFactura(Factura factura) {
        this.factura = factura;
    }

    public DetalleLinea getDetalle() {
        return detalle;
    }

    public void setDetalle(DetalleLinea detalle) {
        this.detalle = detalle;
    }

    public TipoNota getTipo() {
        return tipo;
    }

    public void setTipo(TipoNota tipo) {
        this.tipo = tipo;
    }

    public String getTipoDocumento() {
        if (tipo == TipoNota.CREDITO) {
            if (factura != null && factura.getTipoFactura().equals("B")) {
                return "S";
            }
            return "R";
        } else if (tipo == TipoNota.DEBITO) {
            if (factura != null && factura.getTipoFactura().equals("B")) {
                return "E";
            }
            return "D";
        }
        return null;
    }


    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public double getIva() {
        return iva;
    }

    public void setIva(double iva) {
        this.iva = iva;
    }

    public String getUrlCallback() {
        return urlCallback;
    }

    public void setUrlCallback(String urlCallback) {
        this.urlCallback = urlCallback;
    }

}

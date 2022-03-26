package com.southconqueror.impresiones.entidades;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by smoyano on 16/11/16.
 */
public class Factura {

    private String tipoFactura;
    private String numeroFactura;
    private String fechaEmision;
    private String talonario;
    private Comprador comprador;
    private List<DetalleLinea> detalle = new ArrayList<DetalleLinea>();
    private double total;
    private double iva;

    /**
     * Url a llamar una vez emitido el comprobante.
     */
    private String urlCallback;

    public String getTipoFactura() {
        return tipoFactura;
    }

    public void setTipoFactura(String tipoFactura) {
        this.tipoFactura = tipoFactura;
    }

    public String getNumeroFactura() {
        return numeroFactura;
    }

    public void setNumeroFactura(String numeroFactura) {
        this.numeroFactura = numeroFactura;
    }

    public String getFechaEmision() {
        return fechaEmision;
    }

    public void setFechaEmision(String fechaEmision) {
        this.fechaEmision = fechaEmision;
    }

    public Comprador getComprador() {
        return comprador;
    }

    public void setComprador(Comprador comprador) {
        this.comprador = comprador;
    }

    public List<DetalleLinea> getDetalle() {
        return detalle;
    }

    public void setDetalle(List<DetalleLinea> detalle) {
        this.detalle = detalle;
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

    public String getTalonario() {
        return talonario;
    }

    public void setTalonario(String talonario) {
        this.talonario = talonario;
    }

    public void setUrlCallback(String urlCallback) {
        this.urlCallback = urlCallback;
    }
}

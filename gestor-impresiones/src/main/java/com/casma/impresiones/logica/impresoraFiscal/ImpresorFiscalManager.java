/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.casma.impresiones.logica.impresoraFiscal;

import IFDrivers.HasarTick;
import com.casma.impresiones.entidades.*;
import com.casma.impresiones.logica.propiedades.Utiles;
import com.casma.impresiones.logica.utiles.CryptoUtils;
import com.casma.impresiones.logica.utiles.JsonConverter;
import com.casma.slingr.Json;
import com.casma.slingr.SlingrClient;
import com.casma.slingr.SlingrManager;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author sebastián
 */
public class ImpresorFiscalManager {

    private static Logger logger = LogManager.getLogger(ImpresorFiscalManager.class);

    private static ImpresorFiscalManager instance;
    private HasarTick IPrinter;
    private static String SERIAL_PORT = "COM1"; // TODO: manejar como param
    private static int VELOCIDAD_PUERTO = 9600;
    private boolean driverInicializado;

    private final static String PARAMETRO_DISPLAY_COMPROBANTE_NO_FISCAL = "0";

    private final ReentrantLock imprimirFacturaLock = new ReentrantLock();

    private SlingrClient slingrClient;

    private boolean mockDriver = false;

    // datos comercio
    private String puntoDeVenta;

    private ImpresorFiscalManager() {
        try {
            IPrinter = new HasarTick();
            driverInicializado = true;
        } catch (Throwable e) {
            logger.error("Driver de impresora no pudo ser inicializado", e);
        }
    }

    public synchronized static ImpresorFiscalManager getInstance() {
        if (instance == null) {
            instance = new ImpresorFiscalManager();
        }
        return instance;
    }

    // manejo impresora

    public void iniciarImpresora() throws Exception {
        if (!driverInicializado) {
            logger.error("Driver de impresora no ha sido inicializado");
            return;
        }

        logger.info("Iniciando conexion con impresora");
        IPrinter.setSerial();
        int nError = IPrinter.IF_OPEN(SERIAL_PORT, VELOCIDAD_PUERTO);
        if (nError != 0) {
            throw new IllegalStateException("Error en la apertura del puerto COM de la impresora!");
        }
        // iniciar punto de venta
        obtenerNumeroSucursal();
        logger.info("Impresora iniciada correctamente");
    }

    public void cerrarImpresora() {
        logger.info("Cerrando conexion con impresora");
        IPrinter.IF_CLOSE();
        logger.info("Impresora cerrada correctamente");
    }

    ////////////////////////// FACTURAS //////////////////////////////////

    public String imprimirFactura(Factura factura) throws Exception {
        logger.info("Realizando impresion de comprobante " + JsonConverter.objectToString(factura));

        imprimirFacturaLock.lock();
        try {
            setDatosComprador(factura.getComprador());
            abrirFactura(factura);
            agregarDetallesFactura(factura.getDetalle());
            agregarSubtotal();
            agregarTotal(factura);
            String nroComprobante = getUltimoNumeroFactura(factura.getTipoFactura());
            logger.info(String.format("Comprobante generado [%s]", nroComprobante));
            if (StringUtils.isBlank(nroComprobante)) {
                logger.error("La impresión se realizó correctamente pero no se pudo obtener el número de comprobante generado");
            } else {
                // llamar el callback definido
                if (!StringUtils.isBlank(factura.getUrlCallback()) && !StringUtils.isBlank(factura.getTalonario())) {
                    logger.info(String.format("Actualizando número [%s] en servidor", nroComprobante));
                    String numeroFactura = factura.getTalonario() + "-" + nroComprobante;
                    Json response = actualizarNumeroEnServidor(factura.getUrlCallback(), "numeroFactura", numeroFactura);
                    logger.info(String.format("Respuesta estado servidor: [%s]", response));
                }
            }
            return nroComprobante;
        } catch (Exception e) {
            logger.error("No se pudo realizar impresion", e);
            throw e;
        } finally {
            if (!cerrarFactura()) {
                cancelarDocumentoActual();
            }
            imprimirFacturaLock.unlock();
        }
    }

    private void cancelarDocumentoActual() {
        logger.info("Cancelando documento...");
        int nError = IPrinter.Sincro();
        if (nError != 0) {
            logger.error("No se pudo cancelar documento actual");
        } else {
            logger.info("Documento cancelado correctamente");
        }
    }

    private void abrirFactura(Factura factura) throws Exception {
        logger.info("Abrir factura " + factura.getTipoFactura());
        String valorFijo = "T";// ?
        int nError = IPrinter.OpenFiscalReceipt(factura.getTipoFactura(), valorFijo);
        if (nError != 0) {
            throw new Exception("Error al abrir factura");
        }
    }

    private void agregarDetallesFactura(List<DetalleLinea> lineas) throws Exception {
        for (DetalleLinea linea : lineas) {
            imprimirDetalle(linea);
        }
    }

    private void imprimirDetalle(DetalleLinea linea) throws Exception {
        logger.info("Imprimir linea detalle " + JsonConverter.objectToString(linea));
        int nError = IPrinter.PrintLineItem(linea.getDescripcion(true), linea.getCantidad(), linea.getPrecioUnitario(), linea.getPorcentajeIva(), linea.getCalificadorOperacion(), linea.getImpuestosInternos(), linea.getParametroDisplay(), linea.getPrecioBase());
        if (nError != 0) {
            throw new Exception("Error al agregar detalle factura");
        }
    }

    private void agregarSubtotal() throws Exception {
        String parametroImpresion = "P";
        String reservado = "Subtotal";
        String parametroDisplay = "0";
        int nError = IPrinter.Subtotal(parametroImpresion, reservado, parametroDisplay);
        if (nError != 0) {
            throw new Exception("Error al agregar subtotal a factura");
        }
    }

    private void agregarTotal(Factura factura) throws Exception {
        String formaPago = "Efectivo";
        String calificadorOperacion = "T";
        String parametroDisplay = "0";

        int nError = IPrinter.TotalTender(formaPago, factura.getTotal(), calificadorOperacion, parametroDisplay);
        if (nError != 0) {
            throw new Exception("Error al agregar total a factura");
        }
    }

    /**
     * Recuperar el nro. del ultimo Comprobante Fiscal Factura B o C emitido.
     *
     * @return ultimo nro. comprobante B o C.
     * @throws Exception
     */
    public String getUltimoNumeroFactura(String tipoFactura) throws Exception {
        pedirEstado();
        if (tipoFactura.equals("A")) {
            return leerRespuestaComando(5);
        } else if (tipoFactura.equals("B")) {
            return leerRespuestaComando(3);
        }
        return null;
    }

    private boolean cerrarFactura() {
        if (!driverInicializado) {
            logger.error("Driver de impresora no ha sido inicializado");
            return false;
        }
        boolean facturaCerrada = IPrinter.CloseFiscalReceipt() == 0;
        if (!facturaCerrada) {
            logger.error("Error al cerrar factura");
        }
        return facturaCerrada;
    }

    ////////////////////////// NOTAS CREDITO //////////////////////////////////

    public String imprimirNotaCredito(NotaFiscal notaFiscal) throws Exception {
        logger.info("Realizando impresion de nota de credito " + JsonConverter.objectToString(notaFiscal.getDetalle()));

        try {
            setDatosComprador(notaFiscal.getFactura().getComprador());
            relactionarFactura(notaFiscal);
            abrirNotaCredito(notaFiscal);
            imprimirDetalle(notaFiscal.getDetalle());
        } finally {
            cerrarNotaCredito();
        }
        String nroComprobante = getUltimoNumeroNotaCredito(notaFiscal.getFactura().getTipoFactura());
        logger.info(String.format("Nota de credito generada [%s]", nroComprobante));
        if (StringUtils.isBlank(nroComprobante)) {
            logger.error("La impresión se realizó correctamente pero no se pudo obtener el número de comprobante generado");
        } else {
            // llamar el callback definido
            if (!StringUtils.isBlank(notaFiscal.getUrlCallback())) {
                String numeroNota = getPuntoDeVentaFormatted() + "-";
                numeroNota += nroComprobante;
                logger.info(String.format("Actualizando número [%s] en servidor", numeroNota));
                Json response = actualizarNumeroEnServidor(notaFiscal.getUrlCallback(), "numeroNota", numeroNota);
                logger.info(String.format("Respuesta estado servidor: [%s]", response));
            }
        }
        return nroComprobante;
    }

    private void relactionarFactura(NotaFiscal notaFiscal) throws Exception {
        // Cargar la información de la factura que origina esta nota de crédito
        int nError = IPrinter.SetEmbarkNumber(Integer.toString(1), notaFiscal.getFactura().getNumeroFactura());
        if (nError != 0) {
            throw new Exception("Error al configurar datos factura para nota de credito");
        }
    }

    private void abrirNotaCredito(NotaFiscal notaFiscal) throws Exception {
        // Cargar la información de la factura que origina esta nota de crédito
        int nError = IPrinter.OpenDNFH(notaFiscal.getTipoDocumento(), "T");
        if (nError != 0) {
            throw new Exception("Error al abrir nota de credito");
        }
    }

    public String getUltimoNumeroNotaCredito(String tipoFactura) throws Exception {
        pedirEstado();
        if (tipoFactura.equals("A")) {
            return leerRespuestaComando(8);
        } else if (tipoFactura.equals("B")) {
            return leerRespuestaComando(7);
        }
        return null;
    }

    private void cerrarNotaCredito() throws Exception {
        // Cargar la información de la factura que origina esta nota de crédito
        int nError = IPrinter.CloseDNFH();
        if (nError != 0) {
            throw new Exception("Error al cerrar nota de credito");
        }
    }

    ////////////////////////// NOTAS DEBITO //////////////////////////////////

    public String imprimirNotaDebito(NotaFiscal notaDebito) throws Exception {
        logger.info("Realizando impresion de nota de debito " + JsonConverter.objectToString(notaDebito.getDetalle()));

        try {
            setDatosComprador(notaDebito.getFactura().getComprador());
            abrirNotaDebito(notaDebito);
            imprimirDetalle(notaDebito.getDetalle());
        } finally {
            cerrarNotaDebito();
        }
        String nroComprobante = getUltimoNumeroNotaDebito();
        logger.info(String.format("Nota de debito generada [%s]", nroComprobante));
        if (StringUtils.isBlank(nroComprobante)) {
            logger.error("La impresión se realizó correctamente pero no se pudo obtener el número de comprobante generado");
        } else {
            // llamar el callback definido
            if (!StringUtils.isBlank(notaDebito.getUrlCallback())) {
                String numeroNota = getPuntoDeVentaFormatted() + "-";
                numeroNota += nroComprobante;
                logger.info(String.format("Actualizando número [%s] en servidor", numeroNota));
                Json response = actualizarNumeroEnServidor(notaDebito.getUrlCallback(), "numeroNota", numeroNota);
                logger.info(String.format("Respuesta estado servidor: [%s]", response));
            }
        }
        return nroComprobante;
    }

    private void abrirNotaDebito(NotaFiscal NotaFiscal) throws Exception {
        // Cargar la información de la factura que origina esta nota de crédito
        int nError = IPrinter.OpenDNFH(NotaFiscal.getTipoDocumento(), "T");
        if (nError != 0) {
            throw new Exception("Error al abrir nota de credito");
        }
    }

    public String getUltimoNumeroNotaDebito() throws Exception {
        return leerRespuestaComando(3);
    }

    private void cerrarNotaDebito() throws Exception {
        // Cargar la información de la factura que origina esta nota de crédito
        int nError = IPrinter.CloseFiscalReceipt();
        if (nError != 0) {
            throw new Exception("Error al cerrar nota de debito");
        }
    }

    // comun comprobantes

    private void setDatosComprador(Comprador comprador) throws Exception {
        logger.info("Seteando datos comprador " + JsonConverter.objectToString(comprador));
        int nError = IPrinter.SetCustomerData(comprador.getNombre(), comprador.getCuit(), comprador.getResponsabilidadIva(), comprador.getTipoDocumento(), comprador.getDomicilioComercial());
        if (nError != 0) {
            throw new Exception("Error al configurar datos comprador");
        }
    }

    private String getPuntoDeVentaFormatted() {
        if (!StringUtils.isBlank(puntoDeVenta)) {
            return puntoDeVenta;
        } else {
            obtenerNumeroSucursal();
            if (!StringUtils.isBlank(puntoDeVenta)) {
                return puntoDeVenta;
            }
        }
        return "";
    }

    // manejo comprobantes no fiscales

    public void imprimirComprobanteNoFiscal(ComprobanteNoFiscal comprobanteNoFiscal) throws Exception {
        logger.info("Realizando impresion de comprobante no fiscal " + JsonConverter.objectToString(comprobanteNoFiscal));

        try {
            abrirComprobanteNoFiscal();
            for (String linea : comprobanteNoFiscal.getLineas()) {
                agregarDetalleComprobanteNoFiscal(linea);
            }
        } finally {
            cerrarComprobanteNoFiscal();
        }
    }

    private void abrirComprobanteNoFiscal() throws Exception {
        int nError = IPrinter.OpenNonFiscalReceipt();
        if (nError != 0) {
            throw new Exception("Error al abrir comprobante no fiscal");
        }
    }

    private void agregarDetalleComprobanteNoFiscal(String lineaComprobanteNoFiscal) throws Exception {
        String linea = Utiles.parseString(lineaComprobanteNoFiscal);
        if (linea.length() > 40) {
            linea = linea.substring(0, 40);
        }
        int nError = IPrinter.PrintNonFiscalText(linea, PARAMETRO_DISPLAY_COMPROBANTE_NO_FISCAL);
        if (nError != 0) {
            throw new Exception("Error al imprimir linea documento no fiscal");
        }
    }

    private void cerrarComprobanteNoFiscal() {
        int nError = IPrinter.CloseNonFiscalReceipt();
        if (nError != 0) {
            logger.error("Error al cerrar comprobante no fiscal");
        }
    }

    private void obtenerNumeroSucursal() {
        int nErrorInitData = IPrinter.GetInitData();
        if (nErrorInitData != 0) {
            logger.error("Error al consultar init data!");
        } else {
            puntoDeVenta = IPrinter.IF_READ(7);
        }
    }

    // comandos genericos

    public String leerRespuestaComando(int numeroCampo) throws Exception {
        return IPrinter.IF_READ(numeroCampo);
    }

    public int ejecutarComando(String comando) throws Exception {
        int nError = IPrinter.IF_WRITE(comando);
        if (nError != 0) {
            throw new Exception("Error al ejecutar comando de impresora.");
        }
        return nError;
    }

    // estado impresora

    private void pedirEstado() throws Exception {
        int nError = IPrinter.StatusRequest();
        if (nError != 0) {
            throw new Exception("Error al obtener estado impresora");
        }
    }

    // interaccion server

    private Json actualizarNumeroEnServidor(String url, String numeroField, String numero) {
        Json paylod = Json.map();
        paylod.set(numeroField, numero);
        try {
            return SlingrManager.getInstance().actualizarNumero(url, paylod);
        } catch (Exception e) {
            logger.error("No se pudo actualizar numero en servidor", e);
        }
        return null;
    }


}

package com.southconqueror.impresiones.logica.impresoras.serial;

import IFDrivers.HasarTick;
import com.southconqueror.impresiones.entidades.*;
import com.southconqueror.impresiones.logica.impresoras.ImpresoraFiscal;
import com.southconqueror.impresiones.logica.propiedades.Utiles;
import com.southconqueror.impresiones.logica.slingr.Json;
import com.southconqueror.impresiones.logica.utiles.JsonConverter;
import com.southconqueror.impresiones.logica.utils.AppProperties;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author sebastián
 */
public class HasarSerial extends ImpresoraFiscal {

    private static Logger logger = LogManager.getLogger(HasarSerial.class);

    private static HasarSerial instance;
    private HasarTick IPrinter;
    private static String DEFAULT_SERIAL_PORT = "COM1";
    private static int DEFAULT_PORT_SPEED = 9600;
    private final static String PARAMETRO_DISPLAY_COMPROBANTE_NO_FISCAL = "0";
    private final static String SERIAL_PRINTER_DLL = "H71532Jv";

    private final ReentrantLock imprimirFacturaLock = new ReentrantLock();

    // datos comercio
    private String puntoDeVenta;

    private HasarSerial() {
    }

    public synchronized static HasarSerial getInstance() {
        if (instance == null) {
            instance = new HasarSerial();
        }
        return instance;
    }

    // manejo impresora

    @Override
    public void configurar() {
        try {
            System.loadLibrary(SERIAL_PRINTER_DLL);
            IPrinter = new HasarTick();
        } catch (Throwable e) {
            logger.error("Driver de impresora no pudo ser inicializado", e);
        }

        chequearConexionImpresora();

        IPrinter.setSerial();
        String serialPort = AppProperties.getInstance().getPuertoImpresora();
        if (serialPort == null) {
            serialPort = DEFAULT_SERIAL_PORT;
        }
        logger.info(String.format("Iniciando conexion con impresora en puerto [%s] velocidad [%s]", serialPort, DEFAULT_PORT_SPEED));
        int nError = IPrinter.IF_OPEN(serialPort, DEFAULT_PORT_SPEED);
        if (nError != 0) {
            throw new IllegalStateException("Error en la apertura del puerto COM de la impresora!");
        }
        // iniciar punto de venta
        obtenerNumeroSucursal();
        logger.info("Impresora iniciada correctamente");
    }

    public void cerrarImpresora() {
        chequearConexionImpresora();

        logger.info("Cerrando conexion con impresora");
        IPrinter.IF_CLOSE();
        logger.info("Impresora cerrada correctamente");
    }

    // impresión facturas

    @Override
    public String imprimirFactura(Factura factura) throws Exception {
        logger.info("Realizando impresion de comprobante " + JsonConverter.objectToString(factura));

        imprimirFacturaLock.lock();
        try {
            cargarDatosCliente(factura.getComprador());
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
            try {
                if (!cerrarFactura()) {
                    cancelarDocumentoActual();
                }
            } catch (Exception e) {
                logger.error("No se pudo cerrar comprobante", e);
            }
            imprimirFacturaLock.unlock();
        }
    }

    /**
     * Recuperar el nro. del ultimo Comprobante Fiscal Factura B o C emitido.
     *
     * @return ultimo nro. comprobante B o C.
     * @throws Exception
     */
    @Override
    public String getUltimoNumeroFactura(String tipoFactura) throws Exception {
        pedirEstado();
        if (tipoFactura.equals("A")) {
            return leerRespuestaComando(5);
        } else if (tipoFactura.equals("B")) {
            return leerRespuestaComando(3);
        }
        return null;
    }

    private void cancelarDocumentoActual() {
        chequearConexionImpresora();

        logger.info("Cancelando documento...");
        try {
            int nError = IPrinter.Sincro();
            if (nError != 0) {
                logger.error("No se pudo cancelar documento actual");
            } else {
                logger.info("Documento cancelado correctamente");
            }
        } catch (Exception e) {
            logger.error("No se pudo cancelar documento actual", e);
        }
    }

    private void abrirFactura(Factura factura) throws Exception {
        chequearConexionImpresora();
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
        chequearConexionImpresora();

        logger.info("Imprimir linea detalle " + JsonConverter.objectToString(linea));
        int nError = IPrinter.PrintLineItem(linea.getDescripcion(true), linea.getCantidad(), linea.getPrecioUnitario(), linea.getPorcentajeIva(), linea.getCalificadorOperacion(), linea.getImpuestosInternos(), linea.getParametroDisplay(), linea.getPrecioBase());
        if (nError != 0) {
            throw new Exception("Error al agregar detalle factura para linea: " + linea.getDescripcion());
        }
    }

    private void agregarSubtotal() throws Exception {
        chequearConexionImpresora();

        String parametroImpresion = "P";
        String reservado = "Subtotal";
        String parametroDisplay = "0";
        int nError = IPrinter.Subtotal(parametroImpresion, reservado, parametroDisplay);
        if (nError != 0) {
            throw new Exception("Error al agregar subtotal a factura");
        }
    }

    private void agregarTotal(Factura factura) throws Exception {
        chequearConexionImpresora();

        String formaPago = "Efectivo";
        String calificadorOperacion = "T";
        String parametroDisplay = "0";

        int nError = IPrinter.TotalTender(formaPago, factura.getTotal(), calificadorOperacion, parametroDisplay);
        if (nError != 0) {
            throw new Exception("Error al agregar total a factura. Total: " + factura.getTotal());
        }
    }

    private boolean cerrarFactura() {
        if (IPrinter == null) {
            logger.error("Driver de impresora no ha sido inicializado");
            return false;
        }
        boolean facturaCerrada = false;
        try {
            facturaCerrada = IPrinter.CloseFiscalReceipt() == 0;
            if (!facturaCerrada) {
                logger.error("Error al cerrar factura");
            }
        } catch (Exception e) {
            logger.error("Error al cerrar factura", e);
        }
        return facturaCerrada;
    }

    ////////////////////////// NOTAS CREDITO //////////////////////////////////

    @Override
    public String imprimirNotaCredito(NotaFiscal notaFiscal) throws Exception {
        logger.info("Realizando impresion de nota de credito " + JsonConverter.objectToString(notaFiscal.getDetalle()));

        try {
            cargarDatosCliente(notaFiscal.getFactura().getComprador());
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
        chequearConexionImpresora();

        // Cargar la información de la factura que origina esta nota de crédito
        int nError = IPrinter.SetEmbarkNumber(Integer.toString(1), notaFiscal.getFactura().getNumeroFactura());
        if (nError != 0) {
            throw new Exception("Error al configurar datos factura para nota de credito");
        }
    }

    private void abrirNotaCredito(NotaFiscal notaFiscal) throws Exception {
        chequearConexionImpresora();

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
        chequearConexionImpresora();

        // Cargar la información de la factura que origina esta nota de crédito
        int nError = IPrinter.CloseDNFH();
        if (nError != 0) {
            throw new Exception("Error al cerrar nota de credito");
        }
    }

    ////////////////////////// NOTAS DEBITO //////////////////////////////////

    @Override
    public String imprimirNotaDebito(NotaFiscal notaDebito) throws Exception {
        logger.info("Realizando impresion de nota de debito " + JsonConverter.objectToString(notaDebito.getDetalle()));

        try {
            cargarDatosCliente(notaDebito.getFactura().getComprador());
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
        chequearConexionImpresora();

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
        chequearConexionImpresora();

        // Cargar la información de la factura que origina esta nota de crédito
        int nError = IPrinter.CloseFiscalReceipt();
        if (nError != 0) {
            throw new Exception("Error al cerrar nota de debito");
        }
    }

    // comun comprobantes

    private void cargarDatosCliente(Comprador comprador) throws Exception {
        chequearConexionImpresora();
        logger.info("Cargar datos cliente " + JsonConverter.objectToString(comprador));
        int nError = IPrinter.SetCustomerData(comprador.getNombre(), comprador.getCuit(), comprador.getResponsabilidadIva(), comprador.getTipoDocumento(), comprador.getDomicilioComercial());
        if (nError != 0) {
            throw new Exception("Error al configurar datos comprador. Por favor verificar CUIT o caracteres especiales en el nombre del comprador.");
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

    @Override
    public void cerrarJornadaFiscal() throws Exception {
        ejecutarComando("@DailyClose|Z");
    }

    private void abrirComprobanteNoFiscal() throws Exception {
        chequearConexionImpresora();
        int nError = IPrinter.OpenNonFiscalReceipt();
        if (nError != 0) {
            throw new Exception("Error al abrir comprobante no fiscal");
        }
    }

    private void agregarDetalleComprobanteNoFiscal(String lineaComprobanteNoFiscal) throws Exception {
        chequearConexionImpresora();
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
        chequearConexionImpresora();
        int nError = IPrinter.CloseNonFiscalReceipt();
        if (nError != 0) {
            logger.error("Error al cerrar comprobante no fiscal");
        }
    }

    private void obtenerNumeroSucursal() {
        chequearConexionImpresora();
        int nErrorInitData = IPrinter.GetInitData();
        if (nErrorInitData != 0) {
            logger.error("Error al consultar init data!");
        } else {
            puntoDeVenta = IPrinter.IF_READ(7);
        }
    }

    // comandos genericos

    public String leerRespuestaComando(int numeroCampo) throws Exception {
        chequearConexionImpresora();
        return IPrinter.IF_READ(numeroCampo);
    }

    public int ejecutarComando(String comando) throws Exception {
        chequearConexionImpresora();
        int nError = IPrinter.IF_WRITE(comando);
        if (nError != 0) {
            throw new Exception("Error al ejecutar comando de impresora.");
        }
        return nError;
    }

    // estado impresora

    private void pedirEstado() throws Exception {
        chequearConexionImpresora();
        int nError = IPrinter.StatusRequest();
        if (nError != 0) {
            throw new Exception("Error al obtener estado impresora");
        }
    }

    // interaccion server

    private void chequearConexionImpresora() {
        if (IPrinter == null) {
            logger.error("Driver de impresora no ha sido inicializado");
            throw new RuntimeException("Driver de impresora no ha sido inicializado");
        }
    }

}

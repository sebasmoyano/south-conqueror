package com.southconqueror.impresiones.logica.impresoras.electronica;

import com.southconqueror.impresiones.entidades.*;
import com.southconqueror.impresiones.logica.impresoras.ImpresoraFiscal;
import com.southconqueror.impresiones.logica.slingr.Json;
import com.southconqueror.impresiones.logica.utiles.JsonConverter;
import com.southconqueror.impresiones.logica.utils.AppProperties;
import hfl.argentina.HasarException;
import hfl.argentina.HasarImpresoraFiscalRG3561;
import hfl.argentina.HasarImpresoraFiscalRG3561.*;
import hfl.argentina.Hasar_Funcs;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.text.NumberFormat;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;


public class HasarRg3561 extends ImpresoraFiscal {

    private static Logger logger = LogManager.getLogger(HasarRg3561.class);

    private HasarImpresoraFiscalRG3561 impresora;
    private final ReentrantLock imprimirFacturaLock;
    private String puntoDeVenta;
    private static HasarRg3561 instance;

    private HasarRg3561() {
        impresora = new HasarImpresoraFiscalRG3561();
        imprimirFacturaLock = new ReentrantLock();
    }

    public synchronized static HasarRg3561 getInstance() {
        if (instance == null) {
            instance = new HasarRg3561();
        }
        return instance;
    }

    @Override
    public void configurar() throws HasarException {
        impresora.establecerTiempoDeEsperaConexion(15000);
        impresora.establecerTiempoDeEsperaRespuesta(15000);
        impresora.addFiscalListener(new HasarRg3561EventoFiscalListener());
        impresora.addFiscalListener(new HasarRg3561EventoImpresoraListener());
        final String ipImpresora = AppProperties.getInstance().getIpImpresoraFiscal();
        logger.info(String.format("Conectando a IP [%s]", ipImpresora));
        impresora.conectar(ipImpresora, 80);
        RespuestaConsultarDatosInicializacion rdi = impresora.ConsultarDatosInicializacion();
        logger.info("Razón social:                " + rdi.getRazonSocial());
        logger.info("C.U.I.T.:                    " + rdi.getCUIT());
        logger.info("Ingresos Brutos:             " + rdi.getIngBrutos());
        logger.info("Fecha in                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           icio de actividades: " + rdi.getFechaInicioActividades());
        logger.info("Número de POS:               " + rdi.getNumeroPos());
        logger.info("Responsabilidad IVA:         " + rdi.getResponsabilidadIVA());
        puntoDeVenta = String.format("%05d", rdi.getNumeroPos());
    }

    @Override
    public String imprimirFactura(Factura factura) throws Exception {
        logger.info("Realizando impresion de comprobante " + JsonConverter.objectToString(factura));
        imprimirFacturaLock.lock();
        try {
            cargarDatosCliente(factura.getComprador());
            final String nroComprobante = abrirFactura(factura);
            agregarDetallesFactura(factura.getDetalle());
            imprimirInfoConsumidor(factura);
            impresora.ImprimirPago("Efectivo", factura.getTotal(), ModosDePago.PAGAR);
            logger.info(String.format("Comprobante generado [%s]", nroComprobante));
            if (!StringUtils.isBlank(nroComprobante)) {
                // llamar el callback definido
                if (!StringUtils.isBlank(factura.getUrlCallback())) {
                    logger.info(String.format("Actualizando número [%s] en servidor", nroComprobante));
                    String numeroFactura = getPuntoDeVenta() + "-" + nroComprobante;
                    Json response = actualizarNumeroEnServidor(factura.getUrlCallback(), "numeroFactura", numeroFactura);
                    logger.info(String.format("Respuesta servidor actualizar numero factura: [%s]", response));
                }
            } else {
                logger.error("La impresión se realizó correctamente pero no se pudo obtener el número de comprobante generado");
            }
            return nroComprobante;
        } catch (Exception e) {
            logger.error("No se pudo realizar impresion", e);
            throw e;
        } finally {
            try {
                cerrarDocumento();
            } catch (Exception e) {
                logger.error("No se pudo cerrar comprobante", e);
            }
            imprimirFacturaLock.unlock();
        }
    }

    @Override
    public String getUltimoNumeroFactura(String tipoFactura) throws Exception {
        RespuestaConsultarEstado rce = impresora.ConsultarEstado();
        return Integer.toString(rce.getNumeroUltimoComprobante());
    }

    @Override
    public String imprimirNotaCredito(NotaFiscal notaFiscal) throws Exception {
        logger.info("Realizando impresion de nota de credito " + JsonConverter.objectToString(notaFiscal.getDetalle()));
        try {
            cargarDatosCliente(notaFiscal.getFactura().getComprador());
            relactionarFactura(notaFiscal);
            String nroComprobante = abrirNotaCredito(notaFiscal);
            imprimirDetalle(notaFiscal.getDetalle());
            logger.info(String.format("Nota de credito generada [%s]", nroComprobante));
            if (!StringUtils.isBlank(nroComprobante)) {
                // llamar el callback definido
                if (!StringUtils.isBlank(notaFiscal.getUrlCallback())) {
                    String numeroNota = getPuntoDeVenta() + "-" + nroComprobante;
                    logger.info(String.format("Actualizando número [%s] en servidor", numeroNota));
                    Json response = actualizarNumeroEnServidor(notaFiscal.getUrlCallback(), "numeroNota", numeroNota);
                    logger.info(String.format("Respuesta estado servidor: [%s]", response));
                }
            } else {
                logger.error("La impresión se realizó correctamente pero no se pudo obtener el número de comprobante generado");
            }
            return nroComprobante;
        } finally {
            cerrarDocumento();
        }
    }

    @Override
    public String imprimirNotaDebito(NotaFiscal notaFiscal) throws Exception {
        logger.info("Realizando impresion de nota de debito " + JsonConverter.objectToString(notaFiscal.getDetalle()));
        try {
            cargarDatosCliente(notaFiscal.getFactura().getComprador());
            relactionarFactura(notaFiscal);
            String nroComprobante = abrirNotaDebito(notaFiscal);
            imprimirDetalle(notaFiscal.getDetalle());
            logger.info(String.format("Nota de debito generada [%s]", nroComprobante));
            if (!StringUtils.isBlank(nroComprobante)) {
                // llamar el callback definido
                if (!StringUtils.isBlank(notaFiscal.getUrlCallback())) {
                    String numeroNota = getPuntoDeVenta() + "-" + nroComprobante;
                    logger.info(String.format("Actualizando número [%s] en servidor", numeroNota));
                    Json response = actualizarNumeroEnServidor(notaFiscal.getUrlCallback(), "numeroNota", numeroNota);
                    logger.info(String.format("Respuesta estado servidor: [%s]", response));
                }
            } else {
                logger.error("La impresión se realizó correctamente pero no se pudo obtener el número de comprobante generado");
            }
            return nroComprobante;
        } finally {
            cerrarDocumento();
        }
    }

    @Override
    public void cerrarJornadaFiscal() throws HasarException {
        RespuestaCerrarJornadaFiscal respuestaCerrarJornadaFiscal = impresora.CerrarJornadaFiscal(TipoReporte.REPORTE_Z);
        logger.info("Nro. comprobante:       " + respuestaCerrarJornadaFiscal.Z.getNumero());
        logger.info("Fecha:                  " + respuestaCerrarJornadaFiscal.Z.getFecha());
        logger.info("DF Cantidad cancelados: " + respuestaCerrarJornadaFiscal.Z.getDF_CantidadCancelados());
        logger.info("DF Cantidad emitidos:   " + respuestaCerrarJornadaFiscal.Z.getDF_CantidadEmitidos());
        logger.info("DF Total:               " + respuestaCerrarJornadaFiscal.Z.getDF_Total());
        logger.info("DF Total Exento:        " + respuestaCerrarJornadaFiscal.Z.getDF_TotalExento());
        logger.info("DF Total Gravado:       " + respuestaCerrarJornadaFiscal.Z.getDF_TotalGravado());
        logger.info("DF Total IVA:           " + respuestaCerrarJornadaFiscal.Z.getDF_TotalIVA());
        logger.info("DF Total No Gravado:    " + respuestaCerrarJornadaFiscal.Z.getDF_TotalNoGravado());
        logger.info("DF Total Tributo:       " + respuestaCerrarJornadaFiscal.Z.getDF_TotalTributos());
        logger.info("DNFH Cantidad emitidos: " + respuestaCerrarJornadaFiscal.Z.getDNFH_CantidadEmitidos());
        logger.info("DNFH Total:             " + respuestaCerrarJornadaFiscal.Z.getDNFH_Total());
        logger.info("NC Cantidad cancelados: " + respuestaCerrarJornadaFiscal.Z.getNC_CantidadCancelados());
        logger.info("NC Cantidad emitidos:   " + respuestaCerrarJornadaFiscal.Z.getNC_CantidadEmitidos());
        logger.info("NC Total:               " + respuestaCerrarJornadaFiscal.Z.getNC_Total());
        logger.info("NC Total Exento:        " + respuestaCerrarJornadaFiscal.Z.getNC_TotalExento());
        logger.info("NC Total Gravado:       " + respuestaCerrarJornadaFiscal.Z.getNC_TotalGravado());
        logger.info("NC Total IVA:           " + respuestaCerrarJornadaFiscal.Z.getNC_TotalIVA());
        logger.info("NC Total No Gravado:    " + respuestaCerrarJornadaFiscal.Z.getNC_TotalNoGravado());
        logger.info("NC Total Tributo:       " + respuestaCerrarJornadaFiscal.Z.getNC_TotalTributos());
    }

    @Override
    public void imprimirComprobanteNoFiscal(ComprobanteNoFiscal comprobanteNoFiscal) throws Exception {
        logger.info("Realizando impresion de comprobante no fiscal " + JsonConverter.objectToString(comprobanteNoFiscal));
        try {
            abrirComprobanteNoFiscal();
            for (String linea : comprobanteNoFiscal.getLineas()) {
                agregarDetalleComprobanteNoFiscal(linea);
            }
        } finally {
            cerrarDocumento();
        }
    }

    @Override
    public int ejecutarComando(String comando) throws Exception {
        // TODO: para ser implementado
        return 0;
    }

    @Override
    public String leerRespuestaComando(int campo) throws Exception {
        // TODO: para ser implementado
        return null;
    }

    // utiles

    private void cargarDatosCliente(Comprador comprador) throws Exception {
        logger.info("Cargar datos cliente " + JsonConverter.objectToString(comprador));
        impresora.CargarDatosCliente(comprador.getNombre(), comprador.getCuit(), getResponsabilidadIva(comprador), TiposDeDocumentoCliente.TIPO_CUIT, comprador.getDomicilioComercial(), null, null, null);
    }

    private String abrirFactura(Factura factura) throws HasarException {
        logger.info("Abrir factura tipo: " + factura.getTipoFactura());
        RespuestaAbrirDocumento respuestaAbrirDocumento = impresora.AbrirDocumento(getTipoFactura(factura.getTipoFactura()));
        // left padding with zeros
        return String.format("%08d", respuestaAbrirDocumento.getNumeroComprobante());
    }

    private void agregarDetallesFactura(List<DetalleLinea> lineas) throws HasarException {
        for (DetalleLinea linea : lineas) {
            imprimirDetalle(linea);
        }
    }

    private void imprimirDetalle(DetalleLinea linea) throws HasarException {
        logger.info("Imprimir linea detalle " + JsonConverter.objectToString(linea));
        CondicionesIVA condicionesIva = linea.getPorcentajeIva() != null && linea.getPorcentajeIva().equals("21.00") ? CondicionesIVA.GRAVADO : CondicionesIVA.NO_GRAVADO;
        double porcentajeIva = linea.getPorcentajeIva() != null && linea.getPorcentajeIva().equals("21.00") ? 21 : 0;
        impresora.ImprimirItem(linea.getDescripcion(true),
                linea.getCantidad(),
                linea.getPrecioUnitario(),
                condicionesIva,
                porcentajeIva,
                ModosDeMonto.MODO_SUMA_MONTO,
                ModosDeImpuestosInternos.II_VARIABLE_KIVA,
                0.00,
                ModosDeDisplay.DISPLAY_NO,
                ModosDePrecio.MODO_PRECIO_BASE,
                0,
                "P",
                null,
                HasarImpresoraFiscalRG3561.UnidadesMedida.UNIDAD
        );
    }

    private String abrirNotaCredito(NotaFiscal notaFiscal) throws HasarException {
        logger.info("Abrir nota de credito tipo: " + notaFiscal.getFactura().getTipoFactura());
        RespuestaAbrirDocumento respuestaAbrirNotaCredito = impresora.AbrirDocumento(getTipoNotaCredito(notaFiscal.getFactura().getTipoFactura()));
        // left padding with zeros
        return String.format("%08d", respuestaAbrirNotaCredito.getNumeroComprobante());
    }

    private String abrirNotaDebito(NotaFiscal notaFiscal) throws HasarException {
        logger.info("Abrir nota de debito tipo: " + notaFiscal.getFactura().getTipoFactura());
        RespuestaAbrirDocumento respuestaAbrirNotaDebito = impresora.AbrirDocumento(getTipoNotaDebito(notaFiscal.getFactura().getTipoFactura()));
        // left padding with zeros
        return String.format("%08d", respuestaAbrirNotaDebito.getNumeroComprobante());
    }

    private void relactionarFactura(NotaFiscal notaFiscal) throws Exception {
        // Cargar la información de la factura que origina esta nota de crédito/débito
        impresora.CargarDocumentoAsociado(1, getTipoFactura(notaFiscal.getFactura().getTipoFactura()), 1, getNumeroFactura(notaFiscal.getFactura().getNumeroFactura()));
    }

    private void cerrarDocumento() throws HasarException {
        impresora.CerrarDocumento();
    }

    private TiposDeResponsabilidadesCliente getResponsabilidadIva(Comprador comprador) {
        if (comprador.getResponsabilidadIva() != null) {
            switch (comprador.getResponsabilidadIva()) {
                case "C":
                    return TiposDeResponsabilidadesCliente.CONSUMIDOR_FINAL;
                case "I":
                    return TiposDeResponsabilidadesCliente.RESPONSABLE_INSCRIPTO;
                case "E":
                    return TiposDeResponsabilidadesCliente.RESPONSABLE_EXENTO;
                case "M":
                    return TiposDeResponsabilidadesCliente.MONOTRIBUTO;
            }
        }
        return TiposDeResponsabilidadesCliente.CONSUMIDOR_FINAL;
    }

    private TiposComprobante getTipoFactura(String tipoFactura) {
        if (tipoFactura != null) {
            switch (tipoFactura) {
                case "A":
                    return TiposComprobante.TIQUE_FACTURA_A;
                case "B":
                    return TiposComprobante.TIQUE_FACTURA_B;
                case "C":
                    return TiposComprobante.TIQUE_FACTURA_C;
            }
        }
        return null;
    }

    private TiposComprobante getTipoNotaCredito(String tipoFactura) {
        if (tipoFactura != null) {
            switch (tipoFactura) {
                case "A":
                    return TiposComprobante.NOTA_DE_CREDITO_A;
                case "B":
                    return TiposComprobante.NOTA_DE_CREDITO_B;
                case "C":
                    return TiposComprobante.NOTA_DE_CREDITO_C;
            }
        }
        return null;
    }

    private TiposComprobante getTipoNotaDebito(String tipoFactura) {
        if (tipoFactura != null) {
            switch (tipoFactura) {
                case "A":
                    return TiposComprobante.NOTA_DE_DEBITO_A;
                case "B":
                    return TiposComprobante.NOTA_DE_DEBITO_B;
                case "C":
                    return TiposComprobante.NOTA_DE_DEBITO_C;
            }
        }
        return null;
    }

    private int getNumeroFactura(String numero) {
        if (StringUtils.isBlank(numero)) {
            return 0;
        }
        String[] numeroParts = numero.split("-");
        if (numeroParts.length == 2) {
            return Integer.parseInt(numeroParts[1]);
        }
        return 0;
    }

    private String getPuntoDeVenta() throws HasarException {
        if (!StringUtils.isBlank(puntoDeVenta)) {
            return puntoDeVenta;
        } else {
            setPuntoDeVenta();
            if (!StringUtils.isBlank(puntoDeVenta)) {
                return puntoDeVenta;
            }
        }
        return "";
    }

    private void setPuntoDeVenta() throws HasarException {
        RespuestaConsultarDatosInicializacion datosInicializacion = impresora.ConsultarDatosInicializacion();
        puntoDeVenta = String.format("%05d", datosInicializacion.getNumeroPos());
    }

    private void abrirComprobanteNoFiscal() throws Exception {
        impresora.AbrirDocumento(TiposComprobante.GENERICO);
    }

    private void agregarDetalleComprobanteNoFiscal(String linea) throws HasarException {
        Hasar_Funcs.AtributosDeTexto atributosDeTexto = new Hasar_Funcs.AtributosDeTexto();
        impresora.ImprimirTextoGenerico(atributosDeTexto, linea, ModosDeDisplay.DISPLAY_NO);
    }

    private void imprimirInfoConsumidor(Factura factura) {
        try {
            // pie de factura con detalles de impuestos
            String totalSinImpuestos = NumberFormat.getCurrencyInstance().format(factura.getTotal() - factura.getIva() - factura.getImpuestosNacionalesIndirectos());
            String iva = NumberFormat.getCurrencyInstance().format(factura.getIva());
            String impuestosNacionalesIndirectos = NumberFormat.getCurrencyInstance().format(factura.getImpuestosNacionalesIndirectos());
            impresora.ImprimirTextoFiscal(new Hasar_Funcs.AtributosDeTexto(), "Régimen de Transparencia  Fiscal al Consumidor (Ley 27.743)");
            impresora.ImprimirTextoFiscal(new Hasar_Funcs.AtributosDeTexto(), "Precio sin impuestos: " + totalSinImpuestos);
            impresora.ImprimirTextoFiscal(new Hasar_Funcs.AtributosDeTexto(), "IVA contenido: " + iva);
            impresora.ImprimirTextoFiscal(new Hasar_Funcs.AtributosDeTexto(), "Otros Impuestos Nacionales Indirectos: " + impuestosNacionalesIndirectos);
        } catch (Exception e) {
            logger.error("No se pudo imprimir detalles pie de impresion", e);
        }
    }


}

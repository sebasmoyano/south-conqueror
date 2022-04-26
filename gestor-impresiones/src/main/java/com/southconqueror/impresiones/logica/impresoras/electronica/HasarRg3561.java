package com.southconqueror.impresiones.logica.impresoras.electronica;

import com.southconqueror.impresiones.entidades.Comprador;
import com.southconqueror.impresiones.entidades.Factura;
import com.southconqueror.impresiones.logica.impresoras.ImpresoraFiscal;
import com.southconqueror.impresiones.logica.slingr.Json;
import com.southconqueror.impresiones.logica.utiles.JsonConverter;
import com.southconqueror.impresiones.logica.utils.AppProperties;
import hfl.argentina.Estados_Fiscales_RG3561.EstadoFiscal;
import hfl.argentina.HasarException;
import hfl.argentina.HasarImpresoraFiscalRG3561;
import hfl.argentina.HasarImpresoraFiscalRG3561.*;
import hfl.argentina.Hasar_Funcs.AtributosDeTexto;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.concurrent.locks.ReentrantLock;


public class HasarRg3561 implements ImpresoraFiscal {

    private static Logger logger = LogManager.getLogger(HasarRg3561.class);

    private HasarImpresoraFiscalRG3561 impresora;
    private final ReentrantLock imprimirFacturaLock;

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
        impresora.conectar(ipImpresora, 80);
    }

    @Override
    public String imprimirFactura(Factura factura) throws Exception {
        logger.info("Realizando impresion de comprobante " + JsonConverter.objectToString(factura));

        imprimirFacturaLock.lock();
        try {
            cargarDatosCliente(factura.getComprador());
            final String nroComprobante = abrirDocumento(factura);
            agregarDetallesFactura(factura.getDetalle());
            agregarSubtotal();
            agregarTotal(factura);

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
     * @param args the command line arguments
     */
    public static void main(String[] args) throws FileNotFoundException {
        // TODO code application logic here        
        boolean z = false;
        boolean fac = false;
        boolean facA = false;
        boolean notacredA = false;
        boolean notadeb = false;
        boolean fecha = false;
        boolean aud = false;
        boolean dnf = false;
        boolean memoriatrabajo = false;
        boolean datosIni = true;
        boolean cfgFecha = false;
        boolean reporteZNro = false;

        HasarImpresoraFiscalRG3561 p = new HasarImpresoraFiscalRG3561();
        p.establecerTiempoDeEsperaConexion(15000);
        p.establecerTiempoDeEsperaRespuesta(15000);
        p.addFiscalListener(new HasarRg3561EventoFiscalListener());
        p.addFiscalListener(new HasarRg3561EventoImpresoraListener());
        int rev = p.ObtenerRevision();
        System.out.println("revision: " + rev);

        try {
            p.conectar("10.0.7.27", 80);
            //p.ConsultarEstado(TiposComprobante.TIQUE);
            //p.ConsultarEstado();
            

            RespuestaConsultarEstado rce = p.ConsultarEstado();

            if (rce.EstadoAuxiliar.getModoEntrenamiento())
                System.out.println("MODO Entrenamiento");
            else
                System.out.println("MODO Fiscal");


            if (z) {
                RespuestaCerrarJornadaFiscal rjf = p.CerrarJornadaFiscal(TipoReporte.REPORTE_Z);
                System.out.println("Nro. comprobante:       " + rjf.Z.getNumero());
                System.out.println("Fecha:                  " + rjf.Z.getFecha());
                System.out.println("DF Cantidad cancelados: " + rjf.Z.getDF_CantidadCancelados());
                System.out.println("DF Cantidad emitidos:   " + rjf.Z.getDF_CantidadEmitidos());
                System.out.println("DF Total:               " + rjf.Z.getDF_Total());
                System.out.println("DF Total Exento:        " + rjf.Z.getDF_TotalExento());
                System.out.println("DF Total Gravado:       " + rjf.Z.getDF_TotalGravado());
                System.out.println("DF Total IVA:           " + rjf.Z.getDF_TotalIVA());
                System.out.println("DF Total No Gravado:    " + rjf.Z.getDF_TotalNoGravado());
                System.out.println("DF Total Tributo:       " + rjf.Z.getDF_TotalTributos());
                System.out.println("DNFH Cantidad emitidos: " + rjf.Z.getDNFH_CantidadEmitidos());
                System.out.println("DNFH Total:             " + rjf.Z.getDNFH_Total());
                System.out.println("NC Cantidad cancelados: " + rjf.Z.getNC_CantidadCancelados());
                System.out.println("NC Cantidad emitidos:   " + rjf.Z.getNC_CantidadEmitidos());
                System.out.println("NC Total:               " + rjf.Z.getNC_Total());
                System.out.println("NC Total Exento:        " + rjf.Z.getNC_TotalExento());
                System.out.println("NC Total Gravado:       " + rjf.Z.getNC_TotalGravado());
                System.out.println("NC Total IVA:           " + rjf.Z.getNC_TotalIVA());
                System.out.println("NC Total No Gravado:    " + rjf.Z.getNC_TotalNoGravado());
                System.out.println("NC Total Tributo:       " + rjf.Z.getNC_TotalTributos());

            }

            if (fac) {
                RespuestaAbrirDocumento r = p.AbrirDocumento(TiposComprobante.TIQUE_FACTURA_B);
                int nroComprobante = r.getNumeroComprobante();
                System.out.println("Nro. de comprobante: " + nroComprobante);
                CondicionesIVA i = CondicionesIVA.GRAVADO;
                AtributosDeTexto atri = new AtributosDeTexto();
                atri.setCentrado(true);
                atri.setDobleAncho(true);
                //p.ImprimirTextoFiscal(atri, "Texto fiscal");
                //p.ImprimirItem("PLU 1000", 1.0, 3.50,i/*CondicionesIVA.GRAVADO*/, 21.00,ModosDeMonto.MODO_SUMA_MONTO, ModosDeImpuestosInternos.II_FIJO_MONTO, 0.00, ModosDeDisplay.DISPLAY_NO, ModosDePrecio.MODO_PRECIO_TOTAL, "123");
                //for (int j = 0; j < 10; j++)
                p.ImprimirItem("PLU 1001", 2.10, 10.50,i/*CondicionesIVA.GRAVADO*/, 27.00,ModosDeMonto.MODO_SUMA_MONTO, ModosDeImpuestosInternos.II_FIJO_MONTO, 0.00, ModosDeDisplay.DISPLAY_NO, ModosDePrecio.MODO_PRECIO_TOTAL, "456");
                EstadoFiscal ef = p.ObtenerUltimoEstadoFiscal();
                System.out.println("Error de ejecucion: " + ef.getErrorEjecucion());
                System.out.println("Error de estado:    " + ef.getErrorEstado());
                System.out.println("Error general:      " + ef.getErrorGeneral());
                //p.ImprimirDescuentoItem("desc", 1.0,ModosDeDisplay.DISPLAY_NO, ModosDePrecio.MODO_PRECIO_TOTAL);
                RespuestaImprimirPago rp = p.ImprimirPago("Efectivo", 50.0, ModosDePago.PAGAR);
                System.out.println("Saldo: " + rp.getSaldo());

                p.CerrarDocumento();
                RespuestaConsultarAcumuladosComprobante ra = p.ConsultarAcumuladosComprobante(TiposComprobante.TIQUE_FACTURA_B, nroComprobante);

                System.out.println("IVA 1:            " + ra.RegDF.getAlicuotaIVA_1());
                System.out.println("Monto IVA 1:      " + ra.RegDF.getMontoIVA_1());
                System.out.println("Monto Neto IVA 1: " + ra.RegDF.getMontoNetoSinIVA_1());
                System.out.println("Nro Inicial:      " + ra.RegDF.getNumeroInicial());
                System.out.println("Nro final:        " + ra.RegDF.getNumeroFinal());
                System.out.println("Total:            " + ra.RegDF.getTotal());
                System.out.println("Total Exento:     " + ra.RegDF.getTotalExento());
                System.out.println("Total Gravado:    " + ra.RegDF.getTotalGravado());
                System.out.println("Total IVA:        " + ra.RegDF.getTotalIVA());
                System.out.println("Total No Gravado: " + ra.RegDF.getTotalNoGravado());
                System.out.println("Total Tributo:    " + ra.RegDF.getTotalTributos());

                System.out.println("1. registro actual: " + ra.getRegistro());
                System.out.println("2. registro final: " + ra.getRegistro().REGISTRO_DETALLADO_DF.REGISTRO_FINAL);

                System.out.println("IVA 2:            " + ra.RegDF.getAlicuotaIVA_2());
                System.out.println("Monto IVA 2:      " + ra.RegDF.getMontoIVA_2());
                System.out.println("Monto Neto IVA 2: " + ra.RegDF.getMontoNetoSinIVA_2());
                System.out.println("Nro Inicial:      " + ra.RegDF.getNumeroInicial());
                System.out.println("Nro final:        " + ra.RegDF.getNumeroFinal());
                System.out.println("Total:            " + ra.RegDF.getTotal());
                System.out.println("Total Exento:     " + ra.RegDF.getTotalExento());
                System.out.println("Total Gravado:    " + ra.RegDF.getTotalGravado());
                System.out.println("Total IVA:        " + ra.RegDF.getTotalIVA());
                System.out.println("Total No Gravado: " + ra.RegDF.getTotalNoGravado());
                System.out.println("Total Tributo:    " + ra.RegDF.getTotalTributos());
            }

            if (facA) {
                p.CargarDatosCliente("Cliente prueba", "99999999995", TiposDeResponsabilidadesCliente.RESPONSABLE_INSCRIPTO, TiposDeDocumentoCliente.TIPO_CUIT,"Dom. 1", "Dom. 2", "Dom. 3","Dom. 4");
                RespuestaAbrirDocumento r = p.AbrirDocumento(TiposComprobante.TIQUE_FACTURA_A);
                int nroComprobante = r.getNumeroComprobante();
                System.out.println("Nro. de comprobante: " + nroComprobante);
                CondicionesIVA i = CondicionesIVA.GRAVADO;
                AtributosDeTexto atri = new AtributosDeTexto();
                atri.setCentrado(true);
                atri.setDobleAncho(true);
                p.ImprimirTextoFiscal(atri, "Texto fiscal");
                p.ImprimirItem("PLU 1000", 1.0, 3.50,i/*CondicionesIVA.GRAVADO*/, 21.00,ModosDeMonto.MODO_SUMA_MONTO, ModosDeImpuestosInternos.II_FIJO_MONTO, 0.00, ModosDeDisplay.DISPLAY_NO, ModosDePrecio.MODO_PRECIO_TOTAL, "123");
                p.ImprimirItem("PLU 1001", 2.10, 10.50,i/*CondicionesIVA.GRAVADO*/, 21.00,ModosDeMonto.MODO_SUMA_MONTO, ModosDeImpuestosInternos.II_FIJO_MONTO, 0.00, ModosDeDisplay.DISPLAY_NO, ModosDePrecio.MODO_PRECIO_TOTAL, "456");
                p.ImprimirDescuentoItem("desc", 1.0,ModosDeDisplay.DISPLAY_NO, ModosDePrecio.MODO_PRECIO_TOTAL);
                RespuestaConsultarSubtotal rst = p.ConsultarSubtotal();
                System.out.println("SubTot cantidad items: " + rst.getCantidadItems());
                System.out.println("SubTot monto base:     " + rst.getMontoBase());
                System.out.println("SubTot monto iva:      " + rst.getMontoIVA());
                System.out.println("SubTot monto imp int:  " + rst.getMontoImpInternos());
                System.out.println("SubTot monto tributo:  " + rst.getMontoOtrosTributos());
                System.out.println("SubTot monto pagado:   " + rst.getMontoPagado());
                System.out.println("SubTotal:              " + rst.getSubtotal());

                RespuestaImprimirPago rp = p.ImprimirPago("Efectivo", rst.getSubtotal(), ModosDePago.PAGAR);
                System.out.println("Saldo: " + rp.getSaldo());
                p.CerrarDocumento();
                RespuestaConsultarAcumuladosComprobante ra = p.ConsultarAcumuladosComprobante(TiposComprobante.TIQUE_FACTURA_A, nroComprobante);

                System.out.println("IVA 1:            " + ra.RegDF.getAlicuotaIVA_1());
                System.out.println("Monto IVA 1:      " + ra.RegDF.getMontoIVA_1());
                System.out.println("Monto Neto IVA 1: " + ra.RegDF.getMontoNetoSinIVA_1());
                System.out.println("Nro Inicial:      " + ra.RegDF.getNumeroInicial());
                System.out.println("Nro final:        " + ra.RegDF.getNumeroFinal());
                System.out.println("Total:            " + ra.RegDF.getTotal());
                System.out.println("Total Exento:     " + ra.RegDF.getTotalExento());
                System.out.println("Total Gravado:    " + ra.RegDF.getTotalGravado());
                System.out.println("Total IVA:        " + ra.RegDF.getTotalIVA());
                System.out.println("Total No Gravado: " + ra.RegDF.getTotalNoGravado());
                System.out.println("Total Tributo:    " + ra.RegDF.getTotalTributos());
            }

            if (notadeb) {
                p.AbrirDocumento(TiposComprobante.TIQUE_NOTA_DEBITO_B);
                AtributosDeTexto atri = new AtributosDeTexto();
                atri.setCentrado(true);
                atri.setDobleAncho(true);
                p.ImprimirTextoFiscal(atri, "Texto fiscal 1");
                atri.setCentrado(false);
                atri.setDobleAncho(false);
                p.ImprimirTextoFiscal(atri, "Texto fiscal 2");
                p.ImprimirTextoFiscal(atri, "Texto fiscal 3");
                p.ImprimirItem("PLU 1000", 1.0, 3.50,CondicionesIVA.NO_GRAVADO, 0.00,ModosDeMonto.MODO_SUMA_MONTO, ModosDeImpuestosInternos.II_FIJO_MONTO, 0.00, ModosDeDisplay.DISPLAY_NO, ModosDePrecio.MODO_PRECIO_TOTAL, "123");
                p.CerrarDocumento();

            }
            if (notacredA) {
                p.CargarDatosCliente("Cliente prueba", "99999999995", TiposDeResponsabilidadesCliente.RESPONSABLE_INSCRIPTO, TiposDeDocumentoCliente.TIPO_CUIT,"Dom. 1", "Dom. 2", "Dom. 3","Dom. 4");
                p.CargarDocumentoAsociado(1, TiposComprobante.TIQUE_FACTURA_A, 1, 3);
                p.CargarDocumentoAsociado(2, TiposComprobante.TIQUE_FACTURA_A, 1, 2);
                RespuestaAbrirDocumento r = p.AbrirDocumento(TiposComprobante.TIQUE_NOTA_CREDITO_A);
                int nroComprobante = r.getNumeroComprobante();
                System.out.println("Nro. de comprobante: " + nroComprobante);
                CondicionesIVA i = CondicionesIVA.GRAVADO;
                AtributosDeTexto atri = new AtributosDeTexto();
                atri.setCentrado(true);
                atri.setDobleAncho(true);
                p.ImprimirTextoFiscal(atri, "Texto fiscal");
                p.ImprimirItem("PLU 1000", 1.0, 3.50,i/*CondicionesIVA.GRAVADO*/, 21.00,ModosDeMonto.MODO_SUMA_MONTO, ModosDeImpuestosInternos.II_FIJO_MONTO, 0.00, ModosDeDisplay.DISPLAY_NO, ModosDePrecio.MODO_PRECIO_TOTAL, "123");
                p.ImprimirItem("PLU 1001", 2.10, 10.50,i/*CondicionesIVA.GRAVADO*/, 21.00,ModosDeMonto.MODO_SUMA_MONTO, ModosDeImpuestosInternos.II_FIJO_MONTO, 0.00, ModosDeDisplay.DISPLAY_NO, ModosDePrecio.MODO_PRECIO_TOTAL, "456");
                p.ImprimirDescuentoItem("desc", 1.0,ModosDeDisplay.DISPLAY_NO, ModosDePrecio.MODO_PRECIO_TOTAL);
                RespuestaImprimirPago rp = p.ImprimirPago("Efectivo", 50.0, ModosDePago.PAGAR);
                System.out.println("Saldo: " + rp.getSaldo());

                p.CerrarDocumento();
                RespuestaConsultarAcumuladosComprobante ra = p.ConsultarAcumuladosComprobante(TiposComprobante.TIQUE_NOTA_CREDITO_A, nroComprobante);

                System.out.println("IVA 1:            " + ra.RegDF.getAlicuotaIVA_1());
                System.out.println("Monto IVA 1:      " + ra.RegDF.getMontoIVA_1());
                System.out.println("Monto Neto IVA 1: " + ra.RegDF.getMontoNetoSinIVA_1());
                System.out.println("Nro Inicial:      " + ra.RegDF.getNumeroInicial());
                System.out.println("Nro final:        " + ra.RegDF.getNumeroFinal());
                System.out.println("Total:            " + ra.RegDF.getTotal());
                System.out.println("Total Exento:     " + ra.RegDF.getTotalExento());
                System.out.println("Total Gravado:    " + ra.RegDF.getTotalGravado());
                System.out.println("Total IVA:        " + ra.RegDF.getTotalIVA());
                System.out.println("Total No Gravado: " + ra.RegDF.getTotalNoGravado());
                System.out.println("Total Tributo:    " + ra.RegDF.getTotalTributos());
            }

            if (memoriatrabajo) {
                RespuestaConsultarAcumuladosMemoriaDeTrabajo rmt = p.ConsultarAcumuladosMemoriaDeTrabajo();
                System.out.println("DF Total:            " + rmt.RegDF.getTotal());
                System.out.println("DF Total Exento:     " + rmt.RegDF.getTotalExento());
                System.out.println("DF Total Gravado:    " + rmt.RegDF.getTotalGravado());
                System.out.println("DF Total IVA:        " + rmt.RegDF.getTotalIVA());
                System.out.println("DF Total No Gravado: " + rmt.RegDF.getTotalNoGravado());
                System.out.println("DF Total Tributos:   " + rmt.RegDF.getTotalTributos());
                RespuestaContinuarConsultaAcumulados rca = p.ContinuarConsultaAcumulados();

                while (rca.getRegistro() != rca.getRegistro().REGISTRO_FINAL) {

                    if (rca.getRegistro() == rca.getRegistro().REGISTRO_DETALLADO_DF) {
                        System.out.println("DF Total:            " + rca.RegDF.getTotal());
                        System.out.println("DF Total Exento:     " + rca.RegDF.getTotalExento());
                        System.out.println("DF Total Gravado:    " + rca.RegDF.getTotalGravado());
                        System.out.println("DF Total IVA:        " + rca.RegDF.getTotalIVA());
                        System.out.println("DF Total No Gravado: " + rca.RegDF.getTotalNoGravado());
                        System.out.println("DF Total Tributos:   " + rca.RegDF.getTotalTributos());
                    }

                    if (rca.getRegistro() == rca.getRegistro().REGISTRO_DETALLADO_DNFH) {
                        System.out.println("DNFH Total:            " + rca.RegDNFH.getTotal());
                        System.out.println("Nro. inicial:     " + rca.RegDNFH.getNumeroInicial());
                        System.out.println("Nro. final:    " + rca.RegDNFH.getNumeroFinal());
                    }

                    if (rca.getRegistro() == rca.getRegistro().REGISTRO_GLOBAL) {
                        System.out.println("Global DF Total:            " + rca.RegGlobal.getDF_Total());
                        System.out.println("Global DF Total Exento:     " + rca.RegGlobal.getDF_TotalExento());
                        System.out.println("Global DF Total Gravado:    " + rca.RegGlobal.getDF_TotalGravado());
                        System.out.println("Global DF Total IVA:        " + rca.RegGlobal.getDF_TotalIVA());
                        System.out.println("Global DF Total No Gravado: " + rca.RegGlobal.getDF_TotalNoGravado());
                        System.out.println("Global DF Total Tributos:   " + rca.RegGlobal.getDF_TotalTributos());
                        System.out.println("Global DNFH Total :         " + rca.RegGlobal.getDNFH_Total());
                    }

                    rca = p.ContinuarConsultaAcumulados();                
                }
            }

            if (dnf) {
                p.AbrirDocumento(TiposComprobante.GENERICO);
                AtributosDeTexto atri = new AtributosDeTexto();
                p.ImprimirTextoGenerico(atri, "Texto generico normal");
                atri.setNegrita(true);
                p.ImprimirTextoGenerico(atri, "Texto generico negrita");
                atri.setNegrita(false);
                atri.setDobleAncho(true);
                p.ImprimirTextoGenerico(atri, "Texto generico doble");
                p.CerrarDocumento();
            }

            if (datosIni) {
                RespuestaConsultarDatosInicializacion rdi = p.ConsultarDatosInicializacion();
                System.out.println("Razón social:                " + rdi.getRazonSocial());
                System.out.println("C.U.I.T.:                    " + rdi.getCUIT());
                System.out.println("Ingresos Brutos:             " + rdi.getIngBrutos());
                System.out.println("Fecha inicio de actividades: " + rdi.getFechaInicioActividades());
                System.out.println("Número de POS:               " + rdi.getNumeroPos());
                System.out.println("Responsabilidad IVA:         " + rdi.getResponsabilidadIVA());
            }

            if (cfgFecha) {
                Date fechaHora = new Date();
                System.out.println("Configurar fecha y hora: " + fechaHora);
                p.ConfigurarFechaHora(fechaHora, fechaHora);
            }

            if (fecha) {
                RespuestaConsultarFechaHora r = p.ConsultarFechaHora();
                System.out.println("Leer fecha: " + r.getFecha());
                System.out.println("Leer hora:  " +r.getHora());
            }
            
            if (reporteZNro) {
                p.ReportarZetasPorNumeroZeta(1, 2, TipoReporteAuditoria.REPORTE_AUDITORIA_GLOBAL);
            }

            if (aud) {
                String fileName = "audPrueba.txt";

                try {
                    String str = null;
                    FileOutputStream outputStream = new FileOutputStream(fileName);
                    Calendar iniDate = new GregorianCalendar(2015, Calendar.MAY, 27, 10, 25, 00);
                    System.out.println("Initial Date: " + iniDate.getTime());
                    Date d1 = new Date();
                    RespuestaObtenerPrimerBloqueReporteElectronico rba = p.ObtenerPrimerBloqueReporteElectronico(iniDate.getTime(),d1);
                    str = rba.getInformacion();
                    outputStream.write(str.getBytes());
                    RespuestaObtenerSiguienteBloqueReporteElectronico ros = null;

                    do {
                        ros = p.ObtenerSiguienteBloqueReporteElectronico();
                        str = ros.getInformacion();
                        outputStream.write(str.getBytes());
                    } while (ros.getRegistro() != ros.getRegistro().BLOQUE_FINAL);

                    outputStream.close();
                }
                catch(FileNotFoundException ex) {
                    System.out.println("No está disponible el archivo: " + fileName);
                }
                catch(IOException ex) {
                    System.out.println("Error de lectura: " + fileName);
                    // Or we could just do this:
                    // ex.printStackTrace();
                }
            }

            //p.CerrarJornadaFiscal(TipoReporte.REPORTE_X);
        } catch (HasarException ex) {
            System.out.println(ex.getMessage());
        }
    }


    // utiles

    private void cargarDatosCliente(Comprador comprador) throws Exception {
        logger.info("Cargar datos cliente " + JsonConverter.objectToString(comprador));
        impresora.CargarDatosCliente(comprador.getNombre(), comprador.getCuit(), getResponsabilidadIva(comprador), TiposDeDocumentoCliente.TIPO_CUIT,comprador.getDomicilioComercial(), null, null, null);
    }

    private String abrirDocumento(Factura factura) throws HasarException {
        logger.info("Abrir factura tipo: " + factura.getTipoFactura());
        RespuestaAbrirDocumento respuestaAbrirDocumento = impresora.AbrirDocumento(getTipoComprobante(factura));
        return Integer.toString(respuestaAbrirDocumento.getNumeroComprobante());
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

    private TiposComprobante getTipoComprobante(Factura factura) {
        switch (factura.getTipoFactura()) {
            case "A":
                return TiposComprobante.TIQUE_FACTURA_A;
            case "B":
                return TiposComprobante.TIQUE_FACTURA_B;
            case "C":
                return TiposComprobante.TIQUE_FACTURA_C;
        }
        return null;
    }

}

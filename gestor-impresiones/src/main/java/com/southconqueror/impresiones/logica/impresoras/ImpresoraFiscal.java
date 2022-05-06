package com.southconqueror.impresiones.logica.impresoras;

import com.southconqueror.impresiones.entidades.ComprobanteNoFiscal;
import com.southconqueror.impresiones.entidades.Factura;
import com.southconqueror.impresiones.entidades.NotaFiscal;
import com.southconqueror.impresiones.logica.impresoras.electronica.HasarRg3561;
import com.southconqueror.impresiones.logica.impresoras.serial.HasarSerial;
import com.southconqueror.impresiones.logica.slingr.AppManager;
import com.southconqueror.impresiones.logica.slingr.Json;
import com.southconqueror.impresiones.logica.utils.AppProperties;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public abstract class ImpresoraFiscal {

    private static Logger logger = LogManager.getLogger(ImpresoraFiscal.class);

    private static ImpresoraFiscal instance;

    public abstract void configurar() throws Exception;

    public abstract String imprimirFactura(Factura factura) throws Exception;

    public abstract String getUltimoNumeroFactura(String tipoFactura) throws Exception;

    public abstract String imprimirNotaCredito(NotaFiscal notaFiscal) throws Exception;

    public abstract String imprimirNotaDebito(NotaFiscal notaFiscal) throws Exception;

    public abstract void imprimirComprobanteNoFiscal(ComprobanteNoFiscal comprobanteNoFiscal) throws Exception;

    public abstract int ejecutarComando(String comando) throws Exception;

    public abstract String leerRespuestaComando(int campo) throws Exception;

    public synchronized static ImpresoraFiscal getInstance() {
        if (instance == null) {
            if (AppProperties.getInstance().getModeloImpresora().contains("250F")) {
                instance = HasarRg3561.getInstance();
            } else {
                instance = HasarSerial.getInstance();
            }
        }
        return instance;
    }

    protected Json actualizarNumeroEnServidor(String url, String numeroField, String numero) {
        Json paylod = Json.map();
        paylod.set(numeroField, numero);
        try {
            return AppManager.getInstance().actualizarNumero(url, paylod);
        } catch (Exception e) {
            logger.error("No se pudo actualizar numero en servidor", e);
            throw new RuntimeException("No se pudo actualizar numero de comprobante en servidor");
        }
    }


}

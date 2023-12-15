package com.southconqueror.impresiones.logica.impresoras.electronica;

import hfl.argentina.EventoImpresora;
import hfl.argentina.PrinterEventListener;
import hfl.argentina.PrinterEventObject;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class HasarRg3561EventoImpresoraListener implements PrinterEventListener {

    private static Logger logger = LogManager.getLogger(HasarRg3561EventoImpresoraListener.class);

    public void statusChanged(PrinterEventObject args) {
        EventoImpresora e = args.getEventoImpresora();
        logger.info("*** Hasar Evento impresora ***");
        logger.info("Cajon abierto:            " + e.getPrinterStatus().getCajonAbierto());
        logger.info("Error impresora:          " + e.getPrinterStatus().getErrorImpresora());
        logger.info("Falta papel auditoria:    " + e.getPrinterStatus().getFaltaPapelJournal());
        logger.info("Falta papel tique:        " + e.getPrinterStatus().getFaltaPapelReceipt());
        logger.info("Impresora ocupada:        " + e.getPrinterStatus().getImpresoraOcupada());
        logger.info("Impresora fuera de linea: " + e.getPrinterStatus().getImpresoraOffLine());
        logger.info("Tapa abierta:             " + e.getPrinterStatus().getTapaAbierta());
        logger.info("Or logico:                " + e.getPrinterStatus().getOrLogico());
    }

}

package com.southconqueror.impresiones.logica.impresoras.electronica;

import hfl.argentina.FiscalEventListener;
import hfl.argentina.FiscalEventObject;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class HasarRg3561EventoFiscalListener implements FiscalEventListener {

    private static Logger logger = LogManager.getLogger(HasarRg3561EventoFiscalListener.class);

    @Override
    public void processChanged(FiscalEventObject args) {
        logger.info(String.format("Comando procesado %s", args));
    }

    @Override
    public void endProcessChanged(FiscalEventObject args) {
        logger.info(String.format("Finalizado procesamiento de comando %s", args.getEventoFiscal()));
    }

}

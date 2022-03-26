package com.southconqueror.impresiones;

import com.southconqueror.impresiones.logica.Contexto;
import com.southconqueror.impresiones.logica.impresoraFiscal.ImpresorFiscalManager;
import com.southconqueror.impresiones.serviciosweb.comprobantes.*;
import com.southconqueror.impresiones.serviciosweb.estacionamiento.TicketsEstacionamientoControlador;
import com.southconqueror.impresiones.serviciosweb.filters.ResponseCorsFilter;
import com.southconqueror.impresiones.serviciosweb.rotonda.FormulariosGobiernoControlador;
import com.southconqueror.utils.AppProperties;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import java.util.Arrays;
import java.util.List;

public class Main {

    private static Logger logger = LogManager.getRootLogger();

    public static void main(String[] args) throws Exception {
        iniciarApp();
    }

    private static void iniciarApp() throws Exception {
        logger.info("Iniciando gestor impresiones...");
        // iniciar propiedades
        AppProperties.getInstance().initProperties();
        // iniciar contexto
        final String app = AppProperties.getInstance().getApp();
        logger.info(String.format("La app es [%s]", app));
        if (app == null) {
            throw new Exception("No se ha configurado el nombre de la aplicacion");
        }
        Contexto.setApp(app);
        // inicializar impresora
        iniciarImpresora();
        // inicializar servidor embebido
        iniciarServer();
    }

    private static void iniciarServer() throws Exception {
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");

        Server jettyServer = new Server(8080);
        jettyServer.setHandler(context);

        ServletHolder jerseyServlet = context.addServlet(org.glassfish.jersey.servlet.ServletContainer.class, "/*");
        jerseyServlet.setInitOrder(0);

        List<String> controladores = Arrays.asList(
                FacturasControlador.class.getCanonicalName(),
                NotasControlador.class.getCanonicalName(),
                ComprobantesNoFiscalesControlador.class.getCanonicalName(),
                TicketsEstacionamientoControlador.class.getCanonicalName(),
                ResponseCorsFilter.class.getCanonicalName(),
                ComprobantesGenericosControlador.class.getCanonicalName(),
                FormulariosGobiernoControlador.class.getCanonicalName(),
                ImpresoraFiscalControlador.class.getCanonicalName());
        jerseyServlet.setInitParameter("jersey.config.server.provider.classnames", StringUtils.join(controladores, ","));
        try {
            jettyServer.start();
            jettyServer.join();
        } finally {
            jettyServer.destroy();
        }

    }

    private static void iniciarImpresora() {
        try {
            ImpresorFiscalManager.getInstance().iniciarImpresora();
        } catch (Exception e) {
            logger.error("Error al inicializar impresora", e);
            logger.info("Iniciando como impresora PDF");
        }
    }


}
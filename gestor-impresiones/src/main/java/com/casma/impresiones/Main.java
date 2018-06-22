package com.casma.impresiones;

import com.casma.impresiones.logica.Contexto;
import com.casma.impresiones.logica.impresoraFiscal.ImpresorFiscalManager;
import com.casma.impresiones.serviciosweb.comprobantes.*;
import com.casma.impresiones.serviciosweb.estacionamiento.TicketsEstacionamientoControlador;
import com.casma.impresiones.serviciosweb.filters.ResponseCorsFilter;
import com.casma.impresiones.serviciosweb.rotonda.FormulariosGobiernoControlador;
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
        if (args == null || args.length == 0) {
            throw new Exception("Aplicacion a iniciar debe ser especificada");
        }
        logger.info("Iniciando gestor impresiones para app: " + args[0]);
        iniciarContexto(args[0]);
    }

    private static void iniciarContexto(String app) throws Exception {
        Contexto.setApp(app);
        iniciarImpresora();
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
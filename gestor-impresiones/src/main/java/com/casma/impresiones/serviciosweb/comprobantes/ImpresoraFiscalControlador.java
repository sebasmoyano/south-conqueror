package com.casma.impresiones.serviciosweb.comprobantes;

import com.casma.impresiones.entidades.ComandoImpresora;
import com.casma.impresiones.logica.impresoraFiscal.ImpresorFiscalManager;
import com.casma.impresiones.logica.utiles.JsonConverter;
import com.casma.impresiones.serviciosweb.ResponseError;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Created by smoyano on 14/11/16.
 */
@Path("/impresoraFiscal")
public class ImpresoraFiscalControlador {

    private static Logger logger = LogManager.getLogger(ImpresoraFiscalControlador.class);

    @POST
    @Path("/ejecutarComando")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response ejecutarComando(ComandoImpresora comando) {
        logger.info(String.format("Recibido request para ejecutar comando [%s]", JsonConverter.objectToString(comando)));

        try {
            int rtaComando = ImpresorFiscalManager.getInstance().ejecutarComando(comando.getComando());
            return Response
                    .status(Response.Status.OK)
                    .entity("{\"codigoRespuesta\": \"" + rtaComando + "\"}").build();
        } catch (Exception e) {
            logger.error("Error al ejecutar comando", e);
            ResponseError systemError = new ResponseError(Response.Status.INTERNAL_SERVER_ERROR, e.getMessage());
            return Response.status(systemError.getStatus()).entity(systemError.convertirAJson()).build();
        }
    }

    @GET
    @Path("/leerRespuestaComando")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response leerRespuestaComando(@QueryParam("campo") int campo) {
        logger.info("Recibido request para leer respuesta comando");

        try {
            String respuestaComando = ImpresorFiscalManager.getInstance().leerRespuestaComando(campo);
            return Response
                    .status(Response.Status.OK)
                    .entity("{\"respuestaComando\": \"" + respuestaComando + "\"}").build();
        } catch (Exception e) {
            logger.error("Error al obtener ultimo respuesta comando", e);
            ResponseError systemError = new ResponseError(Response.Status.INTERNAL_SERVER_ERROR, e.getMessage());
            return Response.status(systemError.getStatus()).entity(systemError.convertirAJson()).build();
        }
    }



}

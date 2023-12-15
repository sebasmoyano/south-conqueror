package com.southconqueror.impresiones.serviciosweb.rotonda;

import com.southconqueror.impresiones.entidades.FormularioGobierno;
import com.southconqueror.impresiones.logica.rotonda.FormulariosGobiernoManager;
import com.southconqueror.impresiones.logica.utiles.JsonConverter;
import com.southconqueror.impresiones.serviciosweb.ResponseError;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Created by smoyano on 14/11/17.
 */
@Path("/formularioGobierno")
public class FormulariosGobiernoControlador {

    private static Logger logger = LogManager.getLogger(FormulariosGobiernoControlador.class);

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response crearComprobante(FormularioGobierno formularioGobierno) {
        logger.info(String.format("Recibido request para generar formulario gobierno [%s]", JsonConverter.objectToString(formularioGobierno)));

        try {
            FormulariosGobiernoManager.getInstance().crearComprobante(formularioGobierno);
            return Response
                    .status(Response.Status.OK)
                    .entity("{\"success\": true}").build();
        } catch (Exception e) {
            logger.error("No se pudo crear formulario gobierno", e);
            ResponseError systemError = new ResponseError(Response.Status.INTERNAL_SERVER_ERROR, e.getMessage());
            return Response.status(systemError.getStatus()).entity(systemError.convertirAJson()).build();
        }
    }


}
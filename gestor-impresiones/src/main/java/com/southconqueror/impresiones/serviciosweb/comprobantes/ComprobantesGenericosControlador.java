package com.southconqueror.impresiones.serviciosweb.comprobantes;

import com.southconqueror.impresiones.entidades.ComprobanteGenerico;
import com.southconqueror.impresiones.logica.comprobantes.ComprobantesGenericosManager;
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
@Path("/comprobantesGenericos")
public class ComprobantesGenericosControlador {

    private static Logger logger = LogManager.getLogger(ComprobantesGenericosControlador.class);

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response generarTicket(ComprobanteGenerico comprobante) {
        logger.info(String.format("Recibido request para generar comprobante [%s]", JsonConverter.objectToString(comprobante)));

        try {
            ComprobantesGenericosManager.getInstance().crearComprobante(comprobante);
            return Response
                    .status(Response.Status.OK)
                    .entity("{\"success\": true}").build();
        } catch (Exception e) {
            logger.error("No se pudo crear comprobante", e);
            ResponseError systemError = new ResponseError(Response.Status.INTERNAL_SERVER_ERROR, e.getMessage());
            return Response.status(systemError.getStatus()).entity(systemError.convertirAJson()).build();
        }
    }






}
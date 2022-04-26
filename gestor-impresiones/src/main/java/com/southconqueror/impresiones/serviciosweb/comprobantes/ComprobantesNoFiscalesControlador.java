package com.southconqueror.impresiones.serviciosweb.comprobantes;

import com.southconqueror.impresiones.entidades.*;
import com.southconqueror.impresiones.logica.impresoras.serial.HasarSerial;
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
 * Created by smoyano on 31/3/17.
 */
@Path("/comprobantesNoFiscales")
public class ComprobantesNoFiscalesControlador {

    private static Logger logger = LogManager.getLogger(ComprobantesNoFiscalesControlador.class);

    @POST
    @Path("/imprimir")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response imprimirComprobante(ComprobanteNoFiscal comprobanteNoFiscal) {
        logger.info(String.format("Recibido request para imprimir comprobante no fiscal [%s]", JsonConverter.objectToString(comprobanteNoFiscal)));

        ResponseError error = validarComprobante(comprobanteNoFiscal);
        if (error != null) {
            logger.error("Se encontraron errores de validacion de comprobante " + error.convertirAJson());
            return Response.status(error.getStatus()).entity(error.convertirAJson()).build();
        }
        try {
            HasarSerial.getInstance().imprimirComprobanteNoFiscal(comprobanteNoFiscal);
        } catch (Exception e) {
            logger.error("Error al imprimir comprobante", e);
            ResponseError systemError = new ResponseError(Response.Status.INTERNAL_SERVER_ERROR, e.getMessage());
            return Response.status(systemError.getStatus()).entity(systemError.convertirAJson()).build();
        }

        return Response
                .status(Response.Status.OK)
                .entity("{\"success\": true}").build();
    }

    private ResponseError validarComprobante(ComprobanteNoFiscal comprobanteNoFiscal) {
        if (comprobanteNoFiscal.getLineas() == null || comprobanteNoFiscal.getLineas().isEmpty()) {
            return new ResponseError(Response.Status.BAD_REQUEST, "Debe definirse al menos un detalle a imprimir");
        }
        return null;
    }


}

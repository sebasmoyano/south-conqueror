package com.southconqueror.impresiones.serviciosweb.comprobantes;

import com.southconqueror.impresiones.entidades.*;
import com.southconqueror.impresiones.logica.comprobantes.NotasManager;
import com.southconqueror.impresiones.logica.impresoraFiscal.ImpresorFiscalManager;
import com.southconqueror.impresiones.logica.utiles.JsonConverter;
import com.southconqueror.impresiones.serviciosweb.ResponseError;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Created by smoyano on 14/11/16.
 */
@Path("/notas")
public class NotasControlador {

    private static Logger logger = LogManager.getLogger(NotasControlador.class);

    @POST
    @Path("/imprimir")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response imprimirNota(NotaFiscal notaFiscal) {
        logger.info(String.format("Recibido request para imprimir nota [%s]", JsonConverter.objectToString(notaFiscal.getDetalle())));

        ResponseError error = validarNota(notaFiscal);
        if (error != null) {
            logger.error("Se encontraron errores de validacion de nota " + error.convertirAJson());
            return Response.status(error.getStatus()).entity(error.convertirAJson()).build();
        }
        try {
            String nroComprobante = null;
            switch (notaFiscal.getTipo()) {
                case CREDITO:
                    nroComprobante = ImpresorFiscalManager.getInstance().imprimirNotaCredito(notaFiscal);
                    break;
                case DEBITO:
                    nroComprobante = ImpresorFiscalManager.getInstance().imprimirNotaDebito(notaFiscal);
                    break;
            }
            return Response
                    .status(Response.Status.OK)
                    .entity("{\"numeroComprobante\": \"" + nroComprobante + "\"}").build();
        } catch (Exception e) {
            logger.error("Error al imprimir nota", e);
            ResponseError systemError = new ResponseError(Response.Status.INTERNAL_SERVER_ERROR, e.getMessage());
            return Response.status(systemError.getStatus()).entity(systemError.convertirAJson()).build();
        }
    }

    @POST
    @Path("/exportar/pdf")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response imprimirNotaPdf(NotaFiscal notaFiscal) {
        logger.info(String.format("Recibido request para imprimir nota [%s]", JsonConverter.objectToString(notaFiscal.getDetalle())));

        ResponseError error = validarNota(notaFiscal);
        if (error != null) {
            logger.error("Se encontraron errores de validacion de nota " + error.convertirAJson());
            return Response.status(error.getStatus()).entity(error.convertirAJson()).build();
        }
        try {
            NotasManager.getInstance().exportarAPdf(notaFiscal);
            return Response.status(Response.Status.OK).build();
        } catch (Exception e) {
            logger.error("Error al imprimir nota", e);
            ResponseError systemError = new ResponseError(Response.Status.INTERNAL_SERVER_ERROR, e.getMessage());
            return Response.status(systemError.getStatus()).entity(systemError.convertirAJson()).build();
        }
    }

    private ResponseError validarNota(NotaFiscal notaFiscal) {
        if (notaFiscal.getTipo() == null) {
            return new ResponseError(Response.Status.BAD_REQUEST, "No se ha definido un tipo de nota");
        }
        if (notaFiscal.getFactura() == null) {
            return new ResponseError(Response.Status.BAD_REQUEST, "No se ha definido la factura relacionada a la nota");
        }
        if (notaFiscal.getFactura().getComprador() == null) {
            return new ResponseError(Response.Status.BAD_REQUEST, "No se ha definido los datos del comprador");
        }
        if (notaFiscal.getDetalle() == null) {
            return new ResponseError(Response.Status.BAD_REQUEST, "No se ha definido un motivo de la nota");
        }
        return null;
    }


}

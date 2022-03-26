package com.southconqueror.impresiones.serviciosweb.estacionamiento;

import com.southconqueror.impresiones.entidades.Ticket;
import com.southconqueror.impresiones.logica.estacionamiento.EstacionamientoTicketsManager;
import com.southconqueror.impresiones.serviciosweb.ResponseError;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Created by smoyano on 14/11/16.
 */
@Path("/tickets")
public class TicketsEstacionamientoControlador {

    private static Logger logger = LogManager.getLogger(TicketsEstacionamientoControlador.class);

    @POST
    @Path("/imprimir")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response generarTicket(Ticket ticket) {
        logger.info(String.format("Recibido request para generar ticket [%s]", ticket.getCode()));

        try {
            EstacionamientoTicketsManager.getInstance().crearTicket(ticket);
            return Response
                    .status(Response.Status.OK)
                    .entity("{\"success\": true}").build();
        } catch (Exception e) {
            logger.error("No se pudo crear ticket", e);
            ResponseError systemError = new ResponseError(Response.Status.INTERNAL_SERVER_ERROR, e.getMessage());
            return Response.status(systemError.getStatus()).entity(systemError.convertirAJson()).build();
        }
    }






}
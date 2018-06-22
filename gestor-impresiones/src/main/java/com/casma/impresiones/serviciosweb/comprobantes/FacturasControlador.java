package com.casma.impresiones.serviciosweb.comprobantes;

import com.casma.impresiones.entidades.Comprador;
import com.casma.impresiones.entidades.Factura;
import com.casma.impresiones.entidades.DetalleLinea;
import com.casma.impresiones.entidades.TipoFactura;
import com.casma.impresiones.logica.comprobantes.FacturasManager;
import com.casma.impresiones.logica.impresoraFiscal.ImpresorFiscalManager;
import com.casma.impresiones.logica.utiles.JsonConverter;
import com.casma.impresiones.serviciosweb.ResponseError;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Created by smoyano on 14/11/16.
 */
@Path("/facturas")
public class FacturasControlador {

    private static Logger logger = LogManager.getLogger(FacturasControlador.class);

    @POST
    @Path("/imprimir")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response imprimirFactura(Factura factura) {
        logger.info(String.format("Recibido request para imprimir factura [%s]", JsonConverter.objectToString(factura)));

        ResponseError error = validarFactura(factura);
        if (error != null) {
            logger.error("Se encontraron errores de validacion de factura " + error.convertirAJson());
            return Response.status(error.getStatus()).entity(error.convertirAJson()).build();
        }
        try {
            String nroComprobante = ImpresorFiscalManager.getInstance().imprimirFactura(factura);
            return Response
                    .status(Response.Status.OK)
                    .entity("{\"numeroComprobante\": \"" + nroComprobante + "\"}").build();
        } catch (Exception e) {
            logger.error("Error al imprimir factura", e);
            ResponseError systemError = new ResponseError(Response.Status.INTERNAL_SERVER_ERROR, e.getMessage());
            return Response.status(systemError.getStatus()).entity(systemError.convertirAJson()).build();
        }
    }

    @POST
    @Path("/exportar/pdf")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response exportarAPdf(Factura factura) {
        logger.info(String.format("Recibido request para exportar a PDF factura [%s]", JsonConverter.objectToString(factura)));

        ResponseError error = validarFactura(factura);
        if (error != null) {
            logger.error("Se encontraron errores de validacion de factura " + error.convertirAJson());
            return Response.status(error.getStatus()).entity(error.convertirAJson()).build();
        }
        try {
            FacturasManager.getInstance().exportarAPdf(factura);
            return Response
                    .status(Response.Status.OK)
                    .entity("{\"success\": true}").build();
        } catch (Exception e) {
            logger.error("Error al exportar a PDF factura", e);
            ResponseError systemError = new ResponseError(Response.Status.INTERNAL_SERVER_ERROR, e.getMessage());
            return Response.status(systemError.getStatus()).entity(systemError.convertirAJson()).build();
        }
    }

    @GET
    @Path("/ultimoNumero")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response obtenerUltimoNumero(@DefaultValue("B")@QueryParam("tipoFactura") String tipoFactura) {
        logger.info("Recibido request para obtener ultimo nro de factura");

        try {
            String ultimoNumero = ImpresorFiscalManager.getInstance().getUltimoNumeroFactura(tipoFactura);
            return Response
                    .status(Response.Status.OK)
                    .entity("{\"ultimoNumero\": \"" + ultimoNumero + "\"}").build();
        } catch (Exception e) {
            logger.error("Error al obtener ultimo numero factura", e);
            ResponseError systemError = new ResponseError(Response.Status.INTERNAL_SERVER_ERROR, e.getMessage());
            return Response.status(systemError.getStatus()).entity(systemError.convertirAJson()).build();
        }
    }

    private ResponseError validarFactura(Factura factura) {
        if (TipoFactura.fromString(factura.getTipoFactura()) == null) {
            return new ResponseError(Response.Status.BAD_REQUEST, "Tipo factura no valido");
        }
        if (factura.getComprador() == null) {
            return new ResponseError(Response.Status.BAD_REQUEST, "No se ha definido un comprador");
        }
        Comprador comprador = factura.getComprador();
        if (StringUtils.isBlank(comprador.getNombre())) {
            return new ResponseError(Response.Status.BAD_REQUEST, "Nombre comprador no valido");
        }
        if (StringUtils.isBlank(comprador.getCuit()) || comprador.getCuit().length() != 11) {
            return new ResponseError(Response.Status.BAD_REQUEST, "CUIT no valido");
        }
        if (StringUtils.isBlank(comprador.getResponsabilidadIva())) {
            return new ResponseError(Response.Status.BAD_REQUEST, "Responsabilidad frente al IVA no valido");
        }
        if (StringUtils.isBlank(comprador.getDomicilioComercial())) {
            return new ResponseError(Response.Status.BAD_REQUEST, "Domicilio comercial no valido");
        }
        if (factura.getDetalle() == null || factura.getDetalle().isEmpty()) {
            return new ResponseError(Response.Status.BAD_REQUEST, "No se ha definido un detalle de factura");
        }
        for (int i = 0; i < factura.getDetalle().size(); i++) {
            DetalleLinea linea = factura.getDetalle().get(i);
            if (linea.getCantidad() <= 0) {
                return new ResponseError(Response.Status.BAD_REQUEST, "Cantidad no valida en linea " + i);
            }
            if (StringUtils.isBlank(linea.getDescripcion(false))) {
                return new ResponseError(Response.Status.BAD_REQUEST, "Descripcion producto no valido en linea " + i);
            }
            if (StringUtils.isBlank(linea.getPorcentajeIva())) {
                return new ResponseError(Response.Status.BAD_REQUEST, "Porcentaje de IVA no valido en linea " + i);
            }
            if (linea.getPrecioUnitario() < 0) {
                return new ResponseError(Response.Status.BAD_REQUEST, "Precio unitario no valido en linea " + i);
            }
        }
        if (factura.getTotal() < 0) {
            return new ResponseError(Response.Status.BAD_REQUEST, "Total no valido ");
        }
        return null;
    }


}

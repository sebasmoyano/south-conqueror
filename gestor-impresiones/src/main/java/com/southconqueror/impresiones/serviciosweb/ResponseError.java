package com.southconqueror.impresiones.serviciosweb;

import com.southconqueror.impresiones.logica.utiles.JsonConverter;

import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by smoyano on 02/01/17.
 */
public class ResponseError {

    private Response.Status status;
    private String message;

    public ResponseError(Response.Status status, String message) {
        this.status = status;
        this.message = message;
    }

    public Response.Status getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public String convertirAJson() {
        Map<String, Object> json = new HashMap<String, Object>();
        json.put("success", false);
        json.put("status", status.getStatusCode());
        json.put("message", message);
        return JsonConverter.objectToString(json);
    }

}

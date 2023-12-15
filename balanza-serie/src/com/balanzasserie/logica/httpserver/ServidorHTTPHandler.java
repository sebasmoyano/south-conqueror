package com.balanzasserie.logica.httpserver;

import com.balanzasserie.logica.BalanzasManager;
import com.sun.net.httpserver.Headers;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.util.Map.Entry;
import java.util.Set;

public class ServidorHTTPHandler implements HttpHandler {

    public static final String METHOD_GET = "GET";
    public static final String METHOD_POST = "POST";
    public static final int OK_RESPONSE = 200;
    public final static String PAGINA_IMPRESION = "/mediciones.do";
    public final static String PAGINA_CROSS_DOMAIN = "/crossdomain.xml";

    public void handle(HttpExchange exchange) throws IOException {
        String requestedFileName = exchange.getRequestURI().getRawPath();

        Map<String, String> parameters = getParameters(exchange);

        buildResponse(exchange, requestedFileName, parameters);
    }

    private Map<String, String> getParameters(HttpExchange exchange) {
        Map<String, String> map = new HashMap<String, String>();
        String queryParameters = exchange.getRequestURI().getQuery();
        if (queryParameters != null) {
            String[] parametersArray = queryParameters.split("&");
            for (int i = 0; i < parametersArray.length; i++) {
                String[] parameterPair = parametersArray[i].split("=");
                if (parameterPair != null && parameterPair.length == 2) {
                    map.put(parameterPair[0], parseString(parameterPair[1]));
                }
            }
        }

        return map;
    }

    private String parseString(String value) {
        String rta = value;
        if (value != null) {
            rta = rta.trim();
            // Parse %3A -> :
            rta = rta.replaceAll("%3A", ":");
        }

        return rta;
    }

    private void buildResponse(HttpExchange exchange, String fileName, Map<String, String> parameters) throws IOException {
        boolean validRequest = isValidRequest(fileName);
        byte[] result = null;

        if (validRequest) {
            if (fileName.equals(PAGINA_IMPRESION)) {
                result = BalanzasManager.getInstance().buildResponse(parameters);
            } else if (fileName.equals(PAGINA_CROSS_DOMAIN)) {
                result = ResourcesManager.getInstance().getResource(fileName);
            } else {
                result = BalanzasManager.getInstance().getErrorResponse("Servicio ajax no existente");
            }
        } else {
            result = BalanzasManager.getInstance().getErrorResponse("Servicio no existente");
        }

        DataOutputStream responseBody = new DataOutputStream(exchange.getResponseBody());

        Headers responseHeaders = exchange.getResponseHeaders();
        Set<Entry<String, String>> pairKeyValue = ResourcesManager.getInstance().getHeaders(fileName).entrySet();
        for (Entry<String, String> entry : pairKeyValue) {
            responseHeaders.set(entry.getKey(), entry.getValue());
        }

        if (result != null) {
            exchange.sendResponseHeaders(OK_RESPONSE, result.length);
        } else {
            exchange.sendResponseHeaders(OK_RESPONSE, 0);
        }

        responseBody.write(result);
        responseBody.close();
    }

    private boolean isValidRequest(String fileName) {
        boolean rta = false;
        if (fileName != null) {
            if (fileName.equals(PAGINA_IMPRESION)) {
                rta = true;
            } else if (fileName.equals(PAGINA_CROSS_DOMAIN)) {
                rta = true;
            }
        }

        return rta;
    }
}

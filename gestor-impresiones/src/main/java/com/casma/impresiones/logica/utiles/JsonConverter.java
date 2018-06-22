package com.casma.impresiones.logica.utiles;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonConverter {

    private static final Logger logger = LoggerFactory.getLogger(JsonConverter.class);

    private final static ObjectMapper MAPPER;

    static {
        MAPPER = new ObjectMapper();
    }

    public static String objectToString(Object object) {
        if (object == null) {
            return "null";
        }
        String result = null;
        try {
            result = MAPPER.writeValueAsString(object);
        } catch (Exception e) {
            logger.warn("Could not convert object to string", e);
        }
        return result;
    }


}

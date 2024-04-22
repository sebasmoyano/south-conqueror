package com.southconqueror.conector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.*;

@Service
public class MedicionesService {

    private static final Logger logger = LoggerFactory.getLogger(MedicionesService.class);

    private final JdbcTemplate jdbcTemplate;
    private final RestTemplate restTemplate;

    private final static String apiUrl = "https://canteras.slingrs.io/dev/runtime/api/data/produccion.medicion";

    private Map<String, Object> ultimasLecturasProductos = new HashMap<>();

    @Value("${app.productos}")
    private String[] productosArray;

    @Autowired
    public MedicionesService(JdbcTemplate jdbcTemplate, RestTemplate restTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.restTemplate = restTemplate;
    }

    @Scheduled(cron = "0 0 9-17 * * *") // cada 1 hora de 9 a 17
    //@Scheduled(cron = "0 * * * * *") // Ejecutar cada minuto
    public void leerYEnviarMedicion() {
        List<String> productos = Arrays.asList(productosArray.clone());
        for (String producto : productos) {
            logger.info("Extrayendo medición {}", producto);
            String ultimaMedicionProductoQuery = "SELECT AUX_IN, ACUM_ FROM " + producto + " WHERE ACUM_ != 0 ORDER BY AUX_IN DESC, ACUM_ DESC LIMIT 1";
            try {
                Map<String, Object> ultimaMedicionProducto = jdbcTemplate.queryForMap(ultimaMedicionProductoQuery);
                if (ultimasLecturasProductos.get(producto) == null || !ultimasLecturasProductos.get(producto).equals(ultimaMedicionProducto.get("AUX_IN"))) {
                    logger.info("Última medición producto: {}", ultimaMedicionProducto);
                    enviarMedicionALaAPI(producto, ultimaMedicionProducto);
                    ultimasLecturasProductos.put(producto, ultimaMedicionProducto.get("AUX_IN"));
                }
            } catch (Exception e) {
                logger.error("Error al leer y enviar la medición", e);
            }
        }
    }

    private void enviarMedicionALaAPI(String producto, Map<String, Object> ultimaMedicion) {
        Map<String, Object> medicion = new HashMap<>();
        medicion.put("fechaMedicion", new BigDecimal((Double) ultimaMedicion.get("AUX_IN")).toPlainString());
        medicion.put("codigoProducto", producto);
        medicion.put("valor", ultimaMedicion.get("ACUM_"));
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("token", "5WPGVUHe8oYmPkmmkJXeJsB4EpanLzas");
        HttpEntity<Map<String, Object>> payload = new HttpEntity<>(medicion, headers);
        try {
            logger.info("Medición enviada a la API: {}", medicion);
            ResponseEntity<String> nuevaMedicion = restTemplate.exchange(apiUrl, HttpMethod.POST, payload, String.class);
            logger.info("Medicion creada correctamente: {}", nuevaMedicion);
        } catch (Exception e) {
            logger.error("Error al enviar la medición a la API", e);
        }
    }
}
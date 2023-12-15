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
import java.util.HashMap;
import java.util.Map;

@Service
public class MedicionesService {

    private static final Logger logger = LoggerFactory.getLogger(MedicionesService.class);

    private final JdbcTemplate jdbcTemplate;
    private final RestTemplate restTemplate;

    @Value("${cintaId}")
    private String cintaId;

    @Value("${productoId}")
    private String productoId;

    private final static String apiUrl = "https://canteras.slingrs.io/dev/runtime/api/data/produccion.medicion";

    @Autowired
    public MedicionesService(JdbcTemplate jdbcTemplate, RestTemplate restTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.restTemplate = restTemplate;
    }

    //@Scheduled(cron = "0 0 9-17 * * *") // cada 1 hora de 9 a 17
    @Scheduled(cron = "0 * * * * *") // Ejecutar cada minuto
    public void leerYEnviarMedicion() {
        String sql = "SELECT * FROM mediciones ORDER BY id DESC LIMIT 1";
        try {
            Map<String, Object> resultado = jdbcTemplate.queryForMap(sql);
            logger.info("Última medición: {}", resultado);
            enviarMedicionALaAPI(resultado);
        } catch (Exception e) {
            logger.error("Error al leer y enviar la medición", e);
        }
    }

    private void enviarMedicionALaAPI(Map<String, Object> ultimaMedicion) {
        Map<String, Object> medicion = new HashMap<>();
        medicion.put("cinta", cintaId);
        medicion.put("producto", productoId);
        medicion.put("valor", 5500); // TODO: change this
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
package com.southconqueror.conector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MedicionesApplication {

    private static final Logger logger = LoggerFactory.getLogger(MedicionesApplication.class);

    public static void main(String[] args) {
        logger.info("Inicializando aplicación para cinta: {} y producto: {}", args[0], args[1]);
        SpringApplication.run(MedicionesApplication.class, args);
    }
}
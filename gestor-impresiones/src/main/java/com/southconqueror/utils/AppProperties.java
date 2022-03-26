package com.southconqueror.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class AppProperties {

    private static AppProperties instance;

    private Properties appProperties;

    private AppProperties() {
    }

    public synchronized static AppProperties getInstance() {
        if (instance == null) {
            instance = new AppProperties();
        }
        return instance;
    }

    public void initProperties() throws IOException {
        // create and load default properties
        appProperties = new Properties();
        FileInputStream in = new FileInputStream("config/configuration.properties");
        appProperties.load(in);
        in.close();
    }


    public String getApp() {
        return appProperties.getProperty("general.app");
    }

    public String getPuertoImpresora() {
        return appProperties.getProperty("impresora.puerto");
    }

    public String getModeloImpresora() {
        return appProperties.getProperty("impresora.modelo");
    }
}

package com.casma.slingr;

import com.casma.impresiones.logica.Contexto;
import com.casma.impresiones.logica.utiles.CryptoUtils;

import javax.ws.rs.client.WebTarget;

/**
 * Provides access to the services in the app runtime.
 *
 * Created by dgaviola on 18/9/15.
 */
public class SlingrManager {

    private static SlingrManager instance;

    private SlingrClient client;

    private static String SAN_FERNANDO_DOMAIN = "https://sanfernando.slingrs.io";
    private static String SAN_FERNANDO_USER = "system@sanfernando.slingrs.io";
    private static String SAN_FERNANDO_KEY = "frZbXz+WJYTil+7sNokXpVrWgqmuTC97vJ6DN5tfzipqkK0Mm0uN326qMgC5H5BhIrX7WfRNpWj3BEdIpIRLgnXL7gQwAIie3gFX4mZYHHA=";

    private static String LA_ROTONDA_DOMAIN = "https://larotonda.slingrs.io";
    private static String LA_ROTONDA_USER = "system@larotonda.slingrs.io";
    private static String LA_ROTONDA_KEY = "G4AHJRLJHbHPYtycUIPgQ5ota1H41aB5rWDMuL3MKeZ35OLVwibyh/uIeOISYTMFNw+AjtBk1wN83SS/TWy/uHXL7gQwAIie3gFX4mZYHHA=";

    private static String MICA_DOMAIN = "https://mica.slingrs.io";
    private static String MICA_USER = "system@mica.slingrs.io";
    private static String MICA_KEY = "RUm7o2PAhkf1aS56y+BB7XLp/FUlIqwVaBq/DWFs7I3vKJhAHaSy3imXryybnr7wowYGISve74VAjk6MvjrKnXXL7gQwAIie3gFX4mZYHHA=";

    private SlingrManager() {
        iniciarCliente();
    }

    public synchronized static SlingrManager getInstance() {
        if (instance == null) {
            instance = new SlingrManager();
        }
        return instance;
    }

    private void iniciarCliente() {
        String app = Contexto.getApp();
        String domain = null;
        String user = null;
        String key = null;
        switch (app) {
            case "sanfernando":
                domain = SAN_FERNANDO_DOMAIN;
                user = SAN_FERNANDO_USER;
                key = CryptoUtils.getInstance().decrypt(SAN_FERNANDO_KEY);
                break;
            case "larotonda":
                domain = LA_ROTONDA_DOMAIN;
                user = LA_ROTONDA_USER;
                key = CryptoUtils.getInstance().decrypt(LA_ROTONDA_KEY);
                break;
            case "mica":
                domain = MICA_DOMAIN;
                user = MICA_USER;
                key = CryptoUtils.getInstance().decrypt(MICA_KEY);
                break;
        }
        client = new SlingrClient(domain + "/dev/runtime/api", user, key);
    }


    public Json actualizarNumero(String path, final Json payload) {
        return client.actualizarNumero(path, payload);
    }


}

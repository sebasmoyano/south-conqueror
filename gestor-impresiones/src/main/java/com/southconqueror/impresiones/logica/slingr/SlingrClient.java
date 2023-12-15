package com.southconqueror.impresiones.logica.slingr;

import javax.ws.rs.client.WebTarget;

/**
 * Provides access to the services in the app runtime.
 * <p>
 * Created by dgaviola on 18/9/15.
 */
public class SlingrClient extends CommonSlingrClient {

    public SlingrClient(String apiUri, String email, String password) throws RestException {
        super(apiUri, email, password);
    }

    // Provision management

    public Json actualizarNumero(String path, final Json payload) {
        final WebTarget target = getApiTarget()
                .path(path);
        Json res = call(new Idea2CallTemplate() {
            @Override
            public Json exec() {
                return put(target, payload);
            }
        });
        return res;
    }


}

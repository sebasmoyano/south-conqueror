package com.southconqueror.impresiones.logica.slingr;

import javax.ws.rs.client.WebTarget;
import java.io.InputStream;

/**
 * Provides common services for all Idea2 clients like login and reconnection when token expires.
 * <p>
 * Created by dgaviola on 22/9/15.
 */
public class CommonSlingrClient extends RestClient {
    private String email;
    private String password;
    private String token = null;
    private boolean requiresAuth = true;

    protected CommonSlingrClient(String apiUri, String email, String password) throws RestException {
        super(apiUri);
        this.email = email;
        this.password = password;
        this.token = null;
    }

    protected CommonSlingrClient(String apiUri, String token) throws RestException {
        super(apiUri);
        this.email = null;
        this.password = null;
        this.token = token;
        setupDefaultHeader("token", token);
    }

    protected CommonSlingrClient(String apiUri) throws RestException {
        super(apiUri);
        this.email = null;
        this.password = null;
        this.requiresAuth = false;
    }

    public Json login() {
        WebTarget target = getApiTarget().path("/auth/login");
        Json result = post(target, Json.map().set("email", email).set("password", password));
        this.token = result.string("token");
        setupDefaultHeader("token", token);
        return result;
    }

    public void logout() {
        if (token != null) {
            WebTarget target = getApiTarget().path("/auth/logout");
            try {
                post(target);
            } catch (Exception e) {
                // we do the best to try to logout, but if we can't no worries, token will expire
            }
        }
    }

    protected Json call(Idea2CallTemplate template) {
        return template.wrapExec();
    }

    protected InputStream call(Idea2BinaryCallTemplate template) {
        return template.wrapExec();
    }

    protected abstract class Idea2CallTemplate {
        public abstract Json exec();

        public Json wrapExec() {
            try {
                // if token is empty we try to login first
                if (token == null && requiresAuth) {
                    login();
                }
                return exec();
            } catch (RestException re) {
                // if this is an authentication error we need to retry in case token is expired
                if (re.getStatusCode() == 401) {
                    login();
                    return exec();
                } else {
                    throw re;
                }
            }
        }
    }

    protected abstract class Idea2BinaryCallTemplate {
        public abstract InputStream exec();

        public InputStream wrapExec() {
            try {
                // if token is empty we try to login first
                if (token == null) {
                    login();
                }
                return exec();
            } catch (RestException re) {
                // if this is an authentication error we need to retry in case token is expired
                if (re.getStatusCode() == 401) {
                    login();
                    return exec();
                } else {
                    throw re;
                }
            }
        }
    }
}

package com.southconqueror.impresiones.logica.slingr;

import org.apache.log4j.Logger;

/**
 * Simple Rest client over a unique URI
 * <p>
 * Created by lefunes on 21/03/16.
 */
public class SimpleRestClient extends RestClient {
    private static final Logger logger = Logger.getLogger(SimpleRestClient.class);

    public SimpleRestClient(String apiUri) throws RestException {
        super(apiUri);
    }

    public SimpleRestClient setHeader(String name, Object value) {
        setupDefaultHeader(name, value);
        return this;
    }

    public SimpleRestClient setParam(String name, String value) {
        setupDefaultParam(name, value);
        return this;
    }

    public SimpleRestClient path(String path) {
        super.setPath(path);
        return this;
    }

    public SimpleRestClient retries(Integer retries) {
        this.setRetries(retries);
        return this;
    }

    public SimpleRestClient connectionTimeout(Integer timeout) {
        this.setConnectionTimeout(timeout);
        return this;
    }

    public SimpleRestClient readTimeout(Integer timeout) {
        this.setReadTimeout(timeout);
        return this;
    }

    public SimpleRestClient silenceLogger() {
        this.setSilenceLogger(true);
        return this;
    }

    public SimpleRestClient disableConvertContentToString() {
        this.setConvertContentToString(false);
        return this;
    }

    public static SimpleRestClient uri(String apiUri) {
        return new SimpleRestClient(apiUri);
    }

    public Json get() throws RestException {
        return super.get(null);
    }

    public Json post() throws RestException {
        return super.post(null);
    }

    public Json post(Json content) throws RestException {
        return super.post(null, content);
    }


    public Json put() throws RestException {
        return super.put(null);
    }

    public Json put(Json content) throws RestException {
        return super.put(null, content);
    }

    public Json patch() throws RestException {
        return super.patch(null);
    }

    public Json patch(Json content) throws RestException {
        return super.patch(null, content);
    }

    public Json head() throws RestException {
        return super.head(null);
    }

    public Json delete() throws RestException {
        return super.delete(null);
    }

    public Json options() throws RestException {
        return super.options(null);
    }

    public Json execute(RestMethod method, Json jsonRequest) {
        switch (method) {
            case POST:
                return post(jsonRequest);
            case PUT:
                return put(jsonRequest);
            case PATCH:
                return patch(jsonRequest);
            case DELETE:
                return delete();
            case HEAD:
                return head();
            case OPTIONS:
                return options();
        }
        return get();
    }

}

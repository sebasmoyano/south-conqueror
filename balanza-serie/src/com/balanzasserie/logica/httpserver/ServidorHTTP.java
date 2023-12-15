package com.balanzasserie.logica.httpserver;

import com.sun.net.httpserver.HttpServer;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class ServidorHTTP {

    private static ServidorHTTP instance;

    public static final int SERVER_PORT = 9998;
    public static final String SERVER_CONTEXT = "/";
    public static final String SERVER_DEFAULT_URL = "http://localhost:" + SERVER_PORT;
    private HttpServer server;

    private ServidorHTTP() {
    }

    public static ServidorHTTP getInstance() {
        if (instance == null) {
            instance = new ServidorHTTP();
        }

        return instance;
    }

    public void iniciar() throws Exception {
        InetSocketAddress addr = new InetSocketAddress(SERVER_PORT);

        server = HttpServer.create(addr, 0);
        server.createContext(SERVER_CONTEXT, new ServidorHTTPHandler());
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();

    }

    public void parar() {
        int delaySeconds = 0;
        server.stop(delaySeconds);
    }
}

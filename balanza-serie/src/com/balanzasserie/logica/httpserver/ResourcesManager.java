package com.balanzasserie.logica.httpserver;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class ResourcesManager {

    private static ResourcesManager instance;
    private final static String RESOURCES_FOLDER = "resources";
    public static final String CONTENT_TYPE_HTML = "text/html; charset=utf-8";

    private ResourcesManager() {
    }

    public static ResourcesManager getInstance() {
        if (instance == null) {
            instance = new ResourcesManager();
        }

        return instance;
    }

    public static byte[] getBytesFromInputStream(InputStream is) {
        // Get the size of the file
        try {
            // Create the byte array to hold the data
            byte[] tempBuffer = new byte[2048];

            // Read in the bytes
            int offset = 0;
            int numRead = 0;
            while (offset < tempBuffer.length && (numRead = is.read(tempBuffer, offset, tempBuffer.length - offset)) >= 0) {
                offset += numRead;
            }
            System.out.println("offset " + offset);
            System.out.println("Size " + tempBuffer.length);

            // Close the input stream and return bytes
            is.close();

            byte[] rta = new byte[offset];
            for (int i = 0; i < offset; i++) {
                rta[i] = tempBuffer[i];
            }

            return rta;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public byte[] getResource(String fileName) {
        byte[] bytes = null;
        InputStream fileStream = null;

        fileStream = ResourcesManager.class.getResourceAsStream(RESOURCES_FOLDER + fileName);
        if (fileStream != null) {
            bytes = getBytesFromInputStream(fileStream);
        }

        return bytes;
    }

    private String getContentType(String fileName) {
        String contentType = CONTENT_TYPE_HTML;

        if (fileName != null) {
            if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
                contentType = "image/jpeg";
            } else if (fileName.endsWith(".gif")) {
                contentType = "image/gif";
            } else if (fileName.endsWith(".png")) {
                contentType = "image/png";
            } else if (fileName.endsWith(".css")) {
                contentType = "text/css";
            } else if (fileName.endsWith(".js")) {
                contentType = "text/javascript";
            } else if (fileName.endsWith(".csv")) {
                contentType = "text/csv";
            } else if (fileName.endsWith(".xml")) {
                contentType = "text/xml";
            }
        }

        return contentType;
    }

    public Map<String, String> getHeaders(String fileName) {
        Map<String, String> headers = new HashMap<String, String>();

        String contentType = getContentType(fileName);

        headers.put("Content-Type", contentType);


        return headers;
    }
}

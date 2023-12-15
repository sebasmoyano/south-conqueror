package com.balanzasserie.logica.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PropertiesUtil {

    final static PropertiesUtil me;
    HashMap<String, Properties> lstProperties = new HashMap<String, Properties>();

    static {
        me = new PropertiesUtil();
    }

    private PropertiesUtil() {
        // default constructor
    }

    public static PropertiesUtil getInstance() {
        return me;
    }

    /**
     * Returns a property from the given fileName; Benefits of this method that
     * it caches the properties for each file and don't access the file system
     * each time; the file name can be as 'SomeConfig.properties' or
     * 'SomeConfig';
     *
     * @param fileName
     * @param propName
     * @return
     */
    public String getProperty(String fileName, String propName) {
        Properties prop = (Properties) lstProperties.get(fileName);
        if (prop == null) {
            synchronized (me) {
                prop = lstProperties.get(fileName);

                if (prop == null) {
                    prop = new Properties();
                    try {
                        if (!isPropertiesFile(fileName)) {
                            fileName += ".properties";
                        }

                        if (this.getClass().getClassLoader().getResourceAsStream(fileName) != null) {
                            prop.load(this.getClass().getClassLoader().getResourceAsStream(fileName));
                        } else {
                            prop.load(new FileInputStream(fileName));
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    lstProperties.put(fileName, prop);
                }
            }
        }

        return prop.getProperty(propName);
    }


    public boolean isPropertiesFile(String fileName) {
        if (fileName.indexOf(".properties") == -1) {
            return false;
        } else {
            return true;
        }

    }

    public String[][] getProperties(String fileName) throws IOException {

        String[][] data = null;
        if (isPropertiesFile(fileName)) {

            synchronized (me) {

                Properties prop = new Properties();
                FileInputStream in = new FileInputStream(fileName);
                prop.load(in);
                in.close();
                Enumeration e = prop.keys();
                data = new String[prop.size()][];
                int i = 0;
                String key;
                while (e.hasMoreElements()) {

                    key = (String) e.nextElement();

                    data[i] = new String[]{key, prop.getProperty(key)};
                    i++;

                }

            }

        }

        return data;
    }

    public void setProperty(String fileName, String key, String value) {

        if (isPropertiesFile(fileName)) {

            synchronized (me) {
                FileWriter out2 = null;
                try {
                    Properties prop = (Properties) lstProperties.get(fileName);
                    if (prop != null) {
                        out2 = new FileWriter(new File(fileName), false);
                        prop.put(key, value);
                        prop.store(out2, "Properties");
                    }
                } catch (IOException ex) {
                    Logger.getLogger(PropertiesUtil.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    try {
                        out2.close();
                    } catch (IOException ex) {
                        Logger.getLogger(PropertiesUtil.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

            }
        }
    }

    public void setProperty(String key, String value, String fileName, OutputStream stream) {

        synchronized (me) {

            Properties prop = (Properties) lstProperties.get(fileName);
            if (prop != null) {
                prop.put(key, value);

                try {
                    prop.store(stream, "Properties");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    public void removeProperty(String key, String value, String fileName) throws IOException {

        if (isPropertiesFile(fileName)) {

            synchronized (me) {

                Properties prop = new Properties();

                FileInputStream in = new FileInputStream(fileName);

                prop.load(in);

                in.close();

                FileOutputStream out = new FileOutputStream(fileName);

                prop.remove(key);

                prop.store(out, "Properties");

            }

        }

    }

}

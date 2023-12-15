/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.balanzasserie.logica;

import com.balanzasserie.logica.httpserver.responses.ErrorJSON;
import com.balanzasserie.logica.httpserver.responses.MedicionJSON;
import com.balanzasserie.logica.httpserver.responses.MedicionesJSON;
import com.balanzasserie.logica.httpserver.responses.MensajeJSON;
import com.balanzasserie.logica.puertoserie.SerialManager;
import com.balanzasserie.logica.utils.FechaUtil;
import com.balanzasserie.vista.IVistaListener;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedList;

import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author root
 */
public class BalanzasManager {

    private static BalanzasManager instance;
    private List<IVistaListener> vistaListeners = new ArrayList<IVistaListener>();
    private final static String APP_HOME = System.getProperty("user.home") + File.separator;
    private final static String APP_FOLDER = ".balanzas" + File.separator;
    private final static String BALANZA_FILE = "mediciones_balanzas.txt";
    private final static int KGS_TOLERANCIA_ERROR_MEDICION = 90;
    private final static int CONTADOR_MEDICIONES = 5;
    private final static int MAX_CONTADOR_MEDICIONES = 100;
    private final static int MAX_MIUTOS_PROXIMA_MEDICION = 4;
    private LinkedList<Medicion> ultimasMedicionesBalanza1 = new LinkedList<Medicion>();
    private LinkedList<Medicion> ultimasMedicionesBalanza2 = new LinkedList<Medicion>();
    private LinkedList<Medicion> medicionesBalanza1Checker = new LinkedList<Medicion>();
    private LinkedList<Medicion> medicionesBalanza2Checker = new LinkedList<Medicion>();
    private static Gson JSON_MANAGER = new Gson();
    private static String SERVICIO_OBTENER_MEDICIONES = "obtener_mediciones";
    private static String SERVICIO_BORRAR_MEDICIONES = "borrar_mediciones";
    private static String PARAMETRO_SERVICIO = "servicio";
    private static String PARAMETRO_BALANZA = "balanza";

    private BalanzasManager() {
        iniciarArchivosMediciones();
    }

    public static BalanzasManager getInstance() {
        if (instance == null) {
            instance = new BalanzasManager();
        }

        return instance;
    }

    public List<Medicion> getMedicionesBalanza1() {
        return new ArrayList<Medicion>(ultimasMedicionesBalanza1);
    }

    public List<Medicion> getMedicionesBalanza2() {
        return new ArrayList<Medicion>(ultimasMedicionesBalanza2);
    }

    public void addVistaListener(IVistaListener vista) {
        if (!vistaListeners.contains(vista)) {
            vistaListeners.add(vista);
        }
    }

    public void inicializarBalanza1() throws Exception {
        SerialManager.getInstance(PropertiesManager.getBalanza1SerialParameters());
    }

    public void inicializarBalanza2() throws Exception {
        SerialManager.getInstance(PropertiesManager.getBalanza2SerialParameters());
    }

    public static List<String> listarPuertosDisponibles() {
        return SerialManager.listarPuertosDisponibles();
    }

    public void nuevaMedicionRecibida(Medicion medicion) {
        Medicion medicionValida = gestionarMedicion(medicion);
        if (medicionValida != null) {
            System.out.println("Medicion Recibida CORRECTA..." + medicionValida.toString());

            // escribir en archivo
            escribirArchivo(medicion);

            // Notificar la UI
            for (IVistaListener v : vistaListeners) {
                v.nuevaMedicionRecibida(medicion);
            }

        } else {
            System.out.println("Medicion Recibida no valida..." + medicion.toString());
        }
    }

    private void iniciarArchivosMediciones() {
        File dir = new File(APP_HOME + APP_FOLDER);
        if (!dir.exists()) {
            dir.mkdir();
        }

        File b1 = new File(APP_HOME + APP_FOLDER + BALANZA_FILE);
        if (!b1.exists()) {
            try {
                b1.createNewFile();
            } catch (IOException ex) {
                Logger.getLogger(BalanzasManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void escribirArchivo(Medicion medicion) {
        try {
            FileWriter f = new FileWriter(new File(APP_HOME + APP_FOLDER + BALANZA_FILE), true);
            PrintWriter pw = new PrintWriter(f);
            pw.println(medicion.toString());

            f.close();
            pw.close();
        } catch (IOException ex) {
            Logger.getLogger(BalanzasManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private Medicion gestionarMedicion(Medicion medicion) {
        Medicion rta = null;
        if (medicion.isValid()) {
            if (medicion.getPeso() != 0) {
                if (medicion.getBalanza().equals(Balanza.BALANZA1)) {
                    // si las ultimas mediciones fueros iguales o muy cercanas
                    Medicion ultimaMedicionBalanza1 = null;
                    if (medicionesBalanza1Checker.size() > 0) {
                        ultimaMedicionBalanza1 = medicionesBalanza1Checker.getFirst();
                    }

                    if (ultimaMedicionBalanza1 == null || (sonMedicionesIguales(ultimaMedicionBalanza1, medicion) && (FechaUtil.diferenciaEnMinutos(ultimaMedicionBalanza1.getFecha(), medicion.getFecha()) < MAX_MIUTOS_PROXIMA_MEDICION))) {
                        medicionesBalanza1Checker.addFirst(medicion);
                        if (medicionesBalanza1Checker.size() == CONTADOR_MEDICIONES) {
                            rta = medicion;
                            medicionesBalanza1Checker.clear();
                            agregarNuevaMedicionBalanza1(medicion);
                        }
                    } else {
                        // reinicio el contador
                        medicionesBalanza1Checker.clear();
                        medicionesBalanza1Checker.addFirst(medicion);
                    }
                } else if (medicion.getBalanza().equals(Balanza.BALANZA2)) {
                    // si las ultimas mediciones fueros iguales o muy cercanas
                    Medicion ultimaMedicionBalanza2 = null;
                    if (medicionesBalanza2Checker.size() > 0) {
                        ultimaMedicionBalanza2 = medicionesBalanza2Checker.getFirst();
                    }

                    if (ultimaMedicionBalanza2 == null || (sonMedicionesIguales(ultimaMedicionBalanza2, medicion) && (FechaUtil.diferenciaEnMinutos(ultimaMedicionBalanza2.getFecha(), medicion.getFecha()) < MAX_MIUTOS_PROXIMA_MEDICION))) {
                        medicionesBalanza2Checker.addFirst(medicion);
                        if (medicionesBalanza2Checker.size() == CONTADOR_MEDICIONES) {
                            rta = medicion;
                            medicionesBalanza2Checker.clear();
                            agregarNuevaMedicionBalanza2(medicion);
                        }
                    } else {
                        // reinicio el contador
                        medicionesBalanza2Checker.clear();
                        medicionesBalanza2Checker.addFirst(medicion);
                    }
                }
            }
        }

        return rta;
    }

    private boolean sonMedicionesIguales(Medicion medicion1, Medicion medicion2) {
        // diferencia de 90kg y tiempo no mayor a 4 minutos
        return ((medicion1.getPeso() == medicion2.getPeso())
                || (Math.abs(medicion1.getPeso() - medicion2.getPeso()) < KGS_TOLERANCIA_ERROR_MEDICION));
    }

    private void agregarNuevaMedicionBalanza1(Medicion medicion) {
        Medicion ultimaMedicionGeneralBalanza1 = null;
        if (ultimasMedicionesBalanza1.size() > 0) {
            ultimaMedicionGeneralBalanza1 = ultimasMedicionesBalanza1.getFirst();
        }
        // no agregar mediciones repetidas
        if (ultimaMedicionGeneralBalanza1 == null || !sonMedicionesIguales(ultimaMedicionGeneralBalanza1, medicion)) {
            // que no se superen las x mediciones en memoria
            if (ultimasMedicionesBalanza1.size() > MAX_CONTADOR_MEDICIONES) {
                ultimasMedicionesBalanza1.removeLast();
            }
            ultimasMedicionesBalanza1.addFirst(medicion);
        }
    }

    private void agregarNuevaMedicionBalanza2(Medicion medicion) {
        Medicion ultimaMedicionGeneralBalanza2 = null;
        if (ultimasMedicionesBalanza2.size() > 0) {
            ultimaMedicionGeneralBalanza2 = ultimasMedicionesBalanza2.getFirst();
        }
        // no agregar mediciones repetidas
        if (ultimaMedicionGeneralBalanza2 == null || !sonMedicionesIguales(ultimaMedicionGeneralBalanza2, medicion)) {
            // que no se superen las x mediciones en memoria
            if (ultimasMedicionesBalanza2.size() > MAX_CONTADOR_MEDICIONES) {
                ultimasMedicionesBalanza2.removeLast();
            }

            ultimasMedicionesBalanza2.addFirst(medicion);
        }
    }

    public void borrarMedicionesBalanza1() {
        ultimasMedicionesBalanza1.clear();
    }

    public void borrarMedicionesBalanza2() {
        ultimasMedicionesBalanza2.clear();
    }


    public byte[] buildResponse(Map<String, String> parameters) {
        byte[] response = null;
        if (parameters.get(PARAMETRO_SERVICIO) != null) {
            if (parameters.get(PARAMETRO_BALANZA) != null) {
                if (parameters.get(PARAMETRO_SERVICIO).equals(SERVICIO_OBTENER_MEDICIONES)) {
                    response = getMedicionesResponse(parameters.get(PARAMETRO_BALANZA));
                } else if (parameters.get(PARAMETRO_SERVICIO).equals(SERVICIO_BORRAR_MEDICIONES)) {
                    response = getBorrarMedicionesResponse(parameters.get(PARAMETRO_BALANZA));
                } else {
                    response = getErrorResponse("Servicio solicitado no existente");
                }
            } else {
                response = getErrorResponse("Balanza no especificada");
            }
        } else {
            response = getErrorResponse("Servicio solicitado no existente");
        }

        return response;
    }

    public byte[] getErrorResponse(String mensaje) {
        ErrorJSON error = new ErrorJSON();
        error.setError(mensaje);

        return JSON_MANAGER.toJson(error).getBytes();
    }

    private byte[] getMedicionesResponse(String balanza) {
        MedicionesJSON medicionesJSON = new MedicionesJSON();
        List<Medicion> medicionesLocales = new ArrayList<Medicion>();
        if (balanza.equals("1")) {
            medicionesLocales = getMedicionesBalanza1();
        } else if (balanza.equals("2")) {
            medicionesLocales = getMedicionesBalanza2();
        }

        for (Medicion medicion : medicionesLocales) {
            MedicionJSON medicionJSON = new MedicionJSON();
            medicionJSON.setBalanza(medicion.getBalanza().getLabel());
            medicionJSON.setPeso(medicion.getPeso());
            medicionJSON.setFecha(medicion.getFechaStr());

            medicionesJSON.getUltimasMediciones().add(medicionJSON);
        }

        return JSON_MANAGER.toJson(medicionesJSON).getBytes();
    }

    private byte[] getBorrarMedicionesResponse(String balanza) {
        if (balanza.equals("1")) {
            borrarMedicionesBalanza1();
        } else if (balanza.equals("2")) {
            borrarMedicionesBalanza2();
        }

        MensajeJSON mensaje = new MensajeJSON();
        mensaje.setError("Mediciones borradas");

        return JSON_MANAGER.toJson(mensaje).getBytes();
    }
}

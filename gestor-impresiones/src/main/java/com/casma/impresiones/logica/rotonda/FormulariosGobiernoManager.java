/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.casma.impresiones.logica.rotonda;

import com.casma.impresiones.entidades.ComprobanteGenerico;
import com.casma.impresiones.entidades.ComprobanteLinea;
import com.casma.impresiones.entidades.FormularioGobierno;
import com.casma.impresiones.logica.utiles.PdfFooter;
import com.itextpdf.text.*;
import com.itextpdf.text.Font;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.draw.LineSeparator;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.eclipse.jetty.util.StringUtil;

import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

/**
 * @author sebastián
 */
public class FormulariosGobiernoManager {

    private static Logger logger = LogManager.getLogger(FormulariosGobiernoManager.class);

    private static FormulariosGobiernoManager instance;

    private FormulariosGobiernoManager() {
    }

    public synchronized static FormulariosGobiernoManager getInstance() {
        if (instance == null) {
            instance = new FormulariosGobiernoManager();
        }
        return instance;
    }

    public void crearComprobante(FormularioGobierno formularioGobierno) throws Exception {
        Document document = new Document(new Rectangle(0, 0,567, 1559));
        String workDirectory = System.getProperty("user.dir");
        File comprobantesHome = new File(workDirectory + "/rotonda");
        if (!comprobantesHome.exists()) {
            comprobantesHome.mkdir();
        }
        String comprobanteId = UUID.randomUUID().toString();
        String fileName = workDirectory + "/rotonda/formularioGobierno_" +  comprobanteId + ".pdf";
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(fileName));

        document.open();

        float x = 0;
        float y = 0;
        for (int i = 0; i < 5; i++) {
            if (i == 0) {
                y += 59.527584;
            }
            if (i == 1) {
                y += 65.196864;
            }
            if (i == 2) {
                y += 65.196864;
            }
            if (i == 3) {
                y += 70.866144;
            }
            if (i == 4) {
                y += 65.196864;
            }

            // primera fila (cantera)
            x = 170.07f; // 6cm
            placeChunk(writer, formularioGobierno.getCantera(), x, y);
            y += 14.1732; // 0.5cm

            // segunda fila (localidad, mineral, peso)
            x = 85.0392f; // 3cm
            placeChunk(writer, formularioGobierno.getLocalidad(), x, y);
            x = 283.46472f; // 10cm
            placeChunk(writer, formularioGobierno.getTipoMaterial(), x, y);
            x = 453.54312f; // 16cm
            placeChunk(writer, formularioGobierno.getPeso(), x, y);
            y += 14.1732; // 0.5cm

            // tercera fila (destinatario)
            x = 170.07f; // 6cm
            placeChunk(writer, formularioGobierno.getDestino(), x, y);
            y += 14.1732; // 0.5cm

            // cuarta fila (domicilio destinatario)
            x = 141.732f; // 5cm
            placeChunk(writer, formularioGobierno.getDomicilioDestinatario(), x, y);
            y += 14.1732; // 0.5cm

            // quinta fila (tipo camion, patente, acoplado)
            x = 99.2124f; // 3.5cm
            placeChunk(writer, formularioGobierno.getMarcaCamion(), x, y);
            x = 212.59872f;
            placeChunk(writer, formularioGobierno.getPatenteCamion(), x, y);
            x = 325.984248f; // 11.5 cm
            placeChunk(writer, formularioGobierno.getPatenteAcoplado() != null ? formularioGobierno.getPatenteAcoplado() : "" , x, y);
            y += 14.1732; // 0.5cm

            // sexta fila (chofer)
            x = 170.07f; // 6cm
            placeChunk(writer, formularioGobierno.getNombreChofer(), x, y);
            y += 14.1732; // 0.5cm

            // septima fila (dni/cuit transportista)
            x = 85.0392f; // 3cm
            placeChunk(writer, formularioGobierno.getDniChofer() != null ? formularioGobierno.getDniChofer() : "", x, y);
            y += 14.1732; // 0.5cm

            //octava fila (fecha y hora salida)
            x = 113.3856f; // 4cm
            placeChunk(writer, formularioGobierno.getFechaSalida(), x, y);
            x = 240.94512f;
            placeChunk(writer, formularioGobierno.getHoraSalida() != null ? formularioGobierno.getHoraSalida() : "", x, y);

            y += 141.732f; // 5cm
        }

        // líneas de control de copia del formulario
        placeChunk(writer, "-", 0, 311.81112f);
        placeChunk(writer, "-", 0, 621.96864f);
        placeChunk(writer, "-", 0, 967.95296f);
        placeChunk(writer, "-", 0, 1253.9344f);

        document.close();
        openFile(fileName);
    }

    private static void openFile(String path) throws IOException {
        Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
        if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
            desktop.open(new File(path));
        }
    }

    private void placeChunk(PdfWriter writer, String text, float x, float y) {
        try {
            BaseFont bf = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
            PdfContentByte cb = writer.getDirectContent();
            cb.setTextMatrix(x, y);
            cb.saveState();
            cb.beginText();
            cb.moveText(x, 1559 - y);
            cb.setFontAndSize(bf, 8);
            cb.showText(text);
            cb.endText();
            cb.restoreState();
        } catch (DocumentException e) {
            logger.error("Document error", e);
        } catch (IOException e) {
            logger.error("IOException", e);
        }

    }


}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.casma.impresiones.logica.estacionamiento;

import com.casma.impresiones.entidades.Ticket;
import com.itextpdf.text.*;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.Barcode128;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.LineSeparator;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @author sebastián
 */
public class EstacionamientoTicketsManager {

    private static Logger logger = LogManager.getLogger(EstacionamientoTicketsManager.class);

    private final static Font FONT_PROPERTY = new Font(com.itextpdf.text.Font.FontFamily.TIMES_ROMAN, 18, Font.NORMAL);

    private static EstacionamientoTicketsManager instance;

    private EstacionamientoTicketsManager() {
    }

    public synchronized static EstacionamientoTicketsManager getInstance() {
        if (instance == null) {
            instance = new EstacionamientoTicketsManager();
        }
        return instance;
    }

    public void crearTicket(Ticket ticket) throws Exception {
        Document document = new Document();
        String workDirectory = System.getProperty("user.dir");
        File ticketsFolder = new File(workDirectory + "/tickets");
        if (!ticketsFolder.exists()) {
            ticketsFolder.mkdir();
        }
        String fileName = workDirectory + "/tickets/ticket_" + ticket.getId() + ".pdf";
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(fileName));
        document.open();

        LineSeparator separador = new LineSeparator();
        separador.setOffset(-2);
        document.add(separador);

        // titulo
        Font boldFont = new Font(com.itextpdf.text.Font.FontFamily.TIMES_ROMAN, 24, Font.BOLD);
        Paragraph estacionamientoTag = new Paragraph("MICA SA", boldFont);
        estacionamientoTag.setAlignment(Element.ALIGN_CENTER);
        estacionamientoTag.setSpacingAfter(20);
        document.add(estacionamientoTag);

        // dominio vehiculo
        Paragraph dominioTag = new Paragraph("Dominio: " + ticket.getDominio(), FONT_PROPERTY);
        dominioTag.setAlignment(Element.ALIGN_CENTER);
        document.add(dominioTag);

        // fecha-hora ingreso
        Paragraph fechaHoraTag = new Paragraph("Fecha/Hora ingreso: " + ticket.getFechaHora(), FONT_PROPERTY);
        fechaHoraTag.setAlignment(Element.ALIGN_CENTER);
        document.add(fechaHoraTag);

        // mensaje agradecimiento
        Font boldFontChico = new Font(com.itextpdf.text.Font.FontFamily.TIMES_ROMAN, 18, Font.BOLD);
        Paragraph graciasTag = new Paragraph("Gracias por su visita", boldFontChico);
        graciasTag.setAlignment(Element.ALIGN_CENTER);
        graciasTag.setSpacingBefore(20);
        graciasTag.setSpacingAfter(20);
        document.add(graciasTag);

        // codigo barras
        Barcode128 code128 = new Barcode128();
        code128.setBaseline(-1);
        code128.setSize(22);
        code128.setCode(ticket.getCode());
        code128.setCodeType(Barcode128.CODE128);
        code128.setBarHeight(80f); // great! but what about width???
        code128.setX(2f);
        Image code128Image = code128.createImageWithBarcode(writer.getDirectContent(), null, null);
        code128Image.setAlignment(Element.ALIGN_CENTER);
        document.add(code128Image);

        document.close();
        openFile(fileName);
    }

    private static void openFile(String path) throws IOException {
        Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
        if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
            desktop.open(new File(path));
        }
    }


}

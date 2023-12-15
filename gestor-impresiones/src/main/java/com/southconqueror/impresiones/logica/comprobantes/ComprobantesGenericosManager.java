/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.southconqueror.impresiones.logica.comprobantes;

import com.southconqueror.impresiones.entidades.ComprobanteGenerico;
import com.southconqueror.impresiones.entidades.ComprobanteLinea;
import com.southconqueror.impresiones.logica.utiles.PdfFooter;
import com.itextpdf.text.*;
import com.itextpdf.text.Font;
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
public class ComprobantesGenericosManager {

    private static Logger logger = LogManager.getLogger(ComprobantesGenericosManager.class);

    private final static Font TITULO_FONT = new Font(Font.FontFamily.HELVETICA, 24, Font.BOLD);
    private final static Font CELL_PROPIEDAD_FONT = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
    private final static Font CELL_VALOR_FONT = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL);

    private static ComprobantesGenericosManager instance;

    private ComprobantesGenericosManager() {
    }

    public synchronized static ComprobantesGenericosManager getInstance() {
        if (instance == null) {
            instance = new ComprobantesGenericosManager();
        }
        return instance;
    }

    public void crearComprobante(ComprobanteGenerico comprobanteGenerico) throws Exception {
        Document document = new Document();
        String workDirectory = System.getProperty("user.dir");
        File comprobantesHome = new File(workDirectory + "/comprobantesGenericos");
        if (!comprobantesHome.exists()) {
            comprobantesHome.mkdir();
        }
        String comprobanteId = UUID.randomUUID().toString();
        String fileName = workDirectory + "/comprobantesGenericos/comprobante_" + comprobanteId + ".pdf";
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(fileName));

        // add footer
        PdfFooter event = new PdfFooter();
        writer.setPageEvent(event);

        document.open();

        LineSeparator separador = new LineSeparator();
        separador.setOffset(-2);
        document.add(separador);

        // titulo

        Paragraph titulo = new Paragraph(comprobanteGenerico.getTitulo(), TITULO_FONT);
        titulo.setAlignment(Element.ALIGN_CENTER);
        titulo.setSpacingAfter(50);
        document.add(titulo);

        PdfPTable table = new PdfPTable(2);
        table.setWidths(new int[]{1, 2});

        for (ComprobanteLinea propiedadValor : comprobanteGenerico.getFilas()) {
            if (!StringUtil.isBlank(propiedadValor.getPropiedad())) {
                PdfPCell propiedadCell = new PdfPCell(new Phrase(propiedadValor.getPropiedad(), CELL_PROPIEDAD_FONT));
                propiedadCell.setPadding(8);
                propiedadCell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
                table.addCell(propiedadCell);
                String cellValue = propiedadValor.getValor() != null ? propiedadValor.getValor() : "-";
                PdfPCell valorCell = new PdfPCell(new Phrase(cellValue, CELL_VALOR_FONT));
                valorCell.setPadding(8);
                valorCell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
                table.addCell(valorCell);
            }
        }

        document.add(table);

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

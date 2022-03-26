/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.southconqueror.impresiones.logica.comprobantes;

import com.southconqueror.impresiones.entidades.*;
import com.itextpdf.text.*;
import com.itextpdf.text.Font;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.*;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.eclipse.jetty.util.StringUtil;

import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.UUID;

/**
 * @author sebastián
 */
public class NotasManager {

    private static Logger logger = LogManager.getLogger(NotasManager.class);

    private final static Font CELL_TITULO_EMPRESA = new Font(Font.FontFamily.HELVETICA, 22, Font.BOLD);
    private final static Font CELL_TIPO_FACTURA = new Font(Font.FontFamily.HELVETICA, 26, Font.BOLD);
    private final static Font CELL_TITULO_FACTURA = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
    private final static Font CELL_LETRA_POR_DEFECTO = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL);
    private final static Font CELL_LETRA_POR_DEFECTO_NEGRITA = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD);
    private final static Font CELL_LETRA_CHICA = new Font(Font.FontFamily.HELVETICA, 8, Font.NORMAL);

    private static NotasManager instance;

    private NotasManager() {
    }

    public synchronized static NotasManager getInstance() {
        if (instance == null) {
            instance = new NotasManager();
        }
        return instance;
    }

    public void exportarAPdf(NotaFiscal nota) throws Exception {
        Document document = new Document();
        String workDirectory = System.getProperty("user.dir");
        File notasHome = new File(workDirectory + "/notas");
        if (!notasHome.exists()) {
            notasHome.mkdir();
        }
        String comprobanteId = UUID.randomUUID().toString();
        String fileName = workDirectory + "/notas/nota_" + comprobanteId + ".pdf";
        PdfWriter.getInstance(document, new FileOutputStream(fileName));

        document.open();

        // Tabla cabecera
        PdfPTable tablaCabecera = new PdfPTable(3);
        tablaCabecera.setWidths(new float[]{40, 20, 40});
        tablaCabecera.setTableEvent(new BorderEvent());
        tablaCabecera.getDefaultCell().setBorder(Rectangle.NO_BORDER);
        tablaCabecera.setWidthPercentage(100);

        // izquierda
        PdfPCell empresaCell = new PdfPCell(new Paragraph("MICA S.A.", CELL_TITULO_EMPRESA));
        empresaCell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
        empresaCell.setBorder(Rectangle.NO_BORDER);
        tablaCabecera.addCell(empresaCell);

        // centro
        PdfPCell tipoFacturaCell = new PdfPCell(new Phrase(nota.getFactura().getTipoFactura(), CELL_TIPO_FACTURA));
        tipoFacturaCell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
        tipoFacturaCell.setBorder(Rectangle.NO_BORDER);
        tablaCabecera.addCell(tipoFacturaCell);

        // derecha
        String tipoNota = nota.getTipo() == TipoNota.CREDITO ? "NOTA DE CRÉDITO" : "NOTA DE DÉBITO";
        PdfPCell datosFacturaCell = new PdfPCell(new Phrase(tipoNota, CELL_TITULO_FACTURA));
        datosFacturaCell.setBorder(Rectangle.NO_BORDER);
        datosFacturaCell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
        datosFacturaCell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
        tablaCabecera.addCell(datosFacturaCell);

        // izquierda datos empresa
        PdfPCell datosEmpresaCell = new PdfPCell();
        datosEmpresaCell.setBorder(Rectangle.NO_BORDER);
        datosEmpresaCell.setColspan(2);
        datosEmpresaCell.setPaddingTop(10);
        datosEmpresaCell.setPaddingBottom(20);
        Paragraph direccionLinea = new Paragraph("Domicilio Comercial: Ruta 3, km 1462, Trelew, Chubut.", CELL_LETRA_POR_DEFECTO);
        direccionLinea.setIndentationLeft(10);
        datosEmpresaCell.addElement(direccionLinea);
        Paragraph condicionFrenteAIvaLinea = new Paragraph("Condición IVA: Responsable Inscripto", CELL_LETRA_POR_DEFECTO);
        condicionFrenteAIvaLinea.setIndentationLeft(10);
        datosEmpresaCell.addElement(condicionFrenteAIvaLinea);
        tablaCabecera.addCell(datosEmpresaCell);

        // derecha datos comprobante
        PdfPCell datosComprobanteCell = new PdfPCell();
        datosComprobanteCell.setBorder(Rectangle.NO_BORDER);
        datosComprobanteCell.setPaddingTop(10);
        datosComprobanteCell.setPaddingBottom(20);
        datosComprobanteCell.setPaddingLeft(10);
        datosComprobanteCell.addElement(new Phrase("Número comprobante: " + parseValue(nota.getNumero()), CELL_LETRA_POR_DEFECTO));
        datosComprobanteCell.addElement(new Phrase("Fecha emisión: " + parseValue(nota.getFecha()), CELL_LETRA_POR_DEFECTO));
        Paragraph cuitLinea = new Paragraph("CUIT: 30-70955038-8", CELL_LETRA_CHICA);
        cuitLinea.setSpacingBefore(10);
        datosComprobanteCell.addElement(cuitLinea);
        datosComprobanteCell.addElement(new Phrase("Ingresos Brutos: -", CELL_LETRA_CHICA));
        datosComprobanteCell.addElement(new Phrase("Fecha inicio actividades:  10-03-2006", CELL_LETRA_CHICA));
        tablaCabecera.addCell(datosComprobanteCell);

        document.add(tablaCabecera);

        // tabla datos cliente

        Comprador comprador = nota.getFactura().getComprador();
        if (comprador != null) {
            PdfPTable tablaCliente = new PdfPTable(2);
            tablaCliente.setWidths(new float[]{50, 50});
            tablaCliente.setTableEvent(new BorderEvent());
            tablaCliente.getDefaultCell().setBorder(Rectangle.NO_BORDER);
            tablaCliente.setWidthPercentage(100);

            PdfPCell datosClienteCell = new PdfPCell();
            datosClienteCell.setBorder(Rectangle.NO_BORDER);
            datosClienteCell.setPaddingTop(10);
            datosClienteCell.setPaddingBottom(20);
            Paragraph razonSocialClienteLinea = new Paragraph("Razón social: " + parseValue(comprador.getNombre()), CELL_LETRA_POR_DEFECTO);
            razonSocialClienteLinea.setIndentationLeft(10);
            razonSocialClienteLinea.setSpacingAfter(10);
            datosClienteCell.addElement(razonSocialClienteLinea);
            String domicilioComercial = nota.getFactura().getTipoFactura().equals("A") ? parseValue(comprador.getDomicilioComercial()) : "-";
            Paragraph domicilioComercialLinea = new Paragraph("Domicilio comercial: " + domicilioComercial, CELL_LETRA_POR_DEFECTO);
            domicilioComercialLinea.setIndentationLeft(10);
            datosClienteCell.addElement(domicilioComercialLinea);
            tablaCliente.addCell(datosClienteCell);

            PdfPCell datosCliente2Cell = new PdfPCell();
            datosCliente2Cell.setBorder(Rectangle.NO_BORDER);
            datosCliente2Cell.setPaddingTop(10);
            datosCliente2Cell.setPaddingBottom(20);
            String cuitCliente = nota.getFactura().getTipoFactura().equals("A") ? parseValue(comprador.getCuit()) : "-";
            Paragraph cuitClienteLinea = new Paragraph("CUIT: " + cuitCliente, CELL_LETRA_POR_DEFECTO);
            cuitClienteLinea.setIndentationLeft(10);
            cuitClienteLinea.setSpacingAfter(10);
            datosCliente2Cell.addElement(cuitClienteLinea);
            Paragraph responsabilidadIvaLinea = new Paragraph("Condición IVA: " + getResponsabilidadIva(comprador.getResponsabilidadIva()), CELL_LETRA_POR_DEFECTO);
            responsabilidadIvaLinea.setIndentationLeft(10);
            datosCliente2Cell.addElement(responsabilidadIvaLinea);
            tablaCliente.addCell(datosCliente2Cell);

            document.add(tablaCliente);
        }

        // tabla detalles
        PdfPTable tablaDetalles = new PdfPTable(5);
        tablaDetalles.setWidths(new int[]{40, 15, 15, 15, 15});
        tablaDetalles.setSpacingBefore(30);
        tablaDetalles.setWidthPercentage(100);
        tablaDetalles.addCell(getColumnaDetalle("Detalle"));
        tablaDetalles.addCell(getColumnaDetalle("Cantidad"));
        tablaDetalles.addCell(getColumnaDetalle("Precio"));
        tablaDetalles.addCell(getColumnaDetalle("% IVA"));
        tablaDetalles.addCell(getColumnaDetalle("Subtotal"));
        if (nota.getDetalle() != null) {
            tablaDetalles.addCell(new PdfPCell(new Paragraph(nota.getDetalle().getDescripcion())));
            tablaDetalles.addCell(getCeldaDetalle(Double.toString(nota.getDetalle().getCantidad()), true));
            tablaDetalles.addCell(getCeldaDetalle(NumberFormat.getCurrencyInstance().format(nota.getDetalle().getPrecioUnitario()), true));
            tablaDetalles.addCell(getCeldaDetalle(nota.getDetalle().getPorcentajeIva(), true));
            tablaDetalles.addCell(getCeldaDetalle(NumberFormat.getCurrencyInstance().format(nota.getDetalle().getSubtotal()), true));
        }

        document.add(tablaDetalles);

        // tabla totales
        PdfPTable tablaTotales = new PdfPTable(2);
        tablaTotales.setHorizontalAlignment(PdfPTable.ALIGN_RIGHT);
        tablaTotales.setWidths(new int[]{50, 50});
        tablaTotales.setSpacingBefore(100);
        tablaTotales.setWidthPercentage(30);
        tablaTotales.addCell(getColumnaDetalle("Importe neto"));
        if (nota.getFactura().getTipoFactura().equals("A")) {
            tablaTotales.addCell(getCeldaDetalle(NumberFormat.getCurrencyInstance().format(nota.getTotal() - nota.getIva()), true));
            tablaTotales.addCell(getColumnaDetalle("IVA 21%"));
            tablaTotales.addCell(getCeldaDetalle(NumberFormat.getCurrencyInstance().format(nota.getIva()), true));
        } else if (nota.getFactura().getTipoFactura().equals("B")) {
            tablaTotales.addCell(getCeldaDetalle(NumberFormat.getCurrencyInstance().format(nota.getTotal()), true));
            tablaTotales.addCell(getColumnaDetalle("IVA 0%"));
            tablaTotales.addCell(getCeldaDetalle(NumberFormat.getCurrencyInstance().format(0), true));
        }

        tablaTotales.addCell(getColumnaDetalle("Importe total"));
        tablaTotales.addCell(getCeldaDetalle(NumberFormat.getCurrencyInstance().format(nota.getTotal()), true));

        document.add(tablaTotales);

        document.close();
        openFile(fileName);
    }

    private String getResponsabilidadIva(String responsabilidadIva) {
        if (responsabilidadIva != null) {
            if (responsabilidadIva.equalsIgnoreCase("I")) {
                return "Responsable Inscripto";
            } else if (responsabilidadIva.equalsIgnoreCase("C")) {
                return "Consumidor Final";
            }
        }
        return "-";
    }

    private static void openFile(String path) throws IOException {
        Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
        if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
            desktop.open(new File(path));
        }
    }

    private PdfPCell getColumnaDetalle(String nombre) {
        PdfPCell columnCell = new PdfPCell(new Phrase(nombre, CELL_LETRA_POR_DEFECTO_NEGRITA));
        columnCell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
        columnCell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
        columnCell.setBackgroundColor(BaseColor.LIGHT_GRAY);
        columnCell.setFixedHeight(20);
        return columnCell;
    }

    private PdfPCell getCeldaDetalle(String valor, boolean numeric) {
        PdfPCell columnCell = new PdfPCell(new Phrase(valor, CELL_LETRA_POR_DEFECTO));
        if (numeric) {
            columnCell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
        }
        columnCell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
        columnCell.setFixedHeight(20);
        return columnCell;
    }

    private String parseValue(String value) {
        if (StringUtil.isBlank(value)) {
            return "-";
        }
        return value;
    }

    private class BorderEvent implements PdfPTableEvent {
        public void tableLayout(PdfPTable table, float[][] widths, float[] heights, int headerRows, int rowStart, PdfContentByte[] canvases) {
            float width[] = widths[0];
            float x1 = width[0];
            float x2 = width[width.length - 1];
            float y1 = heights[0];
            float y2 = heights[heights.length - 1];
            PdfContentByte cb = canvases[PdfPTable.LINECANVAS];
            cb.rectangle(x1, y1, x2 - x1, y2 - y1);
            cb.stroke();
            cb.resetRGBColorStroke();
        }
    }


}

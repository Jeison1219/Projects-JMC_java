package com.app.Proyecto.service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import com.app.Proyecto.model.Proyecto;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

@Service
public class PDFService {

    @Autowired
    private ResourceLoader resourceLoader;

    private static final Font TITLE_FONT = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD, BaseColor.DARK_GRAY);
    private static final Font HEADER_FONT = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, BaseColor.WHITE);
    private static final Font CELL_FONT = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL);

    public byte[] generateProjectsPDF(List<Proyecto> proyectos, String nombreFiltro, LocalDate fechaInicio, LocalDate fechaFin) throws DocumentException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, baos);

        document.open();
        addMetaData(document);
        addHeader(document);
        addFilterInfo(document, nombreFiltro, fechaInicio, fechaFin);
        addProjectsTable(document, proyectos);
        document.close();

        return baos.toByteArray();
    }

    private void addMetaData(Document document) {
        document.addTitle("Reporte de Proyectos - Projects-JMC");
        document.addAuthor("Projects-JMC");
        document.addCreator("Projects-JMC System");
    }

    private void addHeader(Document document) throws DocumentException {
        // Agregar logo usando ResourceLoader
        try {
            Resource resource = resourceLoader.getResource("classpath:static/images/logo.png");
            if (resource.exists()) {
                byte[] bytes = resource.getInputStream().readAllBytes();
                Image logo = Image.getInstance(bytes);
                logo.scaleToFit(120, 120);
                logo.setAlignment(Image.ALIGN_CENTER);
                document.add(logo);
            } else {
                System.out.println("Logo no encontrado en la ruta: static/images/logo.png");
            }
        } catch (Exception e) {
            System.out.println("Error al cargar el logo: " + e.getMessage());
            // Si no se encuentra el logo, continuar sin él
        }

        // Título del reporte
        Paragraph title = new Paragraph();
        title.add(new Chunk("Projects-JMC", TITLE_FONT));
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingBefore(10);
        document.add(title);

        Paragraph subtitle = new Paragraph();
        subtitle.add(new Chunk("Reporte de Proyectos", new Font(Font.FontFamily.HELVETICA, 14, Font.NORMAL)));
        subtitle.setAlignment(Element.ALIGN_CENTER);
        subtitle.setSpacingBefore(10);
        subtitle.setSpacingAfter(20);
        document.add(subtitle);
    }

    private void addFilterInfo(Document document, String nombreFiltro, LocalDate fechaInicio, LocalDate fechaFin) throws DocumentException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        Paragraph filterInfo = new Paragraph();
        filterInfo.setSpacingBefore(20);
        filterInfo.setSpacingAfter(20);

        if (nombreFiltro != null && !nombreFiltro.isEmpty()) {
            filterInfo.add(new Chunk("Filtro por nombre: " + nombreFiltro + "\n", CELL_FONT));
        }
        if (fechaInicio != null) {
            filterInfo.add(new Chunk("Fecha inicio: " + fechaInicio.format(formatter) + "\n", CELL_FONT));
        }
        if (fechaFin != null) {
            filterInfo.add(new Chunk("Fecha fin: " + fechaFin.format(formatter) + "\n", CELL_FONT));
        }

        document.add(filterInfo);
    }

    private void addProjectsTable(Document document, List<Proyecto> proyectos) throws DocumentException {
        if (proyectos == null || proyectos.isEmpty()) {
            Paragraph noData = new Paragraph("No se encontraron proyectos registrados con los criterios seleccionados.", new Font(Font.FontFamily.HELVETICA, 12, Font.ITALIC, BaseColor.GRAY));
            noData.setSpacingBefore(30);
            noData.setAlignment(Element.ALIGN_CENTER);
            document.add(noData);
            return;
        }
        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setSpacingBefore(20f);
        table.setSpacingAfter(20f);

        // Configurar anchos de columnas
        float[] columnWidths = {3f, 2f, 2f, 2f};
        table.setWidths(columnWidths);

        // Agregar encabezados
        addTableHeader(table);

        // Agregar datos
        for (Proyecto proyecto : proyectos) {
            addTableRow(table, proyecto);
        }

        document.add(table);
    }

    private void addTableHeader(PdfPTable table) {
        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(new BaseColor(41, 100, 142)); // Color primario de tu aplicación
        cell.setPadding(5);

        String[] headers = {"Nombre", "Fecha Inicio", "Fecha Fin", "Miembros"};
        for (String header : headers) {
            cell.setPhrase(new Phrase(header, HEADER_FONT));
            table.addCell(cell);
        }
    }

    private void addTableRow(PdfPTable table, Proyecto proyecto) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        table.addCell(new Phrase(proyecto.getNombre(), CELL_FONT));
        table.addCell(new Phrase(proyecto.getFechaInicio().format(formatter), CELL_FONT));
        table.addCell(new Phrase(proyecto.getFechaFin().format(formatter), CELL_FONT));
        // Mostrar solo los nombres de los miembros separados por coma
        String miembros = "";
        if (proyecto.getMiembros() != null && !proyecto.getMiembros().isEmpty()) {
            miembros = proyecto.getMiembros().stream()
                .map(u -> u.getName())
                .reduce((a, b) -> a + ", " + b)
                .orElse("");
        }
        table.addCell(new Phrase(miembros, CELL_FONT));
    }
}
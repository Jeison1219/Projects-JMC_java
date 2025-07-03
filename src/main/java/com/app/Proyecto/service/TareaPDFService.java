package com.app.Proyecto.service;

import com.app.Proyecto.model.Tarea;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class TareaPDFService {

    @Autowired
    private ResourceLoader resourceLoader;

    private static final Font TITLE_FONT = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD, BaseColor.DARK_GRAY);
    private static final Font HEADER_FONT = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, BaseColor.WHITE);
    private static final Font CELL_FONT = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL);

    public byte[] generateTareasPDF(List<Tarea> tareas, String proyectoNombre, String prioridad, String estado, String fechaInicio, String fechaFin) throws DocumentException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, baos);

        document.open();
        addMetaData(document);
        addHeader(document);
        addFilterInfo(document, proyectoNombre, prioridad, estado, fechaInicio, fechaFin);
        addTareasTable(document, tareas);
        document.close();

        return baos.toByteArray();
    }

    private void addMetaData(Document document) {
        document.addTitle("Reporte de Tareas - Projects-JMC");
        document.addAuthor("Projects-JMC");
        document.addCreator("Projects-JMC System");
    }

    private void addHeader(Document document) throws DocumentException {
        try {
            Resource resource = resourceLoader.getResource("classpath:static/images/logo.png");
            if (resource.exists()) {
                byte[] bytes = resource.getInputStream().readAllBytes();
                Image logo = Image.getInstance(bytes);
                logo.scaleToFit(120, 120);
                logo.setAlignment(Image.ALIGN_CENTER);
                document.add(logo);
            }
        } catch (Exception e) {
            // Si no se encuentra el logo, continuar sin él
        }
        Paragraph title = new Paragraph();
        title.add(new Chunk("Projects-JMC", TITLE_FONT));
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingBefore(10);
        document.add(title);

        Paragraph subtitle = new Paragraph();
        subtitle.add(new Chunk("Reporte de Tareas", new Font(Font.FontFamily.HELVETICA, 14, Font.NORMAL)));
        subtitle.setAlignment(Element.ALIGN_CENTER);
        subtitle.setSpacingBefore(10);
        subtitle.setSpacingAfter(20);
        document.add(subtitle);
    }

    private void addFilterInfo(Document document, String proyectoNombre, String prioridad, String estado, String fechaInicio, String fechaFin) throws DocumentException {
        Paragraph filterInfo = new Paragraph();
        filterInfo.setSpacingBefore(20);
        filterInfo.setSpacingAfter(20);
        if (proyectoNombre != null && !proyectoNombre.isEmpty()) {
            filterInfo.add(new Chunk("Proyecto: " + proyectoNombre + "\n", CELL_FONT));
        }
        if (prioridad != null && !prioridad.isEmpty()) {
            filterInfo.add(new Chunk("Prioridad: " + prioridad + "\n", CELL_FONT));
        }
        if (estado != null && !estado.isEmpty()) {
            filterInfo.add(new Chunk("Estado: " + estado + "\n", CELL_FONT));
        }
        if (fechaInicio != null && !fechaInicio.isEmpty()) {
            filterInfo.add(new Chunk("Fecha inicio: " + fechaInicio + "\n", CELL_FONT));
        }
        if (fechaFin != null && !fechaFin.isEmpty()) {
            filterInfo.add(new Chunk("Fecha fin: " + fechaFin + "\n", CELL_FONT));
        }
        document.add(filterInfo);
    }

    private void addTareasTable(Document document, List<Tarea> tareas) throws DocumentException {
        if (tareas == null || tareas.isEmpty()) {
            Paragraph noData = new Paragraph("No se encontraron tareas registradas con los criterios seleccionados.", new Font(Font.FontFamily.HELVETICA, 12, Font.ITALIC, BaseColor.GRAY));
            noData.setSpacingBefore(30);
            noData.setAlignment(Element.ALIGN_CENTER);
            document.add(noData);
            return;
        }
        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        table.setSpacingBefore(20f);
        table.setSpacingAfter(20f);
        float[] columnWidths = {2f, 3f, 2f, 2f, 2f};
        table.setWidths(columnWidths);
        addTableHeader(table);
        for (Tarea tarea : tareas) {
            addTableRow(table, tarea);
        }
        document.add(table);
    }

    private void addTableHeader(PdfPTable table) {
        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(new BaseColor(41, 100, 142));
        cell.setPadding(5);
        String[] headers = {"Proyecto", "Título", "Fecha Límite", "Prioridad", "Estado"};
        for (String header : headers) {
            cell.setPhrase(new Phrase(header, HEADER_FONT));
            table.addCell(cell);
        }
    }

    private void addTableRow(PdfPTable table, Tarea tarea) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        table.addCell(new Phrase(tarea.getProyecto() != null ? tarea.getProyecto().getNombre() : "Sin proyecto", CELL_FONT));
        table.addCell(new Phrase(tarea.getTitulo(), CELL_FONT));
        table.addCell(new Phrase(tarea.getFechaLimite() != null ? tarea.getFechaLimite().format(formatter) : "Sin fecha", CELL_FONT));
        table.addCell(new Phrase(tarea.getPrioridad() != null ? tarea.getPrioridad() : "N/A", CELL_FONT));
        table.addCell(new Phrase(tarea.isCompletada() ? "Completada" : "Pendiente", CELL_FONT));
    }
}

package com.app.Proyecto.service;

import com.app.Proyecto.model.Tarea;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.draw.LineSeparator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class TareaPDFService {

    @Autowired
    private ResourceLoader resourceLoader;

    private static final Font TITLE_FONT = new Font(Font.FontFamily.HELVETICA, 20, Font.BOLD, BaseColor.DARK_GRAY);
    private static final Font SUBTITLE_FONT = new Font(Font.FontFamily.HELVETICA, 14, Font.NORMAL, BaseColor.GRAY);
    private static final Font HEADER_FONT = new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD, BaseColor.WHITE);
    private static final Font CELL_FONT = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, BaseColor.BLACK);
    private static final Font FILTER_FONT = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, BaseColor.DARK_GRAY);

    public byte[] generateTareasPDF(List<Tarea> tareas, String proyectoNombre, String prioridad, String estado, String fechaInicio, String fechaFin) throws DocumentException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 36, 36, 54, 36); // margen superior más grande para header
        PdfWriter writer = PdfWriter.getInstance(document, baos);

        document.open();
        addMetaData(document);
        addHeader(document);
        addFilterTable(document, proyectoNombre, prioridad, estado, fechaInicio, fechaFin);
        addTareasTable(document, tareas);
        addFooter(document, writer);
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
                logo.scaleToFit(100, 100);
                logo.setAlignment(Image.ALIGN_CENTER);
                document.add(logo);
            }
        } catch (Exception e) {
            // continuar sin logo
        }

        Paragraph title = new Paragraph("Projects-JMC", TITLE_FONT);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingBefore(5);
        document.add(title);

        Paragraph subtitle = new Paragraph("Reporte de Tareas", SUBTITLE_FONT);
        subtitle.setAlignment(Element.ALIGN_CENTER);
        subtitle.setSpacingBefore(5);
        subtitle.setSpacingAfter(10);
        document.add(subtitle);

        LineSeparator line = new LineSeparator();
        line.setLineColor(BaseColor.LIGHT_GRAY);
        document.add(new Chunk(line));
    }

    private void addFilterTable(Document document, String proyectoNombre, String prioridad, String estado, String fechaInicio, String fechaFin) throws DocumentException {
        PdfPTable filterTable = new PdfPTable(2);
        filterTable.setWidthPercentage(100);
        filterTable.setSpacingBefore(15f);
        filterTable.setSpacingAfter(15f);
        filterTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);

        if (proyectoNombre != null && !proyectoNombre.isEmpty()) {
            filterTable.addCell(new Phrase("Proyecto:", FILTER_FONT));
            filterTable.addCell(new Phrase(proyectoNombre, FILTER_FONT));
        }
        if (prioridad != null && !prioridad.isEmpty()) {
            filterTable.addCell(new Phrase("Prioridad:", FILTER_FONT));
            filterTable.addCell(new Phrase(prioridad, FILTER_FONT));
        }
        if (estado != null && !estado.isEmpty()) {
            filterTable.addCell(new Phrase("Estado:", FILTER_FONT));
            filterTable.addCell(new Phrase(estado, FILTER_FONT));
        }
        if (fechaInicio != null && !fechaInicio.isEmpty()) {
            filterTable.addCell(new Phrase("Fecha inicio:", FILTER_FONT));
            filterTable.addCell(new Phrase(fechaInicio, FILTER_FONT));
        }
        if (fechaFin != null && !fechaFin.isEmpty()) {
            filterTable.addCell(new Phrase("Fecha fin:", FILTER_FONT));
            filterTable.addCell(new Phrase(fechaFin, FILTER_FONT));
        }

        if (filterTable.size() > 0) {
            document.add(filterTable);
        }
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
        table.setSpacingBefore(10f);
        float[] columnWidths = {2f, 3f, 2f, 2f, 2f};
        table.setWidths(columnWidths);

        addTableHeader(table);

        boolean alternate = false;
        for (Tarea tarea : tareas) {
            addTableRow(table, tarea, alternate);
            alternate = !alternate;
        }
        document.add(table);
    }

    private void addTableHeader(PdfPTable table) {
        String[] headers = {"Proyecto", "Título", "Fecha Límite", "Prioridad", "Estado"};
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, HEADER_FONT));
            cell.setBackgroundColor(new BaseColor(41, 100, 142));
            cell.setPadding(6);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);
        }
    }

    private void addTableRow(PdfPTable table, Tarea tarea, boolean alternate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        BaseColor rowColor = alternate ? new BaseColor(245, 245, 245) : BaseColor.WHITE;

        addCell(table, tarea.getProyecto() != null ? tarea.getProyecto().getNombre() : "Sin proyecto", rowColor);
        addCell(table, tarea.getTitulo(), rowColor);
        addCell(table, tarea.getFechaLimite() != null ? tarea.getFechaLimite().format(formatter) : "Sin fecha", rowColor);
        addCell(table, tarea.getPrioridad() != null ? tarea.getPrioridad() : "N/A", rowColor);

        // Estado con iconos
        String estado = tarea.isCompletada() ? "✅ Completada" : "⏳ Pendiente";
        addCell(table, estado, rowColor);
    }

    private void addCell(PdfPTable table, String text, BaseColor background) {
        PdfPCell cell = new PdfPCell(new Phrase(text, CELL_FONT));
        cell.setBackgroundColor(background);
        cell.setPadding(5);
        table.addCell(cell);
    }

    private void addFooter(Document document, PdfWriter writer) throws DocumentException {
        Paragraph footer = new Paragraph("Generado el " +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                new Font(Font.FontFamily.HELVETICA, 8, Font.ITALIC, BaseColor.GRAY));
        footer.setAlignment(Element.ALIGN_RIGHT);
        footer.setSpacingBefore(20);
        document.add(footer);
    }
}



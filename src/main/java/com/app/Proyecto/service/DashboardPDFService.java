package com.app.Proyecto.service;

import com.app.Proyecto.model.Tarea;
import com.app.Proyecto.model.User;
import com.app.Proyecto.repository.TareaRepository;
import com.app.Proyecto.repository.UserRepository;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardPDFService {

    private final TareaRepository tareaRepository;
    private final UserRepository userRepository;

    public byte[] generateDashboardPDF(String email) throws Exception {
        User usuario = userRepository.findByEmail(email).orElseThrow();
        List<Tarea> tareas = tareaRepository.findByUsuario(usuario);

        // Agrupar por prioridad
        Map<String, Long> tareasPorPrioridad = tareas.stream()
                .collect(Collectors.groupingBy(Tarea::getPrioridad, Collectors.counting()));

        // Agrupar por estado
        long completadas = tareas.stream().filter(Tarea::isCompletada).count();
        long pendientes = tareas.size() - completadas;

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document();
        PdfWriter.getInstance(document, baos);
        document.open();

        // Título
        Font tituloFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD, new BaseColor(33, 64, 154));
        Paragraph titulo = new Paragraph("Reporte de Dashboard", tituloFont);
        titulo.setAlignment(Element.ALIGN_CENTER);
        document.add(titulo);
        document.add(new Paragraph(" "));

        // Gráfica: Tareas por Prioridad (tabla simple)
        document.add(new Paragraph("Tareas por Prioridad:", new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD)));
        PdfPTable tablaPrioridad = new PdfPTable(2);
        tablaPrioridad.addCell("Prioridad");
        tablaPrioridad.addCell("Cantidad");
        tablaPrioridad.addCell("Alta");
        tablaPrioridad.addCell(String.valueOf(tareasPorPrioridad.getOrDefault("Alta", 0L)));
        tablaPrioridad.addCell("Media");
        tablaPrioridad.addCell(String.valueOf(tareasPorPrioridad.getOrDefault("Media", 0L)));
        tablaPrioridad.addCell("Baja");
        tablaPrioridad.addCell(String.valueOf(tareasPorPrioridad.getOrDefault("Baja", 0L)));
        document.add(tablaPrioridad);
        document.add(new Paragraph(" "));

        // Gráfica: Progreso de Tareas (tabla simple)
        document.add(new Paragraph("Progreso de Tareas:", new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD)));
        PdfPTable tablaProgreso = new PdfPTable(2);
        tablaProgreso.addCell("Estado");
        tablaProgreso.addCell("Cantidad");
        tablaProgreso.addCell("Completadas");
        tablaProgreso.addCell(String.valueOf(completadas));
        tablaProgreso.addCell("Pendientes");
        tablaProgreso.addCell(String.valueOf(pendientes));
        document.add(tablaProgreso);

        document.close();
        return baos.toByteArray();
    }

    // Nuevo método para PDF con imágenes de gráficas
    public byte[] generateDashboardGraficasPDF(String email, Object requestObj) throws Exception {
        String prioridadImg = (String) requestObj.getClass().getField("prioridadImg").get(requestObj);
        String progresoImg = (String) requestObj.getClass().getField("progresoImg").get(requestObj);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 40, 40, 40, 40);
        PdfWriter.getInstance(document, baos);
        document.open();

        // Logo
        try {
            // Usar ruta absoluta del sistema de archivos para el logo
            String logoPath = "c:/Users/pc/Downloads/Projects-JMC_java-1/src/main/resources/static/images/logo.png";
            Image logo = Image.getInstance(logoPath);
            logo.scaleAbsolute(120, 120);
            logo.setAlignment(Image.ALIGN_CENTER);
            document.add(logo);
        } catch (Exception e) {
            // Si no se encuentra el logo, continuar sin él
        }

        // Título
        Font tituloFont = new Font(Font.FontFamily.HELVETICA, 22, Font.BOLD, new BaseColor(33, 64, 154));
        Paragraph titulo = new Paragraph("Projects-JMC", tituloFont);
        titulo.setAlignment(Element.ALIGN_CENTER);
        document.add(titulo);

        // Subtítulo
        Font subFont = new Font(Font.FontFamily.HELVETICA, 16, Font.NORMAL, new BaseColor(33, 64, 154));
        Paragraph sub = new Paragraph("Reporte de Dashboard", subFont);
        sub.setAlignment(Element.ALIGN_CENTER);
        document.add(sub);
        document.add(new Paragraph(" "));

        // Gráficas
        Font grafFont = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD, new BaseColor(41,100,142));
        Paragraph graf1 = new Paragraph("Tareas por Prioridad", grafFont);
        graf1.setAlignment(Element.ALIGN_CENTER);
        document.add(graf1);
        document.add(new Paragraph(" "));

        Image prioridad = base64ToImage(prioridadImg);
        prioridad.scaleToFit(350, 200);
        prioridad.setAlignment(Image.ALIGN_CENTER);
        document.add(prioridad);
        document.add(new Paragraph(" "));

        Paragraph graf2 = new Paragraph("Progreso de Tareas", grafFont);
        graf2.setAlignment(Element.ALIGN_CENTER);
        document.add(graf2);
        document.add(new Paragraph(" "));

        Image progreso = base64ToImage(progresoImg);
        progreso.scaleToFit(350, 200);
        progreso.setAlignment(Image.ALIGN_CENTER);
        document.add(progreso);

        document.close();
        return baos.toByteArray();
    }

    private Image base64ToImage(String base64) throws Exception {
        String base64Data = base64.split(",",2)[1];
        byte[] imageBytes = java.util.Base64.getDecoder().decode(base64Data);
        return Image.getInstance(imageBytes);
    }
}
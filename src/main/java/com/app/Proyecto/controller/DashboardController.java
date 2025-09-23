package com.app.Proyecto.controller;

import com.app.Proyecto.service.DashboardPDFService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

@Controller
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardPDFService dashboardPDFService;

    @PostMapping("/exportar-pdf-graficas")
    public ResponseEntity<byte[]> exportarDashboardGraficasPDF(@AuthenticationPrincipal UserDetails userDetails,
                                                              @RequestBody GraficasDashboardRequest request) {
        try {
            byte[] pdfBytes = dashboardPDFService.generateDashboardGraficasPDF(userDetails.getUsername(), request);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("filename", "Dashboard-Reporte.pdf");
            return ResponseEntity.ok().headers(headers).body(pdfBytes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

// DTO para recibir las imágenes base64
public static class GraficasDashboardRequest {
    public String prioridadImg;
    public String progresoImg;
    public GraficasDashboardRequest() {}
}
}

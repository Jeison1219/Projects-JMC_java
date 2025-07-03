package com.app.Proyecto.controller;

import com.app.Proyecto.model.Proyecto;
import com.app.Proyecto.model.Tarea;
import com.app.Proyecto.service.ProyectoService;
import com.app.Proyecto.service.TareaService;
import com.app.Proyecto.service.PDFService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/proyectos")
@RequiredArgsConstructor
public class ProyectoController {

    private final ProyectoService proyectoService;
    private final TareaService tareaService;
    private final PDFService pdfService;
    @GetMapping("/exportar-pdf")
    public org.springframework.http.ResponseEntity<byte[]> exportarPDF(
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {
        try {
            List<Proyecto> proyectos = proyectoService.buscarProyectos(nombre, fechaInicio, fechaFin);
            byte[] pdfBytes = pdfService.generateProjectsPDF(proyectos, nombre, fechaInicio, fechaFin);
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("filename", "Projects-JMC-Reporte.pdf");
            return org.springframework.http.ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);
        } catch (Exception e) {
            return org.springframework.http.ResponseEntity.internalServerError().build();
        }
    }

    // Método que se ejecuta antes de cada petición para agregar el username al modelo
    @ModelAttribute
    public void addUsernameToModel(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails != null) {
            model.addAttribute("username", userDetails.getUsername());
        }
    }

    @GetMapping
    public String listar(
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            Model model) {

        List<Proyecto> proyectos = proyectoService.buscarProyectos(nombre, fechaInicio, fechaFin);
        
        model.addAttribute("proyectos", proyectos);
        model.addAttribute("nombre", nombre);
        model.addAttribute("fechaInicio", fechaInicio);
        model.addAttribute("fechaFin", fechaFin);

        return "proyecto-list";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("proyecto", new Proyecto());
        return "proyecto-form";
    }

    @PostMapping
    public String guardar(@ModelAttribute Proyecto proyecto) {
        proyectoService.guardar(proyecto);
        return "redirect:/proyectos";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        Proyecto proyecto = proyectoService.buscarPorId(id);
        model.addAttribute("proyecto", proyecto);
        return "proyecto-form";
    }

    @PostMapping("/actualizar/{id}")
    public String actualizar(@PathVariable Long id, @ModelAttribute Proyecto proyecto) {
        proyectoService.actualizar(id, proyecto);
        return "redirect:/proyectos";
    }

    @PostMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id) {
        proyectoService.eliminar(id);
        return "redirect:/proyectos";
    }

    @GetMapping("/detalles/{id}")
    public String verDetalles(@PathVariable Long id, Model model) {
        Proyecto proyecto = proyectoService.buscarPorId(id);
        List<Tarea> tareas = tareaService.listarTareasPorProyecto(proyecto);
        model.addAttribute("proyecto", proyecto);
        model.addAttribute("tareas", tareas);
        return "proyecto-detalles";
    }
}
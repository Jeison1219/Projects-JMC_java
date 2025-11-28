package com.app.Proyecto.controller;

import java.beans.PropertyEditorSupport;
import java.time.LocalDate;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.app.Proyecto.model.Proyecto;
import com.app.Proyecto.model.Tarea;
import com.app.Proyecto.service.PDFService;
import com.app.Proyecto.service.ProyectoService;
import com.app.Proyecto.service.TareaService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/proyectos")
@RequiredArgsConstructor
public class ProyectoController {

    private static final Logger logger = LoggerFactory.getLogger(ProyectoController.class);
    
    private final ProyectoService proyectoService;
    private final TareaService tareaService;
    private final PDFService pdfService;
    private final com.app.Proyecto.repository.UserRepository userRepository;

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(java.util.List.class, "miembros", new MiembrosEditor(userRepository));
    }

    public static class MiembrosEditor extends PropertyEditorSupport {
        private final com.app.Proyecto.repository.UserRepository userRepository;
        public MiembrosEditor(com.app.Proyecto.repository.UserRepository userRepository) {
            this.userRepository = userRepository;
        }
        @Override
        public void setAsText(String text) {
            if (text == null || text.isEmpty()) {
                setValue(java.util.Collections.emptyList());
            } else {
                String[] ids = text.split(",");
                java.util.List<com.app.Proyecto.model.User> users = new java.util.ArrayList<>();
                for (String id : ids) {
                    userRepository.findById(Long.parseLong(id.trim())).ifPresent(users::add);
                }
                setValue(users);
            }
        }
    }

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
            Model model,
            @AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails userDetails) {

        List<Proyecto> proyectos = proyectoService.buscarProyectos(nombre, fechaInicio, fechaFin);

        // Filtrar proyectos donde el usuario autenticado es miembro
        if (userDetails != null) {
            String email = userDetails.getUsername();
            proyectos = proyectos.stream()
                .filter(p -> p.getMiembros() != null && p.getMiembros().stream().anyMatch(u -> email.equals(u.getEmail())))
                .toList();
        }

        model.addAttribute("proyectos", proyectos);
        model.addAttribute("nombre", nombre);
        model.addAttribute("fechaInicio", fechaInicio);
        model.addAttribute("fechaFin", fechaFin);

        return "proyecto-list";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model, @AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails userDetails) {
        Proyecto proyecto = new Proyecto();
        // Preseleccionar al usuario autenticado como miembro si existe
        if (userDetails != null) {
            userRepository.findByEmail(userDetails.getUsername()).ifPresent(u -> proyecto.setMiembros(java.util.List.of(u)));
        }
        model.addAttribute("proyecto", proyecto);
        model.addAttribute("usuarios", userRepository.findAll());
        return "proyecto-form";
    }

    @PostMapping
    public String guardar(@ModelAttribute Proyecto proyecto, @AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails userDetails) {
        // Asegurarse de que el usuario autenticado esté siempre en la lista de miembros (sin duplicados)
        if (userDetails != null) {
            com.app.Proyecto.model.User creador = userRepository.findByEmail(userDetails.getUsername()).orElse(null);
            if (creador != null) {
                java.util.List<com.app.Proyecto.model.User> miembros = proyecto.getMiembros();
                if (miembros == null) {
                    miembros = new java.util.ArrayList<>();
                }
                boolean presente = miembros.stream().anyMatch(u -> u.getEmail().equalsIgnoreCase(creador.getEmail()));
                if (!presente) {
                    miembros.add(creador);
                }
                proyecto.setMiembros(miembros);
            }
        }
        proyectoService.guardar(proyecto);
        return "redirect:/proyectos";
    }

    @PostMapping("/import")
    public String importJson(@org.springframework.web.bind.annotation.RequestParam("file") org.springframework.web.multipart.MultipartFile file, Model model) {
        try {
            proyectoService.importFromJson(file);
            model.addAttribute("mensaje", "Importación completada");
        } catch (Exception e) {
            model.addAttribute("error", "Error importando JSON: " + e.getMessage());
        }
        return "redirect:/proyectos";
    }

    @GetMapping("/export")
    public org.springframework.http.ResponseEntity<byte[]> exportJson() {
        try {
            byte[] data = proyectoService.exportAllAsJson();
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
            headers.setContentDispositionFormData("attachment", "proyectos-export.json");
            return org.springframework.http.ResponseEntity.ok().headers(headers).body(data);
        } catch (Exception e) {
            logger.error("Error al exportar proyectos a JSON", e);
            return org.springframework.http.ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        Proyecto proyecto = proyectoService.buscarPorId(id);
        model.addAttribute("proyecto", proyecto);
        model.addAttribute("usuarios", userRepository.findAll());
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

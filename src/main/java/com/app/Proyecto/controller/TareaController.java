package com.app.Proyecto.controller;

import com.app.Proyecto.model.Tarea;
import com.app.Proyecto.model.User;
import com.app.Proyecto.repository.ProyectoRepository;
import com.app.Proyecto.repository.UserRepository;
import com.app.Proyecto.service.TareaPDFService;
import com.app.Proyecto.service.TareaService;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/tareas")
@RequiredArgsConstructor
public class TareaController {

    private final TareaService tareaService;
    private final UserRepository userRepository;
    private final ProyectoRepository proyectoRepository;

    @Autowired
    private final TareaPDFService tareaPDFService;

    @ModelAttribute
    public void addUsernameToModel(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails != null) {
            model.addAttribute("username", userDetails.getUsername());
        }
    }

    // 📋 Lista filtrada de tareas por usuario autenticado
    @GetMapping
    public String listar(
            @RequestParam(required = false) String titulo,
            @RequestParam(required = false) Boolean completada,
            @RequestParam(required = false) String prioridad,
            @RequestParam(required = false) Long proyectoId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model
    ) {
        User usuario = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        List<Tarea> tareas = tareaService.buscarTareasConFiltros(usuario, titulo, completada, prioridad, proyectoId, fechaInicio, fechaFin);

        model.addAttribute("tareas", tareas);
        model.addAttribute("proyectos", proyectoRepository.findAll());
        model.addAttribute("titulo", titulo);
        model.addAttribute("completada", completada);
        model.addAttribute("prioridad", prioridad);
        model.addAttribute("proyectoId", proyectoId);
        model.addAttribute("fechaInicio", fechaInicio);
        model.addAttribute("fechaFin", fechaFin);

        return "tareas";
    }

    // 🆕 Formulario de creación de tarea
    @GetMapping("/nueva")
    public String nuevaTarea(Model model) {
        model.addAttribute("tarea", new Tarea());
        model.addAttribute("proyectos", proyectoRepository.findAll());
        model.addAttribute("usuarios", userRepository.findAll()); // Para asignación por admin
        return "tarea-form";
    }

    // 💾 Crear tarea
    @PostMapping
    public String crear(@ModelAttribute Tarea tarea, @AuthenticationPrincipal UserDetails userDetails) {
        // Solo se asigna usuario actual si no se seleccionó uno desde el formulario (usuario solo)
        if (tarea.getUsuario() == null) {
            User usuario = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
            tarea.setUsuario(usuario);
        }
        tareaService.crearTarea(tarea);
        return "redirect:/tareas";
    }

    // ✏ Formulario de edición
    @GetMapping("/editar/{id}")
    public String editarTarea(@PathVariable Long id, Model model) {
        Tarea tarea = tareaService.buscarPorId(id);
        model.addAttribute("tarea", tarea);
        model.addAttribute("proyectos", proyectoRepository.findAll());
        model.addAttribute("usuarios", userRepository.findAll()); // Para cambiar asignación si es admin
        return "tarea-form";
    }

    // 🔄 Actualizar tarea
    @PostMapping("/actualizar/{id}")
    public String actualizarTarea(@PathVariable Long id, @ModelAttribute Tarea tareaActualizada) {
        tareaService.actualizar(id, tareaActualizada);
        return "redirect:/tareas";
    }

    // ✅ Cambiar estado completada (solo asignado)
    @PostMapping("/actualizarEstado/{id}")
    public String actualizarEstado(@PathVariable Long id,
                                   @RequestParam(value = "completada", defaultValue = "false") boolean completada,
                                   @AuthenticationPrincipal UserDetails userDetails) {
        Tarea tarea = tareaService.buscarPorId(id);
        if (tarea.getUsuario().getEmail().equals(userDetails.getUsername())) {
            tarea.setCompletada(completada);
            tareaService.crearTarea(tarea);
        }
        return "redirect:/tareas/detalles/" + id;
    }

    // ❌ Eliminar
    @PostMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id) {
        tareaService.eliminarTarea(id);
        return "redirect:/tareas";
    }

    // 👁 Ver detalles
    @GetMapping("/detalles/{id}")
    public String verDetalles(@PathVariable Long id, Model model,
                              @AuthenticationPrincipal UserDetails userDetails) {
        Tarea tarea = tareaService.buscarPorId(id);
        model.addAttribute("tarea", tarea);

        // Solo mostrar lista de usuarios si es ADMIN
        if (userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            model.addAttribute("usuarios", userRepository.findAll());
        }

        return "tarea-detalles";
    }

    // 👤 Asignar usuario (solo admin)
    @PostMapping("/asignar/{id}")
    public String asignarUsuario(@PathVariable Long id, @RequestParam("usuarioId") Long usuarioId) {
        tareaService.asignarUsuario(id, usuarioId);
        return "redirect:/tareas/detalles/" + id;
    }

    // 📂 Exportar PDF
    @GetMapping("/exportar-pdf")
    public org.springframework.http.ResponseEntity<byte[]> exportarPDF(
            @RequestParam(required = false) String titulo,
            @RequestParam(required = false) Boolean completada,
            @RequestParam(required = false) String prioridad,
            @RequestParam(required = false) Long proyectoId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        User usuario = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        List<Tarea> tareas = tareaService.buscarTareasConFiltros(usuario, titulo, completada, prioridad, proyectoId, fechaInicio, fechaFin);
        String proyectoNombre = proyectoId != null
                ? proyectoRepository.findById(proyectoId).map(p -> p.getNombre()).orElse("")
                : "";
        String estado = completada == null ? "" : (completada ? "Completada" : "Pendiente");
        String fechaInicioStr = fechaInicio != null ? fechaInicio.toString() : "";
        String fechaFinStr = fechaFin != null ? fechaFin.toString() : "";

        try {
            byte[] pdfBytes = tareaPDFService.generateTareasPDF(tareas, proyectoNombre, prioridad, estado, fechaInicioStr, fechaFinStr);
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("filename", "Tareas-Reporte.pdf");
            return org.springframework.http.ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);
        } catch (Exception e) {
            return org.springframework.http.ResponseEntity.internalServerError().build();
        }
    }
}

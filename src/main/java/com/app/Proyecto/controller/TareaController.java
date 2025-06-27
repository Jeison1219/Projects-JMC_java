package com.app.Proyecto.controller;

import com.app.Proyecto.model.Tarea;
import com.app.Proyecto.model.User;
import com.app.Proyecto.repository.UserRepository;
import com.app.Proyecto.repository.ProyectoRepository;
import com.app.Proyecto.service.TareaService;
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
@RequiredArgsConstructor
@RequestMapping("/tareas")
public class TareaController {

    private final TareaService tareaService;
    private final UserRepository userRepository;
    private final ProyectoRepository proyectoRepository;

    // 📋 Mostrar lista de tareas con filtros
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

    // 🆕 Formulario para nueva tarea
    @GetMapping("/nueva")
    public String nuevaTarea(Model model) {
        model.addAttribute("tarea", new Tarea());
        model.addAttribute("proyectos", proyectoRepository.findAll());
        return "tarea-form";
    }

    // 💾 Crear nueva tarea
    @PostMapping
    public String crear(@ModelAttribute Tarea tarea, @AuthenticationPrincipal UserDetails userDetails) {
        User usuario = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        tarea.setUsuario(usuario);
        tareaService.crearTarea(tarea);
        return "redirect:/tareas";
    }

    // ✏ Formulario de edición
    @GetMapping("/editar/{id}")
    public String editarTarea(@PathVariable Long id, Model model) {
        Tarea tarea = tareaService.buscarPorId(id);
        model.addAttribute("tarea", tarea);
        model.addAttribute("proyectos", proyectoRepository.findAll());
        return "tarea-form";
    }

    // 🔄 Actualizar tarea
    @PostMapping("/actualizar/{id}")
    public String actualizarTarea(@PathVariable Long id, @ModelAttribute Tarea tareaActualizada) {
        tareaService.actualizar(id, tareaActualizada);
        return "redirect:/tareas";
    }

    // ✅ Marcar tarea como completada
    @PostMapping("/completar/{id}")
    public String completar(@PathVariable Long id) {
        tareaService.marcarComoCompletada(id);
        return "redirect:/tareas";
    }

    // ❌ Eliminar tarea
    @PostMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id) {
        tareaService.eliminarTarea(id);
        return "redirect:/tareas";
    }

    // 👁 Ver detalles de tarea
    @GetMapping("/detalles/{id}")
    public String verDetalles(@PathVariable Long id, Model model) {
        Tarea tarea = tareaService.buscarPorId(id);
        model.addAttribute("tarea", tarea);
        model.addAttribute("usuarios", userRepository.findAll());
        return "tarea-detalles";
    }

    // 👤 Asignar usuario a tarea
    @PostMapping("/asignar/{id}")
    public String asignarUsuario(@PathVariable Long id, @RequestParam("usuarioId") Long usuarioId) {
        tareaService.asignarUsuario(id, usuarioId);
        return "redirect:/tareas/detalles/" + id;
    }
}

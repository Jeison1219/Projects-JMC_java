package com.app.Proyecto.controller;

import com.app.Proyecto.model.Tarea;
import com.app.Proyecto.model.User;
import com.app.Proyecto.repository.UserRepository;
import com.app.Proyecto.repository.ProyectoRepository;
import com.app.Proyecto.service.TareaService;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/tareas")
public class TareaController {

    private final TareaService tareaService;
    private final UserRepository userRepository;
    private final ProyectoRepository proyectoRepository;

    // Mostrar todas las tareas del usuario autenticado
    @GetMapping
    public String listar(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        User usuario = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        model.addAttribute("tareas", tareaService.listarTareasPorUsuario(usuario));
        return "tareas"; // archivo Thymeleaf tareas.html
    }

    // Mostrar formulario para nueva tarea
    @GetMapping("/nueva")
    public String nuevaTarea(Model model) {
        model.addAttribute("tarea", new Tarea());
        model.addAttribute("proyectos", proyectoRepository.findAll());
        return "tarea-form";
    }

    // Guardar tarea nueva
    @PostMapping
    public String crear(@ModelAttribute Tarea tarea, @AuthenticationPrincipal UserDetails userDetails) {
        User usuario = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        tarea.setUsuario(usuario);
        tareaService.crearTarea(tarea);
        return "redirect:/tareas";
    }

    // Mostrar formulario de edición
    @GetMapping("/editar/{id}")
    public String editarTarea(@PathVariable Long id, Model model) {
        Tarea tarea = tareaService.buscarPorId(id); // ya corregido
        model.addAttribute("tarea", tarea);
        model.addAttribute("proyectos", proyectoRepository.findAll());
        return "tarea-form";
    }

    // Actualizar una tarea editada
    @PostMapping("/actualizar/{id}")
    public String actualizarTarea(@PathVariable Long id, @ModelAttribute Tarea tareaActualizada) {
        tareaService.actualizar(id, tareaActualizada);
        return "redirect:/tareas";
    }

    // Marcar tarea como completada
    @PostMapping("/completar/{id}")
    public String completar(@PathVariable Long id) {
        tareaService.marcarComoCompletada(id);
        return "redirect:/tareas";
    }

    // Eliminar tarea
    @PostMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id) {
        tareaService.eliminarTarea(id);
        return "redirect:/tareas";
    }
}

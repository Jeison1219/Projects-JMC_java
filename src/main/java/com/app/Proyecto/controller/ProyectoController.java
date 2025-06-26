package com.app.Proyecto.controller;

import com.app.Proyecto.model.Proyecto;
import com.app.Proyecto.model.Tarea;
import com.app.Proyecto.service.ProyectoService;
import com.app.Proyecto.service.TareaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/proyectos")
@RequiredArgsConstructor
public class ProyectoController {

    private final ProyectoService proyectoService;
    private final TareaService tareaService;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("proyectos", proyectoService.listarTodos());
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

    // 🔍 Vista de detalles + tareas asociadas
    @GetMapping("/detalles/{id}")
    public String verDetalles(@PathVariable Long id, Model model) {
        Proyecto proyecto = proyectoService.buscarPorId(id);
        List<Tarea> tareas = tareaService.listarTareasPorProyecto(proyecto);
        model.addAttribute("proyecto", proyecto);
        model.addAttribute("tareas", tareas);
        return "proyecto-detalles";
    }
}

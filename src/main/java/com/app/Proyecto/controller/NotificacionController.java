package com.app.Proyecto.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.app.Proyecto.model.Proyecto;
import com.app.Proyecto.model.Tarea;
import com.app.Proyecto.repository.ProyectoRepository;
import com.app.Proyecto.repository.TareaRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notificaciones")
public class NotificacionController {
    @Autowired
    private ProyectoRepository proyectoRepository;
    @Autowired
    private TareaRepository tareaRepository;

    @GetMapping
    public List<Map<String, String>> getNotificacionesHoy() {
        LocalDate hoy = LocalDate.now();
        List<Map<String, String>> notificaciones = new ArrayList<>();

        // Proyectos que vencen hoy
        List<Proyecto> proyectosHoy = proyectoRepository.findByFechaFin(hoy);
        for (Proyecto p : proyectosHoy) {
            Map<String, String> notif = new HashMap<>();
            notif.put("tipo", "proyecto");
            notif.put("mensaje", "El proyecto '" + p.getNombre() + "' vence hoy: " + hoy);
            notificaciones.add(notif);
        }

        // Tareas que vencen hoy
        List<Tarea> tareasHoy = tareaRepository.findByFechaLimite(hoy);
        for (Tarea t : tareasHoy) {
            Map<String, String> notif = new HashMap<>();
            notif.put("tipo", "tarea");
            notif.put("mensaje", "La tarea '" + t.getTitulo() + "' vence hoy: " + hoy);
            notificaciones.add(notif);
        }
        return notificaciones;
    }
}

package com.app.Proyecto.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.app.Proyecto.model.Proyecto;
import com.app.Proyecto.model.Tarea;
import com.app.Proyecto.model.User;
import com.app.Proyecto.repository.ProyectoRepository;
import com.app.Proyecto.repository.TareaRepository;
import com.app.Proyecto.repository.UserRepository;

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
    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public List<Map<String, Object>> getNotificacionesHoy(@AuthenticationPrincipal UserDetails userDetails) {
        LocalDate hoy = LocalDate.now();
        List<Map<String, Object>> notificaciones = new ArrayList<>();

        // Obtener usuario actual
        User usuarioActual = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Proyectos del usuario que vencen hoy
        List<Proyecto> proyectos = proyectoRepository.findAll();
        for (Proyecto p : proyectos) {
            // Solo mostrar si el usuario es miembro del proyecto y vence hoy
            if (p.getMiembros() != null && p.getMiembros().contains(usuarioActual) && p.getFechaFin() != null && p.getFechaFin().equals(hoy)) {
                Map<String, Object> notif = new HashMap<>();
                notif.put("tipo", "proyecto");
                notif.put("titulo", "Proyecto vencido");
                notif.put("mensaje", "El proyecto '" + p.getNombre() + "' vence hoy");
                notif.put("icono", "fa-project-diagram");
                notif.put("color", "warning");
                notificaciones.add(notif);
            }
        }

        // Tareas asignadas al usuario que vencen hoy
        List<Tarea> tareasHoy = tareaRepository.findByFechaLimite(hoy);
        for (Tarea t : tareasHoy) {
            // Solo mostrar si la tarea está asignada al usuario actual
            if (t.getUsuario() != null && t.getUsuario().getId().equals(usuarioActual.getId())) {
                Map<String, Object> notif = new HashMap<>();
                notif.put("tipo", "tarea");
                notif.put("titulo", "Tarea vencida");
                notif.put("mensaje", "La tarea '" + t.getTitulo() + "' vence hoy");
                notif.put("icono", "fa-tasks");
                notif.put("color", t.isCompletada() ? "success" : "danger");
                notificaciones.add(notif);
            }
        }
        return notificaciones;
    }
}

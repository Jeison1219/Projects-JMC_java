package com.app.Proyecto.controller;

import com.app.Proyecto.dto.PrioridadDTO;
import com.app.Proyecto.model.User;
import com.app.Proyecto.repository.UserRepository;
import com.app.Proyecto.service.TareaService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/tareas")
@RequiredArgsConstructor
public class TareaApiController {

    private final TareaService tareaService;
    private final UserRepository userRepository;

    @GetMapping("/prioridad")
    public PrioridadDTO obtenerConteoPorPrioridad(@AuthenticationPrincipal UserDetails userDetails) {
        User usuario = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        PrioridadDTO dto = new PrioridadDTO();
        dto.setAlta(tareaService.contarPorPrioridadPorUsuario(usuario, "ALTA"));
        dto.setMedia(tareaService.contarPorPrioridadPorUsuario(usuario, "MEDIA"));
        dto.setBaja(tareaService.contarPorPrioridadPorUsuario(usuario, "BAJA"));
        return dto;
    }

    @GetMapping("/pendientes")
    public long contarTareasPendientes(@AuthenticationPrincipal UserDetails userDetails) {
        User usuario = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return tareaService.contarTareasPendientesPorUsuario(usuario);
    }

    @GetMapping("/progreso")
    public Map<String, Long> obtenerProgresoTareas(@AuthenticationPrincipal UserDetails userDetails) {
        User usuario = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return tareaService.obtenerEstadisticasProgresoPorUsuario(usuario);
    }
}

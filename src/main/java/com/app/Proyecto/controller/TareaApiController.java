package com.app.Proyecto.controller;

import com.app.Proyecto.dto.PrioridadDTO;
import com.app.Proyecto.service.TareaService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/tareas")
@RequiredArgsConstructor
public class TareaApiController {

    private final TareaService tareaService;

    @GetMapping("/prioridad")
    public PrioridadDTO obtenerConteoPorPrioridad() {
        PrioridadDTO dto = new PrioridadDTO();
        dto.setAlta(tareaService.contarPorPrioridad("ALTA"));
        dto.setMedia(tareaService.contarPorPrioridad("MEDIA"));
        dto.setBaja(tareaService.contarPorPrioridad("BAJA"));
        return dto;
    }

    @GetMapping("/pendientes")
    public long contarTareasPendientes() {
        return tareaService.contarTareasPendientes();
    }

    @GetMapping("/progreso")
    public Map<String, Long> obtenerProgresoTareas() {
        return tareaService.obtenerEstadisticasProgreso();
    }
}

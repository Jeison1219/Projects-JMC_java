package com.app.Proyecto.service;

import com.app.Proyecto.model.Proyecto;
import com.app.Proyecto.model.Tarea;
import com.app.Proyecto.model.User;
import com.app.Proyecto.repository.TareaRepository;
import com.app.Proyecto.repository.UserRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import java.util.Map;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TareaService {

    private final TareaRepository tareaRepository;
    private final UserRepository userRepository;

    // 📋 Listar tareas por usuario
    public List<Tarea> listarTareasPorUsuario(User usuario) {
        return tareaRepository.findByUsuario(usuario);
    }
    // src/main/java/com/app/Proyecto/service/TareaService.java

public long contarPorPrioridad(String prioridad) {
    return tareaRepository.countByPrioridad(prioridad);
}
public long contarTareasPendientes() {
    return tareaRepository.countByCompletada(false); // ✅
}

public Map<String, Long> obtenerEstadisticasProgreso() {
    long completadas = tareaRepository.countByCompletadaTrue();
    long pendientes = tareaRepository.countByCompletadaFalse();

    Map<String, Long> estadisticas = new HashMap<>();
    estadisticas.put("completadas", completadas);
    estadisticas.put("pendientes", pendientes);
    return estadisticas;
}



    
    

    // 💾 Crear nueva tarea
    public Tarea crearTarea(Tarea tarea) {
        return tareaRepository.save(tarea);
    }

    // ❌ Eliminar tarea
    public void eliminarTarea(Long id) {
        tareaRepository.deleteById(id);
    }

    // ✅ Marcar como completada
    public void marcarComoCompletada(Long id) {
        Tarea tarea = buscarPorId(id);
        tarea.setCompletada(true);
        tareaRepository.save(tarea);
    }

    // 🔄 Actualizar tarea (incluye el estado completada)
    public void actualizar(Long id, Tarea nueva) {
        Tarea existente = buscarPorId(id);
        existente.setTitulo(nueva.getTitulo());
        existente.setDescripcion(nueva.getDescripcion());
        existente.setFechaLimite(nueva.getFechaLimite());
        existente.setPrioridad(nueva.getPrioridad());
        existente.setProyecto(nueva.getProyecto());
        existente.setCompletada(nueva.isCompletada());
        tareaRepository.save(existente);
    }

    // 🔍 Buscar por ID
    public Tarea buscarPorId(Long id) {
        return tareaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Tarea no encontrada con ID: " + id));
    }

    // 👤 Asignar usuario a tarea
    public void asignarUsuario(Long tareaId, Long usuarioId) {
        Tarea tarea = buscarPorId(tareaId);
        User usuario = userRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con ID: " + usuarioId));
        tarea.setUsuario(usuario);
        tareaRepository.save(tarea);
    }

    public List<Tarea> listarTareasPorProyecto(Proyecto proyecto) {
        return tareaRepository.findByProyecto(proyecto);
    }

    // 🔍 Buscar con filtros dinámicos
    public List<Tarea> buscarTareasConFiltros(User usuario, String titulo, Boolean completada, String prioridad,
                                              Long proyectoId, LocalDate fechaInicio, LocalDate fechaFin) {

        Specification<Tarea> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(root.get("usuario"), usuario));

            if (titulo != null && !titulo.isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("titulo")), "%" + titulo.toLowerCase() + "%"));
            }

            if (completada != null) {
                predicates.add(cb.equal(root.get("completada"), completada));
            }

            if (prioridad != null && !prioridad.isEmpty()) {
                predicates.add(cb.equal(cb.lower(root.get("prioridad")), prioridad.toLowerCase()));
            }

            if (proyectoId != null) {
                predicates.add(cb.equal(root.get("proyecto").get("id"), proyectoId));
            }

            if (fechaInicio != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("fechaLimite"), fechaInicio));
            }

            if (fechaFin != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("fechaLimite"), fechaFin));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return tareaRepository.findAll(spec);
    }
}

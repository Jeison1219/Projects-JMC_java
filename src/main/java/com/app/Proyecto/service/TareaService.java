package com.app.Proyecto.service;

import com.app.Proyecto.model.Proyecto;
import com.app.Proyecto.model.Tarea;
import com.app.Proyecto.model.User;
import com.app.Proyecto.repository.TareaRepository;
import com.app.Proyecto.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
        existente.setCompletada(nueva.isCompletada()); // ✅ Importante
        tareaRepository.save(existente);
    }

    // 🔍 Buscar por ID (usado en detalles, editar, etc.)
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

}

package com.app.Proyecto.service;

import com.app.Proyecto.model.Tarea;
import com.app.Proyecto.model.User;
import com.app.Proyecto.repository.TareaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TareaService {

    private final TareaRepository tareaRepository;

    public List<Tarea> listarTareasPorUsuario(User usuario) {
        return tareaRepository.findByUsuario(usuario);
    }

    public Tarea crearTarea(Tarea tarea) {
        return tareaRepository.save(tarea);
    }

    public void eliminarTarea(Long id) {
        tareaRepository.deleteById(id);
    }

    public void marcarComoCompletada(Long id) {
        Tarea tarea = tareaRepository.findById(id).orElseThrow();
        tarea.setCompletada(true);
        tareaRepository.save(tarea);
    }

    public void actualizar(Long id, Tarea nueva) {
    Tarea existente = tareaRepository.findById(id).orElseThrow();
    existente.setTitulo(nueva.getTitulo());
    existente.setDescripcion(nueva.getDescripcion());
    existente.setFechaLimite(nueva.getFechaLimite());
    existente.setPrioridad(nueva.getPrioridad());
    existente.setProyecto(nueva.getProyecto());
    tareaRepository.save(existente);
}

public Tarea buscarPorId(Long id) {
    return tareaRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Tarea no encontrada con ID: " + id));
}

}

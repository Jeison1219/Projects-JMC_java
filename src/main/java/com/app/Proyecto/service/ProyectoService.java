package com.app.Proyecto.service;

import com.app.Proyecto.model.Proyecto;
import com.app.Proyecto.repository.ProyectoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProyectoService {

    private final ProyectoRepository proyectoRepository;

    // 🔍 Buscar todos los proyectos
    public List<Proyecto> listarTodos() {
        return proyectoRepository.findAll();
    }

    // 💾 Guardar nuevo proyecto
    public Proyecto guardar(Proyecto proyecto) {
        return proyectoRepository.save(proyecto);
    }

    // 🔍 Buscar por ID
    public Proyecto buscarPorId(Long id) {
        return proyectoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Proyecto no encontrado con ID: " + id));
    }

    // ✏️ Actualizar proyecto existente
    public void actualizar(Long id, Proyecto datosActualizados) {
        Proyecto existente = buscarPorId(id);
        existente.setNombre(datosActualizados.getNombre());
        existente.setFechaInicio(datosActualizados.getFechaInicio());
        existente.setFechaFin(datosActualizados.getFechaFin());
        existente.setMiembros(datosActualizados.getMiembros());
        proyectoRepository.save(existente);
    }

    // ❌ Eliminar proyecto
    public void eliminar(Long id) {
        proyectoRepository.deleteById(id);
    }
}

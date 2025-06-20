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

    public List<Proyecto> listarTodos() {
        return proyectoRepository.findAll();
    }

    public Proyecto guardar(Proyecto proyecto) {
        return proyectoRepository.save(proyecto);
    }

    public Proyecto buscarPorId(Long id) {
        return proyectoRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Proyecto no encontrado"));
    }

    public void eliminar(Long id) {
        proyectoRepository.deleteById(id);
    }

    public Proyecto actualizar(Long id, Proyecto datos) {
        Proyecto existente = buscarPorId(id);
        existente.setNombre(datos.getNombre());
        existente.setDescripcion(datos.getDescripcion());
        existente.setFechaInicio(datos.getFechaInicio());
        existente.setFechaFin(datos.getFechaFin());
        existente.setMiembros(datos.getMiembros());
        return proyectoRepository.save(existente);
    }
}

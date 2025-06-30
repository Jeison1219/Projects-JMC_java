package com.app.Proyecto.service;

import com.app.Proyecto.model.Proyecto;
import com.app.Proyecto.repository.ProyectoRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProyectoService {

    private final ProyectoRepository proyectoRepository;

    public List<Proyecto> listarTodos() {
        return proyectoRepository.findAll();
    }
    

    public List<Proyecto> buscarProyectos(String nombre, LocalDate fechaInicio, LocalDate fechaFin) {
        return proyectoRepository.findAll((root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (nombre != null && !nombre.isEmpty() && !nombre.equalsIgnoreCase("Todos los proyectos")) {
                predicates.add(cb.like(cb.lower(root.get("nombre")), "%" + nombre.toLowerCase() + "%"));
            }

            if (fechaInicio != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("fechaInicio"), fechaInicio));
            }

            if (fechaFin != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("fechaFin"), fechaFin));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        });
    }

    public Proyecto guardar(Proyecto proyecto) {
        return proyectoRepository.save(proyecto);
    }

    public Proyecto buscarPorId(Long id) {
        return proyectoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Proyecto no encontrado con ID: " + id));
    }

    public void actualizar(Long id, Proyecto datosActualizados) {
        Proyecto existente = buscarPorId(id);
        existente.setNombre(datosActualizados.getNombre());
        existente.setFechaInicio(datosActualizados.getFechaInicio());
        existente.setFechaFin(datosActualizados.getFechaFin());
        existente.setMiembros(datosActualizados.getMiembros());
        proyectoRepository.save(existente);
    }

    public void eliminar(Long id) {
        proyectoRepository.deleteById(id);
    }
}

package com.app.Proyecto.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import com.app.Proyecto.model.Proyecto;

public interface ProyectoRepository extends JpaRepository<Proyecto, Long>, JpaSpecificationExecutor<Proyecto> {
    // Ya puedes usar Specification para filtros dinámicos

    // Buscar proyectos que vencen hoy
    java.util.List<Proyecto> findByFechaFin(java.time.LocalDate fechaFin);
    
    // Query con join fetch para cargar miembros y tareas de una vez
    @Query("SELECT DISTINCT p FROM Proyecto p " +
           "LEFT JOIN FETCH p.miembros " +
           "LEFT JOIN FETCH p.tareas " +
           "ORDER BY p.id")
    java.util.List<Proyecto> findAllWithMiembrosAndTareas();
}


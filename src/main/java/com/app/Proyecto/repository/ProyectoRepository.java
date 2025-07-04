package com.app.Proyecto.repository;

import com.app.Proyecto.model.Proyecto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ProyectoRepository extends JpaRepository<Proyecto, Long>, JpaSpecificationExecutor<Proyecto> {
    // Ya puedes usar Specification para filtros dinámicos

    // Buscar proyectos que vencen hoy
    java.util.List<Proyecto> findByFechaFin(java.time.LocalDate fechaFin);
}


package com.app.Proyecto.repository;

import com.app.Proyecto.model.Proyecto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProyectoRepository extends JpaRepository<Proyecto, Long> {
    // Puedes agregar búsquedas por nombre o fecha si lo necesitas más adelante
}

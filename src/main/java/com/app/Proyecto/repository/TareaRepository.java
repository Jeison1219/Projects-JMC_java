package com.app.Proyecto.repository;

import com.app.Proyecto.model.Proyecto;
import com.app.Proyecto.model.Tarea;
import com.app.Proyecto.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface TareaRepository extends JpaRepository<Tarea, Long>, JpaSpecificationExecutor<Tarea> {
    List<Tarea> findByUsuario(User usuario);
    List<Tarea> findByProyecto(Proyecto proyecto);

    // Buscar tareas que vencen hoy
    List<Tarea> findByFechaLimite(java.time.LocalDate fechaLimite);

    long countByPrioridad(String prioridad);
    long countByCompletada(boolean completada); // ✅ Correcto con tu entidad
    long countByCompletadaTrue();
    long countByCompletadaFalse();

    // Métodos para filtrar por usuario (para el dashboard)
    long countByUsuarioAndPrioridad(User usuario, String prioridad);
    long countByUsuarioAndCompletada(User usuario, boolean completada);
    long countByUsuarioAndCompletadaTrue(User usuario);
    long countByUsuarioAndCompletadaFalse(User usuario);

}

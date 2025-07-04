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
  

long countByPrioridad(String prioridad);
long countByCompletada(boolean completada); // ✅ Correcto con tu entidad

long countByCompletadaTrue();
long countByCompletadaFalse();


}

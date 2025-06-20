package com.app.Proyecto.repository;

import com.app.Proyecto.model.Tarea;
import com.app.Proyecto.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TareaRepository extends JpaRepository<Tarea, Long> {
    List<Tarea> findByUsuario(User usuario);
}
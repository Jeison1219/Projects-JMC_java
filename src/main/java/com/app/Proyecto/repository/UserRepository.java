package com.app.Proyecto.repository;

import com.app.Proyecto.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByEmail(String email); // para login
    boolean existsByEmail(String email); // para validar registro único
}

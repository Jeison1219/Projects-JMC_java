package com.app.Proyecto.repository;

import com.app.Proyecto.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);      // login por email
    boolean existsByEmail(String email);           // evitar registros duplicados
}


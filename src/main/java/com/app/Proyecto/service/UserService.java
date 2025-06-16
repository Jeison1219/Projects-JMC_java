package com.app.Proyecto.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.app.Proyecto.dto.UserRegistrationDto;
import com.app.Proyecto.model.User;
import com.app.Proyecto.repository.UserRepository;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Registro desde DTO
    public void register(UserRegistrationDto userDto) {
        User user = new User();
        user.setName(userDto.getName());
        user.setEmail(userDto.getEmail());
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        user.setRole(userDto.getRole());
        userRepository.save(user);
    }

    // Para validación si el correo ya existe
    public boolean existsByEmail(String email) {
        return userRepository.findByEmail(email) != null;
    }

    // Login usando email y contraseña
    public boolean login(String email, String rawPassword) {
        User user = userRepository.findByEmail(email);
        return user != null && passwordEncoder.matches(rawPassword, user.getPassword());
    }
}

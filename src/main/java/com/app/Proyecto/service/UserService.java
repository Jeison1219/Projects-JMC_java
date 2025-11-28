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
    public boolean verificarYActualizarPassword(String email, String actual, String nueva) {
    User user = userRepository.findByEmail(email).orElseThrow();
    
    // Verificamos si la contraseña actual es correcta
    if (!passwordEncoder.matches(actual, user.getPassword())) {
        return false;
    }

    // Si es correcta, actualizamos la contraseña
    user.setPassword(passwordEncoder.encode(nueva));
    userRepository.save(user);
    return true;
}

    // Para validación si el correo ya existe
    public boolean existsByEmail(String email) {
        return userRepository.findByEmail(email).isPresent(); // ✅ usar Optional correctamente
    }
    public String obtenerNombrePorEmail(String email) {
    return userRepository.findByEmail(email)
            .map(user -> user.getName())
            .orElse("Usuario");
}
public User getByEmail(String email) {
    return userRepository.findByEmail(email).orElseThrow();
}

public void actualizarDatosUsuario(String email, User updated) {
    User user = getByEmail(email);
    user.setName(updated.getName());
    user.setEmail(updated.getEmail());
    // No cambiamos contraseña aquí por seguridad
    userRepository.save(user);
}



    // Login usando email y contraseña
    public boolean login(String email, String rawPassword) {
        return userRepository.findByEmail(email)
            .map(user -> passwordEncoder.matches(rawPassword, user.getPassword()))
            .orElse(false); // ✅ manejar ausencia de usuario
    }

    // Eliminar un usuario
    public void delete(User user) {
        userRepository.delete(user);
    }

    // Guardar un usuario
    public void save(User user) {
        userRepository.save(user);
    }
}

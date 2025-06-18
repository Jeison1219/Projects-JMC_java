package com.app.Proyecto.service;

import com.app.Proyecto.model.User;
import com.app.Proyecto.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Aquí username representa el email del formulario
        User user = userRepository.findByEmail(username)
            .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con email: " + username));

        return org.springframework.security.core.userdetails.User
            .withUsername(user.getEmail())  // email como identificador
            .password(user.getPassword())  // contraseña encriptada con BCrypt
            .roles("USER")                 // puedes usar roles dinámicos si los tienes
            .build();
    }
}

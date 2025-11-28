package com.app.Proyecto.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.app.Proyecto.model.User;
import com.app.Proyecto.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private static final Logger logger = LoggerFactory.getLogger(CustomUserDetailsService.class);

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        logger.info("Intentando cargar usuario con email: {}", username);
        
        User user = userRepository.findByEmail(username)
            .orElseThrow(() -> {
                logger.error("Usuario no encontrado con email: {}", username);
                return new UsernameNotFoundException("Usuario no encontrado con email: " + username);
            });

        logger.info("Usuario encontrado: {}, Rol: {}, Verificado: {}", user.getEmail(), user.getRole(), user.isEmailVerificado());

        // Remover prefijo "ROLE_" si existe, ya que .roles() lo agrega automáticamente
        String role = user.getRole();
        if (role.startsWith("ROLE_")) {
            role = role.substring(5); // Remover "ROLE_"
        }

        logger.info("Rol procesado como: {}", role);

        return org.springframework.security.core.userdetails.User
            .withUsername(user.getEmail())
            .password(user.getPassword())
            .roles(role)
            .build();
    }
}

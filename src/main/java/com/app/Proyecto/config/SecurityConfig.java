package com.app.Proyecto.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.app.Proyecto.service.CustomUserDetailsService;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                // Acceso libre
                .requestMatchers("/","/register", "/login", "/home", "/css/**", "/js/**", "/api/auth/**").permitAll()

                // Rutas solo para ADMIN
                .requestMatchers("/tareas/nueva", "/tareas/editar/**", "/tareas/eliminar/**").hasRole("ADMIN")
                .requestMatchers("/proyectos/nuevo", "/proyectos/editar/**", "/proyectos/eliminar/**").hasRole("ADMIN")

                // Ver tareas y proyectos accesible por todos los autenticados
                .requestMatchers("/tareas/**", "/proyectos/**").authenticated()

                // Cualquier otra requiere autenticación
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/dashboard", true)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            )
            .userDetailsService(userDetailsService); // 💡 Usa tu servicio personalizado

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}

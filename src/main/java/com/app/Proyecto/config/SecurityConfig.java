package com.app.Proyecto.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/", 
                    "/register",
                    "/register/**",
                    "/registro",
                    "/registro/**",
                    "/login", 
                    "/home", 
                    "/recuperar", 
                    "/enviar-codigo", 
                    "/verificar-codigo", 
                    "/cambiar-password", 
                    "/verificar-registro-codigo", 
                    "/css/**", 
                    "/js/**", 
                    "/images/**",
                    "/api/auth/**"
                ).permitAll()

                .requestMatchers("/tareas/nueva", "/tareas/editar/**", "/tareas/eliminar/**").hasRole("PROJECT_MANAGER")
                .requestMatchers("/proyectos/nuevo", "/proyectos/editar/**", "/proyectos/eliminar/**").hasRole("PROJECT_MANAGER")

                .requestMatchers("/tareas/**", "/proyectos/**").authenticated()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/dashboard", true)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/home")
                .permitAll()
            )
            .exceptionHandling(ex -> ex
                .accessDeniedPage("/error/403")
            );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

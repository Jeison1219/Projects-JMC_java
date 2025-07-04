package com.app.Proyecto.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(unique = true, nullable = false)
    private String email;

    private String password;

    private String role;

    // Campos para recuperación de contraseña
    private String codigoVerificacion;
    private LocalDateTime codigoExpiracion;
    private boolean codigoUsado = false;

    private int intentosCodigo = 0;
    private LocalDateTime ultimoIntento;
    private String ipSolicitud;
}

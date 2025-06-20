package com.app.Proyecto.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Entity
@Getter
@Setter
public class Proyecto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;

    @Column(length = 1000)
    private String descripcion;

    private LocalDate fechaInicio;
    private LocalDate fechaFin;

    /**
     * Este campo guarda una lista de emails separados por comas, como:
     * "usuario1@email.com, usuario2@email.com"
     */
    @Lob
    private String miembros;

    // Relación con tareas
    @OneToMany(mappedBy = "proyecto", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Tarea> tareas;
}

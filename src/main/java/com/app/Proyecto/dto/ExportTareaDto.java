package com.app.Proyecto.dto;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

import lombok.Data;

@Data
public class ExportTareaDto {
    private Long id;
    private String titulo;
    private String descripcion;
    private LocalDate fechaLimite;
    private String prioridad;
    
    // Puede venir como 'completada' o 'estado'
    @JsonProperty("completada")
    private boolean completada;
    
    @JsonProperty("estado")
    private String estado; // Para mapear el campo estado si viene en el JSON
    
    private Long proyectoId;
    private String usuarioEmail; // asignado por email
    
    // Mapear fechaVencimiento a fechaLimite si viene en el JSON
    @JsonSetter("fechaVencimiento")
    public void setFechaVencimiento(LocalDate fechaVencimiento) {
        this.fechaLimite = fechaVencimiento;
    }
    
    // Mapear asignadoA a usuarioEmail si viene en el JSON
    @JsonSetter("asignadoA")
    public void setAsignadoA(String asignadoA) {
        this.usuarioEmail = asignadoA;
    }
}

package com.app.Proyecto.dto;

import java.time.LocalDate;

import lombok.Data;

@Data
public class ExportTareaDto {
    private Long id;
    private String titulo;
    private String descripcion;
    private LocalDate fechaLimite;
    private String prioridad;
    private boolean completada;
    private Long proyectoId;
    private String usuarioEmail; // asignado por email
}

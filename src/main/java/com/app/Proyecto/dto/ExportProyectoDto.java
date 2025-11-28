package com.app.Proyecto.dto;

import java.time.LocalDate;
import java.util.List;

import lombok.Data;

@Data
public class ExportProyectoDto {
    private Long id;
    private String nombre;
    private String descripcion;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private List<String> miembrosEmails; // lista de emails de miembros
    private List<ExportTareaDto> tareas;
}

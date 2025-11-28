package com.app.Proyecto.dto;

import lombok.Data;

@Data
public class ExportUserDto {
    private Long id;
    private String name;
    private String email;
    private String role;
}

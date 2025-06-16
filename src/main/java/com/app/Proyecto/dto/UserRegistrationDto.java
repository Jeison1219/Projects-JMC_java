package com.app.Proyecto.dto;

import lombok.Data;

@Data
public class UserRegistrationDto {

    private String name;
    private String email;
    private String password;
    private String confirmPassword;
    private String role;
}

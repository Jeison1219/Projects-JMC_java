package com.app.Proyecto.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UserRegistrationDto {

    @NotBlank(message = "El nombre es obligatorio")
    private String name;

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo no es válido")
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    @Pattern(
        regexp = "^(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,}$",
        message = "Debe tener al menos 8 caracteres, una mayúscula, un número y un símbolo"
    )
    private String password;

    @NotBlank(message = "Confirma tu contraseña")
    private String confirmPassword;

    @NotBlank(message = "Selecciona un rol")
    private String role;
}

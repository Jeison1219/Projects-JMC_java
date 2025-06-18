package com.app.Proyecto.controller;

import com.app.Proyecto.dto.UserRegistrationDto;
import com.app.Proyecto.service.UserService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    // 👉 Vista de registro (formulario)
    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("userDto", new UserRegistrationDto());
        return "register";
    }

    // 👉 Registro desde formulario web (Thymeleaf)
    @PostMapping("/register")
    public String registerWeb(
        @ModelAttribute("userDto") @Valid UserRegistrationDto userDto,
        BindingResult result,
        Model model
    ) {
        if (!userDto.getPassword().equals(userDto.getConfirmPassword())) {
            result.rejectValue("confirmPassword", null, "Las contraseñas no coinciden");
        }

        if (userService.existsByEmail(userDto.getEmail())) {
            result.rejectValue("email", null, "Este correo ya está registrado");
        }

        if (result.hasErrors()) {
            return "register";
        }

        userService.register(userDto);
        model.addAttribute("success", "Usuario registrado con éxito");
        return "login"; // redirige a vista de login después de registrar
    }

    // 👉 API para registrar desde Postman o frontend SPA
    @PostMapping("/api/auth/register")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> registerAPI(@RequestBody @Valid UserRegistrationDto userDto) {
        Map<String, Object> response = new HashMap<>();

        if (!userDto.getPassword().equals(userDto.getConfirmPassword())) {
            response.put("success", false);
            response.put("message", "Las contraseñas no coinciden");
            return ResponseEntity.badRequest().body(response);
        }

        if (userService.existsByEmail(userDto.getEmail())) {
            response.put("success", false);
            response.put("message", "Correo ya registrado");
            return ResponseEntity.badRequest().body(response);
        }

        userService.register(userDto);
        response.put("success", true);
        response.put("message", "Usuario registrado correctamente");
        return ResponseEntity.ok(response);
    }

    // 👉 Vista del login (Spring Security se encarga del POST /login)
    @GetMapping("/login")
    public String loginForm() {
        return "login";
    }

    // ❌ Eliminado el login manual /api/auth/login porque usamos Spring Security

    // 👉 Dashboard tras login exitoso
    @GetMapping("/dashboard")
    public String dashboard(Model model, Principal principal) {
        model.addAttribute("username", principal.getName());
        return "dashboard"; // Thymeleaf: dashboard.html en /templates
    }
}

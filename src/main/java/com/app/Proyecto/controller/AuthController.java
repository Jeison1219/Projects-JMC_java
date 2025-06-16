package com.app.Proyecto.controller;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import com.app.Proyecto.model.User;
import com.app.Proyecto.repository.UserRepository;
import com.app.Proyecto.service.UserService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final UserRepository userRepository;

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String registerWeb(User user, Model model) {
        userService.register(user);
        model.addAttribute("success", "Usuario registrado con éxito");
        return "login";
    }

    @PostMapping("/api/auth/register")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> registerAPI(@RequestBody User user) {
        userService.register(user);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Usuario registrado correctamente");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/api/auth/login")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> loginAPI(@RequestBody User user) {
        boolean success = userService.login(user.getUsername(), user.getPassword());
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("message", success ? "Login exitoso" : "Credenciales incorrectas");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/login")
    public String loginForm() {
        return "login";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, Principal principal) {
        model.addAttribute("username", principal.getName());
        return "dashboard";
    }
}

package com.app.Proyecto.controller;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.app.Proyecto.dto.UserRegistrationDto;
import com.app.Proyecto.model.User;
import com.app.Proyecto.service.AuthService;
import com.app.Proyecto.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final AuthService authService;

    // ========= REGISTRO =========

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("userDto", new UserRegistrationDto());
        return "register";
    }
    

    @PostMapping("/register")
    public String registerWeb(
            @ModelAttribute("userDto") @Valid UserRegistrationDto userDto,
            BindingResult result,
            HttpServletRequest request,
            Model model) {

        if (!userDto.getPassword().equals(userDto.getConfirmPassword())) {
            result.rejectValue("confirmPassword", null, "Las contraseñas no coinciden");
        }

        if (userService.existsByEmail(userDto.getEmail())) {
            result.rejectValue("email", null, "Este correo ya está registrado");
        }

        if (result.hasErrors()) {
            model.addAttribute("hasErrors", true);
            model.addAttribute("userDto", userDto);
            return "register";
        }

        // Crear usuario de verdad y enviar código de verificación
        String ip = request.getRemoteAddr();
        String rol = "ROLE_" + userDto.getRole().toUpperCase();
        boolean codigoEnviado = authService.enviarCodigoRegistro(
            userDto.getEmail(), 
            userDto.getName(), 
            userDto.getPassword(),
            rol, 
            ip
        );
        
        if (codigoEnviado) {
            model.addAttribute("email", userDto.getEmail());
            return "verificar-registro"; // Nueva página de verificación
        } else {
            model.addAttribute("hasErrors", true);
            model.addAttribute("error", "Error al enviar el código. Intenta de nuevo.");
            model.addAttribute("userDto", userDto);
            return "register";
        }
    }

    @PostMapping("/verificar-registro-codigo")
    public String verificarRegistroCodigo(
            @RequestParam String email,
            @RequestParam String codigo,
            HttpServletRequest request,
            Model model) {

        String ip = request.getRemoteAddr();
        boolean codigoValido = authService.verificarCodigoRegistro(email, codigo, ip);

        if (codigoValido) {
            // ✅ El usuario YA FUE CREADO en la BD por authService.verificarCodigoRegistro()
            return "redirect:/login?registroExitoso";
        } else {
            model.addAttribute("error", "Código inválido, expirado o IP diferente");
            model.addAttribute("email", email);
            return "verificar-registro";
        }
    }

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

    // ========= LOGIN Y PERFIL =========

    @GetMapping("/login")
    public String loginForm() {
        return "login";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, Principal principal) {
        String email = principal.getName();
        String nombre = userService.obtenerNombrePorEmail(email);
        model.addAttribute("username", nombre);
        return "dashboard";
    }

    @GetMapping("/perfil")
    public String perfil(Model model, Principal principal) {
        String email = principal.getName();
        var user = userService.getByEmail(email);
        model.addAttribute("user", user);
        return "perfil";
    }

    @PostMapping("/perfil")
    public String actualizarPerfil(@ModelAttribute("user") User updatedUser, Principal principal, Model model) {
        String email = principal.getName();
        userService.actualizarDatosUsuario(email, updatedUser);
        model.addAttribute("user", updatedUser);
        model.addAttribute("success", "Perfil actualizado correctamente.");
        return "perfil";
    }

    @PostMapping("/perfil/cambiar-password")
    public String cambiarPasswordDesdePerfil(@RequestParam String actual,
                                             @RequestParam String nueva,
                                             @RequestParam String confirm,
                                             Principal principal,
                                             Model model) {
        String email = principal.getName();

        if (!nueva.equals(confirm)) {
            model.addAttribute("errorPassword", "Las contraseñas no coinciden.");
            model.addAttribute("user", userService.getByEmail(email));
            return "perfil";
        }

        boolean ok = userService.verificarYActualizarPassword(email, actual, nueva);

        if (!ok) {
            model.addAttribute("errorPassword", "La contraseña actual es incorrecta.");
        } else {
            model.addAttribute("successPassword", "Contraseña actualizada con éxito.");
        }

        model.addAttribute("user", userService.getByEmail(email));
        return "perfil";
    }

    // ========= RECUPERACIÓN DE CONTRASEÑA =========

    @GetMapping("/recuperar")
    public String mostrarFormularioRecuperar() {
        return "solicitar-codigo";
    }

    @PostMapping("/enviar-codigo")
    public String enviarCodigo(@RequestParam String email, HttpServletRequest request, Model model) {
        String ip = request.getRemoteAddr();
        boolean enviado = authService.enviarCodigo(email, ip);
        if (enviado) {
            model.addAttribute("email", email);
            return "verificar-codigo";
        } else {
            model.addAttribute("error", "Correo no registrado");
            return "solicitar-codigo";
        }
    }

    @PostMapping("/verificar-codigo")
    public String verificarCodigo(@RequestParam String email,
                                  @RequestParam String codigo,
                                  HttpServletRequest request,
                                  Model model) {
        String ip = request.getRemoteAddr();
        boolean valido = authService.verificarCodigo(email, codigo, ip);
        if (valido) {
            model.addAttribute("email", email);
            return "cambiar-password";
        } else {
            model.addAttribute("error", "Código inválido, expirado o IP diferente");
            model.addAttribute("email", email);
            return "verificar-codigo";
        }
    }

    @PostMapping("/cambiar-password")
    public String cambiarPassword(@RequestParam String email,
                                  @RequestParam String password,
                                  @RequestParam String confirm,
                                  Model model) {
        if (!password.equals(confirm)) {
            model.addAttribute("error", "Las contraseñas no coinciden");
            model.addAttribute("email", email);
            return "cambiar-password";
        }

        authService.cambiarPassword(email, password);
        return "redirect:/login?recuperacionExitosa";
    }
}

package com.app.Proyecto.advice;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.security.access.AccessDeniedException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public String handleMethodNotAllowed(HttpServletRequest request, Exception ex, Model model) {
        model.addAttribute("mensaje", "Método no permitido o acción no autorizada.");
        return "error/permisos";
    }

    @ExceptionHandler(AccessDeniedException.class)
    public String handleAccessDenied(Exception ex, Model model) {
        model.addAttribute("mensaje", "No tienes permisos para realizar esta acción.");
        return "error/permisos";
    }
}

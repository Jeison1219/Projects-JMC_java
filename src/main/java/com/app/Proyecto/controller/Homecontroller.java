package com.app.Proyecto.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class Homecontroller {

    @GetMapping("/home")
    public String home() {
        return "home"; // Busca home.html en /resources/templates/
    }
}

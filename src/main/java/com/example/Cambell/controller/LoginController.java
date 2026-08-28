package com.example.Cambell.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {

    @GetMapping("/login")
    public String mostrarLogin(@RequestParam(required = false) String error,
                                @RequestParam(required = false) String logout,
                                Model model) {
        if (error != null) {
            model.addAttribute("error", "Correo o contraseña incorrectos. Intenta de nuevo.");
        }
        if (logout != null) {
            model.addAttribute("mensaje", "Sesión cerrada correctamente.");
        }
        return "login";
    }
}
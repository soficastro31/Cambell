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
                                @RequestParam(required = false) String registroExitoso,
                                @RequestParam(required = false) String restablecida,
                                Model model) {
        if (error != null) {
            model.addAttribute("error", "Correo o contraseña incorrectos. Intenta de nuevo.");
        }
        if (logout != null) {
            model.addAttribute("mensaje", "Sesión cerrada correctamente.");
        }
        if (registroExitoso != null) {
            model.addAttribute("mensaje", "Registro exitoso. Ya puedes iniciar sesión.");
        }
        if (restablecida != null) {
            model.addAttribute("restablecida", true);
        }
        return "login";
    }
}
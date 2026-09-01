package com.example.Cambell.controller;

import com.example.Cambell.service.RecuperacionPasswordService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class RecuperacionPasswordController {

    @Autowired
    private RecuperacionPasswordService recuperacionService;

    // HU-U03: formulario para solicitar el enlace de restablecimiento
    @GetMapping("/recuperar-password")
    public String formularioSolicitud() {
        return "recuperar-password";
    }

    // HU-U03 Esc.1/2: procesa la solicitud (sin revelar si el correo está registrado)
    @PostMapping("/recuperar-password")
    public String solicitarEnlace(@RequestParam String correo,
                                  HttpServletRequest request,
                                  RedirectAttributes redirectAttributes) {
        String baseUrl = request.getRequestURL().toString().replace("/recuperar-password", "");
        if (recuperacionService.existeCorreo(correo)) {
            recuperacionService.generarTokenYEnviarEnlace(correo, baseUrl);
        }
        redirectAttributes.addFlashAttribute("mensaje",
                "Si el correo está registrado, recibirás un enlace para restablecer tu contraseña.");
        return "redirect:/recuperar-password?enviado";
    }

    // HU-U03: mostrar el formulario para ingresar la nueva contraseña
    @GetMapping("/restablecer-password")
    public String formularioRestablecer(@RequestParam String token, Model model) {
        if (!recuperacionService.tokenValido(token)) {
            model.addAttribute("error", "El enlace es inválido o ha expirado. Solicita uno nuevo.");
            return "restablecer-password";
        }
        model.addAttribute("token", token);
        return "restablecer-password";
    }

    // HU-U03: guardar la nueva contraseña
    @PostMapping("/restablecer-password")
    public String restablecer(@RequestParam String token,
                              @RequestParam String password,
                              @RequestParam String confirmarPassword,
                              RedirectAttributes redirectAttributes) {
        if (password == null || confirmarPassword == null || !password.equals(confirmarPassword)) {
            redirectAttributes.addFlashAttribute("error", "Las contraseñas no coinciden.");
            return "redirect:/restablecer-password?token=" + token;
        }
        if (password == null || password.length() < 8) {
            redirectAttributes.addFlashAttribute("error", "La contraseña debe tener al menos 8 caracteres.");
            return "redirect:/restablecer-password?token=" + token;
        }
        if (!recuperacionService.restablecerPassword(token, password)) {
            redirectAttributes.addFlashAttribute("error", "El enlace es inválido o ha expirado.");
            return "redirect:/restablecer-password?token=" + token;
        }
        redirectAttributes.addFlashAttribute("mensaje", "Contraseña restablecida. Ya puedes iniciar sesión.");
        return "redirect:/login?restablecida";
    }
}
package com.example.Cambell.controller;

import com.example.Cambell.service.MensajeService;
import com.example.Cambell.security.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/solicitudes")
public class MensajeController {

    @Autowired
    private MensajeService mensajeService;

    // HU-C13 / HU-T18: ver el hilo de mensajes de una solicitud
    @GetMapping("/{id}/mensajes")
    public String verHilo(@PathVariable Long id,
                          @AuthenticationPrincipal CustomUserDetails userDetails,
                          Model model) {
        if (!mensajeService.puedeParticipar(id, userDetails.getUsuario())) {
            return "redirect:/";
        }
        mensajeService.marcarLeidos(id, userDetails.getUsuario());
        model.addAttribute("solicitud", mensajeService.buscarSolicitud(id));
        model.addAttribute("mensajes", mensajeService.listarPorSolicitud(id));
        model.addAttribute("usuarioActual", userDetails.getUsuario());
        return "mensajes-solicitud";
    }

    // HU-C13 / HU-T18: enviar un mensaje en la solicitud
    @PostMapping("/{id}/mensajes")
    public String enviar(@PathVariable Long id,
                         @RequestParam("contenido") String contenido,
                         @AuthenticationPrincipal CustomUserDetails userDetails,
                         RedirectAttributes redirectAttributes) {
        if (contenido == null || contenido.isBlank()) {
            redirectAttributes.addFlashAttribute("error", "El mensaje no puede estar vacío.");
            return "redirect:/solicitudes/" + id + "/mensajes";
        }
        try {
            mensajeService.enviar(id, userDetails.getUsuario(), contenido.trim());
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/solicitudes/" + id + "/mensajes";
    }
}

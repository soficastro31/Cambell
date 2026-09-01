package com.example.Cambell.controller;

import com.example.Cambell.model.Suscripcion;
import com.example.Cambell.security.CustomUserDetails;
import com.example.Cambell.service.MensajeService;
import com.example.Cambell.service.SuscripcionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/suscripcion")
public class SuscripcionController {

    @Autowired
    private SuscripcionService suscripcionService;

    @Autowired
    private MensajeService mensajeService;

    // HU-T17: consultar los beneficios de la suscripción y su vigencia
    @GetMapping
    public String miSuscripcion(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        model.addAttribute("suscripcion", suscripcionService.deTrabajador(userDetails.getUsuario()).orElse(null));
        model.addAttribute("noLeidos", mensajeService.contarNoLeidos(userDetails.getUsuario()));
        return "suscripcion";
    }

    // HU-T15 + HU-S16: contratar el plan premium (el pago se confirma al instante)
    @PostMapping("/contratar")
    public String contratar(@AuthenticationPrincipal CustomUserDetails userDetails,
                            RedirectAttributes redirectAttributes) {
        suscripcionService.contratar(userDetails.getUsuario());
        redirectAttributes.addFlashAttribute("exito", "Suscripción premium activada correctamente.");
        return "redirect:/suscripcion";
    }

    // HU-T16: cancelar la suscripción (detiene la renovación automática)
    @PostMapping("/cancelar")
    public String cancelar(@AuthenticationPrincipal CustomUserDetails userDetails,
                           RedirectAttributes redirectAttributes) {
        try {
            suscripcionService.cancelar(userDetails.getUsuario());
            redirectAttributes.addFlashAttribute("exito",
                    "Suscripción cancelada. Los beneficios siguen vigentes hasta el fin del periodo pagado.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/suscripcion";
    }
}
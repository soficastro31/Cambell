package com.example.Cambell.controller;

import com.example.Cambell.security.CustomUserDetails;
import com.example.Cambell.service.NotificacionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/notificaciones")
public class NotificacionController {

    @Autowired
    private NotificacionService notificacionService;

    // Bandeja de notificaciones del usuario. Al abrirla se marcan como leídas.
    @GetMapping
    public String bandeja(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        model.addAttribute("notificaciones", notificacionService.listar(userDetails.getUsuario()));
        notificacionService.marcarLeidas(userDetails.getUsuario());
        model.addAttribute("noLeidos", 0L);
        return "notificaciones";
    }
}

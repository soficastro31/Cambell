package com.example.Cambell.controller;

import com.example.Cambell.model.EstadoVerificacion;
import com.example.Cambell.model.Solicitud;
import com.example.Cambell.security.CustomUserDetails;
import com.example.Cambell.service.SolicitudService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/solicitudes")
public class SolicitudController {

    @Autowired
    private SolicitudService solicitudService;

    // Formulario para que el CLIENTE cree una solicitud
    @GetMapping("/nueva")
    public String mostrarFormulario(Model model) {
        model.addAttribute("solicitud", new Solicitud());
        return "solicitud-nueva";
    }

    @PostMapping("/nueva")
    public String crear(@ModelAttribute Solicitud solicitud,
                         @AuthenticationPrincipal CustomUserDetails userDetails) {
        solicitud.setCliente(userDetails.getUsuario());
        solicitudService.crear(solicitud);
        return "redirect:/solicitudes/mis-solicitudes";
    }

    // Cliente ve sus propias solicitudes
    @GetMapping("/mis-solicitudes")
    public String misSolicitudes(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        model.addAttribute("solicitudes", solicitudService.listarPorCliente(userDetails.getUsuario()));
        return "mis-solicitudes";
    }

@GetMapping("/disponibles")
public String disponibles(@AuthenticationPrincipal CustomUserDetails userDetails,
                           @RequestParam(required = false) String zona,
                           Model model) {
    if (userDetails.getUsuario().getEstadoVerificacion() != EstadoVerificacion.APROBADO) {
        return "verificacion-pendiente";
    }
    model.addAttribute("solicitudes", solicitudService.listarPorZonaDisponibles(zona));
    return "solicitudes-disponibles";
}
    // Trabajador acepta una solicitud
    @PostMapping("/{id}/aceptar")
    public String aceptar(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails userDetails) {
        solicitudService.aceptar(id, userDetails.getUsuario());
        return "redirect:/solicitudes/disponibles";
    }

    // Trabajador rechaza una solicitud
    @PostMapping("/{id}/rechazar")
    public String rechazar(@PathVariable Long id) {
        solicitudService.rechazar(id);
        return "redirect:/solicitudes/disponibles";
    }

    // Cliente cancela su solicitud
    @PostMapping("/{id}/cancelar")
    public String cancelar(@PathVariable Long id) {
        solicitudService.cancelar(id);
        return "redirect:/solicitudes/mis-solicitudes";
    }
}
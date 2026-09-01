package com.example.Cambell.controller;

import com.example.Cambell.model.ReporteSeguridad;
import com.example.Cambell.model.Solicitud;
import com.example.Cambell.model.Usuario;
import com.example.Cambell.service.MensajeService;
import com.example.Cambell.service.ReporteSeguridadService;
import com.example.Cambell.service.SolicitudService;
import com.example.Cambell.security.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ReporteController {

    @Autowired
    private ReporteSeguridadService reporteService;

    @Autowired
    private SolicitudService solicitudService;

    @Autowired
    private MensajeService mensajeService;

    // HU-C12 / HU-T19: formulario para reportar a la otra parte de un servicio
    @GetMapping("/solicitudes/{id}/reportar")
    public String formulario(@PathVariable Long id,
                             @AuthenticationPrincipal CustomUserDetails userDetails,
                             Model model) {
        Solicitud s = solicitudService.buscar(id);
        Usuario actual = userDetails.getUsuario();
        boolean esCliente = s.getCliente().getId().equals(actual.getId());
        boolean esTrabajador = s.getTrabajador() != null && s.getTrabajador().getId().equals(actual.getId());
        if (!esCliente && !esTrabajador) {
            return "redirect:/";
        }
        Usuario reportado = esCliente ? s.getTrabajador() : s.getCliente();
        if (reportado == null) {
            model.addAttribute("errorReporte", "No hay una contraparte para reportar en esta solicitud.");
        }
        model.addAttribute("solicitud", s);
        model.addAttribute("reportado", reportado);
        model.addAttribute("noLeidos", mensajeService.contarNoLeidos(actual));
        return "reportar";
    }

    @PostMapping("/solicitudes/{id}/reportar")
    public String enviar(@PathVariable Long id,
                         @RequestParam String motivo,
                         @AuthenticationPrincipal CustomUserDetails userDetails,
                         RedirectAttributes redirectAttributes) {
        Solicitud s = solicitudService.buscar(id);
        Usuario actual = userDetails.getUsuario();
        boolean esCliente = s.getCliente().getId().equals(actual.getId());
        boolean esTrabajador = s.getTrabajador() != null && s.getTrabajador().getId().equals(actual.getId());
        if (!esCliente && !esTrabajador) {
            return "redirect:/";
        }
        Usuario reportado = esCliente ? s.getTrabajador() : s.getCliente();
        if (reportado == null) {
            return "redirect:/";
        }
        reporteService.crear(actual, reportado, motivo);
        redirectAttributes.addFlashAttribute("ok", "Tu reporte fue enviado. El administrador lo revisará.");
        String destino = esCliente ? "/solicitudes/mis-solicitudes" : "/solicitudes/mis-trabajos";
        return "redirect:" + destino;
    }
}

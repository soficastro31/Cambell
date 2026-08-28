package com.example.Cambell.controller;

import com.example.Cambell.model.EstadoVerificacion;
import com.example.Cambell.model.Rol;
import com.example.Cambell.model.Usuario;
import com.example.Cambell.service.NotificacionService;
import com.example.Cambell.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private NotificacionService notificacionService;

    @GetMapping("/verificaciones")
    public String pendientes(Model model) {
        model.addAttribute("trabajadores",
            usuarioService.listarPorRolYEstado(Rol.TRABAJADOR, EstadoVerificacion.PENDIENTE));
        return "admin-verificaciones";
    }

    @PostMapping("/verificaciones/{id}/aprobar")
    public String aprobar(@PathVariable Long id) {
        Usuario u = usuarioService.cambiarEstadoVerificacion(id, EstadoVerificacion.APROBADO);
        // HU-S18: notificar el resultado de la verificación
        notificacionService.crear(u, "VERIFICACION",
                "Tu verificación fue APROBADA. Ya puedes ofrecer y aceptar trabajos.", null);
        return "redirect:/admin/verificaciones";
    }

    @PostMapping("/verificaciones/{id}/rechazar")
    public String rechazar(@PathVariable Long id) {
        Usuario u = usuarioService.cambiarEstadoVerificacion(id, EstadoVerificacion.RECHAZADO);
        // HU-S18: notificar el resultado de la verificación
        notificacionService.crear(u, "VERIFICACION",
                "Tu verificación fue RECHAZADA. Revisa tus documentos e inténtalo de nuevo.", null);
        return "redirect:/admin/verificaciones";
    }
}
package com.example.Cambell.controller;

import com.example.Cambell.model.*;
import com.example.Cambell.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private NotificacionService notificacionService;

    @Autowired
    private AnuncioService anuncioService;

    @Autowired
    private AdminService adminService;

    @Autowired
    private ReporteSeguridadService reporteService;

    @Autowired
    private ReembolsoService reembolsoService;

    @Autowired
    private PagoService pagoService;

    // Verificaciones pendientes (HU-A01/A02/A03)
    @GetMapping("/verificaciones")
    public String pendientes(Model model) {
        model.addAttribute("trabajadores",
            usuarioService.listarPorRolYEstado(Rol.TRABAJADOR, EstadoVerificacion.PENDIENTE));
        model.addAttribute("seccion", "verificaciones");
        return "admin-verificaciones";
    }

    @PostMapping("/verificaciones/{id}/aprobar")
    public String aprobar(@PathVariable Long id) {
        Usuario u = usuarioService.cambiarEstadoVerificacion(id, EstadoVerificacion.APROBADO);
        notificacionService.crear(u, "VERIFICACION",
                "Tu verificación fue APROBADA. Ya puedes ofrecer y aceptar trabajos.", null);
        return "redirect:/admin/verificaciones";
    }

    @PostMapping("/verificaciones/{id}/rechazar")
    public String rechazar(@PathVariable Long id) {
        Usuario u = usuarioService.cambiarEstadoVerificacion(id, EstadoVerificacion.RECHAZADO);
        notificacionService.crear(u, "VERIFICACION",
                "Tu verificación fue RECHAZADA. Revisa tus documentos e inténtalo de nuevo.", null);
        return "redirect:/admin/verificaciones";
    }

    // HU-A04: gestionar usuarios (listar, bloquear, reactivar)
    @GetMapping("/usuarios")
    public String usuarios(Model model) {
        model.addAttribute("seccion", "usuarios");
        model.addAttribute("clientes", usuarioService.listarPorRol(Rol.CLIENTE));
        model.addAttribute("trabajadores", usuarioService.listarPorRol(Rol.TRABAJADOR));
        model.addAttribute("aliados", usuarioService.listarPorRol(Rol.ALIADO_COMERCIAL));
        return "admin-usuarios";
    }

    @PostMapping("/usuarios/{id}/bloquear")
    public String bloquear(@PathVariable Long id) {
        usuarioService.bloquear(id);
        return "redirect:/admin/usuarios";
    }

    @PostMapping("/usuarios/{id}/reactivar")
    public String reactivar(@PathVariable Long id) {
        usuarioService.reactivar(id);
        return "redirect:/admin/usuarios";
    }

    // HU-A05/A06: revisar reportes de seguridad y bloquear cuentas
    @GetMapping("/reportes")
    public String reportes(Model model) {
        model.addAttribute("seccion", "reportes");
        model.addAttribute("reportes", reporteService.listarEnRevision());
        return "admin-reportes";
    }

    @PostMapping("/reportes/{id}/bloquear")
    public String bloquearReportado(@PathVariable Long id) {
        reporteService.bloquearReportado(id);
        return "redirect:/admin/reportes";
    }

    @PostMapping("/reportes/{id}/descartar")
    public String descartar(@PathVariable Long id) {
        reporteService.descartar(id);
        return "redirect:/admin/reportes";
    }

    // HU-A07/A08: aprobar o rechazar anuncios
    @GetMapping("/anuncios")
    public String anuncios(Model model) {
        model.addAttribute("seccion", "anuncios");
        model.addAttribute("pendientes", anuncioService.listarPorEstado(Anuncio.EstadoAnuncio.PENDIENTE));
        model.addAttribute("publicados", anuncioService.listarPublicados());
        return "admin-anuncios";
    }

    @PostMapping("/anuncios/{id}/aprobar")
    public String aprobarAnuncio(@PathVariable Long id) {
        anuncioService.aprobar(id);
        return "redirect:/admin/anuncios";
    }

    @PostMapping("/anuncios/{id}/rechazar")
    public String rechazarAnuncio(@PathVariable Long id, @RequestParam(required = false) String motivo) {
        anuncioService.rechazar(id, motivo == null || motivo.isBlank() ? "Contenido no apropiado" : motivo);
        return "redirect:/admin/anuncios";
    }

    // HU-A09: reportes y estadísticas de operación
    @GetMapping("/estadisticas")
    public String estadisticas(Model model) {
        model.addAttribute("seccion", "estadisticas");
        model.addAttribute("stats", adminService.estadisticas());
        return "admin-estadisticas";
    }

    // HU-A11: historial de actividad de un usuario
    @GetMapping("/historial/{id}")
    public String historial(@PathVariable Long id, Model model) {
        Usuario usuario = usuarioService.buscarPorId(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        model.addAttribute("seccion", "usuarios");
        model.addAttribute("usuario", usuario);
        model.addAttribute("actividad", adminService.historialActividad(usuario));
        return "admin-historial";
    }

    // HU-A10: listar pagos completados y reembolsos realizados
    @GetMapping("/reembolsos")
    public String reembolsos(Model model) {
        model.addAttribute("seccion", "reembolsos");
        model.addAttribute("pagos", pagoService.pagosCompletados());
        java.util.Set<Long> reembolsados = new java.util.HashSet<>();
        for (var r : reembolsoService.listarTodos()) {
            reembolsados.add(r.getPago().getId());
        }
        model.addAttribute("reembolsados", reembolsados);
        model.addAttribute("reembolsos", reembolsoService.listarTodos());
        return "admin-reembolsos";
    }

    // HU-A10: iniciar un reembolso de una transacción con disputa justificada
    @PostMapping("/reembolsos/{id}/iniciar")
    public String iniciarReembolso(@PathVariable Long id,
                                   @RequestParam(required = false) String motivo,
                                   org.springframework.web.servlet.mvc.support.RedirectAttributes ra) {
        try {
            reembolsoService.reembolsar(id, motivo);
            ra.addFlashAttribute("ok", "Reembolso iniciado correctamente.");
        } catch (RuntimeException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/reembolsos";
    }
}

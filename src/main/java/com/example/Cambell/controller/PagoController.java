package com.example.Cambell.controller;

import com.example.Cambell.model.MetodoPago;
import com.example.Cambell.model.Pago;
import com.example.Cambell.service.MensajeService;
import com.example.Cambell.service.PagoService;
import com.example.Cambell.security.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/pagos")
public class PagoController {

    @Autowired
    private PagoService pagoService;

    @Autowired
    private MensajeService mensajeService;

    // HU-C09: listar y registrar métodos de pago
    @GetMapping("/metodos")
    public String metodos(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        model.addAttribute("metodos", pagoService.listarMetodos(userDetails.getUsuario()));
        model.addAttribute("tipos", MetodoPago.TipoPago.values());
        model.addAttribute("noLeidos", mensajeService.contarNoLeidos(userDetails.getUsuario()));
        return "metodos-pago";
    }

    @PostMapping("/metodos")
    public String registrarMetodo(@RequestParam String tipo,
                                  @RequestParam String titular,
                                  @RequestParam String numero,
                                  @AuthenticationPrincipal CustomUserDetails userDetails,
                                  RedirectAttributes redirectAttributes) {
        try {
            pagoService.registrarMetodo(userDetails.getUsuario(),
                    MetodoPago.TipoPago.valueOf(tipo), titular, numero);
            redirectAttributes.addFlashAttribute("ok", "Método de pago registrado.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "No se pudo registrar el método de pago.");
        }
        return "redirect:/pagos/metodos";
    }

    // HU-C10: ver el detalle del pago de una solicitud completada
    @GetMapping("/solicitud/{id}")
    public String verPago(@PathVariable Long id,
                          @AuthenticationPrincipal CustomUserDetails userDetails,
                          Model model) {
        var solicitud = pagoService.buscarSolicitud(id);
        if (!solicitud.getCliente().getId().equals(userDetails.getUsuario().getId())) {
            return "redirect:/";
        }
        var pago = pagoService.buscarPagoDeSolicitud(id).orElse(null);
        model.addAttribute("solicitud", solicitud);
        model.addAttribute("metodos", pagoService.listarMetodos(userDetails.getUsuario()));
        model.addAttribute("noLeidos", mensajeService.contarNoLeidos(userDetails.getUsuario()));
        if (pago != null) {
            model.addAttribute("pago", pago);
            return "pago-solicitud";
        }
        // Aún sin pago registrado: permitir pagar
        return "pago-solicitud";
    }

    // HU-C10 + HU-S08/S12: realizar el pago
    @PostMapping("/solicitud/{id}/realizar")
    public String realizarPago(@PathVariable Long id,
                               @RequestParam(required = false) Long metodoId,
                               @AuthenticationPrincipal CustomUserDetails userDetails,
                               RedirectAttributes redirectAttributes) {
        var solicitud = pagoService.buscarSolicitud(id);
        if (!solicitud.getCliente().getId().equals(userDetails.getUsuario().getId())) {
            return "redirect:/";
        }
        try {
            Pago pago = pagoService.pagar(id, metodoId);
            if (pago.getEstado() == Pago.EstadoPago.FALLIDO) {
                redirectAttributes.addFlashAttribute("errorPago",
                        "El pago falló. Verifica tu método de pago (la comisión es del 3%).");
            } else {
                redirectAttributes.addFlashAttribute("pagoOk", true);
            }
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorPago", e.getMessage());
        }
        return "redirect:/pagos/solicitud/" + id;
    }

    // HU-C11: historial de pagos del cliente
    @GetMapping("/historial")
    public String historial(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        model.addAttribute("pagos", pagoService.historialCliente(userDetails.getUsuario()));
        model.addAttribute("noLeidos", mensajeService.contarNoLeidos(userDetails.getUsuario()));
        return "historial-pagos";
    }

    // HU-C11: comprobante de un pago
    @GetMapping("/comprobante/{id}")
    public String comprobante(@PathVariable Long id,
                              @AuthenticationPrincipal CustomUserDetails userDetails,
                              Model model) {
        Pago pago = pagoService.buscarPago(id);
        boolean esCliente = pago.getCliente().getId().equals(userDetails.getUsuario().getId());
        boolean esTrabajador = pago.getTrabajador() != null
                && pago.getTrabajador().getId().equals(userDetails.getUsuario().getId());
        if (!esCliente && !esTrabajador) {
            return "redirect:/";
        }
        model.addAttribute("pago", pago);
        model.addAttribute("noLeidos", mensajeService.contarNoLeidos(userDetails.getUsuario()));
        return "comprobante-pago";
    }

    // HU-T13/T14: ingresos e historial del trabajador
    @GetMapping("/ingresos")
    public String ingresos(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        model.addAttribute("pagos", pagoService.historialTrabajador(userDetails.getUsuario()));
        model.addAttribute("totalIngresos", pagoService.ingresosTrabajador(userDetails.getUsuario()));
        model.addAttribute("noLeidos", mensajeService.contarNoLeidos(userDetails.getUsuario()));
        return "ingresos-trabajador";
    }
}

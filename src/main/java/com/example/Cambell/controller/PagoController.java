package com.example.Cambell.controller;

import com.example.Cambell.model.MetodoPago;
import com.example.Cambell.model.Pago;
import com.example.Cambell.service.MensajeService;
import com.example.Cambell.service.PagoService;
import com.example.Cambell.security.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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

    // HU-T14: exportar el reporte de ingresos del trabajador (CSV compatible con Excel)
    @GetMapping("/ingresos/exportar")
    public ResponseEntity<String> exportarIngresos(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate desde,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate hasta) {
        java.time.LocalDateTime desdeDt = desde != null ? desde.atStartOfDay() : null;
        java.time.LocalDateTime hastaDt = hasta != null ? hasta.plusDays(1).atStartOfDay() : null;
        java.util.List<Pago> pagos = pagoService.pagosTrabajadorEnRango(userDetails.getUsuario(), desdeDt, hastaDt);

        StringBuilder csv = new StringBuilder("Fecha,Solicitud,Cliente,Monto_Total,Comision(3%),Neto_Recibido\n");
        for (Pago p : pagos) {
            csv.append(p.getFechaPago() != null ? p.getFechaPago().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "")
               .append(",").append(escapar(p.getSolicitud().getDescripcion())).append(",")
               .append(escapar(p.getCliente().getNombre())).append(",")
               .append(p.getMontoTotal()).append(",").append(p.getComision()).append(",")
               .append(p.getNetoTrabajador()).append("\n");
        }
        double total = pagos.stream().mapToDouble(p -> p.getNetoTrabajador() != null ? p.getNetoTrabajador() : 0).sum();
        csv.append("\nTotal neto,,,,").append(Math.round(total * 100.0) / 100.0).append("\n");

        String nombre = "ingresos_trabajador_" + java.time.LocalDate.now() + ".csv";
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.parseMediaType("text/csv;charset=UTF-8"));
        headers.setContentDispositionFormData("attachment", nombre);
        return new ResponseEntity<>(csv.toString(), headers, org.springframework.http.HttpStatus.OK);
    }

    private String escapar(String valor) {
        if (valor == null) return "";
        String limpio = valor.replace("\"", "\"\"");
        if (limpio.contains(",") || limpio.contains("\"") || limpio.contains("\n")) {
            return "\"" + limpio + "\"";
        }
        return limpio;
    }
}

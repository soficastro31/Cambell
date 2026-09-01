package com.example.Cambell.controller;

import com.example.Cambell.model.CategoriaServicio;
import com.example.Cambell.model.EstadoVerificacion;
import com.example.Cambell.model.Solicitud;
import com.example.Cambell.model.Usuario;
import com.example.Cambell.security.CustomUserDetails;
import com.example.Cambell.service.CoberturaTrabajadorService;
import com.example.Cambell.service.LocalidadesBogota;
import com.example.Cambell.service.MensajeService;
import com.example.Cambell.service.PagoService;
import com.example.Cambell.service.SolicitudService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Controller
@RequestMapping("/solicitudes")
public class SolicitudController {

    @Autowired
    private SolicitudService solicitudService;

    @Autowired
    private MensajeService mensajeService;

    @Autowired
    private PagoService pagoService;

    @Autowired
    private CoberturaTrabajadorService coberturaTrabajadorService;

    private static final String UPLOAD_DIR = "uploads/";

    // Formulario para que el CLIENTE cree una solicitud
    @GetMapping("/nueva")
    public String mostrarFormulario(Model model) {
        model.addAttribute("solicitud", new Solicitud());
        model.addAttribute("localidades", LocalidadesBogota.obtenerLocalidades());
        model.addAttribute("localidadesJson", LocalidadesBogota.obtenerLocalidadesJson());
        model.addAttribute("categorias", CategoriaServicio.values());
        return "solicitud-nueva";
    }

    @PostMapping("/nueva")
    public String crear(@ModelAttribute Solicitud solicitud,
                         @RequestParam(required = false) Double precioOfertado,
                         @AuthenticationPrincipal CustomUserDetails userDetails) {
        solicitud.setCliente(userDetails.getUsuario());
        solicitud.setPrecioOfertado(precioOfertado);
        solicitudService.crear(solicitud);
        return "redirect:/solicitudes/mis-solicitudes";
    }

    // Cliente ve sus propias solicitudes
    @GetMapping("/mis-solicitudes")
    public String misSolicitudes(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        java.util.List<Solicitud> solicitudes = solicitudService.listarPorCliente(userDetails.getUsuario());
        model.addAttribute("solicitudes", solicitudes);
        model.addAttribute("noLeidos", mensajeService.contarNoLeidos(userDetails.getUsuario()));
        java.util.Map<Long, Boolean> pagos = new java.util.HashMap<>();
        for (Solicitud s : solicitudes) {
            pagos.put(s.getId(), pagoService.buscarPagoDeSolicitud(s.getId()).isPresent());
        }
        model.addAttribute("pagosPorSolicitud", pagos);
        return "mis-solicitudes";
    }

    // Trabajador ve solicitudes disponibles (aún sin asignar) con filtro multicriterio
    @GetMapping("/disponibles")
    public String disponibles(@AuthenticationPrincipal CustomUserDetails userDetails,
                               @RequestParam(required = false) String zona,
                               @RequestParam(required = false) String localidad,
                               @RequestParam(required = false) String barrio,
                               @RequestParam(required = false) String categoria,
                               @RequestParam(required = false) Double precioMin,
                               @RequestParam(required = false) Double precioMax,
                               Model model) {
        if (userDetails.getUsuario().getEstadoVerificacion() != EstadoVerificacion.APROBADO) {
            return "verificacion-pendiente";
        }
        CategoriaServicio cat = null;
        if (categoria != null && !categoria.isBlank()) {
            try {
                cat = CategoriaServicio.valueOf(categoria);
            } catch (IllegalArgumentException ignored) {
            }
        }
        model.addAttribute("solicitudes",
                solicitudService.listarDisponiblesFiltradas(localidad, barrio, cat, precioMin, precioMax));
        model.addAttribute("localidades", LocalidadesBogota.obtenerLocalidades());
        model.addAttribute("localidadesJson", LocalidadesBogota.obtenerLocalidadesJson());
        model.addAttribute("categorias", CategoriaServicio.values());
        model.addAttribute("filtroLocalidad", localidad);
        model.addAttribute("filtroBarrio", barrio);
        model.addAttribute("filtroCategoria", categoria);
        model.addAttribute("filtroPrecioMin", precioMin);
        model.addAttribute("filtroPrecioMax", precioMax);
        model.addAttribute("noLeidos", mensajeService.contarNoLeidos(userDetails.getUsuario()));

        // HU-S01: marcar las solicitudes compatibles con la cobertura del trabajador
        java.util.Set<Long> compatibles = new java.util.HashSet<>();
        var cob = coberturaTrabajadorService.obtenerCobertura(userDetails.getUsuario());
        if (cob.isPresent()) {
            for (var s : solicitudService.listarDisponiblesFiltradas(localidad, barrio, cat, precioMin, precioMax)) {
                if (coberturaTrabajadorService.esCompatible(s, cob.get())) {
                    compatibles.add(s.getId());
                }
            }
        }
        model.addAttribute("compatibles", compatibles);
        return "solicitudes-disponibles";
    }

    // Trabajador ve los trabajos que ya aceptó y tiene en curso
    @GetMapping("/mis-trabajos")
    public String misTrabajos(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        java.util.List<Solicitud> trabajos = solicitudService.listarActivasPorTrabajador(userDetails.getUsuario());
        model.addAttribute("trabajos", trabajos);
        model.addAttribute("puntosExactosJson", puntosExactos(trabajos));
        model.addAttribute("noLeidos", mensajeService.contarNoLeidos(userDetails.getUsuario()));
        return "mis-trabajos";
    }

    // HU-T13: historial de trabajos del trabajador (completados/cancelados, calificación e ingreso)
    @GetMapping("/mis-trabajos/historial")
    public String historialTrabajos(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        java.util.List<Solicitud> historial = solicitudService.historialPorTrabajador(userDetails.getUsuario());
        model.addAttribute("historial", historial);
        model.addAttribute("noLeidos", mensajeService.contarNoLeidos(userDetails.getUsuario()));
        return "historial-trabajos";
    }

    // HU-C06: el cliente ve el perfil público del trabajador asignado a su solicitud
    @GetMapping("/{id}/trabajador")
    public String verPerfilTrabajador(@PathVariable Long id, Model model,
                                      @AuthenticationPrincipal CustomUserDetails userDetails) {
        Solicitud solicitud = solicitudService.buscar(id);
        Usuario trabajador = solicitud.getTrabajador();
        if (trabajador == null) {
            return "redirect:/solicitudes/mis-solicitudes";
        }
        model.addAttribute("trabajador", trabajador);
        model.addAttribute("trabajosRealizados", solicitudService.contarCompletadosPorTrabajador(trabajador));
        model.addAttribute("noLeidos", mensajeService.contarNoLeidos(userDetails.getUsuario()));
        return "perfil-trabajador";
    }


    // Genera { id: { lat: x, lon: y } } en JSON válido para el revelado progresivo
    private String puntosExactos(java.util.List<Solicitud> trabajos) {
        StringBuilder sb = new StringBuilder("{");
        boolean primero = true;
        for (Solicitud s : trabajos) {
            if (s.getLatitud() == null || s.getLongitud() == null) {
                continue;
            }
            if (!primero) sb.append(",");
            primero = false;
            sb.append('"').append(s.getId()).append("\":{\"lat\":").append(s.getLatitud())
              .append(",\"lon\":").append(s.getLongitud()).append('}');
        }
        sb.append("}");
        return sb.toString();
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

    // HU-S07: el trabajador libera el trabajo para que se reasigne a otro compatibilizado
    @PostMapping("/{id}/liberar")
    public String liberar(@PathVariable Long id) {
        solicitudService.liberar(id);
        return "redirect:/solicitudes/mis-trabajos";
    }

    // Cliente califica un servicio completado
    @PostMapping("/{id}/calificar")
    public String calificar(@PathVariable Long id,
                             @RequestParam("puntuacion") Integer calificacion,
                             @RequestParam(required = false) String comentario) {
        solicitudService.calificar(id, calificacion, comentario);
        return "redirect:/solicitudes/mis-solicitudes";
    }

    // Trabajador finaliza el servicio adjuntando una foto de evidencia y el código del cliente
    @PostMapping("/{id}/finalizar")
    public String finalizarTrabajo(@PathVariable Long id,
                                    @RequestParam("evidencia") MultipartFile archivoEvidencia,
                                    @RequestParam("codigo") String codigo,
                                    @AuthenticationPrincipal CustomUserDetails userDetails,
                                    org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        if (codigo == null || codigo.isBlank()) {
            redirectAttributes.addFlashAttribute("errorFinalizar", "Debes ingresar el código de finalización.");
            return "redirect:/solicitudes/mis-trabajos";
        }
        if (archivoEvidencia == null || archivoEvidencia.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorFinalizar", "Debes adjuntar una foto del trabajo terminado.");
            return "redirect:/solicitudes/mis-trabajos";
        }

        // Validar el código antes de procesar el archivo
        if (!solicitudService.validarCodigo(id, codigo)) {
            redirectAttributes.addFlashAttribute("errorFinalizar", "El código de finalización ingresado es incorrecto.");
            return "redirect:/solicitudes/mis-trabajos";
        }

        try {
            String rutaRelativa = guardarEvidencia(archivoEvidencia);
            solicitudService.finalizarConEvidencia(id, rutaRelativa);
        } catch (IOException e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorFinalizar", "Error al guardar la evidencia. Inténtalo de nuevo.");
            return "redirect:/solicitudes/mis-trabajos";
        }
        return "redirect:/solicitudes/mis-trabajos";
    }

    private String guardarEvidencia(MultipartFile archivoEvidencia) throws IOException {
        File uploadDir = new File(UPLOAD_DIR);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }
        String nombreArchivo = UUID.randomUUID().toString() + "_" + archivoEvidencia.getOriginalFilename();
        Path rutaCompleta = Paths.get(UPLOAD_DIR + nombreArchivo);
        Files.write(rutaCompleta, archivoEvidencia.getBytes());
        return "/uploads/" + nombreArchivo;
    }
}
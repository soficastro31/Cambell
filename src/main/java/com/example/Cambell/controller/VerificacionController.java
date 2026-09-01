package com.example.Cambell.controller;

import com.example.Cambell.model.DocumentoVersion;
import com.example.Cambell.model.EstadoVerificacion;
import com.example.Cambell.model.Usuario;
import com.example.Cambell.security.CustomUserDetails;
import com.example.Cambell.service.AntecedentesService;
import com.example.Cambell.service.DocumentoVersionService;
import com.example.Cambell.service.UsuarioService;
import com.example.Cambell.service.VerificacionFacialService;
import com.example.Cambell.strategy.EstrategiaVerificacion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.IOException;

@Controller
@RequestMapping("/verificacion")
public class VerificacionController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private VerificacionFacialService verificacionFacialService;

    @Autowired
    private AntecedentesService antecedentesService;

    @Autowired
    private DocumentoVersionService documentoVersionService;

    @Autowired
    @Qualifier("verificacionFacePlusPlus")
    private EstrategiaVerificacion estrategiaVerificacion;

    // HU-T05: el trabajador reintenta la verificación cuando su cuenta fue rechazada
    @GetMapping("/reintentar")
    public String formularioReintento(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        model.addAttribute("trabajador", userDetails.getUsuario());
        return "verificacion-pendiente";
    }

    // HU-T05: procesa el nuevo documento y selfie y re-ejecuta la verificación
    @PostMapping("/reintentar")
    public String reintentar(@AuthenticationPrincipal CustomUserDetails userDetails,
                             @RequestParam MultipartFile documentoFile,
                             @RequestParam MultipartFile selfieFile,
                             RedirectAttributes ra) throws IOException {
        Usuario trabajador = userDetails.getUsuario();

        if ((documentoFile == null || documentoFile.isEmpty()) || (selfieFile == null || selfieFile.isEmpty())) {
            ra.addFlashAttribute("error", "Debes subir tanto la foto del documento como la selfie.");
            return "redirect:/verificacion/reintentar";
        }

        String rutaDocumentoDisco = guardarArchivo(trabajador, DocumentoVersion.TipoDocumento.DOCUMENTO, documentoFile);
        String rutaSelfieDisco = guardarArchivo(trabajador, DocumentoVersion.TipoDocumento.SELFIE, selfieFile);

        EstadoVerificacion nuevoEstado =
                evaluarVerificacion(rutaDocumentoDisco, rutaSelfieDisco, trabajador.getNumeroDocumento());

        trabajador.setRutaDocumento("/uploads/" + new File(rutaDocumentoDisco).getName());
        trabajador.setRutaSelfie("/uploads/" + new File(rutaSelfieDisco).getName());
        trabajador.setEstadoVerificacion(nuevoEstado);

        // HU-S11/S63: consultar antecedentes a la API externa y asociarlo al expediente
        antecedentesService.consultarYRegistrar(trabajador);

        usuarioService.actualizarVerificacion(trabajador);

        if (nuevoEstado == EstadoVerificacion.APROBADO) {
            return "redirect:/solicitudes/disponibles";
        }
        ra.addFlashAttribute("rechazado", true);
        return "redirect:/verificacion/reintentar";
    }

    // HU-T06: el trabajador actualiza un documento vencido y vuelve a validarlo
    @PostMapping("/actualizar-documento")
    public String actualizarDocumento(@AuthenticationPrincipal CustomUserDetails userDetails,
                                      @RequestParam MultipartFile documentoFile,
                                      RedirectAttributes ra,
                                      Model model) throws IOException {
        Usuario trabajador = userDetails.getUsuario();

        if (documentoFile == null || documentoFile.isEmpty()) {
            ra.addFlashAttribute("error", "Debes subir la foto del documento renovado.");
            return "redirect:/solicitudes/mis-trabajos";
        }

        String rutaDocumentoDisco = guardarArchivo(trabajador, DocumentoVersion.TipoDocumento.DOCUMENTO, documentoFile);
        trabajador.setRutaDocumento("/uploads/" + new File(rutaDocumentoDisco).getName());
        trabajador.setEstadoVerificacion(EstadoVerificacion.PENDIENTE);
        usuarioService.actualizarVerificacion(trabajador);

        ra.addFlashAttribute("exitoDocumento",
                "Tu documento fue actualizado y quedó pendiente de verificación por un administrador.");
        return "redirect:/solicitudes/mis-trabajos";
    }

    private EstadoVerificacion evaluarVerificacion(String rutaDocumento, String rutaSelfie, String numeroDocumento) {
        boolean numeroDocumentoValido =
                verificacionFacialService.verificarNumeroDocumento(rutaDocumento, numeroDocumento);
        if (!numeroDocumentoValido) {
            return EstadoVerificacion.RECHAZADO;
        }
        double confianza = estrategiaVerificacion.verificar(rutaDocumento, rutaSelfie);
        if (confianza >= 75) {
            return EstadoVerificacion.APROBADO;
        } else if (confianza >= 0) {
            return EstadoVerificacion.RECHAZADO;
        }
        return EstadoVerificacion.PENDIENTE;
    }

    private String guardarArchivo(Usuario trabajador, DocumentoVersion.TipoDocumento tipo, MultipartFile archivo) throws IOException {
        return documentoVersionService.guardarDocumentoCifrado(trabajador, tipo, archivo);
    }
}

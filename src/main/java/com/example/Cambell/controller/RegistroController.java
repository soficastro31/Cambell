package com.example.Cambell.controller;

import com.example.Cambell.model.EstadoVerificacion;
import com.example.Cambell.model.Usuario;
import com.example.Cambell.service.UsuarioService;
import com.example.Cambell.service.VerificacionFacialService;
import com.example.Cambell.strategy.EstrategiaVerificacion;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Controller
public class RegistroController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    @org.springframework.beans.factory.annotation.Qualifier("verificacionFacePlusPlus")
    private EstrategiaVerificacion estrategiaVerificacion;

    @Autowired
    private VerificacionFacialService verificacionFacialService;

    @Value("${app.upload.dir}")
    private String uploadDir;

    @GetMapping("/registro")
    public String mostrarFormulario(Model model) {
        model.addAttribute("usuario", new Usuario());
        return "registro";
    }

    @PostMapping("/registro")
    public String registrar(@Valid @ModelAttribute("usuario") Usuario usuario,
                             BindingResult resultado,
                             @RequestParam(required = false) MultipartFile documentoFile,
                             @RequestParam(required = false) MultipartFile selfieFile,
                             Model model) throws IOException {

        boolean esTrabajador = usuario.getRol() != null && usuario.getRol().name().equals("TRABAJADOR");

        // Validación extra: si es trabajador, documento y fotos son obligatorios
        if (esTrabajador) {
            if (usuario.getNumeroDocumento() == null || usuario.getNumeroDocumento().isBlank()) {
                resultado.rejectValue("numeroDocumento", "error.usuario", "El número de documento es obligatorio para trabajadores");
            }
            if (documentoFile == null || documentoFile.isEmpty()) {
                model.addAttribute("errorDocumento", "Debes subir una foto de tu documento");
            }
            if (selfieFile == null || selfieFile.isEmpty()) {
                model.addAttribute("errorSelfie", "Debes subir una selfie de verificación");
            }
        }

        boolean faltanArchivosTrabajador = esTrabajador &&
                ((documentoFile == null || documentoFile.isEmpty()) || (selfieFile == null || selfieFile.isEmpty()));

        if (resultado.hasErrors() || faltanArchivosTrabajador) {
            return "registro"; // vuelve al formulario mostrando los errores
        }

        String rutaDocumentoDisco = null;
        String rutaSelfieDisco = null;

        if (documentoFile != null && !documentoFile.isEmpty()) {
            rutaDocumentoDisco = guardarArchivo(documentoFile);
            usuario.setRutaDocumento("/uploads/" + new File(rutaDocumentoDisco).getName());
        }
        if (selfieFile != null && !selfieFile.isEmpty()) {
            rutaSelfieDisco = guardarArchivo(selfieFile);
            usuario.setRutaSelfie("/uploads/" + new File(rutaSelfieDisco).getName());
        }

        if (esTrabajador && rutaDocumentoDisco != null && rutaSelfieDisco != null) {
            // 1) Verificar que el número de documento coincida con el de la foto (OCR)
            boolean numeroDocumentoValido =
                    verificacionFacialService.verificarNumeroDocumento(rutaDocumentoDisco, usuario.getNumeroDocumento());
            if (!numeroDocumentoValido) {
                usuario.setEstadoVerificacion(EstadoVerificacion.RECHAZADO);
            } else {
                // 2) Verificar que la selfie corresponda al rostro del documento
                double confianza = estrategiaVerificacion.verificar(rutaDocumentoDisco, rutaSelfieDisco);

                if (confianza >= 75) {
                    usuario.setEstadoVerificacion(EstadoVerificacion.APROBADO);
                } else if (confianza >= 0) {
                    usuario.setEstadoVerificacion(EstadoVerificacion.RECHAZADO);
                } else {
                    usuario.setEstadoVerificacion(EstadoVerificacion.PENDIENTE);
                }
            }
        }

        usuarioService.registrar(usuario);
        return "redirect:/login?registroExitoso";
    }

    private String guardarArchivo(MultipartFile archivo) throws IOException {
        File carpeta = new File(uploadDir);
        if (!carpeta.exists()) carpeta.mkdirs();

        String nombreUnico = UUID.randomUUID() + "_" + archivo.getOriginalFilename();
        Path destino = Path.of(uploadDir, nombreUnico);
        Files.copy(archivo.getInputStream(), destino);

        return destino.toAbsolutePath().toString();
    }
}
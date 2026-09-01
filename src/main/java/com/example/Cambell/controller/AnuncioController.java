package com.example.Cambell.controller;

import com.example.Cambell.model.Anuncio;
import com.example.Cambell.service.AnuncioService;
import com.example.Cambell.service.MensajeService;
import com.example.Cambell.security.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Controller
@RequestMapping("/anuncios")
public class AnuncioController {

    @Autowired
    private AnuncioService anuncioService;

    @Autowired
    private MensajeService mensajeService;

    @Value("${app.upload.dir}")
    private String uploadDir;

    // HU-AL04: mis anuncios con su estado
    @GetMapping("/mios")
    public String misAnuncios(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        model.addAttribute("anuncios", anuncioService.listarDelAliado(userDetails.getUsuario()));
        model.addAttribute("noLeidos", mensajeService.contarNoLeidos(userDetails.getUsuario()));
        return "mis-anuncios";
    }

    // HU-AL02: formulario para crear un anuncio
    @GetMapping("/mios/nuevo")
    public String nuevoFormulario(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        model.addAttribute("anuncio", new Anuncio());
        model.addAttribute("noLeidos", mensajeService.contarNoLeidos(userDetails.getUsuario()));
        return "nuevo-anuncio";
    }

    @PostMapping("/mios/nuevo")
    public String crearAnuncio(@RequestParam String titulo,
                               @RequestParam String descripcion,
                               @RequestParam(required = false) MultipartFile imagen,
                               @AuthenticationPrincipal CustomUserDetails userDetails,
                               RedirectAttributes redirectAttributes) throws IOException {
        String rutaImagen = null;
        if (imagen != null && !imagen.isEmpty()) {
            rutaImagen = guardarArchivo(imagen);
        }
        anuncioService.crear(userDetails.getUsuario(), titulo, descripcion, rutaImagen);
        redirectAttributes.addFlashAttribute("ok", "Anuncio enviado a revisión. Verás su estado aquí.");
        return "redirect:/anuncios/mios";
    }

    // HU-AL03: editar un anuncio pendiente
    @GetMapping("/mios/{id}/editar")
    public String editarFormulario(@PathVariable Long id,
                                   @AuthenticationPrincipal CustomUserDetails userDetails,
                                   Model model) {
        Anuncio a = anuncioService.buscar(id);
        if (!a.getAliado().getId().equals(userDetails.getUsuario().getId())) {
            return "redirect:/anuncios/mios";
        }
        model.addAttribute("anuncio", a);
        model.addAttribute("noLeidos", mensajeService.contarNoLeidos(userDetails.getUsuario()));
        return "editar-anuncio";
    }

    @PostMapping("/mios/{id}/editar")
    public String editarAnuncio(@PathVariable Long id,
                                @RequestParam String titulo,
                                @RequestParam String descripcion,
                                @RequestParam(required = false) MultipartFile imagen,
                                @AuthenticationPrincipal CustomUserDetails userDetails,
                                RedirectAttributes redirectAttributes) throws IOException {
        try {
            String rutaImagen = null;
            if (imagen != null && !imagen.isEmpty()) {
                rutaImagen = guardarArchivo(imagen);
            }
            anuncioService.editarPendiente(id, userDetails.getUsuario(), titulo, descripcion, rutaImagen);
            redirectAttributes.addFlashAttribute("ok", "Anuncio actualizado. Sigue en revisión.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/anuncios/mios";
    }

    private String guardarArchivo(MultipartFile archivo) throws IOException {
        File carpeta = new File(uploadDir);
        if (!carpeta.exists()) carpeta.mkdirs();

        String nombreUnico = UUID.randomUUID() + "_" + archivo.getOriginalFilename();
        Path destino = Path.of(uploadDir, nombreUnico);
        Files.copy(archivo.getInputStream(), destino);

        return "/uploads/" + new File(destino.toString()).getName();
    }
}

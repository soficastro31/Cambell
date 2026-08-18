package com.example.Cambell.controller;

import com.example.Cambell.model.Usuario;
import com.example.Cambell.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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

    @Value("${app.upload.dir}")
    private String uploadDir;

    @GetMapping("/registro")
    public String mostrarFormulario(Model model) {
        model.addAttribute("usuario", new Usuario());
        return "registro";
    }

    @PostMapping("/registro")
    public String registrar(@ModelAttribute Usuario usuario,
                             @RequestParam(required = false) MultipartFile documentoFile,
                             @RequestParam(required = false) MultipartFile selfieFile) throws IOException {

        if (documentoFile != null && !documentoFile.isEmpty()) {
            usuario.setRutaDocumento(guardarArchivo(documentoFile));
        }
        if (selfieFile != null && !selfieFile.isEmpty()) {
            usuario.setRutaSelfie(guardarArchivo(selfieFile));
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

        return "/uploads/" + nombreUnico;
    }
}
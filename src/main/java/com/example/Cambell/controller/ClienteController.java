package com.example.Cambell.controller;

import com.example.Cambell.model.Usuario;
import com.example.Cambell.security.CustomUserDetails;
import com.example.Cambell.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Controller
@RequestMapping("/cliente")
public class ClienteController {

    @Autowired
    private UsuarioService usuarioService;

    // Ver perfil (HU-U04)
    @GetMapping("/perfil")
    public String perfil(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        model.addAttribute("usuario", userDetails.getUsuario());
        return "perfil";
    }

    // Formulario para editar datos del perfil
    @GetMapping("/editar")
    public String mostrarEditar(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        model.addAttribute("usuario", userDetails.getUsuario());
        return "editar-perfil";
    }

    // Guardar cambios del perfil
    @PostMapping("/editar")
    public String guardarEditar(@AuthenticationPrincipal CustomUserDetails userDetails,
                                 @Valid DatosPerfil datos,
                                 org.springframework.validation.BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes) {
        Long id = userDetails.getUsuario().getId();
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Datos inválidos. Revisa los campos.");
            return "redirect:/cliente/editar";
        }
        // Evitar duplicar el correo con otro usuario
        usuarioService.actualizarPerfil(id, datos.getNombre(), datos.getCorreo());
        redirectAttributes.addFlashAttribute("exito", "Perfil actualizado correctamente.");
        return "redirect:/cliente/perfil";
    }

    // Formulario para cambiar contraseña
    @GetMapping("/cambiar-password")
    public String mostrarCambiarPassword() {
        return "cambiar-password";
    }

    // Guardar nueva contraseña
    @PostMapping("/cambiar-password")
    public String guardarCambiarPassword(@AuthenticationPrincipal CustomUserDetails userDetails,
                                          @RequestParam("passwordActual") String passwordActual,
                                          @RequestParam("nuevaPassword") String nuevaPassword,
                                          RedirectAttributes redirectAttributes) {
        Long id = userDetails.getUsuario().getId();
        if (!usuarioService.cambiarPassword(id, passwordActual, nuevaPassword)) {
            redirectAttributes.addFlashAttribute("error", "La contraseña actual es incorrecta.");
            return "redirect:/cliente/cambiar-password";
        }
        redirectAttributes.addFlashAttribute("exito", "Contraseña actualizada correctamente.");
        return "redirect:/cliente/perfil";
    }
}

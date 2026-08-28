package com.example.Cambell.controller;

import com.example.Cambell.model.CategoriaServicio;
import com.example.Cambell.model.CoberturaTrabajador;
import com.example.Cambell.security.CustomUserDetails;
import com.example.Cambell.service.CoberturaTrabajadorService;
import com.example.Cambell.service.LocalidadesBogota;
import com.example.Cambell.service.NotificacionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Controller
@RequestMapping("/cobertura")
public class CoberturaController {

    @Autowired
    private CoberturaTrabajadorService coberturaService;

    @Autowired
    private NotificacionService notificacionService;

    // HU-T10/T11: ver y configurar la cobertura del trabajador
    @GetMapping
    public String configurar(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        CoberturaTrabajador cob = coberturaService.obtenerCobertura(userDetails.getUsuario()).orElse(null);
        model.addAttribute("cobertura", cob);
        model.addAttribute("localidades", LocalidadesBogota.obtenerLocalidades().keySet());
        model.addAttribute("categorias", CategoriaServicio.values());
        model.addAttribute("noLeidos", notificacionService.contarNoLeidos(userDetails.getUsuario()));
        return "cobertura";
    }

    @PostMapping
    public String guardar(@RequestParam Double radioKm,
                          @RequestParam(required = false) List<String> localidades,
                          @RequestParam(required = false) List<String> categorias,
                          @RequestParam(required = false) Boolean disponible,
                          @RequestParam(required = false) @DateTimeFormat(pattern = "HH:mm") LocalTime horaInicio,
                          @RequestParam(required = false) @DateTimeFormat(pattern = "HH:mm") LocalTime horaFin,
                          @AuthenticationPrincipal CustomUserDetails userDetails,
                          RedirectAttributes redirectAttributes) {
        Set<String> localidadesSet = localidades == null ? new HashSet<>() : new HashSet<>(localidades);
        Set<CategoriaServicio> categoriasSet = new HashSet<>();
        if (categorias != null) {
            for (String c : categorias) {
                try {
                    categoriasSet.add(CategoriaServicio.valueOf(c));
                } catch (IllegalArgumentException ignored) {
                }
            }
        }
        coberturaService.guardarCobertura(userDetails.getUsuario(), radioKm,
                localidadesSet, categoriasSet, disponible != null, horaInicio, horaFin);
        redirectAttributes.addFlashAttribute("ok", "Tu cobertura y disponibilidad se guardaron.");
        return "redirect:/cobertura";
    }
}

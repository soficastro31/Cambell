package com.example.Cambell.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                         Authentication authentication) throws IOException, ServletException {

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String rol = userDetails.getUsuario().getRol().name();

        if (rol.equals("TRABAJADOR")) {
            // HU-T05: el trabajador que aún no está aprobado debe completar/reintentar su verificación
            boolean aprobado = userDetails.getUsuario().getEstadoVerificacion() != null
                    && userDetails.getUsuario().getEstadoVerificacion().name().equals("APROBADO");
            response.sendRedirect(aprobado ? "/solicitudes/disponibles" : "/verificacion/reintentar");
        } else if (rol.equals("CLIENTE")) {
            response.sendRedirect("/solicitudes/nueva");
        } else if (rol.equals("ADMINISTRADOR")) {
            response.sendRedirect("/admin/verificaciones");
        } else if (rol.equals("ALIADO_COMERCIAL")) {
            response.sendRedirect("/anuncios/mios");
        } else {
            response.sendRedirect("/");
        }
    }
}
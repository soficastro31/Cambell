package com.example.Cambell.service;

import com.example.Cambell.model.TokenRecuperacion;
import com.example.Cambell.model.Usuario;
import com.example.Cambell.repository.TokenRecuperacionRepository;
import com.example.Cambell.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class RecuperacionPasswordService {

    // Duración del enlace de restablecimiento (minutos)
    private static final long HORAS_VALIDEZ_TOKEN = 24;

    @Autowired
    private TokenRecuperacionRepository tokenRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public boolean existeCorreo(String correo) {
        return correo != null && !correo.isBlank()
                && usuarioRepository.findByCorreo(correo.trim()).isPresent();
    }

    /**
     * HU-U03: crea un token de restablecimiento y "envía" el enlace al correo.
     * En este entorno de desarrollo el enlace se imprime en consola simulando el correo.
     */
    public Optional<String> generarTokenYEnviarEnlace(String correo, String baseUrl) {
        if (!existeCorreo(correo)) {
            return Optional.empty();
        }
        Usuario usuario = usuarioRepository.findByCorreo(correo.trim()).orElseThrow();
        String token = UUID.randomUUID().toString();

        TokenRecuperacion t = new TokenRecuperacion();
        t.setUsuario(usuario);
        t.setToken(token);
        t.setExpiracion(LocalDateTime.now().plusHours(HORAS_VALIDEZ_TOKEN));
        t.setUsado(false);
        tokenRepository.save(t);

        String enlace = baseUrl + "/restablecer-password?token=" + token;
        System.out.println("📧 [HU-U03] Enlace de restablecimiento para " + correo + ": " + enlace);
        return Optional.of(enlace);
    }

    public boolean tokenValido(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }
        return tokenRepository.findByToken(token)
                .map(t -> !t.isUsado() && t.getExpiracion().isAfter(LocalDateTime.now()))
                .orElse(false);
    }

    public boolean restablecerPassword(String token, String nuevaPassword) {
        Optional<TokenRecuperacion> opt = tokenRepository.findByToken(token);
        if (opt.isEmpty() || opt.get().isUsado() || opt.get().getExpiracion().isBefore(LocalDateTime.now())) {
            return false;
        }
        TokenRecuperacion t = opt.get();
        Usuario usuario = t.getUsuario();
        usuario.setPassword(passwordEncoder.encode(nuevaPassword));
        usuarioRepository.save(usuario);

        t.setUsado(true);
        tokenRepository.save(t);
        return true;
    }
}
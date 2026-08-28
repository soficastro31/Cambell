package com.example.Cambell.config;

import com.example.Cambell.model.Rol;
import com.example.Cambell.model.Usuario;
import com.example.Cambell.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminInitializer(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        String correoAdmin = "admin@cambell.com";

        if (usuarioRepository.findByCorreo(correoAdmin).isEmpty()) {
            Usuario admin = new Usuario();
            admin.setNombre("Administrador");
            admin.setCorreo(correoAdmin);
            admin.setPassword(passwordEncoder.encode("Admin123*"));
            admin.setRol(Rol.ADMINISTRADOR);

            usuarioRepository.save(admin);
            System.out.println("Usuario administrador creado -> " + correoAdmin + " / Admin123*");
        }
    }
}
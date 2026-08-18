package com.example.Cambell.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.Cambell.model.Usuario;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByCorreo(String correo);
    boolean existsByCorreo(String correo);
}
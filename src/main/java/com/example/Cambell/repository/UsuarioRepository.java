package com.example.Cambell.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.Cambell.model.EstadoVerificacion;
import com.example.Cambell.model.Rol;
import com.example.Cambell.model.Usuario;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByCorreo(String correo);
    boolean existsByCorreo(String correo);
    boolean existsByNumeroDocumento(String numeroDocumento);
    boolean existsByTelefono(String telefono);
    List<Usuario> findByRolAndEstadoVerificacion(Rol rol, EstadoVerificacion estado);
    List<Usuario> findByRolOrderById(Rol rol);
}
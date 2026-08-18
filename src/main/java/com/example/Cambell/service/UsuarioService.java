package com.example.Cambell.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.Cambell.model.EstadoVerificacion;
import com.example.Cambell.model.Rol;
import com.example.Cambell.model.Usuario;
import com.example.Cambell.repository.UsuarioRepository;
import java.util.List;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public Usuario registrar(Usuario usuario) {
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        return usuarioRepository.save(usuario);
    }

    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    public Optional<Usuario> buscarPorId(Long id) {
        return usuarioRepository.findById(id);
    }

    public Optional<Usuario> buscarPorCorreo(String correo) {
        return usuarioRepository.findByCorreo(correo);
    } 
    public List<Usuario> listarPorRolYEstado(Rol rol, EstadoVerificacion estado) {
    return usuarioRepository.findByRolAndEstadoVerificacion(rol, estado);
}

    public void cambiarEstadoVerificacion(Long id, EstadoVerificacion estado) {
    Usuario usuario = usuarioRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    usuario.setEstadoVerificacion(estado);
    usuarioRepository.save(usuario);
}
}
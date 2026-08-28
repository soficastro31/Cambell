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

    public Usuario cambiarEstadoVerificacion(Long id, EstadoVerificacion estado) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        usuario.setEstadoVerificacion(estado);
        return usuarioRepository.save(usuario);
    }

    // Actualiza datos básicos del perfil (HU-U04)
    public void actualizarPerfil(Long id, String nombre, String correo) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        usuario.setNombre(nombre);
        usuario.setCorreo(correo);
        usuarioRepository.save(usuario);
    }

    // Cambia la contraseña verificando que la actual sea correcta (parte de HU-U04)
    public boolean cambiarPassword(Long id, String passwordActual, String nuevaPassword) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        if (!passwordEncoder.matches(passwordActual, usuario.getPassword())) {
            return false;
        }
        usuario.setPassword(passwordEncoder.encode(nuevaPassword));
        usuarioRepository.save(usuario);
        return true;
    }
}
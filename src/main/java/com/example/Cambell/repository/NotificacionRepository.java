package com.example.Cambell.repository;

import com.example.Cambell.model.Notificacion;
import com.example.Cambell.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {
    List<Notificacion> findByUsuarioOrderByFechaDesc(Usuario usuario);
    long countByUsuarioAndLeidoFalse(Usuario usuario);
}

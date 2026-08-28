package com.example.Cambell.repository;

import com.example.Cambell.model.Mensaje;
import com.example.Cambell.model.Solicitud;
import com.example.Cambell.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MensajeRepository extends JpaRepository<Mensaje, Long> {
    List<Mensaje> findBySolicitudOrderByFechaAsc(Solicitud solicitud);

    // Mensajes no leídos en las solicitudes en las que participa el usuario
    @Query("SELECT COUNT(m) FROM Mensaje m WHERE m.leido = false " +
           "AND m.emisor != :usuario " +
           "AND (m.solicitud.cliente = :usuario OR m.solicitud.trabajador = :usuario)")
    long contarNoLeidos(@Param("usuario") Usuario usuario);

    // Mensajes no leídos de una solicitud en concreto dirigidos al usuario
    @Query("SELECT m FROM Mensaje m WHERE m.solicitud = :solicitud " +
           "AND m.emisor != :usuario AND m.leido = false")
    List<Mensaje> noLeidosDe(@Param("solicitud") Solicitud solicitud, @Param("usuario") Usuario usuario);
}

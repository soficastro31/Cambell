package com.example.Cambell.repository;

import com.example.Cambell.model.EstadoSolicitud;
import com.example.Cambell.model.Solicitud;
import com.example.Cambell.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SolicitudRepository extends JpaRepository<Solicitud, Long> {
    List<Solicitud> findByZonaAndEstado(String zona, EstadoSolicitud estado);
    List<Solicitud> findByEstado(EstadoSolicitud estado);
    List<Solicitud> findByCliente(Usuario cliente);
    List<Solicitud> findByTrabajador(Usuario trabajador);
}
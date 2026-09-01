package com.example.Cambell.repository;

import com.example.Cambell.model.Pago;
import com.example.Cambell.model.Solicitud;
import com.example.Cambell.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PagoRepository extends JpaRepository<Pago, Long> {
    Optional<Pago> findBySolicitud(Solicitud solicitud);
    List<Pago> findByClienteOrderByFechaPagoDesc(Usuario cliente);
    List<Pago> findByTrabajadorOrderByFechaPagoDesc(Usuario trabajador);
    List<Pago> findByEstado(Pago.EstadoPago estado);
    List<Pago> findByEstadoAndTransferenciaCompletadaFalse(Pago.EstadoPago estado);
}

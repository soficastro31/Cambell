package com.example.Cambell.repository;

import com.example.Cambell.model.Pago;
import com.example.Cambell.model.Reembolso;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReembolsoRepository extends JpaRepository<Reembolso, Long> {
    Optional<Reembolso> findByPago(Pago pago);
    List<Reembolso> findAllByOrderByFechaDesc();
    List<Reembolso> findByEstadoOrderByFechaDesc(Reembolso.EstadoReembolso estado);
}
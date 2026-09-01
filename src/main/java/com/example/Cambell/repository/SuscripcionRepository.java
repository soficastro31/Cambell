package com.example.Cambell.repository;

import com.example.Cambell.model.Suscripcion;
import com.example.Cambell.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SuscripcionRepository extends JpaRepository<Suscripcion, Long> {
    Optional<Suscripcion> findByTrabajador(Usuario trabajador);
    List<Suscripcion> findByActivaTrue();
    List<Suscripcion> findByActivaTrueAndRenovacionAutomaticaTrueAndFechaVencimientoBefore(LocalDateTime fecha);
    List<Suscripcion> findByActivaTrueAndRenovacionAutomaticaTrue();
    List<Suscripcion> findByActivaTrueAndFechaVencimientoBetween(LocalDateTime desde, LocalDateTime hasta);
}
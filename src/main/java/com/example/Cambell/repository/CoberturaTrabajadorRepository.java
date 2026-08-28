package com.example.Cambell.repository;

import com.example.Cambell.model.CoberturaTrabajador;
import com.example.Cambell.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CoberturaTrabajadorRepository extends JpaRepository<CoberturaTrabajador, Long> {
    Optional<CoberturaTrabajador> findByTrabajador(Usuario trabajador);
}

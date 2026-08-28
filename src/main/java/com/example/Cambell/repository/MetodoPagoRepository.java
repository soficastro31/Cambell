package com.example.Cambell.repository;

import com.example.Cambell.model.MetodoPago;
import com.example.Cambell.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MetodoPagoRepository extends JpaRepository<MetodoPago, Long> {
    List<MetodoPago> findByCliente(Usuario cliente);
}

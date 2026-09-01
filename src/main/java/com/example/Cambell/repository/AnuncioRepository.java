package com.example.Cambell.repository;

import com.example.Cambell.model.Anuncio;
import com.example.Cambell.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnuncioRepository extends JpaRepository<Anuncio, Long> {
    List<Anuncio> findByAliadoOrderByFechaCreacionDesc(Usuario aliado);
    List<Anuncio> findByEstadoOrderByFechaCreacionDesc(Anuncio.EstadoAnuncio estado);
}

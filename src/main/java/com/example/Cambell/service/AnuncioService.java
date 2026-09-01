package com.example.Cambell.service;

import com.example.Cambell.model.Anuncio;
import com.example.Cambell.model.Usuario;
import com.example.Cambell.repository.AnuncioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AnuncioService {

    @Autowired
    private AnuncioRepository anuncioRepository;

    // HU-AL02: crear un anuncio en estado PENDIENTE de revisión
    public Anuncio crear(Usuario aliado, String titulo, String descripcion, String rutaImagen) {
        Anuncio a = new Anuncio();
        a.setAliado(aliado);
        a.setTitulo(titulo);
        a.setDescripcion(descripcion);
        a.setRutaImagen(rutaImagen);
        return anuncioRepository.save(a);
    }

    public List<Anuncio> listarDelAliado(Usuario aliado) {
        return anuncioRepository.findByAliadoOrderByFechaCreacionDesc(aliado);
    }

    public List<Anuncio> listarPublicados() {
        return anuncioRepository.findByEstadoOrderByFechaCreacionDesc(Anuncio.EstadoAnuncio.PUBLICADO);
    }

    public List<Anuncio> listarPorEstado(Anuncio.EstadoAnuncio estado) {
        return anuncioRepository.findByEstadoOrderByFechaCreacionDesc(estado);
    }

    public Anuncio buscar(Long id) {
        return anuncioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Anuncio no encontrado"));
    }

    // HU-AL03: editar un anuncio aún pendiente (mantiene el estado pendiente)
    public Anuncio editarPendiente(Long id, Usuario aliado, String titulo, String descripcion, String rutaImagen) {
        Anuncio a = buscar(id);
        if (!a.getAliado().getId().equals(aliado.getId())) {
            throw new RuntimeException("No puedes editar este anuncio");
        }
        if (a.getEstado() != Anuncio.EstadoAnuncio.PENDIENTE) {
            throw new RuntimeException("Solo puedes editar anuncios pendientes de revisión");
        }
        a.setTitulo(titulo);
        a.setDescripcion(descripcion);
        if (rutaImagen != null && !rutaImagen.isBlank()) {
            a.setRutaImagen(rutaImagen);
        }
        return anuncioRepository.save(a);
    }

    // HU-A07: aprobar un anuncio (lo hace público)
    public Anuncio aprobar(Long id) {
        Anuncio a = buscar(id);
        a.setEstado(Anuncio.EstadoAnuncio.PUBLICADO);
        a.setMotivoRechazo(null);
        return anuncioRepository.save(a);
    }

    // HU-A08: rechazar un anuncio con motivo
    public Anuncio rechazar(Long id, String motivo) {
        Anuncio a = buscar(id);
        a.setEstado(Anuncio.EstadoAnuncio.RECHAZADO);
        a.setMotivoRechazo(motivo);
        return anuncioRepository.save(a);
    }
}

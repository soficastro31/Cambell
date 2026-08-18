package com.example.Cambell.service;

import com.example.Cambell.model.*;
import com.example.Cambell.repository.SolicitudRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class SolicitudService {

    @Autowired
    private SolicitudRepository solicitudRepository;

    public Solicitud crear(Solicitud solicitud) {
        solicitud.setEstado(EstadoSolicitud.PENDIENTE);
        return solicitudRepository.save(solicitud);
    }

  public List<Solicitud> listarPorZonaDisponibles(String zona) {
    if (zona == null || zona.isBlank()) {
        return solicitudRepository.findByEstado(EstadoSolicitud.PENDIENTE);
    }
    return solicitudRepository.findByZonaAndEstado(zona, EstadoSolicitud.PENDIENTE);
}
    public List<Solicitud> listarPorCliente(Usuario cliente) {
        return solicitudRepository.findByCliente(cliente);
    }

    public List<Solicitud> listarPorTrabajador(Usuario trabajador) {
        return solicitudRepository.findByTrabajador(trabajador);
    }

    public Optional<Solicitud> buscarPorId(Long id) {
        return solicitudRepository.findById(id);
    }

    public Solicitud aceptar(Long id, Usuario trabajador) {
        Solicitud solicitud = solicitudRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));
        solicitud.setTrabajador(trabajador);
        solicitud.setEstado(EstadoSolicitud.ACEPTADA);
        return solicitudRepository.save(solicitud);
    }

    public Solicitud rechazar(Long id) {
        Solicitud solicitud = solicitudRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));
        solicitud.setEstado(EstadoSolicitud.RECHAZADA);
        return solicitudRepository.save(solicitud);
    }

    public Solicitud cancelar(Long id) {
        Solicitud solicitud = solicitudRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));
        solicitud.setEstado(EstadoSolicitud.CANCELADA);
        return solicitudRepository.save(solicitud);
    }
}
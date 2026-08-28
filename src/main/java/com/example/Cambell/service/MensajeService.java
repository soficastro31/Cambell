package com.example.Cambell.service;

import com.example.Cambell.model.Mensaje;
import com.example.Cambell.model.Solicitud;
import com.example.Cambell.model.Usuario;
import com.example.Cambell.repository.MensajeRepository;
import com.example.Cambell.repository.SolicitudRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MensajeService {

    @Autowired
    private MensajeRepository mensajeRepository;

    @Autowired
    private SolicitudRepository solicitudRepository;

    // HU-C13 / HU-T18: envío de mensaje dentro de una solicitud.
    // Solo puede participar el cliente dueño y el trabajador asignado.
    public boolean puedeParticipar(Long solicitudId, Usuario usuario) {
        return solicitudRepository.findById(solicitudId)
                .map(s -> s.getCliente().getId().equals(usuario.getId())
                        || (s.getTrabajador() != null && s.getTrabajador().getId().equals(usuario.getId())))
                .orElse(false);
    }

    public Mensaje enviar(Long solicitudId, Usuario emisor, String contenido) {
        if (!puedeParticipar(solicitudId, emisor)) {
            throw new RuntimeException("No puedes enviar mensajes en esta solicitud");
        }
        Solicitud solicitud = solicitudRepository.findById(solicitudId)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));
        Mensaje mensaje = new Mensaje();
        mensaje.setSolicitud(solicitud);
        mensaje.setEmisor(emisor);
        mensaje.setContenido(contenido);
        return mensajeRepository.save(mensaje);
    }

    public List<Mensaje> listarPorSolicitud(Long solicitudId) {
        Solicitud solicitud = solicitudRepository.findById(solicitudId)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));
        return mensajeRepository.findBySolicitudOrderByFechaAsc(solicitud);
    }

    public Solicitud buscarSolicitud(Long solicitudId) {
        return solicitudRepository.findById(solicitudId)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));
    }

    // HU-S20: contador de mensajes no leídos dirigidos al usuario (para el menú)
    public long contarNoLeidos(Usuario usuario) {
        return mensajeRepository.contarNoLeidos(usuario);
    }

    // HU-S20: marcar como leídos los mensajes de una solicitud dirigidos al usuario
    public void marcarLeidos(Long solicitudId, Usuario usuario) {
        Solicitud solicitud = solicitudRepository.findById(solicitudId)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));
        List<Mensaje> pendientes = mensajeRepository.noLeidosDe(solicitud, usuario);
        for (Mensaje m : pendientes) {
            m.setLeido(true);
        }
        if (!pendientes.isEmpty()) {
            mensajeRepository.saveAll(pendientes);
        }
    }
}

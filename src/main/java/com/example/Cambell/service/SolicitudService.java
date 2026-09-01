package com.example.Cambell.service;

import com.example.Cambell.model.*;
import com.example.Cambell.observer.EventoSolicitud;
import com.example.Cambell.observer.NotificadorSolicitud;
import com.example.Cambell.repository.SolicitudRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class SolicitudService {

    @Autowired
    private SolicitudRepository solicitudRepository;

    @Autowired
    private NotificadorSolicitud notificadorSolicitud;

    public Solicitud crear(Solicitud solicitud) {
        solicitud.setEstado(EstadoSolicitud.PENDIENTE);
        solicitud.setCodigoFinalizacion(generarCodigo());
        solicitud.setFechaCreacion(java.time.LocalDateTime.now());
        // Reconstruir zona a partir de localidad/barrio si no se indicó manualmente
        if (solicitud.getZona() == null || solicitud.getZona().isBlank()) {
            solicitud.setZona(componerZona(solicitud.getLocalidad(), solicitud.getBarrio()));
        }
        Solicitud guardada = solicitudRepository.save(solicitud);
        notificadorSolicitud.notificar(new EventoSolicitud(EventoSolicitud.Tipo.CREADA, guardada));
        return guardada;
    }

    private String componerZona(String localidad, String barrio) {
        StringBuilder sb = new StringBuilder();
        if (barrio != null && !barrio.isBlank()) {
            sb.append(barrio);
        }
        if (localidad != null && !localidad.isBlank()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(localidad);
        }
        sb.append(", Bogotá");
        return sb.toString();
    }

    private String generarCodigo() {
        String caracteres = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        StringBuilder sb = new StringBuilder();
        java.security.SecureRandom rnd = new java.security.SecureRandom();
        for (int i = 0; i < 6; i++) {
            sb.append(caracteres.charAt(rnd.nextInt(caracteres.length())));
        }
        return sb.toString();
    }

    public List<Solicitud> listarPorZonaDisponibles(String zona) {
        if (zona == null || zona.isBlank()) {
            return solicitudRepository.findByEstado(EstadoSolicitud.PENDIENTE);
        }
        return solicitudRepository.findByZonaAndEstado(zona, EstadoSolicitud.PENDIENTE);
    }

    // Filtro multicriterio para solicitudes disponibles
    public List<Solicitud> listarDisponiblesFiltradas(String localidad, String barrio,
                                                       CategoriaServicio categoria,
                                                       Double precioMin, Double precioMax) {
        return solicitudRepository.filtrarDisponibles(EstadoSolicitud.PENDIENTE,
                (localidad == null || localidad.isBlank()) ? null : localidad,
                (barrio == null || barrio.isBlank()) ? null : barrio,
                categoria,
                precioMin,
                precioMax);
    }

    public List<Solicitud> listarPorCliente(Usuario cliente) {
        return solicitudRepository.findByCliente(cliente);
    }

    public List<Solicitud> listarPorTrabajador(Usuario trabajador) {
        return solicitudRepository.findByTrabajador(trabajador);
    }

    public List<Solicitud> listarActivasPorTrabajador(Usuario trabajador) {
        return solicitudRepository.findByTrabajadorAndEstado(trabajador, EstadoSolicitud.ACEPTADA);
    }

    // HU-T13: historial de trabajos del trabajador (excluye las pendientes por asignar)
    public List<Solicitud> historialPorTrabajador(Usuario trabajador) {
        return solicitudRepository.findByTrabajador(trabajador).stream()
                .filter(s -> s.getEstado() != EstadoSolicitud.PENDIENTE)
                .sorted((a, b) -> b.getId().compareTo(a.getId()))
                .collect(java.util.stream.Collectors.toList());
    }

    // HU-C06: cantidad de trabajos completados por un trabajador
    public long contarCompletadosPorTrabajador(Usuario trabajador) {
        return solicitudRepository.findByTrabajadorAndEstado(trabajador, EstadoSolicitud.COMPLETADA).size();
    }

    public Optional<Solicitud> buscarPorId(Long id) {
        return solicitudRepository.findById(id);
    }

    public Solicitud buscar(Long id) {
        return solicitudRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));
    }

    public Solicitud aceptar(Long id, Usuario trabajador) {
        Solicitud solicitud = solicitudRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));
        solicitud.setTrabajador(trabajador);
        solicitud.setEstado(EstadoSolicitud.ACEPTADA);
        Solicitud guardada = solicitudRepository.save(solicitud);
        notificadorSolicitud.notificar(new EventoSolicitud(EventoSolicitud.Tipo.ACEPTADA, guardada));
        return guardada;
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

    // HU-S07: el trabajador libera un trabajo que no puede atender; la solicitud
    // vuelve a estar disponible para ser reasignada a otro trabajador compatible.
    public Solicitud liberar(Long id) {
        Solicitud solicitud = solicitudRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));
        solicitud.setEstado(EstadoSolicitud.PENDIENTE);
        solicitud.setTrabajador(null);
        Solicitud guardada = solicitudRepository.save(solicitud);
        notificadorSolicitud.notificar(new EventoSolicitud(EventoSolicitud.Tipo.REASIGNADA, guardada));
        return guardada;
    }

    // Método para calificar un servicio completado
    public Solicitud calificar(Long id, Integer calificacion, String comentario) {
        Solicitud solicitud = solicitudRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));

        solicitud.setCalificacion(calificacion);
        solicitud.setComentario(comentario);

        Solicitud guardada = solicitudRepository.save(solicitud);
        notificadorSolicitud.notificar(new EventoSolicitud(EventoSolicitud.Tipo.CALIFICADA, guardada));
        return guardada;
    }

    // Método para que el trabajador adjunte la evidencia y finalice el servicio
    public Solicitud finalizarConEvidencia(Long id, String rutaEvidencia) {
        Solicitud solicitud = solicitudRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));

        solicitud.setRutaEvidencia(rutaEvidencia);
        solicitud.setEstado(EstadoSolicitud.COMPLETADA);
        solicitud.setFechaFinalizacion(java.time.LocalDateTime.now());

        Solicitud guardada = solicitudRepository.save(solicitud);
        notificadorSolicitud.notificar(new EventoSolicitud(EventoSolicitud.Tipo.COMPLETADA, guardada));
        return guardada;
    }

    // Valida el código de finalización
    public boolean validarCodigo(Long id, String codigo) {
        Solicitud solicitud = solicitudRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));
        return solicitud.getCodigoFinalizacion() != null
                && solicitud.getCodigoFinalizacion().equalsIgnoreCase(codigo.trim());
    }

}
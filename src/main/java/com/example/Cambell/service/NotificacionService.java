package com.example.Cambell.service;

import com.example.Cambell.model.Notificacion;
import com.example.Cambell.model.Solicitud;
import com.example.Cambell.model.Usuario;
import com.example.Cambell.repository.NotificacionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificacionService {

    @Autowired
    private NotificacionRepository notificacionRepository;

    public Notificacion crear(Usuario usuario, String tipo, String mensaje, Solicitud solicitud) {
        Notificacion n = new Notificacion();
        n.setUsuario(usuario);
        n.setTipo(tipo);
        n.setMensaje(mensaje);
        if (solicitud != null) {
            n.setSolicitud(solicitud);
        }
        return notificacionRepository.save(n);
    }

    public Notificacion crear(Usuario usuario, String tipo, String mensaje) {
        return crear(usuario, tipo, mensaje, null);
    }

    public List<Notificacion> listar(Usuario usuario) {
        return notificacionRepository.findByUsuarioOrderByFechaDesc(usuario);
    }

    public long contarNoLeidos(Usuario usuario) {
        return notificacionRepository.countByUsuarioAndLeidoFalse(usuario);
    }

    // Marca todas las notificaciones del usuario como leídas
    public void marcarLeidas(Usuario usuario) {
        List<Notificacion> todas = notificacionRepository.findByUsuarioOrderByFechaDesc(usuario);
        boolean cambio = false;
        for (Notificacion n : todas) {
            if (!n.isLeido()) {
                n.setLeido(true);
                cambio = true;
            }
        }
        if (cambio) {
            notificacionRepository.saveAll(todas);
        }
    }
}

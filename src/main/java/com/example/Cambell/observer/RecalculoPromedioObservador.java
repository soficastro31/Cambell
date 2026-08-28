package com.example.Cambell.observer;

import com.example.Cambell.model.EstadoSolicitud;
import com.example.Cambell.model.Solicitud;
import com.example.Cambell.model.Usuario;
import com.example.Cambell.repository.SolicitudRepository;
import com.example.Cambell.repository.UsuarioRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RecalculoPromedioObservador implements ObservadorSolicitud {

    @Autowired
    private NotificadorSolicitud notificadorSolicitud;

    @Autowired
    private SolicitudRepository solicitudRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @PostConstruct
    public void suscribirse() {
        notificadorSolicitud.suscribir(this);
    }

    @Override
    public void actualizar(EventoSolicitud evento) {
        if (evento.getTipo() == EventoSolicitud.Tipo.CALIFICADA) {
            Usuario trabajador = evento.getSolicitud().getTrabajador();
            recalcularPromedio(trabajador);
            System.out.println("Recalculando promedio del trabajador: " + trabajador.getNombre());
        }
    }

    private void recalcularPromedio(Usuario trabajador) {
        List<Solicitud> solicitudes = solicitudRepository.findByTrabajadorAndEstado(trabajador, EstadoSolicitud.COMPLETADA);

        double suma = 0;
        int total = 0;
        for (Solicitud s : solicitudes) {
            if (s.getCalificacion() != null) {
                suma += s.getCalificacion();
                total++;
            }
        }

        trabajador.setCalificacionPromedio(total > 0 ? suma / total : 0.0);
        trabajador.setTotalCalificaciones(total);
        usuarioRepository.save(trabajador);
    }
}
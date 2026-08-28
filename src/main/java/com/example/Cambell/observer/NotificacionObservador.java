package com.example.Cambell.observer;

import com.example.Cambell.model.Solicitud;
import com.example.Cambell.model.Usuario;
import com.example.Cambell.service.CoberturaTrabajadorService;
import com.example.Cambell.service.NotificacionService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * HU-S09 / HU-S02 / HU-S17 / HU-S18: emite notificaciones de estado y de
 * solicitudes compatibles usando el patrón observador existente.
 */
@Component
public class NotificacionObservador implements ObservadorSolicitud {

    @Autowired
    private NotificadorSolicitud notificadorSolicitud;

    @Autowired
    private NotificacionService notificacionService;

    @Autowired
    private CoberturaTrabajadorService coberturaTrabajadorService;

    @PostConstruct
    public void suscribirse() {
        notificadorSolicitud.suscribir(this);
    }

    @Override
    public void actualizar(EventoSolicitud evento) {
        Solicitud s = evento.getSolicitud();
        switch (evento.getTipo()) {
            case CREADA:
                notificarCompatibles(s);
                break;
            case ACEPTADA:
                // HU-S09 / HU-S02: avisar al cliente que su solicitud fue asignada
                notificacionService.crear(s.getCliente(), "ESTADO",
                        "Tu solicitud \"" + s.getDescripcion() + "\" fue aceptada por " +
                                s.getTrabajador().getNombre() + ". El servicio ya está en camino.",
                        s);
                break;
            case COMPLETADA:
                // HU-S09: avisar al cliente que puede calificar y pagar
                notificacionService.crear(s.getCliente(), "ESTADO",
                        "El servicio \"" + s.getDescripcion() + "\" fue completado. No olvides calificar y pagar.",
                        s);
                break;
            case REASIGNADA:
                // HU-S07: la solicitud quedó libre de nuevo; notificar a los compatibles
                notificarCompatibles(s);
                break;
            default:
                break;
        }
    }

    // HU-S17: notificar a los trabajadores compatibles una nueva solicitud
    private void notificarCompatibles(Solicitud s) {
        List<Usuario> compatibles = coberturaTrabajadorService.trabajadoresCompatibles(s);
        for (Usuario u : compatibles) {
            notificacionService.crear(u, "ASIGNACION",
                    "Nueva solicitud compatible con tu cobertura: \"" + s.getDescripcion() +
                            "\" en " + (s.getZona() != null ? s.getZona() : "tu zona") +
                            " por $" + (s.getPrecioOfertado() != null ? s.getPrecioOfertado() : 0) + ".",
                    s);
        }
    }
}

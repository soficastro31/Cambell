package com.example.Cambell.observer;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class NotificacionLogObservador implements ObservadorSolicitud {

    @Autowired
    private NotificadorSolicitud notificadorSolicitud;

    @PostConstruct
    public void suscribirse() {
        notificadorSolicitud.suscribir(this);
    }

    @Override
    public void actualizar(EventoSolicitud evento) {
        switch (evento.getTipo()) {
            case CREADA -> System.out.println("📩 Notificación: nueva solicitud disponible en " + evento.getSolicitud().getZona());
            case ACEPTADA -> System.out.println("📩 Notificación al cliente: tu solicitud fue aceptada");
            case COMPLETADA -> System.out.println("📩 Notificación al cliente: tu servicio fue marcado como completado");
            case CALIFICADA -> System.out.println("📩 Notificación al trabajador: recibiste una nueva calificación");
        }
    }
}
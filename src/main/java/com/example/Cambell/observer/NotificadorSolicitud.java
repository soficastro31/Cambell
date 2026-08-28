package com.example.Cambell.observer;

import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;

@Component
public class NotificadorSolicitud {

    private final List<ObservadorSolicitud> observadores = new ArrayList<>();

    public void suscribir(ObservadorSolicitud observador) {
        observadores.add(observador);
    }

    public void notificar(EventoSolicitud evento) {
        for (ObservadorSolicitud observador : observadores) {
            observador.actualizar(evento);
        }
    }
}
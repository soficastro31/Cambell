package com.example.Cambell.observer;

import com.example.Cambell.model.Solicitud;

public class EventoSolicitud {
    public enum Tipo { CREADA, ACEPTADA, COMPLETADA, CALIFICADA, REASIGNADA }

    private final Tipo tipo;
    private final Solicitud solicitud;

    public EventoSolicitud(Tipo tipo, Solicitud solicitud) {
        this.tipo = tipo;
        this.solicitud = solicitud;
    }

    public Tipo getTipo() { return tipo; }
    public Solicitud getSolicitud() { return solicitud; }
}
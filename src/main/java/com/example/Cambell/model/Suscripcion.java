package com.example.Cambell.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "suscripciones")
public class Suscripcion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Trabajador dueño de la suscripción premium (HU-T15)
    @ManyToOne
    @JoinColumn(name = "trabajador_id", nullable = false, unique = true)
    private Usuario trabajador;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlanEnumeracion plan = PlanEnumeracion.PREMIUM;

    @Column(nullable = false)
    private boolean activa = false;

    @Column(nullable = false)
    private boolean renovacionAutomatica = true;

    @Column(nullable = false)
    private LocalDateTime fechaInicio;

    @Column(nullable = false)
    private LocalDateTime fechaVencimiento;

    public enum PlanEnumeracion {
        PREMIUM
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Usuario getTrabajador() { return trabajador; }
    public void setTrabajador(Usuario trabajador) { this.trabajador = trabajador; }

    public PlanEnumeracion getPlan() { return plan; }
    public void setPlan(PlanEnumeracion plan) { this.plan = plan; }

    public boolean isActiva() { return activa; }
    public void setActiva(boolean activa) { this.activa = activa; }

    public boolean isRenovacionAutomatica() { return renovacionAutomatica; }
    public void setRenovacionAutomatica(boolean renovacionAutomatica) { this.renovacionAutomatica = renovacionAutomatica; }

    public LocalDateTime getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDateTime fechaInicio) { this.fechaInicio = fechaInicio; }

    public LocalDateTime getFechaVencimiento() { return fechaVencimiento; }
    public void setFechaVencimiento(LocalDateTime fechaVencimiento) { this.fechaVencimiento = fechaVencimiento; }
}
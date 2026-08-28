package com.example.Cambell.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "pagos")
public class Pago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Cada solicitud completada genera un pago
    @OneToOne
    @JoinColumn(name = "solicitud_id", nullable = false, unique = true)
    private Solicitud solicitud;

    @ManyToOne
    @JoinColumn(name = "cliente_id", nullable = false)
    private Usuario cliente;

    @ManyToOne
    @JoinColumn(name = "trabajador_id", nullable = false)
    private Usuario trabajador;

    // Monto total que pagó el cliente (precio ofertado)
    @Column(nullable = false)
    private Double montoTotal;

    // Comisión de la plataforma (3%)
    @Column(nullable = false)
    private Double comision;

    // Monto neto que recibe el trabajador tras descontar la comisión
    @Column(nullable = false)
    private Double netoTrabajador;

    @ManyToOne
    @JoinColumn(name = "metodo_pago_id")
    private MetodoPago metodoPago;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoPago estado = EstadoPago.PENDIENTE;

    @Column
    private LocalDateTime fechaPago;

    public enum EstadoPago {
        PENDIENTE, COMPLETADO, FALLIDO
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Solicitud getSolicitud() { return solicitud; }
    public void setSolicitud(Solicitud solicitud) { this.solicitud = solicitud; }

    public Usuario getCliente() { return cliente; }
    public void setCliente(Usuario cliente) { this.cliente = cliente; }

    public Usuario getTrabajador() { return trabajador; }
    public void setTrabajador(Usuario trabajador) { this.trabajador = trabajador; }

    public Double getMontoTotal() { return montoTotal; }
    public void setMontoTotal(Double montoTotal) { this.montoTotal = montoTotal; }

    public Double getComision() { return comision; }
    public void setComision(Double comision) { this.comision = comision; }

    public Double getNetoTrabajador() { return netoTrabajador; }
    public void setNetoTrabajador(Double netoTrabajador) { this.netoTrabajador = netoTrabajador; }

    public MetodoPago getMetodoPago() { return metodoPago; }
    public void setMetodoPago(MetodoPago metodoPago) { this.metodoPago = metodoPago; }

    public EstadoPago getEstado() { return estado; }
    public void setEstado(EstadoPago estado) { this.estado = estado; }

    public LocalDateTime getFechaPago() { return fechaPago; }
    public void setFechaPago(LocalDateTime fechaPago) { this.fechaPago = fechaPago; }
}

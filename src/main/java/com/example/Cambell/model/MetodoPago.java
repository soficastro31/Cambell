package com.example.Cambell.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "metodos_pago")
public class MetodoPago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Cliente dueño del método de pago (HU-C09: registrar método de pago)
    @ManyToOne
    @JoinColumn(name = "cliente_id", nullable = false)
    private Usuario cliente;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoPago tipo;

    @Column(nullable = false)
    private String titular;

    // Número enmascarado (solo últimas 4) para no almacenar datos completos
    @Column(nullable = false)
    private String numeroEnmascarado;

    @Column(nullable = false)
    private LocalDateTime fechaRegistro = LocalDateTime.now();

    public enum TipoPago {
        TARJETA, NEQUI, EFECTIVO
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Usuario getCliente() { return cliente; }
    public void setCliente(Usuario cliente) { this.cliente = cliente; }

    public TipoPago getTipo() { return tipo; }
    public void setTipo(TipoPago tipo) { this.tipo = tipo; }

    public String getTitular() { return titular; }
    public void setTitular(String titular) { this.titular = titular; }

    public String getNumeroEnmascarado() { return numeroEnmascarado; }
    public void setNumeroEnmascarado(String numeroEnmascarado) { this.numeroEnmascarado = numeroEnmascarado; }

    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; }
}

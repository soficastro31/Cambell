package com.example.Cambell.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "anuncios")
public class Anuncio {

    public enum EstadoAnuncio {
        PENDIENTE, PUBLICADO, RECHAZADO
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titulo;

    @Column(nullable = false, length = 1000)
    private String descripcion;

    @Column
    private String rutaImagen;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoAnuncio estado = EstadoAnuncio.PENDIENTE;

    @Column
    private String motivoRechazo;

    @ManyToOne
    @JoinColumn(name = "aliado_id", nullable = false)
    private Usuario aliado;

    @Column(nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getRutaImagen() { return rutaImagen; }
    public void setRutaImagen(String rutaImagen) { this.rutaImagen = rutaImagen; }

    public EstadoAnuncio getEstado() { return estado; }
    public void setEstado(EstadoAnuncio estado) { this.estado = estado; }

    public String getMotivoRechazo() { return motivoRechazo; }
    public void setMotivoRechazo(String motivoRechazo) { this.motivoRechazo = motivoRechazo; }

    public Usuario getAliado() { return aliado; }
    public void setAliado(Usuario aliado) { this.aliado = aliado; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
}

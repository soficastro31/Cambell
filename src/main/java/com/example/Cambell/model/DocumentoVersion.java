package com.example.Cambell.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * HU-S23 / HU-S75: versiones de los documentos de identidad de un trabajador.
 * Cada vez que el trabajador reemplaza un documento, se crea una nueva versión
 * vigente y la anterior se conserva en el historial.
 */
@Entity
@Table(name = "documentos_version")
public class DocumentoVersion {

    public enum TipoDocumento { DOCUMENTO, SELFIE }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "trabajador_id", nullable = false)
    private Usuario trabajador;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoDocumento tipo;

    // Ruta física del archivo cifrado en disco
    @Column(nullable = false)
    private String rutaArchivo;

    // Número de versión (1, 2, 3, ...)
    @Column(nullable = false)
    private int version = 1;

    @Column(nullable = false)
    private boolean vigente = true;

    @Column(nullable = false)
    private LocalDateTime fecha = LocalDateTime.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Usuario getTrabajador() { return trabajador; }
    public void setTrabajador(Usuario trabajador) { this.trabajador = trabajador; }

    public TipoDocumento getTipo() { return tipo; }
    public void setTipo(TipoDocumento tipo) { this.tipo = tipo; }

    public String getRutaArchivo() { return rutaArchivo; }
    public void setRutaArchivo(String rutaArchivo) { this.rutaArchivo = rutaArchivo; }

    public int getVersion() { return version; }
    public void setVersion(int version) { this.version = version; }

    public boolean isVigente() { return vigente; }
    public void setVigente(boolean vigente) { this.vigente = vigente; }

    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
}
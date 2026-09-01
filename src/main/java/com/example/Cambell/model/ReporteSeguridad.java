package com.example.Cambell.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reportes_seguridad")
public class ReporteSeguridad {

    public enum EstadoReporte {
        EN_REVISION, BLOQUEADO, DESCARTADO
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Usuario que realiza el reporte (cliente o trabajador)
    @ManyToOne
    @JoinColumn(name = "reportante_id", nullable = false)
    private Usuario reportante;

    // Usuario reportado por comportamiento inapropiado o inseguro
    @ManyToOne
    @JoinColumn(name = "reportado_id", nullable = false)
    private Usuario reportado;

    // Motivo de la conducta inapropiada o insegura
    @Column(nullable = false, length = 1000)
    private String motivo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoReporte estado = EstadoReporte.EN_REVISION;

    @Column(nullable = false)
    private LocalDateTime fecha = LocalDateTime.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Usuario getReportante() { return reportante; }
    public void setReportante(Usuario reportante) { this.reportante = reportante; }

    public Usuario getReportado() { return reportado; }
    public void setReportado(Usuario reportado) { this.reportado = reportado; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }

    public EstadoReporte getEstado() { return estado; }
    public void setEstado(EstadoReporte estado) { this.estado = estado; }

    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
}

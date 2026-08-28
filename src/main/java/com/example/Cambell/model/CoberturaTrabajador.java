package com.example.Cambell.model;

import jakarta.persistence.*;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "cobertura_trabajador")
public class CoberturaTrabajador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Un trabajador tiene una sola configuración de cobertura
    @OneToOne
    @JoinColumn(name = "trabajador_id", nullable = false, unique = true)
    private Usuario trabajador;

    // HU-T11: radio de cobertura en kilómetros alrededor de su zona
    @Column(nullable = false)
    private Double radioKm = 10.0;

    // Localidades de Bogotá donde ofrece servicio
    @ElementCollection
    @CollectionTable(name = "cobertura_localidades", joinColumns = @JoinColumn(name = "cobertura_id"))
    @Column(name = "localidad")
    private Set<String> localidades = new HashSet<>();

    // HU-S01: categorías de especialidad
    @ElementCollection
    @CollectionTable(name = "cobertura_categorias", joinColumns = @JoinColumn(name = "cobertura_id"))
    @Column(name = "categoria")
    @Enumerated(EnumType.STRING)
    private Set<CategoriaServicio> categorias = new HashSet<>();

    // HU-T10: disponibilidad horaria
    @Column(nullable = false)
    private boolean disponible = true;

    @Column
    private LocalTime horaInicio = LocalTime.of(8, 0);

    @Column
    private LocalTime horaFin = LocalTime.of(18, 0);

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Usuario getTrabajador() { return trabajador; }
    public void setTrabajador(Usuario trabajador) { this.trabajador = trabajador; }

    public Double getRadioKm() { return radioKm; }
    public void setRadioKm(Double radioKm) { this.radioKm = radioKm; }

    public Set<String> getLocalidades() { return localidades; }
    public void setLocalidades(Set<String> localidades) { this.localidades = localidades; }

    public Set<CategoriaServicio> getCategorias() { return categorias; }
    public void setCategorias(Set<CategoriaServicio> categorias) { this.categorias = categorias; }

    public boolean isDisponible() { return disponible; }
    public void setDisponible(boolean disponible) { this.disponible = disponible; }

    public LocalTime getHoraInicio() { return horaInicio; }
    public void setHoraInicio(LocalTime horaInicio) { this.horaInicio = horaInicio; }

    public LocalTime getHoraFin() { return horaFin; }
    public void setHoraFin(LocalTime horaFin) { this.horaFin = horaFin; }
}

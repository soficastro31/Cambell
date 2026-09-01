package com.example.Cambell.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres")
    @Column(nullable = false)
    private String nombre;

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "Ingresa un correo válido (ejemplo@dominio.com)")
    @Column(unique = true, nullable = false)
    private String correo;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 8, max = 64, message = "La contraseña debe tener entre 8 y 64 caracteres")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$",
             message = "La contraseña debe tener al menos 8 caracteres, una mayúscula, una minúscula y un número")
    @Column(nullable = false)
    private String password;

    @NotNull(message = "Debes seleccionar un rol")
    @Enumerated(EnumType.STRING)
    private Rol rol;

    @Enumerated(EnumType.STRING)
    private EstadoVerificacion estadoVerificacion = EstadoVerificacion.PENDIENTE;

    @Column(name = "calificacion_promedio")
    private Double calificacionPromedio = 0.0;

    @Column(name = "total_calificaciones")
    private Integer totalCalificaciones = 0;

    @Pattern(regexp = "^[0-9]{6,15}$", message = "El documento debe tener solo números (6 a 15 dígitos)")
    @Column(name = "numero_documento")
    private String numeroDocumento;

    @Column(name = "ruta_documento")
    private String rutaDocumento;

    @Column(name = "ruta_selfie")
    private String rutaSelfie;

    // Datos del cliente (HU-C01): teléfono obligatorio y ubicación de Bogotá
    @Pattern(regexp = "^[0-9]{7,15}$", message = "El teléfono debe tener solo números (7 a 15 dígitos)")
    @Column(name = "telefono")
    private String telefono;

    // Vigencia del documento de identidad (HU-S03): permite notificar a los
    // trabajadores cuando su documento está próximo a vencer o ya venció.
    @Column
    private java.time.LocalDate fechaVencimientoDocumento;

    // Resultado de la consulta de antecedentes a la API externa (HU-S11/S63)
    @Column(length = 2000)
    private String antecedentes;

    @Column
    private java.time.LocalDateTime fechaConsultaAntecedentes;

    @Column
    private String localidad;

    @Column
    private String barrio;

    // Cuenta bloqueada por el administrador (HU-A04/A06) o desactivada por el usuario (HU-U05)
    @Column(nullable = false)
    private boolean bloqueado = false;

    // Getters y setters (igual que antes)
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public Rol getRol() { return rol; }
    public void setRol(Rol rol) { this.rol = rol; }

    public EstadoVerificacion getEstadoVerificacion() { return estadoVerificacion; }
    public void setEstadoVerificacion(EstadoVerificacion estadoVerificacion) { this.estadoVerificacion = estadoVerificacion; }

    public String getNumeroDocumento() { return numeroDocumento; }
    public void setNumeroDocumento(String numeroDocumento) { this.numeroDocumento = numeroDocumento; }

    public String getRutaDocumento() { return rutaDocumento; }
    public void setRutaDocumento(String rutaDocumento) { this.rutaDocumento = rutaDocumento; }

    public String getRutaSelfie() { return rutaSelfie; }
    public void setRutaSelfie(String rutaSelfie) { this.rutaSelfie = rutaSelfie; }

    public Double getCalificacionPromedio() { return calificacionPromedio; }
public void setCalificacionPromedio(Double calificacionPromedio) { this.calificacionPromedio = calificacionPromedio; }

public Integer getTotalCalificaciones() { return totalCalificaciones; }
public void setTotalCalificaciones(Integer totalCalificaciones) { this.totalCalificaciones = totalCalificaciones; }

public boolean isBloqueado() { return bloqueado; }
public void setBloqueado(boolean bloqueado) { this.bloqueado = bloqueado; }
public String getTelefono() { return telefono; }

    public void setTelefono(String telefono) { this.telefono = telefono; }

    public java.time.LocalDate getFechaVencimientoDocumento() { return fechaVencimientoDocumento; }
    public void setFechaVencimientoDocumento(java.time.LocalDate fechaVencimientoDocumento) { this.fechaVencimientoDocumento = fechaVencimientoDocumento; }

    public String getAntecedentes() { return antecedentes; }
    public void setAntecedentes(String antecedentes) { this.antecedentes = antecedentes; }

    public java.time.LocalDateTime getFechaConsultaAntecedentes() { return fechaConsultaAntecedentes; }
    public void setFechaConsultaAntecedentes(java.time.LocalDateTime fechaConsultaAntecedentes) { this.fechaConsultaAntecedentes = fechaConsultaAntecedentes; }

public String getLocalidad() { return localidad; }
public void setLocalidad(String localidad) { this.localidad = localidad; }

public String getBarrio() { return barrio; }
public void setBarrio(String barrio) { this.barrio = barrio; }
}
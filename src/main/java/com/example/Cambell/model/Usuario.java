package com.example.Cambell.model;

import jakarta.persistence.*;

@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(unique = true, nullable = false)
    private String correo;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    private Rol rol;

    @Enumerated(EnumType.STRING)
    private EstadoVerificacion estadoVerificacion = EstadoVerificacion.PENDIENTE;

    // Getters y setters
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
   
   
    @Column(name = "numero_documento")
    private String numeroDocumento;

    @Column(name = "ruta_documento")
    private String rutaDocumento;

    @Column(name = "ruta_selfie")
    private String rutaSelfie;

    public String getNumeroDocumento() { return numeroDocumento; }
public void setNumeroDocumento(String numeroDocumento) { this.numeroDocumento = numeroDocumento; }

public String getRutaDocumento() { return rutaDocumento; }
public void setRutaDocumento(String rutaDocumento) { this.rutaDocumento = rutaDocumento; }

public String getRutaSelfie() { return rutaSelfie; }
public void setRutaSelfie(String rutaSelfie) { this.rutaSelfie = rutaSelfie; }
}
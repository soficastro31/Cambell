package com.example.Cambell.model;

import jakarta.persistence.*;

@Entity
@Table(name = "solicitudes")
public class Solicitud {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String descripcion;

    @Column(nullable = false)
    private String zona; // geolocalización simulada (ej. "Chapinero, Bogotá")

    // Localidad y barrio de Bogotá seleccionados con el filtro multicriterio
    @Column
    private String localidad;

    @Column
    private String barrio;

    // Categoría del servicio (plomería, electricidad, etc.)
    @Enumerated(EnumType.STRING)
    private CategoriaServicio categoria;

    // Precio que oferta el cliente
    @Column
    private Double precioOfertado;

    // Coordenadas del punto elegido en el mapa Leaflet
    @Column
    private Double latitud;

    @Column
    private Double longitud;

    @Enumerated(EnumType.STRING)
    private EstadoSolicitud estado = EstadoSolicitud.PENDIENTE;

    @ManyToOne
    @JoinColumn(name = "cliente_id", nullable = false)
    private Usuario cliente;

    @ManyToOne
    @JoinColumn(name = "trabajador_id")
    private Usuario trabajador; // null hasta que alguien la acepte

    // Nuevos campos para la calificación y reseña del servicio
    @Column
    private Integer calificacion; // Ej: 1 a 5

    @Column(length = 500)
    private String comentario;

    // Nuevo campo para almacenar la ruta de la foto de evidencia del trabajador
    @Column
    private String rutaEvidencia;

    // Código secreto generado para que el trabajador finalice el trabajo
    @Column
    private String codigoFinalizacion;

    // Getters y setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getZona() { return zona; }
    public void setZona(String zona) { this.zona = zona; }

    public String getLocalidad() { return localidad; }
    public void setLocalidad(String localidad) { this.localidad = localidad; }

    public String getBarrio() { return barrio; }
    public void setBarrio(String barrio) { this.barrio = barrio; }

    public CategoriaServicio getCategoria() { return categoria; }
    public void setCategoria(CategoriaServicio categoria) { this.categoria = categoria; }

    public Double getPrecioOfertado() { return precioOfertado; }
    public void setPrecioOfertado(Double precioOfertado) { this.precioOfertado = precioOfertado; }

    public Double getLatitud() { return latitud; }
    public void setLatitud(Double latitud) { this.latitud = latitud; }

    public Double getLongitud() { return longitud; }
    public void setLongitud(Double longitud) { this.longitud = longitud; }

    public EstadoSolicitud getEstado() { return estado; }
    public void setEstado(EstadoSolicitud estado) { this.estado = estado; }

    public Usuario getCliente() { return cliente; }
    public void setCliente(Usuario cliente) { this.cliente = cliente; }

    public Usuario getTrabajador() { return trabajador; }
    public void setTrabajador(Usuario trabajador) { this.trabajador = trabajador; }

    public Integer getCalificacion() { return calificacion; }
    public void setCalificacion(Integer calificacion) { this.calificacion = calificacion; }

    public String getComentario() { return comentario; }
    public void setComentario(String comentario) { this.comentario = comentario; }

    public String getRutaEvidencia() { return rutaEvidencia; }
    public void setRutaEvidencia(String rutaEvidencia) { this.rutaEvidencia = rutaEvidencia; }

    public String getCodigoFinalizacion() { return codigoFinalizacion; }
    public void setCodigoFinalizacion(String codigoFinalizacion) { this.codigoFinalizacion = codigoFinalizacion; }
}
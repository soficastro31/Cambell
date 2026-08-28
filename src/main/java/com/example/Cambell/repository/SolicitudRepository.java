package com.example.Cambell.repository;

import com.example.Cambell.model.CategoriaServicio;
import com.example.Cambell.model.EstadoSolicitud;
import com.example.Cambell.model.Solicitud;
import com.example.Cambell.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SolicitudRepository extends JpaRepository<Solicitud, Long> {
    List<Solicitud> findByZonaAndEstado(String zona, EstadoSolicitud estado);
    List<Solicitud> findByEstado(EstadoSolicitud estado);
    List<Solicitud> findByCliente(Usuario cliente);
    List<Solicitud> findByTrabajador(Usuario trabajador);
    List<Solicitud> findByTrabajadorAndEstado(Usuario trabajador, EstadoSolicitud estado);

    // Filtro multicriterio para solicitudes disponibles (PENDIENTES)
    @Query("SELECT s FROM Solicitud s WHERE s.estado = :estado " +
           "AND (:localidad IS NULL OR s.localidad = :localidad) " +
           "AND (:barrio IS NULL OR s.barrio = :barrio) " +
           "AND (:categoria IS NULL OR s.categoria = :categoria) " +
           "AND (:precioMin IS NULL OR s.precioOfertado IS NULL OR s.precioOfertado >= :precioMin) " +
           "AND (:precioMax IS NULL OR s.precioOfertado IS NULL OR s.precioOfertado <= :precioMax)")
    List<Solicitud> filtrarDisponibles(@Param("estado") EstadoSolicitud estado,
                                       @Param("localidad") String localidad,
                                       @Param("barrio") String barrio,
                                       @Param("categoria") CategoriaServicio categoria,
                                       @Param("precioMin") Double precioMin,
                                       @Param("precioMax") Double precioMax);
}

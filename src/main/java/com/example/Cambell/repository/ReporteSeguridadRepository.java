package com.example.Cambell.repository;

import com.example.Cambell.model.ReporteSeguridad;
import com.example.Cambell.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReporteSeguridadRepository extends JpaRepository<ReporteSeguridad, Long> {
    List<ReporteSeguridad> findByEstadoOrderByFechaDesc(ReporteSeguridad.EstadoReporte estado);
    List<ReporteSeguridad> findByReportadoOrderByFechaDesc(Usuario reportado);
    List<ReporteSeguridad> findByReportanteOrderByFechaDesc(Usuario reportante);
    List<ReporteSeguridad> findAllByOrderByFechaDesc();
}

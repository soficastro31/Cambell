package com.example.Cambell.service;

import com.example.Cambell.model.ReporteSeguridad;
import com.example.Cambell.model.Usuario;
import com.example.Cambell.repository.ReporteSeguridadRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReporteSeguridadService {

    @Autowired
    private ReporteSeguridadRepository reporteRepository;

    @Autowired
    private UsuarioService usuarioService;

    // HU-C12 / HU-T19: registrar un reporte de seguridad
    public ReporteSeguridad crear(Usuario reportante, Usuario reportado, String motivo) {
        ReporteSeguridad r = new ReporteSeguridad();
        r.setReportante(reportante);
        r.setReportado(reportado);
        r.setMotivo(motivo);
        return reporteRepository.save(r);
    }

    public List<ReporteSeguridad> listarEnRevision() {
        return reporteRepository.findByEstadoOrderByFechaDesc(ReporteSeguridad.EstadoReporte.EN_REVISION);
    }

    public List<ReporteSeguridad> listarTodos() {
        return reporteRepository.findAllByOrderByFechaDesc();
    }

    public ReporteSeguridad buscar(Long id) {
        return reporteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reporte no encontrado"));
    }

    // Número de reportes que acumula un usuario (para HU-S06 en el futuro)
    public long contarReportes(Usuario usuario) {
        return reporteRepository.findByReportadoOrderByFechaDesc(usuario).size();
    }

    // HU-A06: bloquear la cuenta del usuario reportado tras revisión del reporte
    public void bloquearReportado(Long reporteId) {
        ReporteSeguridad r = buscar(reporteId);
        usuarioService.bloquear(r.getReportado().getId());
        r.setEstado(ReporteSeguridad.EstadoReporte.BLOQUEADO);
        reporteRepository.save(r);
    }

    // Marcar un reporte como descartado (sin sanción)
    public void descartar(Long reporteId) {
        ReporteSeguridad r = buscar(reporteId);
        r.setEstado(ReporteSeguridad.EstadoReporte.DESCARTADO);
        reporteRepository.save(r);
    }
}

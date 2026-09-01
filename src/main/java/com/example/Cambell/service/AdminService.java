package com.example.Cambell.service;

import com.example.Cambell.model.*;
import com.example.Cambell.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminService {

    @Autowired
    private SolicitudRepository solicitudRepository;

    @Autowired
    private PagoRepository pagoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private AnuncioRepository anuncioRepository;

    @Autowired
    private ReporteSeguridadRepository reporteRepository;

    // HU-A09: estadísticas generales de operación
    public Map<String, Object> estadisticas() {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalUsuarios", usuarioRepository.count());
        stats.put("clientes", usuarioRepository.findByRolOrderById(Rol.CLIENTE).size());
        stats.put("trabajadores", usuarioRepository.findByRolOrderById(Rol.TRABAJADOR).size());
        stats.put("aliados", usuarioRepository.findByRolOrderById(Rol.ALIADO_COMERCIAL).size());

        List<Solicitud> solicitudes = solicitudRepository.findAll();
        stats.put("totalSolicitudes", solicitudes.size());
        for (EstadoSolicitud estado : EstadoSolicitud.values()) {
            long cont = solicitudes.stream().filter(s -> s.getEstado() == estado).count();
            stats.put("solicitudes_" + estado.name().toLowerCase(), cont);
        }

        List<Pago> pagos = pagoRepository.findByEstado(Pago.EstadoPago.COMPLETADO);
        double ingresosBrutos = pagos.stream()
                .mapToDouble(p -> p.getMontoTotal() == null ? 0 : p.getMontoTotal()).sum();
        double comisionTotal = pagos.stream()
                .mapToDouble(p -> p.getComision() == null ? 0 : p.getComision()).sum();
        stats.put("ingresosBrutos", Math.round(ingresosBrutos * 100.0) / 100.0);
        stats.put("comisionTotal", Math.round(comisionTotal * 100.0) / 100.0);
        stats.put("totalPagosCompletados", pagos.size());

        stats.put("anunciosPendientes",
                anuncioRepository.findByEstadoOrderByFechaCreacionDesc(Anuncio.EstadoAnuncio.PENDIENTE).size());
        stats.put("reportesEnRevision",
                reporteRepository.findByEstadoOrderByFechaDesc(ReporteSeguridad.EstadoReporte.EN_REVISION).size());
        return stats;
    }

    // HU-A11: historial de actividad de un usuario (solicitudes, pagos, mensajes, reportes) en orden cronológico
    public List<Map<String, Object>> historialActividad(Usuario usuario) {
        List<Map<String, Object>> actividad = new ArrayList<>();

        for (Solicitud s : solicitudRepository.findByCliente(usuario)) {
            actividad.add(registro(now(), "Solicitud",
                    "Solicitó: " + s.getDescripcion() + " (" + s.getEstado() + ")"));
        }
        for (Solicitud s : solicitudRepository.findByTrabajador(usuario)) {
            actividad.add(registro(now(), "Solicitud",
                    "Atendió: " + s.getDescripcion() + " (" + s.getEstado() + ")"));
        }
        for (Pago p : pagoRepository.findByClienteOrderByFechaPagoDesc(usuario)) {
            actividad.add(registro(p.getFechaPago(), "Pago", "Pagó $" + p.getMontoTotal() + " (" + p.getEstado() + ")"));
        }
        for (Pago p : pagoRepository.findByTrabajadorOrderByFechaPagoDesc(usuario)) {
            actividad.add(registro(p.getFechaPago(), "Pago", "Recibió $" + p.getNetoTrabajador() + " (" + p.getEstado() + ")"));
        }
        for (ReporteSeguridad r : reporteRepository.findByReportadoOrderByFechaDesc(usuario)) {
            actividad.add(registro(r.getFecha(), "Reporte", "Recibió un reporte: " + r.getMotivo()));
        }
        for (ReporteSeguridad r : reporteRepository.findByReportanteOrderByFechaDesc(usuario)) {
            actividad.add(registro(r.getFecha(), "Reporte", "Realizó un reporte: " + r.getMotivo()));
        }

        actividad.sort((a, b) -> {
            var fa = (java.time.LocalDateTime) a.get("fecha");
            var fb = (java.time.LocalDateTime) b.get("fecha");
            if (fa == null) return 1;
            if (fb == null) return -1;
            return fb.compareTo(fa);
        });
        return actividad;
    }

    private Map<String, Object> registro(java.time.LocalDateTime fecha, String tipo, String detalle) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("fecha", fecha);
        m.put("tipo", tipo);
        m.put("detalle", detalle);
        return m;
    }

    private java.time.LocalDateTime now() {
        return java.time.LocalDateTime.now();
    }
}

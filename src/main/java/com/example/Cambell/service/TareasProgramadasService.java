package com.example.Cambell.service;

import com.example.Cambell.model.*;
import com.example.Cambell.observer.EventoSolicitud;
import com.example.Cambell.observer.NotificadorSolicitud;
import com.example.Cambell.repository.ReporteSeguridadRepository;
import com.example.Cambell.repository.SolicitudRepository;
import com.example.Cambell.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class TareasProgramadasService {

    @Autowired
    private SolicitudRepository solicitudRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ReporteSeguridadRepository reporteRepository;

    @Autowired
    private NotificacionService notificacionService;

    @Autowired
    private SuscripcionService suscripcionService;

    @Autowired
    private NotificadorSolicitud notificadorSolicitud;

    @Autowired
    private PagoService pagoService;

    @Autowired
    private DocumentoVersionService documentoVersionService;

    // Tiempos límite configurados
    @Value("${cambell.tiempo.no-atendida-horas:24}")
    private long horasNoAtendida;

    @Value("${cambell.reportes.umbral:3}")
    private int umbralReportes;

    @Value("${cambell.reasignacion.horas:1}")
    private long horasReasignacion;

    @Value("${cambell.documentos.dias-aviso:30}")
    private int diasAvisoDocumento;

    @Value("${cambell.calificacion.dias-recordatorio:2}")
    private int diasRecordatorioCalificacion;

    // HU-S02 / HU-S54: reasignar (re-notificar) una solicitud que ningún
    // trabajador ha aceptado o rechazado dentro del tiempo límite.
    @Scheduled(cron = "${cambell.cron.reasignacion:0 * * * * *}")
    public void reasignarSolicitudesSinRespuesta() {
        LocalDateTime limite = LocalDateTime.now().minus(Duration.ofHours(horasReasignacion));
        List<Solicitud> pendientes = solicitudRepository.findByEstado(EstadoSolicitud.PENDIENTE);
        for (Solicitud solicitud : pendientes) {
            LocalDateTime referencia = solicitud.getFechaUltimaReasignacion() != null
                    ? solicitud.getFechaUltimaReasignacion()
                    : solicitud.getFechaCreacion();
            if (referencia != null && referencia.isBefore(limite)) {
                solicitud.setFechaUltimaReasignacion(LocalDateTime.now());
                solicitudRepository.save(solicitud);
                notificadorSolicitud.notificar(
                        new EventoSolicitud(EventoSolicitud.Tipo.REASIGNADA, solicitud));
            }
        }
    }

    // HU-S07: cerrar solicitudes que no fueron aceptadas por ningún trabajador
    // dentro del tiempo límite. Se marcan como NO_ATENDIDA y se avisa al cliente.
    @Scheduled(cron = "${cambell.cron.no-atendida:0 0 * * * *}")
    public void cerrarSolicitudesNoAtendidas() {
        LocalDateTime limite = LocalDateTime.now().minus(Duration.ofHours(horasNoAtendida));
        List<Solicitud> stale = solicitudRepository
                .findByEstadoAndFechaCreacionBefore(EstadoSolicitud.PENDIENTE, limite);
        for (Solicitud solicitud : stale) {
            solicitud.setEstado(EstadoSolicitud.NO_ATENDIDA);
            solicitudRepository.save(solicitud);
            notificacionService.crear(solicitud.getCliente(), "SOLICITUD",
                    "Tu solicitud \"" + solicitud.getDescripcion()
                            + "\" no fue atendida por ningún trabajador en el tiempo límite.",
                    solicitud);
        }
    }

    // HU-S03: verificar periódicamente la vigencia de los documentos de los
    // trabajadores y notificar cuando están próximos a vencer o ya vencieron.
    @Scheduled(cron = "${cambell.cron.documentos:0 0 6 * * *}")
    public void verificarVigenciaDocumentos() {
        LocalDate hoy = LocalDate.now();
        LocalDate aviso = hoy.plusDays(diasAvisoDocumento);
        List<Usuario> trabajadores = usuarioRepository.findByRolOrderById(Rol.TRABAJADOR);
        for (Usuario u : trabajadores) {
            if (u.getFechaVencimientoDocumento() == null) continue;
            if (u.getFechaVencimientoDocumento().isBefore(hoy)) {
                notificacionService.crear(u, "DOCUMENTO",
                        "Tu documento de identidad venció el "
                                + u.getFechaVencimientoDocumento() + ". Actualízalo para continuar trabajando.");
            } else if (!u.getFechaVencimientoDocumento().isAfter(aviso)) {
                notificacionService.crear(u, "DOCUMENTO",
                        "Tu documento de identidad vence el "
                                + u.getFechaVencimientoDocumento() + ". Actualízalo para evitar bloqueos.");
            }
        }
    }

    // HU-S06: suspender temporalmente cuentas que acumulan reportes de seguridad
    // por encima del umbral configurado y notificar al administrador.
    @Scheduled(cron = "${cambell.cron.reportes:0 0 * * * *}")
    public void suspenderCuentasConReportes() {
        // Contar reportes en revisión agrupados por usuario reportado
        List<ReporteSeguridad> enRevision = reporteRepository
                .findByEstadoOrderByFechaDesc(ReporteSeguridad.EstadoReporte.EN_REVISION);
        java.util.Map<Usuario, Long> conteo = new java.util.HashMap<>();
        for (ReporteSeguridad r : enRevision) {
            conteo.merge(r.getReportado(), 1L, Long::sum);
        }
        List<Usuario> admins = usuarioRepository.findByRolOrderById(Rol.ADMINISTRADOR);
        for (java.util.Map.Entry<Usuario, Long> e : conteo.entrySet()) {
            Usuario usuario = e.getKey();
            if (!usuario.isBloqueado() && e.getValue() >= umbralReportes) {
                usuario.setBloqueado(true);
                usuarioRepository.save(usuario);
                notificacionService.crear(usuario, "SEGURIDAD",
                        "Tu cuenta fue suspendida temporalmente por acumular " + e.getValue()
                                + " reportes de seguridad.");
                for (Usuario admin : admins) {
                    notificacionService.crear(admin, "SEGURIDAD",
                            "La cuenta de " + usuario.getNombre() + " (" + usuario.getCorreo()
                                    + ") fue suspendida por superar el umbral de reportes.");
                }
            }
        }
    }

    // HU-S09: recordar a los clientes calificar servicios completados que aún no
    // han sido calificados dentro del tiempo configurado.
    @Scheduled(cron = "${cambell.cron.calificacion:0 0 12 * * *}")
    public void recordarCalificarServicios() {
        LocalDateTime limite = LocalDateTime.now().minus(Duration.ofDays(diasRecordatorioCalificacion));
        List<Solicitud> sinCalificar = solicitudRepository
                .findByEstadoAndCalificacionIsNullAndFechaFinalizacionBefore(EstadoSolicitud.COMPLETADA, limite);
        for (Solicitud solicitud : sinCalificar) {
            notificacionService.crear(solicitud.getCliente(), "CALIFICACION",
                    "Aún no calificas el servicio \"" + solicitud.getDescripcion()
                            + "\". Tu valoración ayuda a la comunidad.");
        }
    }

    // HU-S15: renovar automáticamente las suscripciones vencidas
    @Scheduled(cron = "${cambell.cron.suscripciones:0 0 2 * * *}")
    public void renovarSuscripciones() {
        suscripcionService.renovarVencidas();
    }

    // HU-S05: recordar a los trabajadores cuando su suscripción está próxima a vencer
    @Scheduled(cron = "${cambell.cron.suscripciones-aviso:0 0 9 * * *}")
    public void avisarSuscripcionesProximasAVencer() {
        suscripcionService.recordarProximosAVencer();
    }

    // HU-S13/S65: transferir el pago neto al medio de cobro del trabajador
    @Scheduled(cron = "${cambell.cron.transferencias:0 30 * * * *}")
    public void transferirPagosNetos() {
        pagoService.transferirNetoTrabajadores();
    }

    // HU-S22/S74: eliminar archivos obsoletos (documentos vencidos o reemplazados)
    @Scheduled(cron = "${cambell.cron.limpieza:0 0 4 * * *}")
    public void limpiarArchivosObsoletos() {
        documentoVersionService.limpiarArchivosObsoletos();
    }
}
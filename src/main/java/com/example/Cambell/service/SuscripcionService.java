package com.example.Cambell.service;

import com.example.Cambell.model.Notificacion;
import com.example.Cambell.model.Suscripcion;
import com.example.Cambell.model.Usuario;
import com.example.Cambell.repository.SuscripcionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class SuscripcionService {

    // Duración del periodo premium (meses). HU-T15/T16/T17.
    private static final long MESES_PERIODO = 1;
    private static final long MESES_DESDE_ACTIVACION = 12;

    @Autowired
    private SuscripcionRepository suscripcionRepository;

    @Autowired
    private NotificacionService notificacionService;

    // HU-T17: ver la suscripción vigente del trabajador (si existe)
    public Optional<Suscripcion> deTrabajador(Usuario trabajador) {
        return suscripcionRepository.findByTrabajador(trabajador);
    }

    // HU-T15 + HU-S16: contratar y activar el plan premium tras confirmar el pago.
    public Suscripcion contratar(Usuario trabajador) {
        Suscripcion s = suscripcionRepository.findByTrabajador(trabajador).orElseGet(Suscripcion::new);
        if (s.getTrabajador() == null) {
            s.setTrabajador(trabajador);
        }
        s.setActiva(true);
        s.setRenovacionAutomatica(true);
        s.setFechaInicio(LocalDateTime.now());
        s.setFechaVencimiento(LocalDateTime.now().plusMonths(MESES_PERIODO));
        suscripcionRepository.save(s);
        notificacionService.crear(trabajador, "SUSCRIPCION",
                "Tu suscripción premium está activa. Ya disfrutas de mayor visibilidad en las búsquedas.");
        return s;
    }

    // HU-T16: cancelar la suscripción (se detiene la renovación automática y los
    // beneficios se mantienen hasta el fin del periodo pagado).
    public Suscripcion cancelar(Usuario trabajador) {
        Suscripcion s = suscripcionRepository.findByTrabajador(trabajador)
                .orElseThrow(() -> new RuntimeException("No tienes una suscripción activa"));
        s.setRenovacionAutomatica(false);
        suscripcionRepository.save(s);
        notificacionService.crear(trabajador, "SUSCRIPCION",
                "Tu suscripción no se renovará automáticamente. Los beneficios siguen activos hasta el " +
                        s.getFechaVencimiento() + ".");
        return s;
    }

    // HU-T17: sólo se considera activa si no ha vencido
    public boolean estaActiva(Usuario trabajador) {
        return suscripcionRepository.findByTrabajador(trabajador)
                .map(Suscripcion::isActiva)
                .orElse(false);
    }

    // HU-S15: renovar una suscripción vencida con renovación automática activa
    public void renovarVencidas() {
        List<Suscripcion> vencidas = suscripcionRepository
                .findByActivaTrueAndRenovacionAutomaticaTrueAndFechaVencimientoBefore(LocalDateTime.now());
        for (Suscripcion s : vencidas) {
            s.setFechaInicio(LocalDateTime.now());
            s.setFechaVencimiento(LocalDateTime.now().plusMonths(MESES_PERIODO));
            suscripcionRepository.save(s);
            notificacionService.crear(s.getTrabajador(), "SUSCRIPCION",
                    "Tu suscripción premium se renovó automáticamente. Nueva vigencia hasta " +
                            s.getFechaVencimiento() + ".");
        }
    }

    // HU-S05: recordar a los trabajadores con suscripción activa próxima a vencer
    public void recordarProximosAVencer() {
        LocalDateTime ahora = LocalDateTime.now();
        List<Suscripcion> proximas = suscripcionRepository
                .findByActivaTrueAndFechaVencimientoBetween(ahora, ahora.plusDays(7));
        for (Suscripcion s : proximas) {
            notificacionService.crear(s.getTrabajador(), "SUSCRIPCION",
                    "Tu suscripción premium vence el " + s.getFechaVencimiento().toLocalDate()
                            + ". Si no la cancelas, se renovará automáticamente.");
        }
    }
}
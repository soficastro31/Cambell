package com.example.Cambell.service;

import com.example.Cambell.model.*;
import com.example.Cambell.repository.CoberturaTrabajadorRepository;
import com.example.Cambell.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class CoberturaTrabajadorService {

    @Autowired
    private CoberturaTrabajadorRepository coberturaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    // HU-T10/T11: guardar la configuración de cobertura del trabajador
    public CoberturaTrabajador guardarCobertura(Usuario trabajador, Double radioKm,
                                                Set<String> localidades, Set<CategoriaServicio> categorias,
                                                boolean disponible, LocalTime horaInicio, LocalTime horaFin) {
        CoberturaTrabajador cob = coberturaRepository.findByTrabajador(trabajador).orElseGet(CoberturaTrabajador::new);
        cob.setTrabajador(trabajador);
        cob.setRadioKm(radioKm == null ? 10.0 : radioKm);
        cob.setLocalidades(localidades == null ? new java.util.HashSet<>() : localidades);
        cob.setCategorias(categorias == null ? new java.util.HashSet<>() : categorias);
        cob.setDisponible(disponible);
        if (horaInicio != null) cob.setHoraInicio(horaInicio);
        if (horaFin != null) cob.setHoraFin(horaFin);
        return coberturaRepository.save(cob);
    }

    public Optional<CoberturaTrabajador> obtenerCobertura(Usuario trabajador) {
        return coberturaRepository.findByTrabajador(trabajador);
    }

    // HU-S01: ¿es esta solicitud compatible con la cobertura del trabajador?
    public boolean esCompatible(Solicitud solicitud, CoberturaTrabajador cob) {
        if (!cob.isDisponible()) {
            return false;
        }
        // Disponibilidad horaria actual
        LocalTime ahora = LocalTime.now();
        if (ahora.isBefore(cob.getHoraInicio()) || ahora.isAfter(cob.getHoraFin())) {
            return false;
        }
        // Especialidad
        if (!cob.getCategorias().isEmpty() && solicitud.getCategoria() != null
                && !cob.getCategorias().contains(solicitud.getCategoria())) {
            return false;
        }
        // Ubicación: si define localidades de cobertura
        if (!cob.getLocalidades().isEmpty()) {
            boolean enLocalidad = solicitud.getLocalidad() != null
                    && cob.getLocalidades().contains(solicitud.getLocalidad());
            boolean enRadio = enRadioDeCobertura(solicitud, cob);
            if (!enLocalidad && !enRadio) {
                return false;
            }
        }
        return true;
    }

    // Comprueba si el punto exacto de la solicitud está a radioKm de alguna localidad cubierta
    private boolean enRadioDeCobertura(Solicitud solicitud, CoberturaTrabajador cob) {
        if (solicitud.getLatitud() == null || solicitud.getLongitud() == null) {
            return false;
        }
        for (String loc : cob.getLocalidades()) {
            Double[] centro = centroLocalidad(loc);
            if (centro == null) continue;
            double d = distanciaKm(centro[0], centro[1], solicitud.getLatitud(), solicitud.getLongitud());
            if (d <= cob.getRadioKm()) {
                return true;
            }
        }
        return false;
    }

    private Double[] centroLocalidad(String localidad) {
        List<Object[]> barrios = LocalidadesBogota.LOCALIDADES.get(localidad);
        if (barrios == null || barrios.isEmpty()) return null;
        Object[] b = barrios.get(0);
        return new Double[]{(Double) b[1], (Double) b[2]};
    }

    private double distanciaKm(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    // Trabajadores APROBADOS con cobertura compatible para una solicitud (HU-S01/S17)
    public List<Usuario> trabajadoresCompatibles(Solicitud solicitud) {
        List<Usuario> compatibles = new ArrayList<>();
        for (Usuario u : usuarioRepository.findAll()) {
            if (u.getRol() != Rol.TRABAJADOR || u.getEstadoVerificacion() != EstadoVerificacion.APROBADO) {
                continue;
            }
            Optional<CoberturaTrabajador> cob = coberturaRepository.findByTrabajador(u);
            if (cob.isPresent() && esCompatible(solicitud, cob.get())) {
                compatibles.add(u);
            }
        }
        return compatibles;
    }
}

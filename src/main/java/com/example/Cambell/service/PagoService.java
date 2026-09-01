package com.example.Cambell.service;

import com.example.Cambell.model.*;
import com.example.Cambell.repository.MetodoPagoRepository;
import com.example.Cambell.repository.PagoRepository;
import com.example.Cambell.repository.SolicitudRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class PagoService {

    // Comisión de la plataforma (HU-S08): 3% del monto total
    public static final double COMISION = 0.03;

    @Autowired
    private PagoRepository pagoRepository;

    @Autowired
    private MetodoPagoRepository metodoPagoRepository;

    @Autowired
    private SolicitudRepository solicitudRepository;

    // HU-C09: registrar un método de pago (se guarda el número enmascarado, no el completo)
    public MetodoPago registrarMetodo(Usuario cliente, MetodoPago.TipoPago tipo,
                                      String titular, String numeroCompleto) {
        MetodoPago metodo = new MetodoPago();
        metodo.setCliente(cliente);
        metodo.setTipo(tipo);
        metodo.setTitular(titular);
        metodo.setNumeroEnmascarado(enmascarar(numeroCompleto));
        return metodoPagoRepository.save(metodo);
    }

    private String enmascarar(String numero) {
        String limpio = numero == null ? "" : numero.replaceAll("[^0-9]", "");
        if (limpio.length() <= 4) {
            return limpio;
        }
        return "•••• " + limpio.substring(limpio.length() - 4);
    }

    public List<MetodoPago> listarMetodos(Usuario cliente) {
        return metodoPagoRepository.findByCliente(cliente);
    }

    // HU-C10: pagar un servicio completado.
    // HU-S08: se aplica la comisión del 3%.
    // HU-S12: el trabajador recibe el neto tras descontar la comisión.
    public Pago pagar(Long solicitudId, Long metodoId) {
        Solicitud solicitud = solicitudRepository.findById(solicitudId)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));

        if (pagoRepository.findBySolicitud(solicitud).isPresent()) {
            throw new RuntimeException("Esta solicitud ya fue pagada");
        }
        if (solicitud.getEstado() != EstadoSolicitud.COMPLETADA) {
            throw new RuntimeException("El servicio debe estar completado para poder pagarlo");
        }

        MetodoPago metodo = metodoId == null ? null
                : metodoPagoRepository.findById(metodoId).orElse(null);

        Pago pago = new Pago();
        pago.setSolicitud(solicitud);
        pago.setCliente(solicitud.getCliente());
        pago.setTrabajador(solicitud.getTrabajador());

        Double monto = solicitud.getPrecioOfertado();
        pago.setMontoTotal(monto);

        if (monto == null || monto <= 0 || metodo == null) {
            // HU-S14: transacción fallida (monto inválido o sin método de pago)
            pago.setMontoTotal(monto != null ? monto : 0.0);
            pago.setComision(0.0);
            pago.setNetoTrabajador(0.0);
            pago.setEstado(Pago.EstadoPago.FALLIDO);
            return pagoRepository.save(pago);
        }

        double comision = Math.round(monto * COMISION * 100.0) / 100.0;
        pago.setComision(comision);
        pago.setNetoTrabajador(Math.round((monto - comision) * 100.0) / 100.0);
        pago.setMetodoPago(metodo);
        pago.setEstado(Pago.EstadoPago.COMPLETADO);
        pago.setFechaPago(LocalDateTime.now());
        return pagoRepository.save(pago);
    }

    // HU-C11: historial de pagos del cliente y comprobante
    public Optional<Pago> buscarPagoDeSolicitud(Long solicitudId) {
        Optional<Solicitud> solicitud = solicitudRepository.findById(solicitudId);
        return solicitud.flatMap(pagoRepository::findBySolicitud);
    }

    public Solicitud buscarSolicitud(Long solicitudId) {
        return solicitudRepository.findById(solicitudId)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));
    }

    public Pago buscarPago(Long id) {
        return pagoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pago no encontrado"));
    }

    public List<Pago> historialCliente(Usuario cliente) {
        return pagoRepository.findByClienteOrderByFechaPagoDesc(cliente);
    }

    public List<Pago> historialTrabajador(Usuario trabajador) {
        return pagoRepository.findByTrabajadorOrderByFechaPagoDesc(trabajador);
    }

    // HU-T13/T14: ingresos acumulados netos del trabajador
    public Double ingresosTrabajador(Usuario trabajador) {
        List<Pago> pagos = pagoRepository.findByTrabajadorOrderByFechaPagoDesc(trabajador);
        double total = 0;
        for (Pago p : pagos) {
            if (p.getEstado() == Pago.EstadoPago.COMPLETADO && p.getNetoTrabajador() != null) {
                total += p.getNetoTrabajador();
            }
        }
        return Math.round(total * 100.0) / 100.0;
    }

    // HU-T14: pagos completados del trabajador dentro de un rango de fechas (opcional)
    public List<Pago> pagosTrabajadorEnRango(Usuario trabajador, LocalDateTime desde, LocalDateTime hasta) {
        List<Pago> pagos = pagoRepository.findByTrabajadorOrderByFechaPagoDesc(trabajador);
        return pagos.stream()
                .filter(p -> p.getEstado() == Pago.EstadoPago.COMPLETADO && p.getFechaPago() != null)
                .filter(p -> (desde == null || !p.getFechaPago().isBefore(desde))
                        && (hasta == null || !p.getFechaPago().isAfter(hasta)))
                .collect(java.util.stream.Collectors.toList());
    }

    public List<Pago> pagosCompletados() {
        return pagoRepository.findByEstado(Pago.EstadoPago.COMPLETADO);
    }

    // HU-S13/S65: transferir el neto al medio de cobro del trabajador una vez
    // procesado el pago y retenida la comisión. Se acredita el neto y se marca
    // la transacción como transferida.
    public void transferirNetoTrabajadores() {
        List<Pago> pendientes = pagoRepository.findByEstadoAndTransferenciaCompletadaFalse(Pago.EstadoPago.COMPLETADO);
        for (Pago pago : pendientes) {
            if (pago.getNetoTrabajador() == null || pago.getNetoTrabajador() <= 0) {
                pago.setEstadoTransferencia(Pago.EstadoTransferencia.FALLIDO);
                continue;
            }
            pago.setTransferenciaCompletada(true);
            pago.setEstadoTransferencia(Pago.EstadoTransferencia.TRANSFERIDO);
            pago.setFechaTransferencia(LocalDateTime.now());
            pagoRepository.save(pago);
        }
    }
}

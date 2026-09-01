package com.example.Cambell.service;

import com.example.Cambell.model.Pago;
import com.example.Cambell.model.Reembolso;
import com.example.Cambell.repository.PagoRepository;
import com.example.Cambell.repository.ReembolsoRepository;
import com.example.Cambell.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ReembolsoService {

    @Autowired
    private ReembolsoRepository reembolsoRepository;

    @Autowired
    private PagoRepository pagoRepository;

    @Autowired
    private NotificacionService notificacionService;

    public List<Reembolso> listarTodos() {
        return reembolsoRepository.findAllByOrderByFechaDesc();
    }

    public boolean yaReembolsado(Long pagoId) {
        return pagoRepository.findById(pagoId)
                .flatMap(reembolsoRepository::findByPago)
                .isPresent();
    }

    // HU-A10: iniciar un reembolso de un pago justificado. Si el pago ya se reembolsó se rechaza.
    public Reembolso reembolsar(Long pagoId, String motivo) {
        Pago pago = pagoRepository.findById(pagoId)
                .orElseThrow(() -> new RuntimeException("Pago no encontrado"));
        if (reembolsoRepository.findByPago(pago).isPresent()) {
            throw new RuntimeException("Este pago ya fue reembolsado");
        }
        Reembolso r = new Reembolso();
        r.setPago(pago);
        r.setMotivo(motivo == null || motivo.isBlank() ? "Disputa justificada" : motivo.trim());
        r.setFecha(LocalDateTime.now());
        reembolsoRepository.save(r);
        notificacionService.crear(pago.getCliente(), "PAGO",
                "Se inició un reembolso de $" + pago.getMontoTotal() + " por el servicio: "
                        + pago.getSolicitud().getDescripcion() + ".");
        return r;
    }
}
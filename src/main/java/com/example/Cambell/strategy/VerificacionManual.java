package com.example.Cambell.strategy;

import org.springframework.stereotype.Component;

@Component
public class VerificacionManual implements EstrategiaVerificacion {

    @Override
    public double verificar(String rutaDocumento, String rutaSelfie) {
        return -1; // siempre cae a revisión manual del administrador
    }
}
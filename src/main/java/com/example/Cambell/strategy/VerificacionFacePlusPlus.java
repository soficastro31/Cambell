package com.example.Cambell.strategy;

import com.example.Cambell.service.VerificacionFacialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class VerificacionFacePlusPlus implements EstrategiaVerificacion {

    @Autowired
    private VerificacionFacialService verificacionFacialService;

    @Override
    public double verificar(String rutaDocumento, String rutaSelfie) {
        return verificacionFacialService.compararRostros(rutaDocumento, rutaSelfie);
    }
}
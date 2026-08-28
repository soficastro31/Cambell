package com.example.Cambell.strategy;

public interface EstrategiaVerificacion {
    /**
     * Retorna un porcentaje de confianza (0-100), o -1 si no se pudo determinar.
     */
    double verificar(String rutaDocumento, String rutaSelfie);
}
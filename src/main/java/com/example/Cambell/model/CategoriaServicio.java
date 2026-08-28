package com.example.Cambell.model;

public enum CategoriaServicio {
    PLOMERIA("Plomería"),
    ELECTRICIDAD("Electricidad"),
    CERRAJERIA("Cerrajería"),
    ELECTRODOMESTICOS("Electrodomésticos"),
    AIRE_ACONDICIONADO("Aire acondicionado / Refrigeración"),
    CARPINTERIA("Carpintería");

    private final String etiqueta;

    CategoriaServicio(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}

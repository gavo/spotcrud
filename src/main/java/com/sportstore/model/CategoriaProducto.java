package com.sportstore.model;

/**
 * Categorías disponibles para los productos deportivos.
 */
public enum CategoriaProducto {
    CALZADO("Calzado Deportivo"),
    ROPA_DEPORTIVA("Ropa Deportiva"),
    BALONES("Balones y Pelotas"),
    FITNESS("Fitness y Gimnasio"),
    RAQUETAS("Tenis y Pádel"),
    NATACION("Natación"),
    CICLISMO("Ciclismo"),
    ACCESORIOS("Accesorios");

    private final String etiqueta;

    CategoriaProducto(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    @Override
    public String toString() {
        return etiqueta;
    }
}

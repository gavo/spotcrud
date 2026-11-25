package com.sportstore.model;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Representa un producto deportivo en el inventario.
 */
public class Producto {
    private long id;
    private String codigo;
    private String nombre;
    private CategoriaProducto categoria;
    private String marca;
    private String talla;
    private BigDecimal precio;
    private int stock;
    private boolean activo;

    public Producto() {
        this.activo = true;
        this.precio = BigDecimal.ZERO;
    }

    public Producto(long id, String codigo, String nombre, CategoriaProducto categoria,
                    String marca, String talla, BigDecimal precio, int stock, boolean activo) {
        this.id = id;
        this.codigo = codigo;
        this.nombre = nombre;
        this.categoria = categoria;
        this.marca = marca;
        this.talla = talla;
        this.precio = precio != null ? precio : BigDecimal.ZERO;
        this.stock = stock;
        this.activo = activo;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public CategoriaProducto getCategoria() {
        return categoria;
    }

    public void setCategoria(CategoriaProducto categoria) {
        this.categoria = categoria;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getTalla() {
        return talla;
    }

    public void setTalla(String talla) {
        this.talla = talla;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio != null ? precio : BigDecimal.ZERO;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Producto other)) return false;
        return id == other.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return codigo + " - " + nombre + " (" + marca + ")";
    }
}

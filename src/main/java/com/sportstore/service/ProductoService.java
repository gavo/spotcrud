package com.sportstore.service;

import com.sportstore.model.CategoriaProducto;
import com.sportstore.model.Producto;
import com.sportstore.repository.ProductoRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Servicio de negocio para el CRUD y búsqueda de productos deportivos.
 */
public class ProductoService {

    private final ProductoRepository repository;

    public ProductoService(ProductoRepository repository) {
        this.repository = repository;
    }

    public List<Producto> listarTodos() {
        return repository.findAll();
    }

    public List<Producto> listarActivosConStock() {
        return repository.findAll().stream()
                .filter(p -> p.isActivo() && p.getStock() > 0)
                .toList();
    }

    public List<Producto> buscar(String texto, CategoriaProducto categoria) {
        String filtro = texto == null ? "" : texto.trim().toLowerCase(Locale.ROOT);
        return repository.findAll().stream()
                .filter(p -> categoria == null || p.getCategoria() == categoria)
                .filter(p -> filtro.isEmpty()
                        || p.getCodigo().toLowerCase(Locale.ROOT).contains(filtro)
                        || p.getNombre().toLowerCase(Locale.ROOT).contains(filtro)
                        || p.getMarca().toLowerCase(Locale.ROOT).contains(filtro))
                .toList();
    }

    public Optional<Producto> buscarPorId(long id) {
        return repository.findById(id);
    }

    public Producto crear(Producto producto) {
        validar(producto, true);
        normalizar(producto);
        producto.setId(0);
        return repository.save(producto);
    }

    public Producto actualizar(Producto producto) {
        if (producto.getId() <= 0 || repository.findById(producto.getId()).isEmpty()) {
            throw new ValidationException("El producto que intenta actualizar no existe.");
        }
        validar(producto, false);
        normalizar(producto);
        return repository.save(producto);
    }

    public boolean eliminar(long id) {
        return repository.deleteById(id);
    }

    private void validar(Producto p, boolean esNuevo) {
        if (p == null) {
            throw new ValidationException("El producto no puede ser nulo.");
        }
        if (p.getCodigo() == null || p.getCodigo().isBlank()) {
            throw new ValidationException("El código SKU es obligatorio.");
        }
        if (p.getNombre() == null || p.getNombre().isBlank()) {
            throw new ValidationException("El nombre del producto es obligatorio.");
        }
        if (p.getCategoria() == null) {
            throw new ValidationException("Debe seleccionar una categoría.");
        }
        if (p.getMarca() == null || p.getMarca().isBlank()) {
            throw new ValidationException("La marca es obligatoria.");
        }
        if (p.getTalla() == null || p.getTalla().isBlank()) {
            throw new ValidationException("La talla o medida es obligatoria.");
        }
        if (p.getPrecio() == null || p.getPrecio().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("El precio debe ser mayor a 0.");
        }
        if (p.getStock() < 0) {
            throw new ValidationException("El stock no puede ser negativo.");
        }

        Optional<Producto> existente = repository.findByCodigo(p.getCodigo());
        if (existente.isPresent() && (esNuevo || existente.get().getId() != p.getId())) {
            throw new ValidationException("Ya existe un producto con el código '" + p.getCodigo().trim() + "'.");
        }
    }

    private void normalizar(Producto p) {
        p.setCodigo(p.getCodigo().trim().toUpperCase(Locale.ROOT));
        p.setNombre(p.getNombre().trim());
        p.setMarca(p.getMarca().trim());
        p.setTalla(p.getTalla().trim());
    }
}

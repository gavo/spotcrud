package com.sportstore.repository;

import com.sportstore.model.CategoriaProducto;
import com.sportstore.model.Producto;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para gestionar la persistencia de productos deportivos en CSV.
 */
public class ProductoRepository {

    private static final String HEADER = "id,codigo,nombre,categoria,marca,talla,precio,stock,activo";

    private final Path filePath;
    private final List<Producto> productos = new ArrayList<>();
    private long nextId = 1;

    public ProductoRepository(Path filePath) {
        this(filePath, true);
    }

    public ProductoRepository(Path filePath, boolean seedIfMissing) {
        this.filePath = filePath;
        cargar(seedIfMissing);
    }

    public List<Producto> findAll() {
        return new ArrayList<>(productos);
    }

    public Optional<Producto> findById(long id) {
        return productos.stream().filter(p -> p.getId() == id).findFirst();
    }

    public Optional<Producto> findByCodigo(String codigo) {
        if (codigo == null) {
            return Optional.empty();
        }
        String normalized = codigo.trim();
        return productos.stream()
                .filter(p -> p.getCodigo() != null && p.getCodigo().equalsIgnoreCase(normalized))
                .findFirst();
    }

    public Producto save(Producto producto) {
        if (producto.getId() == 0) {
            producto.setId(nextId++);
            productos.add(copiar(producto));
        } else {
            for (int i = 0; i < productos.size(); i++) {
                if (productos.get(i).getId() == producto.getId()) {
                    productos.set(i, copiar(producto));
                    guardar();
                    return producto;
                }
            }
            if (producto.getId() >= nextId) {
                nextId = producto.getId() + 1;
            }
            productos.add(copiar(producto));
        }
        guardar();
        return producto;
    }

    public boolean deleteById(long id) {
        boolean removed = productos.removeIf(p -> p.getId() == id);
        if (removed) {
            guardar();
        }
        return removed;
    }

    private void cargar(boolean seedIfMissing) {
        productos.clear();
        nextId = 1;

        if (!Files.exists(filePath)) {
            if (seedIfMissing) {
                cargarDatosIniciales();
                guardar();
            }
            return;
        }

        try {
            List<String> lines = Files.readAllLines(filePath, StandardCharsets.UTF_8);
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i).trim();
                if (line.isEmpty() || (i == 0 && line.startsWith("id,"))) {
                    continue;
                }
                List<String> cols = CsvHelper.parseCsvLine(line);
                if (cols.size() < 9) {
                    continue;
                }
                Producto p = new Producto(
                        Long.parseLong(cols.get(0)),
                        cols.get(1),
                        cols.get(2),
                        CategoriaProducto.valueOf(cols.get(3)),
                        cols.get(4),
                        cols.get(5),
                        new BigDecimal(cols.get(6)),
                        Integer.parseInt(cols.get(7)),
                        Boolean.parseBoolean(cols.get(8))
                );
                productos.add(p);
                if (p.getId() >= nextId) {
                    nextId = p.getId() + 1;
                }
            }
        } catch (IOException | RuntimeException e) {
            throw new IllegalStateException("Error al cargar productos desde " + filePath, e);
        }
    }

    private void guardar() {
        try {
            Path parent = filePath.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            List<String> lines = new ArrayList<>();
            lines.add(HEADER);
            for (Producto p : productos) {
                lines.add(CsvHelper.toCsvLine(
                        String.valueOf(p.getId()),
                        p.getCodigo(),
                        p.getNombre(),
                        p.getCategoria().name(),
                        p.getMarca(),
                        p.getTalla(),
                        p.getPrecio().toPlainString(),
                        String.valueOf(p.getStock()),
                        String.valueOf(p.isActivo())
                ));
            }
            Files.write(filePath, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Error al guardar productos en " + filePath, e);
        }
    }

    private void cargarDatosIniciales() {
        productos.add(new Producto(nextId++, "DEP-001", "Balón Fútbol Pro Match", CategoriaProducto.BALONES, "Adidas", "N° 5", new BigDecimal("49.90"), 25, true));
        productos.add(new Producto(nextId++, "DEP-002", "Tenis Running Air Velocity", CategoriaProducto.CALZADO, "Nike", "42", new BigDecimal("119.50"), 14, true));
        productos.add(new Producto(nextId++, "DEP-003", "Camiseta Dry-Fit Entrenamiento", CategoriaProducto.ROPA_DEPORTIVA, "Puma", "M", new BigDecimal("34.00"), 32, true));
        productos.add(new Producto(nextId++, "DEP-004", "Par de Mancuernas Hexagonales", CategoriaProducto.FITNESS, "Everlast", "10 kg", new BigDecimal("58.00"), 12, true));
        productos.add(new Producto(nextId++, "DEP-005", "Raqueta Tenis Carbon Pro", CategoriaProducto.RAQUETAS, "Wilson", "L3", new BigDecimal("165.00"), 7, true));
        productos.add(new Producto(nextId++, "DEP-006", "Gafas Natación Antiempañante", CategoriaProducto.NATACION, "Speedo", "Única", new BigDecimal("24.50"), 20, true));
        productos.add(new Producto(nextId++, "DEP-007", "Casco Ciclismo Aerodinámico", CategoriaProducto.CICLISMO, "Giro", "M/L", new BigDecimal("89.00"), 9, true));
        productos.add(new Producto(nextId++, "DEP-008", "Botella Térmica Deportiva 1L", CategoriaProducto.ACCESORIOS, "Under Armour", "1 L", new BigDecimal("19.90"), 40, true));
    }

    private static Producto copiar(Producto p) {
        return new Producto(
                p.getId(),
                p.getCodigo(),
                p.getNombre(),
                p.getCategoria(),
                p.getMarca(),
                p.getTalla(),
                p.getPrecio(),
                p.getStock(),
                p.isActivo()
        );
    }
}

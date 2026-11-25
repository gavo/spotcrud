package com.sportstore.repository;

import com.sportstore.model.DetalleVenta;
import com.sportstore.model.Venta;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Repositorio para gestionar ventas y sus detalles en archivos CSV.
 */
public class VentaRepository {

    private static final String VENTAS_HEADER = "id,folio,fecha,cliente,metodo_pago,subtotal,impuesto,total";
    private static final String DETALLES_HEADER = "venta_id,producto_id,codigo_producto,nombre_producto,cantidad,precio_unitario,subtotal";

    private final Path ventasPath;
    private final Path detallesPath;
    private final List<Venta> ventas = new ArrayList<>();
    private long nextId = 1;

    public VentaRepository(Path ventasPath, Path detallesPath) {
        this.ventasPath = ventasPath;
        this.detallesPath = detallesPath;
        cargar();
    }

    public List<Venta> findAll() {
        return new ArrayList<>(ventas);
    }

    public Optional<Venta> findById(long id) {
        return ventas.stream().filter(v -> v.getId() == id).findFirst();
    }

    public Venta save(Venta venta) {
        if (venta.getId() == 0) {
            venta.setId(nextId++);
            if (venta.getFolio() == null || venta.getFolio().isBlank()) {
                venta.setFolio(String.format("V-%05d", venta.getId()));
            }
            ventas.add(venta);
        } else {
            boolean updated = false;
            for (int i = 0; i < ventas.size(); i++) {
                if (ventas.get(i).getId() == venta.getId()) {
                    ventas.set(i, venta);
                    updated = true;
                    break;
                }
            }
            if (!updated) {
                if (venta.getId() >= nextId) {
                    nextId = venta.getId() + 1;
                }
                ventas.add(venta);
            }
        }
        guardar();
        return venta;
    }

    public boolean deleteById(long id) {
        boolean removed = ventas.removeIf(v -> v.getId() == id);
        if (removed) {
            guardar();
        }
        return removed;
    }

    private void cargar() {
        ventas.clear();
        nextId = 1;
        Map<Long, Venta> porId = new LinkedHashMap<>();

        if (Files.exists(ventasPath)) {
            try {
                List<String> lines = Files.readAllLines(ventasPath, StandardCharsets.UTF_8);
                for (int i = 0; i < lines.size(); i++) {
                    String line = lines.get(i).trim();
                    if (line.isEmpty() || (i == 0 && line.startsWith("id,"))) {
                        continue;
                    }
                    List<String> cols = CsvHelper.parseCsvLine(line);
                    if (cols.size() < 8) {
                        continue;
                    }
                    Venta v = new Venta();
                    v.setId(Long.parseLong(cols.get(0)));
                    v.setFolio(cols.get(1));
                    v.setFecha(LocalDateTime.parse(cols.get(2)));
                    v.setCliente(cols.get(3));
                    v.setMetodoPago(cols.get(4));
                    v.setSubtotal(new BigDecimal(cols.get(5)));
                    v.setImpuesto(new BigDecimal(cols.get(6)));
                    v.setTotal(new BigDecimal(cols.get(7)));
                    porId.put(v.getId(), v);
                    ventas.add(v);
                    if (v.getId() >= nextId) {
                        nextId = v.getId() + 1;
                    }
                }
            } catch (IOException | RuntimeException e) {
                throw new IllegalStateException("Error al cargar ventas desde " + ventasPath, e);
            }
        }

        if (Files.exists(detallesPath)) {
            try {
                List<String> lines = Files.readAllLines(detallesPath, StandardCharsets.UTF_8);
                for (int i = 0; i < lines.size(); i++) {
                    String line = lines.get(i).trim();
                    if (line.isEmpty() || (i == 0 && line.startsWith("venta_id,"))) {
                        continue;
                    }
                    List<String> cols = CsvHelper.parseCsvLine(line);
                    if (cols.size() < 7) {
                        continue;
                    }
                    long ventaId = Long.parseLong(cols.get(0));
                    Venta venta = porId.get(ventaId);
                    if (venta != null) {
                        DetalleVenta d = new DetalleVenta();
                        d.setProductoId(Long.parseLong(cols.get(1)));
                        d.setCodigoProducto(cols.get(2));
                        d.setNombreProducto(cols.get(3));
                        d.setCantidad(Integer.parseInt(cols.get(4)));
                        d.setPrecioUnitario(new BigDecimal(cols.get(5)));
                        d.setSubtotal(new BigDecimal(cols.get(6)));
                        venta.getDetalles().add(d);
                    }
                }
            } catch (IOException | RuntimeException e) {
                throw new IllegalStateException("Error al cargar detalles desde " + detallesPath, e);
            }
        }
    }

    private void guardar() {
        try {
            if (ventasPath.getParent() != null) {
                Files.createDirectories(ventasPath.getParent());
            }
            if (detallesPath.getParent() != null) {
                Files.createDirectories(detallesPath.getParent());
            }

            List<String> ventasLines = new ArrayList<>();
            ventasLines.add(VENTAS_HEADER);
            List<String> detallesLines = new ArrayList<>();
            detallesLines.add(DETALLES_HEADER);

            for (Venta v : ventas) {
                ventasLines.add(CsvHelper.toCsvLine(
                        String.valueOf(v.getId()),
                        v.getFolio(),
                        v.getFecha().toString(),
                        v.getCliente(),
                        v.getMetodoPago(),
                        v.getSubtotal().toPlainString(),
                        v.getImpuesto().toPlainString(),
                        v.getTotal().toPlainString()
                ));
                for (DetalleVenta d : v.getDetalles()) {
                    detallesLines.add(CsvHelper.toCsvLine(
                            String.valueOf(v.getId()),
                            String.valueOf(d.getProductoId()),
                            d.getCodigoProducto(),
                            d.getNombreProducto(),
                            String.valueOf(d.getCantidad()),
                            d.getPrecioUnitario().toPlainString(),
                            d.getSubtotal().toPlainString()
                    ));
                }
            }

            Files.write(ventasPath, ventasLines, StandardCharsets.UTF_8);
            Files.write(detallesPath, detallesLines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Error al guardar ventas", e);
        }
    }
}

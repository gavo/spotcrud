package com.sportstore.service;

import com.sportstore.model.DetalleVenta;
import com.sportstore.model.Producto;
import com.sportstore.model.Venta;
import com.sportstore.repository.ProductoRepository;
import com.sportstore.repository.VentaRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Servicio de negocio para registrar, consultar y anular ventas.
 */
public class VentaService {

    public static final BigDecimal TASA_IMPUESTO = new BigDecimal("0.16");

    private final VentaRepository ventaRepository;
    private final ProductoRepository productoRepository;

    public VentaService(VentaRepository ventaRepository, ProductoRepository productoRepository) {
        this.ventaRepository = ventaRepository;
        this.productoRepository = productoRepository;
    }

    public List<Venta> listarVentas() {
        return ventaRepository.findAll();
    }

    public Venta registrarVenta(String cliente, String metodoPago, List<DetalleVenta> items) {
        if (items == null || items.isEmpty()) {
            throw new ValidationException("Debe agregar al menos un producto al carrito de venta.");
        }
        String clienteFinal = (cliente == null || cliente.isBlank()) ? "Público General" : cliente.trim();
        String metodoFinal = (metodoPago == null || metodoPago.isBlank()) ? "Efectivo" : metodoPago.trim();

        List<Producto> productosParaActualizar = new ArrayList<>();
        List<DetalleVenta> detallesValidados = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;

        for (DetalleVenta item : items) {
            if (item.getCantidad() <= 0) {
                throw new ValidationException("La cantidad de cada producto debe ser mayor a 0.");
            }
            Producto producto = productoRepository.findById(item.getProductoId())
                    .orElseThrow(() -> new ValidationException("Producto no encontrado: " + item.getNombreProducto()));
            if (!producto.isActivo()) {
                throw new ValidationException("El producto '" + producto.getNombre() + "' está inactivo.");
            }
            if (producto.getStock() < item.getCantidad()) {
                throw new ValidationException("Stock insuficiente para '" + producto.getNombre()
                        + "'. Disponible: " + producto.getStock());
            }

            producto.setStock(producto.getStock() - item.getCantidad());
            productosParaActualizar.add(producto);

            DetalleVenta detalle = new DetalleVenta(
                    producto.getId(),
                    producto.getCodigo(),
                    producto.getNombre(),
                    item.getCantidad(),
                    producto.getPrecio()
            );
            detallesValidados.add(detalle);
            subtotal = subtotal.add(detalle.getSubtotal());
        }

        BigDecimal impuesto = subtotal.multiply(TASA_IMPUESTO).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = subtotal.add(impuesto).setScale(2, RoundingMode.HALF_UP);

        for (Producto p : productosParaActualizar) {
            productoRepository.save(p);
        }

        Venta venta = new Venta();
        venta.setFecha(LocalDateTime.now());
        venta.setCliente(clienteFinal);
        venta.setMetodoPago(metodoFinal);
        venta.setSubtotal(subtotal.setScale(2, RoundingMode.HALF_UP));
        venta.setImpuesto(impuesto);
        venta.setTotal(total);
        venta.setDetalles(detallesValidados);

        return ventaRepository.save(venta);
    }

    public boolean anularVenta(long ventaId) {
        Venta venta = ventaRepository.findById(ventaId)
                .orElseThrow(() -> new ValidationException("La venta seleccionada no existe."));

        for (DetalleVenta d : venta.getDetalles()) {
            productoRepository.findById(d.getProductoId()).ifPresent(p -> {
                p.setStock(p.getStock() + d.getCantidad());
                productoRepository.save(p);
            });
        }
        return ventaRepository.deleteById(ventaId);
    }

    public BigDecimal calcularTotalIngresos() {
        return ventaRepository.findAll().stream()
                .map(Venta::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }
}

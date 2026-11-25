package com.sportstore;

import com.sportstore.model.CategoriaProducto;
import com.sportstore.model.DetalleVenta;
import com.sportstore.model.Producto;
import com.sportstore.model.Venta;
import com.sportstore.repository.ProductoRepository;
import com.sportstore.repository.VentaRepository;
import com.sportstore.service.ProductoService;
import com.sportstore.service.ValidationException;
import com.sportstore.service.VentaService;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Pruebas unitarias y de integración para el CRUD de productos y el sistema de ventas.
 * Ejecutar con: java -ea ... com.sportstore.SportStoreTests
 */
public class SportStoreTests {

    public static void main(String[] args) throws IOException {
        Path tempDir = Files.createTempDirectory("sportstore-test-");
        try {
            testProductoCrudYPersistencia(tempDir);
            testValidacionesProducto(tempDir);
            testRegistroYAnulacionDeVenta(tempDir);
            System.out.println("TODAS LAS PRUEBAS PASARON EXITOSAMENTE (3/3 suites).");
        } finally {
            // Limpieza básica de archivos temporales
            try (var stream = Files.walk(tempDir)) {
                stream.sorted((a, b) -> b.compareTo(a)).forEach(p -> {
                    try {
                        Files.deleteIfExists(p);
                    } catch (IOException ignored) {
                    }
                });
            }
        }
    }

    private static void testProductoCrudYPersistencia(Path tempDir) {
        Path csv = tempDir.resolve("productos_crud.csv");
        ProductoRepository repo = new ProductoRepository(csv, false);
        ProductoService service = new ProductoService(repo);

        // Create
        Producto creado = service.crear(new Producto(
                0, "tst-001", "Balón Basket Pro", CategoriaProducto.BALONES,
                "Spalding", "N° 7", new BigDecimal("65.00"), 10, true
        ));
        assert creado.getId() == 1 : "El ID generado debe ser 1";
        assert "TST-001".equals(creado.getCodigo()) : "El código debe normalizarse a mayúsculas";

        // Read & Search
        List<Producto> encontrados = service.buscar("Spalding", CategoriaProducto.BALONES);
        assert encontrados.size() == 1 : "Debe encontrar 1 producto por marca y categoría";

        // Update
        creado.setPrecio(new BigDecimal("70.00"));
        creado.setStock(15);
        service.actualizar(creado);

        // Verificar persistencia recargando repositorio desde disco
        ProductoRepository repoRecargado = new ProductoRepository(csv, false);
        Producto recargado = repoRecargado.findById(creado.getId()).orElseThrow();
        assert recargado.getPrecio().compareTo(new BigDecimal("70.00")) == 0 : "Precio actualizado debe persistir";
        assert recargado.getStock() == 15 : "Stock actualizado debe persistir";

        // Delete
        boolean eliminado = service.eliminar(creado.getId());
        assert eliminado : "Debe eliminar el producto existente";
        assert service.listarTodos().isEmpty() : "La lista debe quedar vacía tras eliminar";
    }

    private static void testValidacionesProducto(Path tempDir) {
        Path csv = tempDir.resolve("productos_val.csv");
        ProductoRepository repo = new ProductoRepository(csv, false);
        ProductoService service = new ProductoService(repo);

        service.crear(new Producto(
                0, "DUP-01", "Guantes Gym", CategoriaProducto.FITNESS,
                "Nike", "M", new BigDecimal("25.00"), 5, true
        ));

        boolean falloDuplicado = false;
        try {
            service.crear(new Producto(
                    0, "dup-01", "Otro producto", CategoriaProducto.FITNESS,
                    "Adidas", "L", new BigDecimal("30.00"), 3, true
            ));
        } catch (ValidationException ex) {
            falloDuplicado = true;
        }
        assert falloDuplicado : "Debe rechazar códigos SKU duplicados";

        boolean falloPrecio = false;
        try {
            service.crear(new Producto(
                    0, "NEW-02", "Cuerda Saltar", CategoriaProducto.FITNESS,
                    "Puma", "Única", BigDecimal.ZERO, 5, true
            ));
        } catch (ValidationException ex) {
            falloPrecio = true;
        }
        assert falloPrecio : "Debe rechazar precio <= 0";
    }

    private static void testRegistroYAnulacionDeVenta(Path tempDir) {
        Path prodCsv = tempDir.resolve("productos_venta.csv");
        Path ventasCsv = tempDir.resolve("ventas.csv");
        Path detallesCsv = tempDir.resolve("venta_detalles.csv");

        ProductoRepository prodRepo = new ProductoRepository(prodCsv, false);
        VentaRepository ventaRepo = new VentaRepository(ventasCsv, detallesCsv);
        ProductoService prodService = new ProductoService(prodRepo);
        VentaService ventaService = new VentaService(ventaRepo, prodRepo);

        Producto p = prodService.crear(new Producto(
                0, "TEN-100", "Raqueta Pro", CategoriaProducto.RAQUETAS,
                "Babolat", "L2", new BigDecimal("100.00"), 8, true
        ));

        Venta venta = ventaService.registrarVenta(
                "Carlos Pérez",
                "Tarjeta de Débito",
                List.of(new DetalleVenta(p.getId(), p.getCodigo(), p.getNombre(), 2, p.getPrecio()))
        );

        assert "V-00001".equals(venta.getFolio()) : "Folio esperado V-00001";
        assert venta.getSubtotal().compareTo(new BigDecimal("200.00")) == 0 : "Subtotal esperado 200.00";
        assert venta.getImpuesto().compareTo(new BigDecimal("32.00")) == 0 : "IVA 16% esperado 32.00";
        assert venta.getTotal().compareTo(new BigDecimal("232.00")) == 0 : "Total esperado 232.00";

        Producto trasVenta = prodRepo.findById(p.getId()).orElseThrow();
        assert trasVenta.getStock() == 6 : "El stock debe descontarse de 8 a 6";

        // Verificar recarga de venta desde CSV
        VentaRepository ventaRepoRecargado = new VentaRepository(ventasCsv, detallesCsv);
        assert ventaRepoRecargado.findAll().size() == 1 : "La venta debe persistir en CSV";
        assert ventaRepoRecargado.findAll().get(0).getDetalles().size() == 1 : "Los detalles deben persistir en CSV";

        // Anular venta y verificar restauración de stock
        boolean anulada = ventaService.anularVenta(venta.getId());
        assert anulada : "La venta debe anularse";
        Producto trasAnulacion = prodRepo.findById(p.getId()).orElseThrow();
        assert trasAnulacion.getStock() == 8 : "El stock debe restaurarse a 8 al anular";
    }
}

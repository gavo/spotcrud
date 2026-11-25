package com.sportstore;

import com.sportstore.repository.ProductoRepository;
import com.sportstore.repository.VentaRepository;
import com.sportstore.service.ProductoService;
import com.sportstore.service.VentaService;
import com.sportstore.ui.MainFrame;

import javax.swing.*;
import java.nio.file.Path;

/**
 * Punto de entrada de la aplicación Swing.
 */
public class App {

    public static void main(String[] args) {
        configurarLookAndFeel();

        Path dataDir = Path.of("data");
        ProductoRepository productoRepo = new ProductoRepository(dataDir.resolve("productos.csv"));
        VentaRepository ventaRepo = new VentaRepository(
                dataDir.resolve("ventas.csv"),
                dataDir.resolve("venta_detalles.csv")
        );

        ProductoService productoService = new ProductoService(productoRepo);
        VentaService ventaService = new VentaService(ventaRepo, productoRepo);

        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame(productoService, ventaService);
            frame.setVisible(true);
        });
    }

    private static void configurarLookAndFeel() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignored) {
            // Usa el Look and Feel por defecto si Nimbus no está disponible.
        }
    }
}

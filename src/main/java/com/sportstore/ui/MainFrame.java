package com.sportstore.ui;

import com.sportstore.service.ProductoService;
import com.sportstore.service.VentaService;

import javax.swing.*;
import java.awt.*;

/**
 * Ventana principal del Sistema de Ventas de Productos Deportivos.
 */
public class MainFrame extends JFrame {

    private ProductosPanel productosPanel;
    private PuntoVentaPanel puntoVentaPanel;
    private HistorialVentasPanel historialVentasPanel;

    public MainFrame(ProductoService productoService, VentaService ventaService) {
        super("SportStore — Sistema de Ventas de Productos Deportivos");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 760);
        setMinimumSize(new Dimension(980, 640));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        add(crearHeader(), BorderLayout.NORTH);

        productosPanel = new ProductosPanel(productoService, () -> {
            if (puntoVentaPanel != null) {
                puntoVentaPanel.recargarProductosDisponibles();
            }
        });

        puntoVentaPanel = new PuntoVentaPanel(productoService, ventaService, () -> {
            productosPanel.recargarTabla();
            if (historialVentasPanel != null) {
                historialVentasPanel.recargarVentas();
            }
        });

        historialVentasPanel = new HistorialVentasPanel(ventaService, () -> {
            productosPanel.recargarTabla();
            puntoVentaPanel.recargarProductosDisponibles();
        });

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(Theme.FONT_SUBTITLE);
        tabs.addTab("  Productos (CRUD)  ", productosPanel);
        tabs.addTab("  Punto de Venta  ", puntoVentaPanel);
        tabs.addTab("  Historial de Ventas  ", historialVentasPanel);

        add(tabs, BorderLayout.CENTER);
        add(crearFooter(), BorderLayout.SOUTH);
    }

    private JPanel crearHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Theme.HEADER_BG);
        header.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));

        JLabel title = new JLabel("SportStore — Sistema de Ventas de Productos Deportivos");
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.HEADER_FG);

        JLabel subtitle = new JLabel("Java 17 • Swing • Persistencia local CSV");
        subtitle.setFont(Theme.FONT_SMALL);
        subtitle.setForeground(new Color(148, 163, 184));

        header.add(title, BorderLayout.WEST);
        header.add(subtitle, BorderLayout.EAST);
        return header;
    }

    private JPanel crearFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 6));
        footer.setBackground(new Color(226, 232, 240));
        JLabel info = new JLabel("Datos almacenados automáticamente en la carpeta ./data/");
        info.setFont(Theme.FONT_SMALL);
        info.setForeground(Theme.TEXT_MUTED);
        footer.add(info);
        return footer;
    }
}

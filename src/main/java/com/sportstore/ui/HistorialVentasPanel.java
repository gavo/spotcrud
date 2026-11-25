package com.sportstore.ui;

import com.sportstore.model.DetalleVenta;
import com.sportstore.model.Venta;
import com.sportstore.service.VentaService;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Panel para consultar el historial de ventas, sus detalles y anular ventas.
 */
public class HistorialVentasPanel extends JPanel {

    private static final DateTimeFormatter FECHA_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final VentaService ventaService;
    private final Runnable onVentaAnulada;

    private final VentaTableModel ventaModel = new VentaTableModel();
    private final JTable tablaVentas = new JTable(ventaModel);

    private final DetalleTableModel detalleModel = new DetalleTableModel();
    private final JTable tablaDetalles = new JTable(detalleModel);

    private final JLabel lblResumen = new JLabel("Ventas: 0 | Ingresos totales: $0.00");
    private final JButton btnAnular = Theme.createButton("Anular Venta Seleccionada", Theme.DANGER);

    public HistorialVentasPanel(VentaService ventaService, Runnable onVentaAnulada) {
        this.ventaService = ventaService;
        this.onVentaAnulada = onVentaAnulada;

        setLayout(new BorderLayout(12, 12));
        setBackground(Theme.BG_APP);
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        add(crearBarraSuperior(), BorderLayout.NORTH);
        add(crearSplitTablas(), BorderLayout.CENTER);

        configurarEventos();
        recargarVentas();
    }

    private JPanel crearBarraSuperior() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Theme.BG_CARD);
        panel.setBorder(Theme.cardBorder(null));

        lblResumen.setFont(Theme.FONT_SUBTITLE);
        lblResumen.setForeground(Theme.TEXT_PRIMARY);

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        acciones.setBackground(Theme.BG_CARD);
        JButton btnRefrescar = Theme.createButton("Actualizar", Theme.PRIMARY);
        btnRefrescar.addActionListener(e -> recargarVentas());
        btnAnular.setEnabled(false);

        acciones.add(btnRefrescar);
        acciones.add(btnAnular);

        panel.add(lblResumen, BorderLayout.WEST);
        panel.add(acciones, BorderLayout.EAST);
        return panel;
    }

    private JSplitPane crearSplitTablas() {
        Theme.styleTable(tablaVentas);
        Theme.styleTable(tablaDetalles);

        JScrollPane scrollVentas = new JScrollPane(tablaVentas);
        scrollVentas.setBorder(Theme.cardBorder("Ventas Registradas"));
        scrollVentas.getViewport().setBackground(Color.WHITE);

        JScrollPane scrollDetalles = new JScrollPane(tablaDetalles);
        scrollDetalles.setBorder(Theme.cardBorder("Detalle de la Venta Seleccionada"));
        scrollDetalles.getViewport().setBackground(Color.WHITE);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scrollVentas, scrollDetalles);
        split.setResizeWeight(0.55);
        split.setDividerSize(8);
        return split;
    }

    private void configurarEventos() {
        tablaVentas.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = tablaVentas.getSelectedRow();
                if (row >= 0) {
                    Venta v = ventaModel.getVentaAt(row);
                    detalleModel.setDetalles(v.getDetalles());
                    btnAnular.setEnabled(true);
                } else {
                    detalleModel.setDetalles(List.of());
                    btnAnular.setEnabled(false);
                }
            }
        });

        btnAnular.addActionListener(e -> anularVentaSeleccionada());
    }

    public void recargarVentas() {
        List<Venta> ventas = ventaService.listarVentas();
        ventaModel.setVentas(ventas);
        detalleModel.setDetalles(List.of());
        btnAnular.setEnabled(false);
        lblResumen.setText("Ventas registradas: " + ventas.size()
                + "   |   Ingresos totales: " + Theme.formatCurrency(ventaService.calcularTotalIngresos()));
    }

    private void anularVentaSeleccionada() {
        int row = tablaVentas.getSelectedRow();
        if (row < 0) {
            return;
        }
        Venta v = ventaModel.getVentaAt(row);
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "¿Anular la venta " + v.getFolio() + "? Se restaurará el stock de los productos vendidos.",
                "Confirmar anulación",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );
        if (confirm == JOptionPane.YES_OPTION) {
            ventaService.anularVenta(v.getId());
            recargarVentas();
            if (onVentaAnulada != null) {
                onVentaAnulada.run();
            }
        }
    }

    private static class VentaTableModel extends AbstractTableModel {
        private final String[] columns = {"ID", "Folio", "Fecha", "Cliente", "Método Pago", "Subtotal", "IVA", "Total"};
        private List<Venta> ventas = new ArrayList<>();

        public void setVentas(List<Venta> ventas) {
            this.ventas = new ArrayList<>(ventas);
            fireTableDataChanged();
        }

        public Venta getVentaAt(int row) {
            return ventas.get(row);
        }

        @Override
        public int getRowCount() {
            return ventas.size();
        }

        @Override
        public int getColumnCount() {
            return columns.length;
        }

        @Override
        public String getColumnName(int column) {
            return columns[column];
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            Venta v = ventas.get(rowIndex);
            return switch (columnIndex) {
                case 0 -> v.getId();
                case 1 -> v.getFolio();
                case 2 -> v.getFecha().format(FECHA_FMT);
                case 3 -> v.getCliente();
                case 4 -> v.getMetodoPago();
                case 5 -> Theme.formatCurrency(v.getSubtotal());
                case 6 -> Theme.formatCurrency(v.getImpuesto());
                case 7 -> Theme.formatCurrency(v.getTotal());
                default -> "";
            };
        }
    }

    private static class DetalleTableModel extends AbstractTableModel {
        private final String[] columns = {"Código", "Producto", "Cantidad", "Precio Unit.", "Subtotal"};
        private List<DetalleVenta> detalles = new ArrayList<>();

        public void setDetalles(List<DetalleVenta> detalles) {
            this.detalles = new ArrayList<>(detalles);
            fireTableDataChanged();
        }

        @Override
        public int getRowCount() {
            return detalles.size();
        }

        @Override
        public int getColumnCount() {
            return columns.length;
        }

        @Override
        public String getColumnName(int column) {
            return columns[column];
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            DetalleVenta d = detalles.get(rowIndex);
            return switch (columnIndex) {
                case 0 -> d.getCodigoProducto();
                case 1 -> d.getNombreProducto();
                case 2 -> d.getCantidad();
                case 3 -> Theme.formatCurrency(d.getPrecioUnitario());
                case 4 -> Theme.formatCurrency(d.getSubtotal());
                default -> "";
            };
        }
    }
}

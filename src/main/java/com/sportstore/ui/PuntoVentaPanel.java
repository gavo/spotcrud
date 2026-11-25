package com.sportstore.ui;

import com.sportstore.model.DetalleVenta;
import com.sportstore.model.Producto;
import com.sportstore.model.Venta;
import com.sportstore.service.ProductoService;
import com.sportstore.service.ValidationException;
import com.sportstore.service.VentaService;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Panel de Punto de Venta para registrar ventas de productos deportivos.
 */
public class PuntoVentaPanel extends JPanel {

    private final ProductoService productoService;
    private final VentaService ventaService;
    private final Runnable onSaleCompleted;

    private final JComboBox<Producto> cmbProducto = new JComboBox<>();
    private final JSpinner spnCantidad = new JSpinner(new SpinnerNumberModel(1, 1, 999, 1));
    private final JLabel lblStockInfo = new JLabel("Stock disponible: -");

    private final CarritoTableModel carritoModel = new CarritoTableModel();
    private final JTable tablaCarrito = new JTable(carritoModel);

    private final JTextField txtCliente = new JTextField("Público General");
    private final JComboBox<String> cmbMetodoPago = new JComboBox<>(
            new String[]{"Efectivo", "Tarjeta de Débito", "Tarjeta de Crédito", "Transferencia"}
    );
    private final JLabel lblSubtotal = new JLabel("$0.00");
    private final JLabel lblImpuesto = new JLabel("$0.00");
    private final JLabel lblTotal = new JLabel("$0.00");

    public PuntoVentaPanel(ProductoService productoService, VentaService ventaService, Runnable onSaleCompleted) {
        this.productoService = productoService;
        this.ventaService = ventaService;
        this.onSaleCompleted = onSaleCompleted;

        setLayout(new BorderLayout(12, 12));
        setBackground(Theme.BG_APP);
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        add(crearPanelSeleccion(), BorderLayout.NORTH);
        add(crearPanelCarrito(), BorderLayout.CENTER);
        add(crearPanelResumen(), BorderLayout.EAST);

        recargarProductosDisponibles();
    }

    private JPanel crearPanelSeleccion() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        panel.setBackground(Theme.BG_CARD);
        panel.setBorder(Theme.cardBorder("Agregar Producto al Carrito"));

        cmbProducto.setPreferredSize(new Dimension(340, 28));
        cmbProducto.setFont(Theme.FONT_BODY);
        cmbProducto.addActionListener(e -> actualizarInfoStock());

        spnCantidad.setPreferredSize(new Dimension(70, 28));
        spnCantidad.setFont(Theme.FONT_BODY);

        lblStockInfo.setFont(Theme.FONT_SMALL);
        lblStockInfo.setForeground(Theme.TEXT_MUTED);

        JButton btnAgregar = Theme.createButton("Agregar al Carrito", Theme.PRIMARY);
        btnAgregar.addActionListener(e -> agregarAlCarrito());

        panel.add(new JLabel("Producto:"));
        panel.add(cmbProducto);
        panel.add(new JLabel("Cantidad:"));
        panel.add(spnCantidad);
        panel.add(btnAgregar);
        panel.add(lblStockInfo);
        return panel;
    }

    private JPanel crearPanelCarrito() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(Theme.BG_CARD);
        panel.setBorder(Theme.cardBorder("Carrito de Compra"));

        Theme.styleTable(tablaCarrito);
        JScrollPane scroll = new JScrollPane(tablaCarrito);
        scroll.getViewport().setBackground(Color.WHITE);

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        acciones.setBackground(Theme.BG_CARD);
        JButton btnQuitar = Theme.createButton("Quitar Seleccionado", Theme.WARNING);
        JButton btnVaciar = Theme.createButton("Vaciar Carrito", Theme.NEUTRAL);

        btnQuitar.addActionListener(e -> {
            int row = tablaCarrito.getSelectedRow();
            if (row >= 0) {
                carritoModel.removeAt(row);
                actualizarTotales();
            }
        });
        btnVaciar.addActionListener(e -> {
            carritoModel.clear();
            actualizarTotales();
        });

        acciones.add(btnQuitar);
        acciones.add(btnVaciar);

        panel.add(scroll, BorderLayout.CENTER);
        panel.add(acciones, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel crearPanelResumen() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Theme.BG_CARD);
        panel.setBorder(Theme.cardBorder("Resumen de Venta"));
        panel.setPreferredSize(new Dimension(320, 0));

        JPanel form = new JPanel(new GridLayout(0, 1, 6, 8));
        form.setBackground(Theme.BG_CARD);

        JLabel lblCli = new JLabel("Cliente:");
        lblCli.setFont(Theme.FONT_BOLD);
        txtCliente.setFont(Theme.FONT_BODY);

        JLabel lblMet = new JLabel("Método de Pago:");
        lblMet.setFont(Theme.FONT_BOLD);
        cmbMetodoPago.setFont(Theme.FONT_BODY);

        form.add(lblCli);
        form.add(txtCliente);
        form.add(lblMet);
        form.add(cmbMetodoPago);

        JPanel totales = new JPanel(new GridLayout(3, 2, 8, 10));
        totales.setBackground(Theme.BG_CARD);
        totales.setBorder(BorderFactory.createEmptyBorder(16, 0, 16, 0));

        lblSubtotal.setFont(Theme.FONT_BODY);
        lblSubtotal.setHorizontalAlignment(SwingConstants.RIGHT);
        lblImpuesto.setFont(Theme.FONT_BODY);
        lblImpuesto.setHorizontalAlignment(SwingConstants.RIGHT);
        lblTotal.setFont(Theme.FONT_TITLE);
        lblTotal.setForeground(Theme.PRIMARY);
        lblTotal.setHorizontalAlignment(SwingConstants.RIGHT);

        totales.add(new JLabel("Subtotal:"));
        totales.add(lblSubtotal);
        totales.add(new JLabel("IVA (16%):"));
        totales.add(lblImpuesto);
        JLabel lblTotTitle = new JLabel("TOTAL:");
        lblTotTitle.setFont(Theme.FONT_TITLE);
        totales.add(lblTotTitle);
        totales.add(lblTotal);

        JButton btnRegistrar = Theme.createButton("Registrar Venta", Theme.SUCCESS);
        btnRegistrar.setFont(Theme.FONT_SUBTITLE);
        btnRegistrar.addActionListener(e -> registrarVenta());

        panel.add(form, BorderLayout.NORTH);
        panel.add(totales, BorderLayout.CENTER);
        panel.add(btnRegistrar, BorderLayout.SOUTH);
        return panel;
    }

    public void recargarProductosDisponibles() {
        Producto seleccionado = (Producto) cmbProducto.getSelectedItem();
        cmbProducto.removeAllItems();
        for (Producto p : productoService.listarActivosConStock()) {
            cmbProducto.addItem(p);
        }
        if (seleccionado != null) {
            for (int i = 0; i < cmbProducto.getItemCount(); i++) {
                if (cmbProducto.getItemAt(i).getId() == seleccionado.getId()) {
                    cmbProducto.setSelectedIndex(i);
                    break;
                }
            }
        }
        actualizarInfoStock();
    }

    private void actualizarInfoStock() {
        Producto p = (Producto) cmbProducto.getSelectedItem();
        if (p == null) {
            lblStockInfo.setText("Sin productos disponibles");
        } else {
            lblStockInfo.setText("Stock: " + p.getStock() + " | Precio: " + Theme.formatCurrency(p.getPrecio()));
        }
    }

    private void agregarAlCarrito() {
        Producto p = (Producto) cmbProducto.getSelectedItem();
        if (p == null) {
            JOptionPane.showMessageDialog(this, "No hay productos disponibles para vender.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int cantidad = (Integer) spnCantidad.getValue();
        int enCarrito = carritoModel.getCantidadDeProducto(p.getId());
        if (cantidad + enCarrito > p.getStock()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Stock insuficiente. Disponible: " + p.getStock() + ", en carrito: " + enCarrito,
                    "Stock insuficiente",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }
        carritoModel.addItem(p, cantidad);
        spnCantidad.setValue(1);
        actualizarTotales();
    }

    private void actualizarTotales() {
        BigDecimal subtotal = carritoModel.calcularSubtotal();
        BigDecimal impuesto = subtotal.multiply(VentaService.TASA_IMPUESTO).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = subtotal.add(impuesto).setScale(2, RoundingMode.HALF_UP);

        lblSubtotal.setText(Theme.formatCurrency(subtotal));
        lblImpuesto.setText(Theme.formatCurrency(impuesto));
        lblTotal.setText(Theme.formatCurrency(total));
    }

    private void registrarVenta() {
        try {
            Venta venta = ventaService.registrarVenta(
                    txtCliente.getText(),
                    (String) cmbMetodoPago.getSelectedItem(),
                    carritoModel.getItems()
            );
            JOptionPane.showMessageDialog(
                    this,
                    "Venta registrada con folio " + venta.getFolio() + "\nTotal: " + Theme.formatCurrency(venta.getTotal()),
                    "Venta Exitosa",
                    JOptionPane.INFORMATION_MESSAGE
            );
            carritoModel.clear();
            txtCliente.setText("Público General");
            actualizarTotales();
            recargarProductosDisponibles();
            if (onSaleCompleted != null) {
                onSaleCompleted.run();
            }
        } catch (ValidationException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validación", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al registrar venta: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static class CarritoTableModel extends AbstractTableModel {
        private final String[] columns = {"Código", "Producto", "Cantidad", "Precio Unit.", "Subtotal"};
        private final List<DetalleVenta> items = new ArrayList<>();

        public void addItem(Producto p, int cantidad) {
            for (DetalleVenta d : items) {
                if (d.getProductoId() == p.getId()) {
                    d.setCantidad(d.getCantidad() + cantidad);
                    fireTableDataChanged();
                    return;
                }
            }
            items.add(new DetalleVenta(p.getId(), p.getCodigo(), p.getNombre(), cantidad, p.getPrecio()));
            fireTableDataChanged();
        }

        public int getCantidadDeProducto(long productoId) {
            return items.stream()
                    .filter(d -> d.getProductoId() == productoId)
                    .mapToInt(DetalleVenta::getCantidad)
                    .sum();
        }

        public void removeAt(int row) {
            items.remove(row);
            fireTableDataChanged();
        }

        public void clear() {
            items.clear();
            fireTableDataChanged();
        }

        public List<DetalleVenta> getItems() {
            return new ArrayList<>(items);
        }

        public BigDecimal calcularSubtotal() {
            return items.stream()
                    .map(DetalleVenta::getSubtotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        @Override
        public int getRowCount() {
            return items.size();
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
            DetalleVenta d = items.get(rowIndex);
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

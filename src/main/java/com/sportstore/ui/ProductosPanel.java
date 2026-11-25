package com.sportstore.ui;

import com.sportstore.model.CategoriaProducto;
import com.sportstore.model.Producto;
import com.sportstore.service.ProductoService;
import com.sportstore.service.ValidationException;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Panel CRUD para la administración de productos deportivos.
 */
public class ProductosPanel extends JPanel {

    private final ProductoService productoService;
    private final Runnable onDataChanged;

    private final JTextField txtBuscar = new JTextField(20);
    private final JComboBox<Object> cmbFiltroCategoria = new JComboBox<>();

    private final ProductoTableModel tableModel = new ProductoTableModel();
    private final JTable tabla = new JTable(tableModel);

    private final JTextField txtId = new JTextField();
    private final JTextField txtCodigo = new JTextField();
    private final JTextField txtNombre = new JTextField();
    private final JComboBox<CategoriaProducto> cmbCategoria = new JComboBox<>(CategoriaProducto.values());
    private final JTextField txtMarca = new JTextField();
    private final JTextField txtTalla = new JTextField();
    private final JTextField txtPrecio = new JTextField();
    private final JSpinner spnStock = new JSpinner(new SpinnerNumberModel(0, 0, 100000, 1));
    private final JCheckBox chkActivo = new JCheckBox("Producto activo para venta", true);

    private final JButton btnGuardar = Theme.createButton("Guardar Producto", Theme.SUCCESS);
    private final JButton btnNuevo = Theme.createButton("Nuevo / Limpiar", Theme.NEUTRAL);
    private final JButton btnEliminar = Theme.createButton("Eliminar", Theme.DANGER);

    public ProductosPanel(ProductoService productoService, Runnable onDataChanged) {
        this.productoService = productoService;
        this.onDataChanged = onDataChanged;
        setLayout(new BorderLayout(12, 12));
        setBackground(Theme.BG_APP);
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        add(crearBarraBusqueda(), BorderLayout.NORTH);
        add(crearPanelCentral(), BorderLayout.CENTER);
        add(crearFormulario(), BorderLayout.EAST);

        configurarEventos();
        recargarTabla();
    }

    private JPanel crearBarraBusqueda() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        panel.setBackground(Theme.BG_CARD);
        panel.setBorder(Theme.cardBorder(null));

        JLabel lblBuscar = new JLabel("Buscar (código, nombre, marca):");
        lblBuscar.setFont(Theme.FONT_BOLD);
        txtBuscar.setFont(Theme.FONT_BODY);

        cmbFiltroCategoria.addItem("Todas las categorías");
        for (CategoriaProducto c : CategoriaProducto.values()) {
            cmbFiltroCategoria.addItem(c);
        }
        cmbFiltroCategoria.setFont(Theme.FONT_BODY);

        JButton btnBuscar = Theme.createButton("Filtrar", Theme.PRIMARY);
        JButton btnLimpiarFiltro = Theme.createButton("Mostrar Todos", Theme.NEUTRAL);

        btnBuscar.addActionListener(e -> recargarTabla());
        btnLimpiarFiltro.addActionListener(e -> {
            txtBuscar.setText("");
            cmbFiltroCategoria.setSelectedIndex(0);
            recargarTabla();
        });
        txtBuscar.addActionListener(e -> recargarTabla());
        cmbFiltroCategoria.addActionListener(e -> recargarTabla());

        panel.add(lblBuscar);
        panel.add(txtBuscar);
        panel.add(new JLabel("Categoría:"));
        panel.add(cmbFiltroCategoria);
        panel.add(btnBuscar);
        panel.add(btnLimpiarFiltro);
        return panel;
    }

    private JScrollPane crearPanelCentral() {
        Theme.styleTable(tabla);
        tabla.getColumnModel().getColumn(0).setPreferredWidth(45);
        tabla.getColumnModel().getColumn(1).setPreferredWidth(90);
        tabla.getColumnModel().getColumn(2).setPreferredWidth(210);
        tabla.getColumnModel().getColumn(3).setPreferredWidth(130);
        tabla.getColumnModel().getColumn(4).setPreferredWidth(100);
        tabla.getColumnModel().getColumn(5).setPreferredWidth(70);
        tabla.getColumnModel().getColumn(6).setPreferredWidth(90);
        tabla.getColumnModel().getColumn(7).setPreferredWidth(65);
        tabla.getColumnModel().getColumn(8).setPreferredWidth(80);

        tabla.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : Theme.ROW_ALT);
                    Producto p = tableModel.getProductoAt(row);
                    if (!p.isActivo()) {
                        c.setForeground(Theme.TEXT_MUTED);
                    } else if (column == 7 && p.getStock() <= 5) {
                        c.setForeground(Theme.DANGER);
                    } else {
                        c.setForeground(Theme.TEXT_PRIMARY);
                    }
                }
                setHorizontalAlignment((column == 0 || column == 6 || column == 7) ? SwingConstants.RIGHT : SwingConstants.LEFT);
                return c;
            }
        });

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(Theme.cardBorder("Catálogo de Productos Deportivos"));
        scroll.getViewport().setBackground(Color.WHITE);
        return scroll;
    }

    private JPanel crearFormulario() {
        JPanel container = new JPanel(new BorderLayout(8, 8));
        container.setBackground(Theme.BG_CARD);
        container.setBorder(Theme.cardBorder("Formulario de Producto"));
        container.setPreferredSize(new Dimension(340, 0));

        JPanel fields = new JPanel(new GridBagLayout());
        fields.setBackground(Theme.BG_CARD);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 4, 5, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        txtId.setEditable(false);
        txtId.setBackground(new Color(241, 245, 249));
        txtId.setText("(Nuevo)");

        int row = 0;
        addField(fields, gbc, row++, "ID:", txtId);
        addField(fields, gbc, row++, "Código SKU *:", txtCodigo);
        addField(fields, gbc, row++, "Nombre *:", txtNombre);
        addField(fields, gbc, row++, "Categoría *:", cmbCategoria);
        addField(fields, gbc, row++, "Marca *:", txtMarca);
        addField(fields, gbc, row++, "Talla / Medida *:", txtTalla);
        addField(fields, gbc, row++, "Precio ($) *:", txtPrecio);
        addField(fields, gbc, row++, "Stock *:", spnStock);

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        chkActivo.setBackground(Theme.BG_CARD);
        chkActivo.setFont(Theme.FONT_BODY);
        fields.add(chkActivo, gbc);

        JPanel botones = new JPanel(new GridLayout(3, 1, 8, 8));
        botones.setBackground(Theme.BG_CARD);
        botones.add(btnGuardar);
        botones.add(btnNuevo);
        botones.add(btnEliminar);
        btnEliminar.setEnabled(false);

        container.add(fields, BorderLayout.NORTH);
        container.add(botones, BorderLayout.SOUTH);
        return container;
    }

    private void addField(JPanel panel, GridBagConstraints gbc, int row, String label, JComponent comp) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        gbc.weightx = 0.35;
        JLabel lbl = new JLabel(label);
        lbl.setFont(Theme.FONT_BOLD);
        panel.add(lbl, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.65;
        comp.setFont(Theme.FONT_BODY);
        panel.add(comp, gbc);
    }

    private void configurarEventos() {
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = tabla.getSelectedRow();
                if (row >= 0) {
                    cargarEnFormulario(tableModel.getProductoAt(row));
                }
            }
        });

        btnNuevo.addActionListener(e -> limpiarFormulario());
        btnGuardar.addActionListener(e -> guardarProducto());
        btnEliminar.addActionListener(e -> eliminarProducto());
    }

    public void recargarTabla() {
        CategoriaProducto cat = cmbFiltroCategoria.getSelectedItem() instanceof CategoriaProducto c ? c : null;
        List<Producto> lista = productoService.buscar(txtBuscar.getText(), cat);
        tableModel.setProductos(lista);
    }

    private void cargarEnFormulario(Producto p) {
        txtId.setText(String.valueOf(p.getId()));
        txtCodigo.setText(p.getCodigo());
        txtNombre.setText(p.getNombre());
        cmbCategoria.setSelectedItem(p.getCategoria());
        txtMarca.setText(p.getMarca());
        txtTalla.setText(p.getTalla());
        txtPrecio.setText(p.getPrecio().toPlainString());
        spnStock.setValue(p.getStock());
        chkActivo.setSelected(p.isActivo());
        btnGuardar.setText("Actualizar Producto");
        btnEliminar.setEnabled(true);
    }

    private void limpiarFormulario() {
        tabla.clearSelection();
        txtId.setText("(Nuevo)");
        txtCodigo.setText("");
        txtNombre.setText("");
        cmbCategoria.setSelectedIndex(0);
        txtMarca.setText("");
        txtTalla.setText("");
        txtPrecio.setText("");
        spnStock.setValue(0);
        chkActivo.setSelected(true);
        btnGuardar.setText("Guardar Producto");
        btnEliminar.setEnabled(false);
        txtCodigo.requestFocus();
    }

    private void guardarProducto() {
        try {
            BigDecimal precio;
            try {
                precio = new BigDecimal(txtPrecio.getText().trim().replace(",", "."));
            } catch (NumberFormatException ex) {
                throw new ValidationException("Ingrese un precio numérico válido (ej. 49.90).");
            }

            Producto p = new Producto();
            if (!"(Nuevo)".equals(txtId.getText())) {
                p.setId(Long.parseLong(txtId.getText()));
            }
            p.setCodigo(txtCodigo.getText());
            p.setNombre(txtNombre.getText());
            p.setCategoria((CategoriaProducto) cmbCategoria.getSelectedItem());
            p.setMarca(txtMarca.getText());
            p.setTalla(txtTalla.getText());
            p.setPrecio(precio);
            p.setStock((Integer) spnStock.getValue());
            p.setActivo(chkActivo.isSelected());

            boolean esNuevo = p.getId() == 0;
            if (esNuevo) {
                productoService.crear(p);
                JOptionPane.showMessageDialog(this, "Producto creado correctamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            } else {
                productoService.actualizar(p);
                JOptionPane.showMessageDialog(this, "Producto actualizado correctamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            }

            limpiarFormulario();
            recargarTabla();
            if (onDataChanged != null) {
                onDataChanged.run();
            }
        } catch (ValidationException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validación", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error inesperado: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void eliminarProducto() {
        int row = tabla.getSelectedRow();
        if (row < 0) {
            return;
        }
        Producto seleccionado = tableModel.getProductoAt(row);
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "¿Está seguro de eliminar el producto '" + seleccionado.getNombre() + "'?",
                "Confirmar eliminación",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );
        if (confirm == JOptionPane.YES_OPTION) {
            productoService.eliminar(seleccionado.getId());
            limpiarFormulario();
            recargarTabla();
            if (onDataChanged != null) {
                onDataChanged.run();
            }
        }
    }

    private static class ProductoTableModel extends AbstractTableModel {
        private final String[] columns = {
                "ID", "Código", "Nombre", "Categoría", "Marca", "Talla", "Precio", "Stock", "Estado"
        };
        private List<Producto> productos = new ArrayList<>();

        public void setProductos(List<Producto> productos) {
            this.productos = new ArrayList<>(productos);
            fireTableDataChanged();
        }

        public Producto getProductoAt(int row) {
            return productos.get(row);
        }

        @Override
        public int getRowCount() {
            return productos.size();
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
            Producto p = productos.get(rowIndex);
            return switch (columnIndex) {
                case 0 -> p.getId();
                case 1 -> p.getCodigo();
                case 2 -> p.getNombre();
                case 3 -> p.getCategoria().getEtiqueta();
                case 4 -> p.getMarca();
                case 5 -> p.getTalla();
                case 6 -> Theme.formatCurrency(p.getPrecio());
                case 7 -> p.getStock();
                case 8 -> p.isActivo() ? "Activo" : "Inactivo";
                default -> "";
            };
        }
    }
}

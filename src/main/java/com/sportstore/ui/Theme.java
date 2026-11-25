package com.sportstore.ui;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Paleta de colores, fuentes y utilidades de estilo para la interfaz Swing.
 */
public final class Theme {

    public static final Color BG_APP = new Color(241, 245, 249);
    public static final Color BG_CARD = Color.WHITE;
    public static final Color HEADER_BG = new Color(30, 41, 59);
    public static final Color HEADER_FG = Color.WHITE;

    public static final Color PRIMARY = new Color(37, 99, 235);
    public static final Color SUCCESS = new Color(5, 150, 105);
    public static final Color DANGER = new Color(220, 38, 38);
    public static final Color WARNING = new Color(217, 119, 6);
    public static final Color NEUTRAL = new Color(100, 116, 139);

    public static final Color BORDER = new Color(203, 213, 225);
    public static final Color ROW_ALT = new Color(248, 250, 252);
    public static final Color TEXT_PRIMARY = new Color(15, 23, 42);
    public static final Color TEXT_MUTED = new Color(100, 116, 139);

    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 18);
    public static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_BODY = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 12);

    private static final NumberFormat CURRENCY = NumberFormat.getCurrencyInstance(new Locale("es", "MX"));

    private Theme() {
    }

    public static String formatCurrency(BigDecimal amount) {
        return CURRENCY.format(amount != null ? amount : BigDecimal.ZERO);
    }

    public static JButton createButton(String text, Color bgColor) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BOLD);
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setOpaque(true);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setMargin(new Insets(8, 14, 8, 14));
        return btn;
    }

    public static void styleTable(JTable table) {
        table.setFont(FONT_BODY);
        table.setRowHeight(28);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setGridColor(new Color(226, 232, 240));
        table.setShowVerticalLines(false);
        table.setFillsViewportHeight(true);

        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_BOLD);
        header.setBackground(new Color(226, 232, 240));
        header.setForeground(TEXT_PRIMARY);
        header.setReorderingAllowed(false);
    }

    public static Border cardBorder(String title) {
        Border line = BorderFactory.createLineBorder(BORDER, 1, true);
        Border pad = BorderFactory.createEmptyBorder(10, 12, 10, 12);
        if (title == null || title.isBlank()) {
            return BorderFactory.createCompoundBorder(line, pad);
        }
        return BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(line, title, 0, 0, FONT_SUBTITLE, TEXT_PRIMARY),
                pad
        );
    }
}

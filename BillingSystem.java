import javax.swing.*;
import javax.swing.table.*;

import java.awt.*;
import java.awt.event.*;

import java.awt.print.*;
import java.text.*;
import java.util.*;


public class BillingSystem extends JFrame {

    // ─── Color Palette ───────────────────────────────────────────────
    static final Color BG         = new Color(0xF7F6F2);
    static final Color PANEL_BG   = new Color(0xFFFFFF);
    static final Color ACCENT     = new Color(0x2563EB);
    static final Color ACCENT2    = new Color(0x1E40AF);
    static final Color SUCCESS    = new Color(0x16A34A);
    static final Color DANGER     = new Color(0xDC2626);
    static final Color TEXT_DARK  = new Color(0x1E293B);
    static final Color TEXT_MID   = new Color(0x64748B);
    static final Color TEXT_LIGHT = new Color(0x94A3B8);
    static final Color BORDER_COL = new Color(0xE2E8F0);
    static final Color ROW_ALT    = new Color(0xF8FAFC);
    static final Color HEADER_BG  = new Color(0x1E293B);

    // ─── Fonts ────────────────────────────────────────────────────────
    static final Font FONT_TITLE  = new Font("SansSerif", Font.BOLD,  22);
    static final Font FONT_HEAD   = new Font("SansSerif", Font.BOLD,  13);
    static final Font FONT_BODY   = new Font("SansSerif", Font.PLAIN, 13);
    static final Font FONT_SMALL  = new Font("SansSerif", Font.PLAIN, 11);
    static final Font FONT_MONO   = new Font("Monospaced", Font.BOLD, 14);

    // ─── Item Catalog ─────────────────────────────────────────────────
    static final Object[][] CATALOG = {
        {"ITM-001", "Wireless Mouse",        599.00, "Electronics"},
        {"ITM-002", "Mechanical Keyboard",  1299.00, "Electronics"},
        {"ITM-003", "USB-C Hub",             849.00, "Electronics"},
        {"ITM-004", "Notebook (A4, 200pg)",   89.00, "Stationery"},
        {"ITM-005", "Ball Pen (Pack of 10)",   45.00, "Stationery"},
        {"ITM-006", "Desk Lamp",             499.00, "Furniture"},
        {"ITM-007", "Office Chair",         4999.00, "Furniture"},
        {"ITM-008", "Sticky Notes (5pk)",     79.00, "Stationery"},
        {"ITM-009", "HDMI Cable 2m",         199.00, "Electronics"},
        {"ITM-010", "Water Bottle 1L",       249.00, "Accessories"},
    };

    // ─── State ────────────────────────────────────────────────────────
    DefaultTableModel billModel;
    JTextField tfCustomer, tfPhone, tfDiscount;
    JLabel lblSubtotal, lblTax, lblDiscount, lblTotal, lblInvoiceNo;
    JComboBox<String> cbItem;
    JSpinner spQty;
    JTextField tfPrice;
    int invoiceCounter = 1001;

    // ═════════════════════════════════════════════════════════════════
    public BillingSystem() {
        super("BillDesk — Item Billing System");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1100, 760);
        setMinimumSize(new Dimension(950, 680));
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG);
        setLayout(new BorderLayout(0, 0));

        add(buildTopBar(),    BorderLayout.NORTH);
        add(buildCenter(),    BorderLayout.CENTER);
        add(buildStatusBar(), BorderLayout.SOUTH);

        setVisible(true);
        refreshTotals();
    }

    // ─── Top Bar ──────────────────────────────────────────────────────
    JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout()) {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0,0, HEADER_BG, getWidth(), 0, new Color(0x0F172A));
                g2.setPaint(gp);
                g2.fillRect(0,0,getWidth(),getHeight());
            }
        };
        bar.setPreferredSize(new Dimension(0, 64));
        bar.setBorder(BorderFactory.createEmptyBorder(0, 24, 0, 24));

        // Logo + Title
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);
        JLabel icon = new JLabel("🧾");
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 26));
        JLabel title = new JLabel("BillDesk");
        title.setFont(FONT_TITLE);
        title.setForeground(Color.WHITE);
        JLabel sub = new JLabel("  Item Billing System");
        sub.setFont(FONT_SMALL);
        sub.setForeground(TEXT_LIGHT);
        left.add(icon); left.add(title); left.add(sub);

        // Invoice number (right)
        lblInvoiceNo = new JLabel("Invoice #" + invoiceCounter);
        lblInvoiceNo.setFont(FONT_MONO);
        lblInvoiceNo.setForeground(new Color(0x93C5FD));

        bar.add(left, BorderLayout.WEST);
        bar.add(lblInvoiceNo, BorderLayout.EAST);
        return bar;
    }

    // ─── Center (2-column layout) ─────────────────────────────────────
    JPanel buildCenter() {
        JPanel center = new JPanel(new GridBagLayout());
        center.setBackground(BG);
        center.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));

        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.BOTH;
        gc.insets = new Insets(0, 0, 0, 12);

        // Left column: Customer + Add Item + Bill Table
        gc.gridx = 0; gc.gridy = 0; gc.weightx = 0.62; gc.weighty = 1.0;
        center.add(buildLeftPanel(), gc);

        // Right column: Summary + Actions
        gc.gridx = 1; gc.weightx = 0.38; gc.insets = new Insets(0,0,0,0);
        center.add(buildRightPanel(), gc);

        return center;
    }

    // ─── Left Panel ───────────────────────────────────────────────────
    JPanel buildLeftPanel() {
        JPanel p = new JPanel(new BorderLayout(0, 12));
        p.setBackground(BG);
        p.add(buildCustomerCard(), BorderLayout.NORTH);
        p.add(buildAddItemCard(),  BorderLayout.CENTER);
        return p;
    }

    JPanel buildCustomerCard() {
        JPanel card = card("Customer Details");
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JPanel row = new JPanel(new GridLayout(1, 2, 12, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));

        tfCustomer = styledField("Customer Name");
        tfPhone    = styledField("Phone Number");
        row.add(labeledField("Customer Name", tfCustomer));
        row.add(labeledField("Phone", tfPhone));

        card.add(Box.createVerticalStrut(4));
        card.add(row);
        card.add(Box.createVerticalStrut(4));
        return card;
    }

    JPanel buildAddItemCard() {
        JPanel outer = new JPanel(new BorderLayout(0, 10));
        outer.setBackground(BG);
        outer.add(buildItemInputCard(), BorderLayout.NORTH);
        outer.add(buildBillTableCard(), BorderLayout.CENTER);
        return outer;
    }

    JPanel buildItemInputCard() {
        JPanel card = card("Add Item to Bill");

        // Item dropdown
        String[] items = new String[CATALOG.length + 1];
        items[0] = "— Select Item —";
        for (int i = 0; i < CATALOG.length; i++)
            items[i+1] = CATALOG[i][0] + "  |  " + CATALOG[i][1];
        cbItem = new JComboBox<>(items);
        styleCombo(cbItem);
        cbItem.addActionListener(e -> onItemSelected());

        // Price field
        tfPrice = styledField("0.00");
        tfPrice.setEditable(false);
        tfPrice.setBackground(ROW_ALT);

        // Qty spinner
        spQty = new JSpinner(new SpinnerNumberModel(1, 1, 999, 1));
        spQty.setFont(FONT_BODY);
        styleSpinner(spQty);

        // Add button
        JButton btnAdd = accentButton("+ Add", ACCENT);
        btnAdd.addActionListener(e -> addItemToBill());

        JPanel grid = new JPanel(new GridBagLayout());
        grid.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4,4,4,4);
        g.fill = GridBagConstraints.HORIZONTAL;

        g.gridx=0; g.gridy=0; g.weightx=0.5; grid.add(labeledField("Item", cbItem), g);
        g.gridx=1;             g.weightx=0.25; grid.add(labeledField("Unit Price (₹)", tfPrice), g);
        g.gridx=2;             g.weightx=0.1;  grid.add(labeledField("Qty", spQty), g);
        g.gridx=3;             g.weightx=0.15; g.anchor=GridBagConstraints.SOUTH;
        JPanel btnWrap = new JPanel(new BorderLayout()); btnWrap.setOpaque(false);
        btnWrap.add(btnAdd, BorderLayout.SOUTH);
        grid.add(btnWrap, g);

        card.add(grid);
        return card;
    }

    JPanel buildBillTableCard() {
        JPanel card = card("Bill Items");
        card.setLayout(new BorderLayout(0, 8));

        String[] cols = {"#", "Item Code", "Description", "Qty", "Unit Price", "Total"};
        billModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable table = new JTable(billModel);
        table.setFont(FONT_BODY);
        table.setRowHeight(36);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setBackground(PANEL_BG);
        table.setForeground(TEXT_DARK);
        table.setSelectionBackground(new Color(0xDBEAFE));
        table.setSelectionForeground(TEXT_DARK);

        // Header
        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_HEAD);
        header.setBackground(new Color(0xF1F5F9));
        header.setForeground(TEXT_MID);
        header.setBorder(BorderFactory.createMatteBorder(0,0,2,0, BORDER_COL));
        header.setPreferredSize(new Dimension(0, 38));

        // Column widths
        int[] widths = {35, 85, 200, 50, 100, 100};
        for (int i = 0; i < widths.length; i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        // Alternating rows renderer
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object v,
                    boolean sel, boolean foc, int r, int c) {
                Component comp = super.getTableCellRendererComponent(t,v,sel,foc,r,c);
                if (!sel) comp.setBackground(r % 2 == 0 ? PANEL_BG : ROW_ALT);
                ((JLabel)comp).setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                if (c == 5) ((JLabel)comp).setHorizontalAlignment(SwingConstants.RIGHT);
                if (c == 3) ((JLabel)comp).setHorizontalAlignment(SwingConstants.CENTER);
                return comp;
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER_COL));
        scroll.getViewport().setBackground(PANEL_BG);

        // Delete row button
        JButton btnDel = accentButton("🗑 Remove Selected", DANGER);
        btnDel.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) { billModel.removeRow(row); renumberRows(); refreshTotals(); }
            else JOptionPane.showMessageDialog(this, "Please select a row to remove.", "No Selection", JOptionPane.WARNING_MESSAGE);
        });

        card.add(scroll, BorderLayout.CENTER);
        card.add(btnDel, BorderLayout.SOUTH);
        return card;
    }

    // ─── Right Panel ──────────────────────────────────────────────────
    JPanel buildRightPanel() {
        JPanel p = new JPanel(new BorderLayout(0, 12));
        p.setBackground(BG);
        p.add(buildSummaryCard(), BorderLayout.CENTER);
        p.add(buildActionsCard(), BorderLayout.SOUTH);
        return p;
    }

    JPanel buildSummaryCard() {
        JPanel card = card("Invoice Summary");
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        // Discount
        tfDiscount = styledField("0");
        JPanel discRow = labeledField("Discount (%)", tfDiscount);
        discRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));
        tfDiscount.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { refreshTotals(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { refreshTotals(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { refreshTotals(); }
        });

        card.add(Box.createVerticalStrut(4));
        card.add(discRow);
        card.add(Box.createVerticalStrut(16));
        card.add(divider());
        card.add(Box.createVerticalStrut(12));

        lblSubtotal = summaryLine("Subtotal",       "₹ 0.00", false);
        lblDiscount = summaryLine("Discount",        "₹ 0.00", false);
        lblTax      = summaryLine("GST (18%)",       "₹ 0.00", false);
        card.add(summaryRow("Subtotal",  lblSubtotal));
        card.add(Box.createVerticalStrut(8));
        card.add(summaryRow("Discount",  lblDiscount));
        card.add(Box.createVerticalStrut(8));
        card.add(summaryRow("GST (18%)", lblTax));
        card.add(Box.createVerticalStrut(12));
        card.add(divider());
        card.add(Box.createVerticalStrut(12));

        lblTotal = new JLabel("₹ 0.00");
        lblTotal.setFont(new Font("SansSerif", Font.BOLD, 26));
        lblTotal.setForeground(ACCENT);
        JPanel totalRow = new JPanel(new BorderLayout());
        totalRow.setOpaque(false);
        totalRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        JLabel totalLbl = new JLabel("TOTAL");
        totalLbl.setFont(new Font("SansSerif", Font.BOLD, 14));
        totalLbl.setForeground(TEXT_DARK);
        totalRow.add(totalLbl,  BorderLayout.WEST);
        totalRow.add(lblTotal,  BorderLayout.EAST);
        card.add(totalRow);
        card.add(Box.createVerticalGlue());
        return card;
    }

    JPanel buildActionsCard() {
        JPanel card = card("Actions");
        card.setLayout(new GridLayout(3, 1, 0, 8));

        JButton btnPrint = accentButton("🖨   Print / Save Invoice", ACCENT);
        JButton btnNew   = accentButton("📄  New Invoice",           SUCCESS);
        JButton btnClear = accentButton("🗑   Clear All",             DANGER);

        btnPrint.addActionListener(e -> printInvoice());
        btnNew.addActionListener(e   -> newInvoice());
        btnClear.addActionListener(e -> clearAll());

        card.add(btnPrint);
        card.add(btnNew);
        card.add(btnClear);
        return card;
    }

    // ─── Status Bar ───────────────────────────────────────────────────
    JPanel buildStatusBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(HEADER_BG);
        bar.setPreferredSize(new Dimension(0, 28));
        bar.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 16));
        JLabel lbl = new JLabel("BillDesk v1.0  •  GST 18% applied on discounted subtotal");
        lbl.setFont(FONT_SMALL);
        lbl.setForeground(TEXT_LIGHT);
        JLabel date = new JLabel(new SimpleDateFormat("dd MMM yyyy  HH:mm").format(new Date()));
        date.setFont(FONT_SMALL);
        date.setForeground(TEXT_LIGHT);
        bar.add(lbl,  BorderLayout.WEST);
        bar.add(date, BorderLayout.EAST);
        return bar;
    }

    // ─── Logic ────────────────────────────────────────────────────────
    void onItemSelected() {
        int idx = cbItem.getSelectedIndex();
        if (idx <= 0) { tfPrice.setText(""); return; }
        double price = (Double) CATALOG[idx - 1][2];
        tfPrice.setText(String.format("%.2f", price));
    }

    void addItemToBill() {
        int idx = cbItem.getSelectedIndex();
        if (idx <= 0) {
            shake(cbItem);
            JOptionPane.showMessageDialog(this, "Please select an item.", "No Item", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Object[] cat = CATALOG[idx - 1];
        int qty = (Integer) spQty.getValue();
        double price = (Double) cat[2];
        double total = price * qty;
        int rowNum = billModel.getRowCount() + 1;
        billModel.addRow(new Object[]{
            rowNum,
            cat[0],
            cat[1],
            qty,
            String.format("₹ %.2f", price),
            String.format("₹ %.2f", total)
        });
        cbItem.setSelectedIndex(0);
        spQty.setValue(1);
        tfPrice.setText("");
        refreshTotals();
    }

    void refreshTotals() {
        double sub = 0;
        for (int i = 0; i < billModel.getRowCount(); i++) {
            String s = ((String) billModel.getValueAt(i, 5)).replace("₹", "").trim();
            try { sub += Double.parseDouble(s); } catch (Exception ignored) {}
        }
        double discPct = 0;
        try { discPct = Double.parseDouble(tfDiscount.getText().trim()); } catch (Exception ignored) {}
        discPct = Math.max(0, Math.min(100, discPct));
        double discAmt = sub * discPct / 100.0;
        double taxable = sub - discAmt;
        double tax     = taxable * 0.18;
        double total   = taxable + tax;

        lblSubtotal.setText(String.format("₹ %.2f", sub));
        lblDiscount.setText(String.format("₹ %.2f", discAmt));
        lblTax.setText(String.format("₹ %.2f", tax));
        lblTotal.setText(String.format("₹ %.2f", total));
    }

    void renumberRows() {
        for (int i = 0; i < billModel.getRowCount(); i++)
            billModel.setValueAt(i + 1, i, 0);
    }

    void clearAll() {
        int r = JOptionPane.showConfirmDialog(this,
            "Clear all bill items?", "Confirm Clear",
            JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (r == JOptionPane.YES_OPTION) {
            billModel.setRowCount(0);
            tfDiscount.setText("0");
            refreshTotals();
        }
    }

    void newInvoice() {
        int r = JOptionPane.showConfirmDialog(this,
            "Start a new invoice? Current bill will be cleared.",
            "New Invoice", JOptionPane.YES_NO_OPTION);
        if (r == JOptionPane.YES_OPTION) {
            billModel.setRowCount(0);
            tfCustomer.setText("");
            tfPhone.setText("");
            tfDiscount.setText("0");
            invoiceCounter++;
            lblInvoiceNo.setText("Invoice #" + invoiceCounter);
            refreshTotals();
        }
    }

    void printInvoice() {
        if (billModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "No items in the bill.", "Empty Bill", JOptionPane.WARNING_MESSAGE);
            return;
        }
        JDialog dlg = new JDialog(this, "Invoice Preview", true);
        dlg.setSize(540, 720);
        dlg.setLocationRelativeTo(this);

        InvoicePreviewPanel preview = new InvoicePreviewPanel(
            "Invoice #" + invoiceCounter,
            tfCustomer.getText().trim().isEmpty() ? "Walk-in Customer" : tfCustomer.getText().trim(),
            tfPhone.getText().trim(),
            billModel,
            lblSubtotal.getText(), lblDiscount.getText(), lblTax.getText(), lblTotal.getText()
        );

        JScrollPane sp = new JScrollPane(preview);
        sp.setBorder(null);
        dlg.add(sp, BorderLayout.CENTER);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 8));
        btns.setBackground(BG);
        JButton btnPrint = accentButton("🖨  Print", ACCENT);
        JButton btnClose = accentButton("Close", TEXT_MID);
        btnClose.setBackground(BORDER_COL);
        btnClose.setForeground(TEXT_DARK);
        btnPrint.addActionListener(e -> {
            PrinterJob job = PrinterJob.getPrinterJob();
            job.setPrintable(preview);
            if (job.printDialog()) {
                try { job.print(); }
                catch (PrinterException ex) {
                    JOptionPane.showMessageDialog(dlg, "Print failed: " + ex.getMessage());
                }
            }
        });
        btnClose.addActionListener(e -> dlg.dispose());
        btns.add(btnClose); btns.add(btnPrint);
        dlg.add(btns, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    // ─── UI Helpers ───────────────────────────────────────────────────
    JPanel card(String title) {
        JPanel p = new JPanel();
        p.setBackground(PANEL_BG);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COL),
            BorderFactory.createEmptyBorder(14, 16, 14, 16)
        ));
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        if (title != null && !title.isEmpty()) {
            JLabel lbl = new JLabel(title);
            lbl.setFont(FONT_HEAD);
            lbl.setForeground(TEXT_DARK);
            lbl.setBorder(BorderFactory.createEmptyBorder(0,0,10,0));
            lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
            p.add(lbl);
        }
        return p;
    }

    JTextField styledField(String placeholder) {
        JTextField tf = new JTextField() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getText().isEmpty() && !isFocusOwner()) {
                    Graphics2D g2 = (Graphics2D)g;
                    g2.setColor(TEXT_LIGHT);
                    g2.setFont(FONT_BODY);
                    g2.drawString(placeholder, 8, getHeight()/2 + 5);
                }
            }
        };
        tf.setFont(FONT_BODY);
        tf.setForeground(TEXT_DARK);
        tf.setBackground(Color.WHITE);
        tf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COL),
            BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));
        tf.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                tf.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(ACCENT),
                    BorderFactory.createEmptyBorder(6, 8, 6, 8)));
            }
            public void focusLost(FocusEvent e) {
                tf.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER_COL),
                    BorderFactory.createEmptyBorder(6, 8, 6, 8)));
            }
        });
        return tf;
    }

    void styleCombo(JComboBox<String> cb) {
        cb.setFont(FONT_BODY);
        cb.setBackground(Color.WHITE);
        cb.setForeground(TEXT_DARK);
        cb.setBorder(BorderFactory.createLineBorder(BORDER_COL));
    }

    void styleSpinner(JSpinner sp) {
        sp.setBorder(BorderFactory.createLineBorder(BORDER_COL));
        ((JSpinner.DefaultEditor) sp.getEditor()).getTextField().setFont(FONT_BODY);
    }

    JButton accentButton(String text, Color bg) {
        JButton btn = new JButton(text) {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D)g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color c = getModel().isPressed() ? bg.darker() :
                          getModel().isRollover() ? bg.brighter() : bg;
                g2.setColor(c);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(Color.WHITE);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), x, y);
            }
        };
        btn.setFont(FONT_HEAD);
        btn.setForeground(Color.WHITE);
        btn.setPreferredSize(new Dimension(180, 38));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    JPanel labeledField(String label, JComponent field) {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setOpaque(false);
        JLabel lbl = new JLabel(label);
        lbl.setFont(FONT_SMALL);
        lbl.setForeground(TEXT_MID);
        p.add(lbl,   BorderLayout.NORTH);
        p.add(field, BorderLayout.CENTER);
        return p;
    }

    JPanel summaryRow(String label, JLabel valueLabel) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
        JLabel lbl = new JLabel(label);
        lbl.setFont(FONT_BODY);
        lbl.setForeground(TEXT_MID);
        row.add(lbl,        BorderLayout.WEST);
        row.add(valueLabel, BorderLayout.EAST);
        return row;
    }

    JLabel summaryLine(String label, String val, boolean bold) {
        JLabel l = new JLabel(val);
        l.setFont(bold ? FONT_HEAD : FONT_BODY);
        l.setForeground(TEXT_DARK);
        return l;
    }

    JSeparator divider() {
        JSeparator sep = new JSeparator();
        sep.setForeground(BORDER_COL);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        return sep;
    }

    void shake(Component c) {
        Point orig = c.getLocation();
        javax.swing.Timer t = new javax.swing.Timer(30, null);
        int[] offsets = {-6,6,-4,4,-2,2,0};
        int[] step = {0};
        t.addActionListener(e -> {
            if (step[0] >= offsets.length) { c.setLocation(orig); t.stop(); return; }
            c.setLocation(orig.x + offsets[step[0]++], orig.y);
        });
        t.start();
    }

    // ═════════════════════════════════════════════════════════════════
    // Invoice Preview Panel (also Printable)
    // ═════════════════════════════════════════════════════════════════
    static class InvoicePreviewPanel extends JPanel implements Printable {
        String invoiceNo, customer, phone, subtotal, discount, tax, total;
        DefaultTableModel model;

        InvoicePreviewPanel(String inv, String cust, String phone,
                            DefaultTableModel model,
                            String sub, String disc, String tax, String total) {
            this.invoiceNo = inv; this.customer = cust; this.phone = phone;
            this.model = model;  this.subtotal = sub;   this.discount = disc;
            this.tax = tax;      this.total = total;
            setBackground(Color.WHITE);
            setPreferredSize(new Dimension(520, 700));
        }

        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            drawInvoice((Graphics2D) g, getWidth(), 1.0);
        }

        public int print(Graphics g, PageFormat pf, int page) {
            if (page > 0) return NO_SUCH_PAGE;
            Graphics2D g2 = (Graphics2D) g;
            g2.translate(pf.getImageableX(), pf.getImageableY());
            double scale = Math.min(
                pf.getImageableWidth()  / 520.0,
                pf.getImageableHeight() / 700.0);
            g2.scale(scale, scale);
            drawInvoice(g2, 520, scale);
            return PAGE_EXISTS;
        }

        void drawInvoice(Graphics2D g2, int width, double scale) {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            int pad = 36, y = 0, w = width - pad * 2;

            // Header band
            g2.setColor(HEADER_BG);
            g2.fillRect(0, 0, width, 80);
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("SansSerif", Font.BOLD, 22));
            g2.drawString("BillDesk", pad, 38);
            g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
            g2.setColor(new Color(0x94A3B8));
            g2.drawString("Tax Invoice", pad, 56);
            g2.setColor(new Color(0x93C5FD));
            g2.setFont(new Font("Monospaced", Font.BOLD, 13));
            String invStr = invoiceNo;
            g2.drawString(invStr, width - pad - g2.getFontMetrics().stringWidth(invStr), 38);
            g2.setColor(new Color(0x94A3B8));
            g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
            String dateStr = new SimpleDateFormat("dd MMM yyyy").format(new Date());
            g2.drawString(dateStr, width - pad - g2.getFontMetrics().stringWidth(dateStr), 56);

            y = 100;
            // Customer
            g2.setColor(TEXT_DARK);
            g2.setFont(new Font("SansSerif", Font.BOLD, 12));
            g2.drawString("BILL TO", pad, y);
            y += 18;
            g2.setFont(new Font("SansSerif", Font.BOLD, 14));
            g2.drawString(customer, pad, y);
            y += 16;
            if (!phone.isEmpty()) {
                g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
                g2.setColor(TEXT_MID);
                g2.drawString("📞 " + phone, pad, y);
            }
            y += 24;

            // Table header
            g2.setColor(new Color(0xF1F5F9));
            g2.fillRoundRect(pad, y, w, 28, 6, 6);
            g2.setColor(TEXT_MID);
            g2.setFont(new Font("SansSerif", Font.BOLD, 11));
            String[] heads = {"#", "Code", "Description", "Qty", "Price", "Total"};
            int[] headX = {pad+8, pad+48, pad+170, pad+285, pad+348, w+pad-8};
            g2.drawString(heads[0], headX[0], y+18);
            g2.drawString(heads[1], headX[1], y+18);
            g2.drawString(heads[2], headX[2], y+18);
            g2.drawString(heads[3], headX[3], y+18);
            g2.drawString(heads[4], headX[4], y+18);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(heads[5], headX[5]-fm.stringWidth(heads[5]), y+18);
            y += 34;

            // Rows
            for (int i = 0; i < model.getRowCount(); i++) {
                if (i % 2 == 1) {
                    g2.setColor(ROW_ALT);
                    g2.fillRect(pad, y-14, w, 26);
                }
                g2.setColor(TEXT_DARK);
                g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
                g2.drawString("" + model.getValueAt(i,0), headX[0], y+6);
                g2.drawString("" + model.getValueAt(i,1), headX[1], y+6);
                String desc = "" + model.getValueAt(i,2);
                if (desc.length() > 22) desc = desc.substring(0,20) + "…";
                g2.drawString(desc, headX[2], y+6);
                String qtyStr = "" + model.getValueAt(i,3);
                g2.drawString(qtyStr, headX[3]+8, y+6);
                g2.drawString("" + model.getValueAt(i,4), headX[4], y+6);
                String tot = "" + model.getValueAt(i,5);
                g2.setFont(new Font("SansSerif", Font.BOLD, 11));
                g2.drawString(tot, headX[5]-g2.getFontMetrics().stringWidth(tot), y+6);
                y += 26;
            }

            // Divider
            y += 8;
            g2.setColor(BORDER_COL);
            g2.drawLine(pad, y, pad+w, y);
            y += 16;

            // Totals
            int lx = pad + w - 200, rx = pad + w;
            g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
            g2.setColor(TEXT_MID);
            String[] labels = {"Subtotal", "Discount", "GST (18%)"};
            String[] vals   = {subtotal, discount, tax};
            for (int i = 0; i < 3; i++) {
                g2.setColor(TEXT_MID);
                g2.drawString(labels[i], lx, y);
                FontMetrics fmv = g2.getFontMetrics();
                g2.setColor(TEXT_DARK);
                g2.drawString(vals[i], rx - fmv.stringWidth(vals[i]), y);
                y += 20;
            }

            y += 4;
            g2.setColor(ACCENT);
            g2.fillRoundRect(lx-8, y-16, rx-lx+16, 32, 8, 8);
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("SansSerif", Font.BOLD, 14));
            g2.drawString("TOTAL", lx, y+7);
            FontMetrics fmT = g2.getFontMetrics();
            g2.drawString(total, rx - fmT.stringWidth(total), y+7);

            // Footer
            y += 50;
            g2.setColor(BORDER_COL);
            g2.drawLine(pad, y, pad+w, y);
            y += 14;
            g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
            g2.setColor(TEXT_LIGHT);
            String footer = "Thank you for your business!  •  Generated by BillDesk";
            g2.drawString(footer, pad + (w - g2.getFontMetrics().stringWidth(footer))/2, y);
        }
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch (Exception ignored) {}
        SwingUtilities.invokeLater(BillingSystem::new);
    }
}

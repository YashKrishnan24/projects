import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

public class SupermarketBillingSystem extends JFrame {

    private JComboBox<String> itemBox;
    private JTextField priceField, quantityField;
    private JLabel subTotalLabel, taxLabel, grandTotalLabel;
    private DefaultTableModel model;
    private JTable table;
    
    private double subTotal = 0;
    private final double TAX_RATE = 0.05;

    private final HashMap<String, Double> productPrices;

    public SupermarketBillingSystem() {
        setTitle("Modern Supermarket POS System");
        setSize(850, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(new Color(245, 245, 245));

        productPrices = new HashMap<>();
        productPrices.put("Milk (1L)", 50.0);
        productPrices.put("Bread (Large)", 35.0);
        productPrices.put("Basmati Rice (1kg)", 120.0);
        productPrices.put("Sugar (1kg)", 45.0);
        productPrices.put("Organic Eggs (12)", 90.0);
        productPrices.put("Olive Oil (1L)", 450.0);
        productPrices.put("Premium Biscuits", 40.0);
        productPrices.put("Bath Soap", 35.0);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(createHeader(), BorderLayout.NORTH);
        topPanel.add(createInputPanel(), BorderLayout.CENTER);
        
        add(topPanel, BorderLayout.NORTH);
        add(createTable(), BorderLayout.CENTER);
        add(createFooter(), BorderLayout.SOUTH);

        updatePrice();
    }

    private JPanel createHeader() {
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(41, 128, 185));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));
        
        JLabel headerLabel = new JLabel("🛒 SUPERMARKET BILLING DASHBOARD");
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        headerLabel.setForeground(Color.WHITE);
        headerPanel.add(headerLabel);
        
        return headerPanel;
    }

    private JPanel createInputPanel() {
        JPanel topContainer = new JPanel(new BorderLayout());
        topContainer.setOpaque(false);

        JPanel inputPanel = new JPanel(new GridLayout(2, 4, 15, 5));
        inputPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Product Entry"));
        inputPanel.setBackground(Color.WHITE);

        itemBox = new JComboBox<>(productPrices.keySet().toArray(new String[0]));
        itemBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        priceField = new JTextField();
        priceField.setEditable(false);
        priceField.setFont(new Font("Segoe UI", Font.BOLD, 14));
        priceField.setBackground(new Color(236, 240, 241));
        
        quantityField = new JTextField("1");
        quantityField.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JButton addButton = createStyledButton("Add Item", new Color(39, 174, 96));

        inputPanel.add(new JLabel("Select Item:"));
        inputPanel.add(new JLabel("Unit Price (₹):"));
        inputPanel.add(new JLabel("Quantity:"));
        inputPanel.add(new JLabel(""));

        inputPanel.add(itemBox);
        inputPanel.add(priceField);
        inputPanel.add(quantityField);
        inputPanel.add(addButton);

        topContainer.add(inputPanel, BorderLayout.CENTER);
        topContainer.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));

        itemBox.addActionListener(e -> updatePrice());
        addButton.addActionListener(e -> addItem());

        return topContainer;
    }

    private JScrollPane createTable() {
        model = new DefaultTableModel(new String[]{"Item Name", "Price (₹)", "Quantity", "Total (₹)"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(model);
        table.setRowHeight(30);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.getTableHeader().setBackground(new Color(223, 230, 233));
        table.setSelectionBackground(new Color(116, 185, 255));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 1; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        scrollPane.getViewport().setBackground(Color.WHITE);
        
        return scrollPane;
    }

    private JPanel createFooter() {
        JPanel bottomPanel = new JPanel(new BorderLayout(10, 10));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 15, 10));
        bottomPanel.setOpaque(false);

        JPanel summaryPanel = new JPanel(new GridLayout(3, 1, 0, 5));
        summaryPanel.setOpaque(false);
        
        subTotalLabel = new JLabel("Subtotal: ₹0.00");
        taxLabel = new JLabel("Tax (5% GST): ₹0.00");
        grandTotalLabel = new JLabel("Grand Total: ₹0.00");
        
        Font summaryFont = new Font("Segoe UI", Font.BOLD, 14);
        subTotalLabel.setFont(summaryFont);
        taxLabel.setFont(summaryFont);
        grandTotalLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        grandTotalLabel.setForeground(new Color(192, 57, 43));

        summaryPanel.add(subTotalLabel);
        summaryPanel.add(taxLabel);
        summaryPanel.add(grandTotalLabel);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setOpaque(false);

        JButton removeButton = createStyledButton("Remove Item", new Color(231, 76, 60));
        JButton clearButton = createStyledButton("Clear Cart", new Color(149, 165, 166));
        JButton billButton = createStyledButton("Generate Bill", new Color(41, 128, 185));

        buttonPanel.add(removeButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(billButton);

        bottomPanel.add(summaryPanel, BorderLayout.WEST);
        bottomPanel.add(buttonPanel, BorderLayout.EAST);

        removeButton.addActionListener(e -> removeItem());
        clearButton.addActionListener(e -> clearAll());
        billButton.addActionListener(e -> generateBill());

        return bottomPanel;
    }

    private JButton createStyledButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    private void updatePrice() {
        String selected = (String) itemBox.getSelectedItem();
        if (selected != null) {
            priceField.setText(String.format("%.2f", productPrices.get(selected)));
        }
    }

    private void updateTotals() {
        double tax = subTotal * TAX_RATE;
        double grandTotal = subTotal + tax;

        subTotalLabel.setText(String.format("Subtotal: ₹%.2f", subTotal));
        taxLabel.setText(String.format("Tax (5%% GST): ₹%.2f", tax));
        grandTotalLabel.setText(String.format("Grand Total: ₹%.2f", grandTotal));
    }

    private void addItem() {
        try {
            String item = (String) itemBox.getSelectedItem();
            double price = Double.parseDouble(priceField.getText());
            int qty = Integer.parseInt(quantityField.getText());

            if (qty <= 0) {
                JOptionPane.showMessageDialog(this, "Quantity must be greater than zero.", "Input Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            double total = price * qty;
            subTotal += total;

            model.addRow(new Object[]{item, String.format("%.2f", price), qty, String.format("%.2f", total)});
            updateTotals();
            
            quantityField.setText("1");

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid numeric quantity.", "Input Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void removeItem() {
        int row = table.getSelectedRow();
        if (row >= 0) {
            String valStr = (String) model.getValueAt(row, 3);
            double val = Double.parseDouble(valStr);
            
            subTotal -= val;
            model.removeRow(row);
            updateTotals();
        } else {
            JOptionPane.showMessageDialog(this, "Please select an item from the table to remove.", "Selection Error", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void clearAll() {
        if (model.getRowCount() == 0) return;
        
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to clear the cart?", "Confirm Clear", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            model.setRowCount(0);
            subTotal = 0;
            updateTotals();
        }
    }

    private void generateBill() {
        if (model.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Cart is empty. Add items to generate a bill.", "Empty Cart", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        StringBuilder bill = new StringBuilder();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");  
        LocalDateTime now = LocalDateTime.now();  

        bill.append("==============================================\n");
        bill.append("           MODERN SUPERMARKET POS             \n");
        bill.append("==============================================\n");
        bill.append("Date: ").append(dtf.format(now)).append("\n");
        bill.append("----------------------------------------------\n");
        bill.append(String.format("%-20s %-8s %-5s %-10s\n", "Item", "Price", "Qty", "Total"));
        bill.append("----------------------------------------------\n");

        for (int i = 0; i < model.getRowCount(); i++) {
            String name = (String) model.getValueAt(i, 0);
            if (name.length() > 18) name = name.substring(0, 15) + "...";
            
            String price = (String) model.getValueAt(i, 1);
            String qty = String.valueOf(model.getValueAt(i, 2));
            String total = (String) model.getValueAt(i, 3);

            bill.append(String.format("%-20s ₹%-7s %-5s ₹%-10s\n", name, price, qty, total));
        }

        double tax = subTotal * TAX_RATE;
        double grandTotal = subTotal + tax;

        bill.append("----------------------------------------------\n");
        bill.append(String.format("%-34s ₹%.2f\n", "Subtotal:", subTotal));
        bill.append(String.format("%-34s ₹%.2f\n", "GST (5%):", tax));
        bill.append("==============================================\n");
        bill.append(String.format("%-34s ₹%.2f\n", "GRAND TOTAL:", grandTotal));
        bill.append("==============================================\n");
        bill.append("         Thank you for shopping with us!      \n");

        JTextArea area = new JTextArea(bill.toString());
        area.setFont(new Font("Monospaced", Font.PLAIN, 14));
        area.setEditable(false);
        area.setBackground(new Color(253, 253, 227));
        
        JOptionPane.showMessageDialog(this, new JScrollPane(area), "Final Invoice", JOptionPane.PLAIN_MESSAGE);
    }

    public static void main(String[] args) {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ex) {}
        }

        SwingUtilities.invokeLater(() -> {
            new SupermarketBillingSystem().setVisible(true);
        });
    }
}

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;

public class SupermarketBillingSystem extends JFrame {

    JComboBox<String> itemBox;
    JTextField priceField, quantityField;
    JLabel totalLabel;
    DefaultTableModel model;
    JTable table;
    double grandTotal = 0;

    HashMap<String, Double> productPrices;

    public SupermarketBillingSystem() {
        setTitle("Supermarket Billing System");
        setSize(750, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        productPrices = new HashMap<>();
        productPrices.put("Milk", 50.0);
        productPrices.put("Bread", 30.0);
        productPrices.put("Rice (1kg)", 70.0);
        productPrices.put("Sugar (1kg)", 45.0);
        productPrices.put("Eggs (12)", 80.0);
        productPrices.put("Oil (1L)", 150.0);
        productPrices.put("Biscuits", 20.0);
        productPrices.put("Soap", 35.0);

        JPanel inputPanel = new JPanel(new GridLayout(2, 4, 10, 10));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        itemBox = new JComboBox<>(productPrices.keySet().toArray(new String[0]));
        priceField = new JTextField();
        priceField.setEditable(false);
        quantityField = new JTextField();

        JButton addButton = new JButton("Add Item");

        inputPanel.add(new JLabel("Item"));
        inputPanel.add(new JLabel("Price"));
        inputPanel.add(new JLabel("Quantity"));
        inputPanel.add(new JLabel(""));

        inputPanel.add(itemBox);
        inputPanel.add(priceField);
        inputPanel.add(quantityField);
        inputPanel.add(addButton);

        add(inputPanel, BorderLayout.NORTH);

        model = new DefaultTableModel();
        model.addColumn("Item");
        model.addColumn("Price");
        model.addColumn("Quantity");
        model.addColumn("Total");

        table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());

        totalLabel = new JLabel("Total: ₹0.0");
        totalLabel.setFont(new Font("Arial", Font.BOLD, 18));

        JButton removeButton = new JButton("Remove Selected");
        JButton clearButton = new JButton("Clear");
        JButton billButton = new JButton("Generate Bill");

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(removeButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(billButton);

        bottomPanel.add(totalLabel, BorderLayout.WEST);
        bottomPanel.add(buttonPanel, BorderLayout.EAST);

        add(bottomPanel, BorderLayout.SOUTH);

        itemBox.addActionListener(e -> updatePrice());
        addButton.addActionListener(e -> addItem());
        removeButton.addActionListener(e -> removeItem());
        clearButton.addActionListener(e -> clearAll());
        billButton.addActionListener(e -> generateBill());

        updatePrice();
    }

    void updatePrice() {
        String selected = (String) itemBox.getSelectedItem();
        priceField.setText(String.valueOf(productPrices.get(selected)));
    }

    void addItem() {
        try {
            String item = (String) itemBox.getSelectedItem();
            double price = Double.parseDouble(priceField.getText());
            int qty = Integer.parseInt(quantityField.getText());

            double total = price * qty;
            grandTotal += total;

            model.addRow(new Object[]{item, price, qty, total});
            totalLabel.setText("Total: ₹" + grandTotal);

            quantityField.setText("");

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Invalid Quantity");
        }
    }

    void removeItem() {
        int row = table.getSelectedRow();
        if (row >= 0) {
            double val = (double) model.getValueAt(row, 3);
            grandTotal -= val;
            model.removeRow(row);
            totalLabel.setText("Total: ₹" + grandTotal);
        }
    }

    void clearAll() {
        model.setRowCount(0);
        grandTotal = 0;
        totalLabel.setText("Total: ₹0.0");
    }

    void generateBill() {
        StringBuilder bill = new StringBuilder();
        bill.append("------ Supermarket Bill ------\n\n");

        for (int i = 0; i < model.getRowCount(); i++) {
            bill.append(model.getValueAt(i, 0)).append("  x")
                .append(model.getValueAt(i, 2)).append("  = ₹")
                .append(model.getValueAt(i, 3)).append("\n");
        }

        bill.append("\n-----------------------------\n");
        bill.append("Grand Total: ₹").append(grandTotal);

        JTextArea area = new JTextArea(bill.toString());
        area.setFont(new Font("Monospaced", Font.PLAIN, 14));
        JOptionPane.showMessageDialog(this, new JScrollPane(area), "Bill", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new SupermarketBillingSystem().setVisible(true);
        });
    }
}

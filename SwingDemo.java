import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class SwingDemo extends JFrame {

    // ── State ──────────────────────────────────────────────────────────────
    private JLabel     statusLabel;
    private JTextArea  logArea;
    private DrawPanel  drawPanel;
    private JTextField inputField;
    private JButton    clearBtn, colorBtn, undoBtn;
    private JComboBox<String> shapeBox;
    private JSlider    sizeSlider;
    private JCheckBox  fillCheck;
    private Color      currentColor = Color.BLUE;

    // ── Entry point ────────────────────────────────────────────────────────
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            SwingDemo app = new SwingDemo();
            app.setVisible(true);
        });
    }

    // ── Constructor: build the UI ──────────────────────────────────────────
    public SwingDemo() {
        super("Java AWT & Swing — Full Demo");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(900, 650);
        setLocationRelativeTo(null);

        // ── Menu bar ──────────────────────────────────────────────────────
        setJMenuBar(buildMenuBar());

        // ── Main layout ───────────────────────────────────────────────────
        setLayout(new BorderLayout(6, 6));
        add(buildToolPanel(),   BorderLayout.NORTH);
        add(buildCenterPanel(), BorderLayout.CENTER);
        add(buildSouthPanel(),  BorderLayout.SOUTH);
    }

    // ── Menu bar ──────────────────────────────────────────────────────────
    private JMenuBar buildMenuBar() {
        JMenuBar bar = new JMenuBar();

        // File menu
        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic(KeyEvent.VK_F);

        JMenuItem newItem  = new JMenuItem("New",  KeyEvent.VK_N);
        JMenuItem saveItem = new JMenuItem("Save", KeyEvent.VK_S);
        JMenuItem exitItem = new JMenuItem("Exit", KeyEvent.VK_X);

        // Keyboard shortcut: Ctrl+N
        newItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK));
        newItem.addActionListener(e -> {
            drawPanel.clear();
            log("Canvas cleared via menu.");
        });

        saveItem.addActionListener(e ->
            JOptionPane.showMessageDialog(this, "Save not implemented in demo.", "Save", JOptionPane.INFORMATION_MESSAGE));

        exitItem.addActionListener(e -> System.exit(0));

        fileMenu.add(newItem);
        fileMenu.add(saveItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);

        // Help menu
        JMenu helpMenu = new JMenu("Help");
        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(e ->
            JOptionPane.showMessageDialog(this,
                "AWT & Swing Demo\nDraw shapes on the canvas.\nRight-click for context menu.",
                "About", JOptionPane.INFORMATION_MESSAGE));
        helpMenu.add(aboutItem);

        bar.add(fileMenu);
        bar.add(helpMenu);
        return bar;
    }

    // ── Top toolbar ───────────────────────────────────────────────────────
    private JPanel buildToolPanel() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        p.setBorder(new TitledBorder("Controls"));

        // Shape chooser
        p.add(new JLabel("Shape:"));
        shapeBox = new JComboBox<>(new String[]{"Circle", "Rectangle", "Line"});
        shapeBox.addActionListener(e -> log("Shape → " + shapeBox.getSelectedItem()));
        p.add(shapeBox);

        // Size slider
        p.add(new JLabel("Size:"));
        sizeSlider = new JSlider(10, 100, 40);
        sizeSlider.setMajorTickSpacing(30);
        sizeSlider.setPaintTicks(true);
        sizeSlider.setPaintLabels(true);
        sizeSlider.addChangeListener(e -> log("Size → " + sizeSlider.getValue()));
        p.add(sizeSlider);

        // Fill checkbox
        fillCheck = new JCheckBox("Fill", true);
        fillCheck.addItemListener(e ->
            log("Fill → " + (e.getStateChange() == ItemEvent.SELECTED ? "on" : "off")));
        p.add(fillCheck);

        // Color button
        colorBtn = new JButton("Color…");
        colorBtn.addActionListener(e -> {
            Color c = JColorChooser.showDialog(this, "Pick a color", currentColor);
            if (c != null) {
                currentColor = c;
                colorBtn.setBackground(c);
                log("Color → " + String.format("#%06X", c.getRGB() & 0xFFFFFF));
            }
        });
        p.add(colorBtn);

        // Clear & Undo
        clearBtn = new JButton("Clear");
        clearBtn.addActionListener(e -> { drawPanel.clear(); log("Cleared."); });
        p.add(clearBtn);

        undoBtn = new JButton("Undo");
        undoBtn.addActionListener(e -> { drawPanel.undo(); log("Undo."); });
        p.add(undoBtn);

        return p;
    }

    // ── Center: canvas + log ───────────────────────────────────────────────
    private JSplitPane buildCenterPanel() {
        // Left: drawing canvas
        drawPanel = new DrawPanel();
        JScrollPane canvasScroll = new JScrollPane(drawPanel);
        canvasScroll.setBorder(new TitledBorder("Canvas — click / drag to draw"));

        // Right: event log
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane logScroll = new JScrollPane(logArea);
        logScroll.setBorder(new TitledBorder("Event log"));
        logScroll.setPreferredSize(new Dimension(260, 0));

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, canvasScroll, logScroll);
        split.setResizeWeight(0.72);
        return split;
    }

    // ── South: text input + status ─────────────────────────────────────────
    private JPanel buildSouthPanel() {
        JPanel p = new JPanel(new BorderLayout(6, 0));
        p.setBorder(new EmptyBorder(4, 6, 6, 6));

        // Text input with ActionListener (Enter key)
        inputField = new JTextField();
        inputField.setToolTipText("Type something and press Enter");
        inputField.addActionListener(e -> {
            String text = inputField.getText().trim();
            if (!text.isEmpty()) {
                log("Input → \"" + text + "\"");
                inputField.setText("");
            }
        });

        // Key listener on the field
        inputField.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                log("Key pressed: " + KeyEvent.getKeyText(e.getKeyCode()));
            }
        });

        JButton sendBtn = new JButton("Send");
        sendBtn.addActionListener(inputField.getActionListeners()[0]);

        statusLabel = new JLabel("Ready");
        statusLabel.setBorder(new EmptyBorder(0, 6, 0, 0));

        p.add(new JLabel("Input: "), BorderLayout.WEST);
        p.add(inputField,            BorderLayout.CENTER);
        p.add(sendBtn,               BorderLayout.EAST);
        p.add(statusLabel,           BorderLayout.SOUTH);
        return p;
    }

    // ── Utility: append to log and update status ───────────────────────────
    private void log(String msg) {
        logArea.append(msg + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
        statusLabel.setText(msg);
    }

    // ══════════════════════════════════════════════════════════════════════
    // Inner class: the drawing canvas
    // ══════════════════════════════════════════════════════════════════════
    class DrawPanel extends JPanel {

        // A simple record for each drawn shape
        record Shape(String type, int x, int y, int w, int h, Color color, boolean fill) {}

        private final List<Shape> shapes  = new ArrayList<>();
        private Point             dragStart;  // for live drag preview
        private Point             dragEnd;

        DrawPanel() {
            setBackground(Color.WHITE);
            setPreferredSize(new Dimension(600, 500));

            // ── Mouse listener (press / release / click) ──────────────────
            addMouseListener(new MouseAdapter() {

                @Override
                public void mousePressed(MouseEvent e) {
                    dragStart = e.getPoint();
                    dragEnd   = e.getPoint();
                    log("Mouse pressed at (" + e.getX() + ", " + e.getY() + ")");

                    // Right-click → context menu
                    if (SwingUtilities.isRightMouseButton(e)) {
                        showContextMenu(e);
                    }
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    if (SwingUtilities.isLeftMouseButton(e) && dragStart != null) {
                        commitShape(dragStart, e.getPoint());
                        dragStart = dragEnd = null;
                        repaint();
                    }
                    log("Mouse released at (" + e.getX() + ", " + e.getY() + ")");
                }

                @Override
                public void mouseClicked(MouseEvent e) {
                    log("Clicks: " + e.getClickCount()
                        + " | Button: " + e.getButton()
                        + " | Mods: " + MouseEvent.getModifiersExText(e.getModifiersEx()));
                }

                @Override public void mouseEntered(MouseEvent e) { log("Mouse entered canvas."); }
                @Override public void mouseExited (MouseEvent e) { log("Mouse exited canvas."); }
            });

            // ── Mouse motion listener (move / drag) ───────────────────────
            addMouseMotionListener(new MouseMotionAdapter() {

                @Override
                public void mouseDragged(MouseEvent e) {
                    dragEnd = e.getPoint();
                    repaint();   // live preview
                    statusLabel.setText("Dragging to (" + e.getX() + ", " + e.getY() + ")");
                }

                @Override
                public void mouseMoved(MouseEvent e) {
                    statusLabel.setText("(" + e.getX() + ", " + e.getY() + ")");
                }
            });

            // ── Mouse wheel listener ───────────────────────────────────────
            addMouseWheelListener(e -> {
                int rot = e.getWheelRotation();
                int cur = sizeSlider.getValue();
                sizeSlider.setValue(Math.max(10, Math.min(100, cur - rot * 3)));
                log("Wheel: " + rot + " → size " + sizeSlider.getValue());
            });

            // ── Window focus listener (on the parent frame) ────────────────
            SwingDemo.this.addWindowFocusListener(new WindowFocusListener() {
                @Override public void windowGainedFocus(WindowEvent e) { log("Window gained focus."); }
                @Override public void windowLostFocus  (WindowEvent e) { log("Window lost focus."); }
            });

            // ── Component listener ─────────────────────────────────────────
            addComponentListener(new ComponentAdapter() {
                @Override public void componentResized(ComponentEvent e) {
                    log("Canvas resized → " + getWidth() + "×" + getHeight());
                }
            });
        }

        // ── Paint ──────────────────────────────────────────────────────────
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Draw committed shapes
            for (Shape s : shapes) drawShape(g2, s);

            // Live drag preview
            if (dragStart != null && dragEnd != null) {
                g2.setColor(new Color(currentColor.getRed(), currentColor.getGreen(),
                                      currentColor.getBlue(), 120));
                drawPreview(g2, dragStart, dragEnd);
            }
        }

        private void drawShape(Graphics2D g2, Shape s) {
            g2.setColor(s.color());
            switch (s.type()) {
                case "Circle" -> {
                    if (s.fill()) g2.fillOval(s.x(), s.y(), s.w(), s.h());
                    else          g2.drawOval(s.x(), s.y(), s.w(), s.h());
                }
                case "Rectangle" -> {
                    if (s.fill()) g2.fillRect(s.x(), s.y(), s.w(), s.h());
                    else          g2.drawRect(s.x(), s.y(), s.w(), s.h());
                }
                case "Line" -> g2.drawLine(s.x(), s.y(), s.x() + s.w(), s.y() + s.h());
            }
        }

        private void drawPreview(Graphics2D g2, Point a, Point b) {
            int x = Math.min(a.x, b.x), y = Math.min(a.y, b.y);
            int w = Math.abs(b.x - a.x),  h = Math.abs(b.y - a.y);
            String type = (String) shapeBox.getSelectedItem();
            switch (type) {
                case "Circle"    -> g2.drawOval(x, y, w, h);
                case "Rectangle" -> g2.drawRect(x, y, w, h);
                case "Line"      -> g2.drawLine(a.x, a.y, b.x, b.y);
            }
        }

        // ── Commit drag to shape list ──────────────────────────────────────
        private void commitShape(Point a, Point b) {
            String type = (String) shapeBox.getSelectedItem();
            int x = Math.min(a.x, b.x), y = Math.min(a.y, b.y);
            int w = Math.abs(b.x - a.x),  h = Math.abs(b.y - a.y);
            if (w < 2 && h < 2) return;   // ignore accidental clicks
            shapes.add(new Shape(type, x, y, w, h, currentColor, fillCheck.isSelected()));
            log("Drew " + type + " at (" + x + "," + y + ") " + w + "×" + h);
        }

        // ── Context menu (right-click) ─────────────────────────────────────
        private void showContextMenu(MouseEvent e) {
            JPopupMenu menu = new JPopupMenu();

            JMenuItem clearItem = new JMenuItem("Clear canvas");
            clearItem.addActionListener(a -> { clear(); log("Cleared via context menu."); });

            JMenuItem undoItem = new JMenuItem("Undo last");
            undoItem.addActionListener(a -> undo());

            JMenuItem colorItem = new JMenuItem("Change color…");
            colorItem.addActionListener(a -> colorBtn.doClick());

            menu.add(clearItem);
            menu.add(undoItem);
            menu.addSeparator();
            menu.add(colorItem);
            menu.show(this, e.getX(), e.getY());
        }

        void clear() { shapes.clear(); repaint(); }
        void undo()  { if (!shapes.isEmpty()) { shapes.remove(shapes.size() - 1); repaint(); } }
    }
}

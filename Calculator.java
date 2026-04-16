import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;

public class Calculator extends JFrame {

    private JTextField display;
    private JLabel historyLabel;
    private JLabel modeLabel;
    private String currentInput = "";
    private String operator = "";
    private double firstOperand = 0;
    private boolean newInput = true;
    private boolean isRadians = true;

    private static final Color BG         = new Color(10, 10, 18);
    private static final Color PANEL_BG   = new Color(16, 16, 28);
    private static final Color DISPLAY_BG = new Color(8, 8, 16);
    private static final Color ACCENT     = new Color(99, 220, 190);
    private static final Color ACCENT2    = new Color(255, 120, 160);
    private static final Color NUM_BG     = new Color(22, 22, 38);
    private static final Color NUM_HOVER  = new Color(32, 32, 54);
    private static final Color OP_BG      = new Color(30, 28, 52);
    private static final Color OP_HOVER   = new Color(46, 42, 80);
    private static final Color FUNC_BG    = new Color(18, 30, 42);
    private static final Color FUNC_HOVER = new Color(26, 46, 64);
    private static final Color EQ_BG      = new Color(99, 220, 190);
    private static final Color EQ_HOVER   = new Color(130, 240, 210);
    private static final Color TEXT_DIM   = new Color(120, 120, 160);
    private static final Color TEXT_MAIN  = new Color(230, 230, 255);

    public Calculator() {
        setTitle("CALC·X");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setBackground(BG);

        JPanel root = new JPanel(new BorderLayout()) {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, new Color(14, 12, 28), getWidth(), getHeight(), new Color(8, 18, 28));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                drawGrid(g2);
            }
            private void drawGrid(Graphics2D g2) {
                g2.setColor(new Color(40, 40, 70, 40));
                g2.setStroke(new BasicStroke(0.5f));
                for (int x = 0; x < getWidth(); x += 30) g2.drawLine(x, 0, x, getHeight());
                for (int y = 0; y < getHeight(); y += 30) g2.drawLine(0, y, getWidth(), y);
                g2.setColor(new Color(99, 220, 190, 15));
                g2.setStroke(new BasicStroke(1f));
                g2.drawLine(0, getHeight() / 2, getWidth(), getHeight() / 2);
                g2.drawLine(getWidth() / 2, 0, getWidth() / 2, getHeight());
            }
        };
        root.setOpaque(false);
        root.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel header = buildHeader();
        JPanel displayPanel = buildDisplay();
        JPanel buttons = buildButtons();

        root.add(header, BorderLayout.NORTH);
        root.add(displayPanel, BorderLayout.CENTER);
        root.add(buttons, BorderLayout.SOUTH);

        setContentPane(root);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(0, 4, 14, 4));

        JLabel title = new JLabel("CALC·X") {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, ACCENT, getWidth(), 0, ACCENT2);
                g2.setPaint(gp);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), 0, fm.getAscent());
            }
        };
        title.setFont(new Font("Courier New", Font.BOLD, 22));
        title.setPreferredSize(new Dimension(100, 28));

        modeLabel = new JLabel("RAD") {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(99, 220, 190, 40));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(ACCENT);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.setFont(getFont());
                g2.setColor(ACCENT);
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), x, y);
            }
        };
        modeLabel.setFont(new Font("Courier New", Font.BOLD, 11));
        modeLabel.setPreferredSize(new Dimension(48, 22));
        modeLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        modeLabel.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                isRadians = !isRadians;
                modeLabel.setText(isRadians ? "RAD" : "DEG");
                modeLabel.repaint();
            }
        });

        p.add(title, BorderLayout.WEST);
        p.add(modeLabel, BorderLayout.EAST);
        return p;
    }

    private JPanel buildDisplay() {
        JPanel outer = new JPanel(new BorderLayout()) {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(DISPLAY_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                GradientPaint border = new GradientPaint(0, 0, ACCENT, getWidth(), getHeight(), ACCENT2);
                g2.setPaint(border);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 16, 16);
            }
        };
        outer.setOpaque(false);
        outer.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        historyLabel = new JLabel(" ");
        historyLabel.setFont(new Font("Courier New", Font.PLAIN, 12));
        historyLabel.setForeground(TEXT_DIM);
        historyLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        display = new JTextField("0") {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(new Color(0, 0, 0, 0));
                g2.fillRect(0, 0, getWidth(), getHeight());
                super.paintComponent(g);
            }
        };
        display.setFont(new Font("Courier New", Font.BOLD, 36));
        display.setForeground(TEXT_MAIN);
        display.setBackground(new Color(0, 0, 0, 0));
        display.setOpaque(false);
        display.setBorder(null);
        display.setHorizontalAlignment(JTextField.RIGHT);
        display.setEditable(false);
        display.setCaretColor(ACCENT);

        JPanel inner = new JPanel(new BorderLayout(0, 4));
        inner.setOpaque(false);
        inner.add(historyLabel, BorderLayout.NORTH);
        inner.add(display, BorderLayout.CENTER);

        outer.add(inner);
        outer.setPreferredSize(new Dimension(380, 100));
        return outer;
    }

    private JPanel buildButtons() {
        JPanel p = new JPanel(new GridLayout(7, 4, 8, 8));
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(14, 0, 0, 0));

        String[][] layout = {
            {"sin",  "cos",  "tan",  "π"},
            {"sin⁻¹","cos⁻¹","tan⁻¹","e"},
            {"x²",  "√",    "%",    "1/x"},
            {"C",   "⌫",    "(",    ")"},
            {"7",   "8",    "9",    "÷"},
            {"4",   "5",    "6",    "×"},
            {"1",   "2",    "3",    "−"},
            {"±",   "0",    ".",    "+"},
            {"",    "",     "",     "="}
        };

        String[][] extLayout = {
            {"sin","cos","tan","π"},
            {"sin⁻¹","cos⁻¹","tan⁻¹","e"},
            {"x²","√","%","1/x"},
            {"C","⌫","(", ")"},
            {"7","8","9","÷"},
            {"4","5","6","×"},
            {"1","2","3","−"},
            {"±","0",".","+" },
        };

        p.setLayout(new GridLayout(8, 4, 8, 8));

        for (String[] row : extLayout) {
            for (String label : row) {
                p.add(createButton(label));
            }
        }

        JPanel bottomRow = new JPanel(new GridLayout(1, 4, 8, 0));
        bottomRow.setOpaque(false);
        bottomRow.add(createButton("±"));
        bottomRow.add(createButton("0"));
        bottomRow.add(createButton("."));

        CalcButton eq = new CalcButton("=", EQ_BG, EQ_HOVER, new Color(10, 10, 18));
        eq.addActionListener(e -> handleInput("="));
        bottomRow.add(eq);

        JPanel wrapper = new JPanel(new BorderLayout(0, 8));
        wrapper.setOpaque(false);
        wrapper.add(p, BorderLayout.CENTER);
        wrapper.add(bottomRow, BorderLayout.SOUTH);
        return wrapper;
    }

    private JButton createButton(String label) {
        Color bg, hover, fg;
        if (label.isEmpty()) {
            JPanel ph = new JPanel(); ph.setOpaque(false); return new JButton();
        }
        switch (label) {
            case "C": case "⌫":
                bg = new Color(50, 20, 30); hover = new Color(80, 30, 45); fg = ACCENT2; break;
            case "÷": case "×": case "−": case "+":
                bg = OP_BG; hover = OP_HOVER; fg = ACCENT2; break;
            case "sin": case "cos": case "tan": case "sin⁻¹": case "cos⁻¹": case "tan⁻¹":
            case "x²": case "√": case "%": case "1/x": case "π": case "e":
            case "(": case ")": case "±":
                bg = FUNC_BG; hover = FUNC_HOVER; fg = ACCENT; break;
            default:
                bg = NUM_BG; hover = NUM_HOVER; fg = TEXT_MAIN; break;
        }
        CalcButton btn = new CalcButton(label, bg, hover, fg);
        btn.addActionListener(e -> handleInput(label));
        return btn;
    }

    private void handleInput(String cmd) {
        switch (cmd) {
            case "C":
                currentInput = ""; firstOperand = 0; operator = ""; newInput = true;
                display.setText("0"); historyLabel.setText(" "); break;
            case "⌫":
                if (currentInput.length() > 0) {
                    currentInput = currentInput.substring(0, currentInput.length() - 1);
                    display.setText(currentInput.isEmpty() ? "0" : currentInput);
                } break;
            case "(": case ")":
                currentInput += cmd; display.setText(currentInput); break;
            case "±":
                if (!currentInput.isEmpty() && !currentInput.equals("0")) {
                    if (currentInput.startsWith("-")) currentInput = currentInput.substring(1);
                    else currentInput = "-" + currentInput;
                    display.setText(currentInput);
                } break;
            case ".":
                if (!currentInput.contains(".")) {
                    currentInput = currentInput.isEmpty() ? "0." : currentInput + ".";
                    display.setText(currentInput);
                } break;
            case "π":
                currentInput = String.valueOf(Math.PI); display.setText(formatResult(Math.PI)); newInput = false; break;
            case "e":
                currentInput = String.valueOf(Math.E); display.setText(formatResult(Math.E)); newInput = false; break;
            case "÷": case "×": case "−": case "+":
                if (!currentInput.isEmpty()) firstOperand = Double.parseDouble(currentInput);
                operator = cmd; newInput = true; currentInput = "";
                historyLabel.setText(formatResult(firstOperand) + " " + cmd); break;
            case "=":
                if (!operator.isEmpty() && !currentInput.isEmpty()) {
                    double second = Double.parseDouble(currentInput);
                    historyLabel.setText(formatResult(firstOperand) + " " + operator + " " + formatResult(second) + " =");
                    double result = compute(firstOperand, second, operator);
                    display.setText(formatResult(result));
                    currentInput = String.valueOf(result);
                    operator = ""; newInput = true;
                } break;
            case "x²":
                if (!currentInput.isEmpty()) {
                    double v = Double.parseDouble(currentInput);
                    double r = v * v;
                    historyLabel.setText("(" + formatResult(v) + ")²");
                    display.setText(formatResult(r)); currentInput = String.valueOf(r);
                } break;
            case "√":
                applyUnary("√", v -> Math.sqrt(v)); break;
            case "%":
                if (!currentInput.isEmpty()) {
                    double v = Double.parseDouble(currentInput) / 100.0;
                    historyLabel.setText(currentInput + "%");
                    display.setText(formatResult(v)); currentInput = String.valueOf(v);
                } break;
            case "1/x":
                applyUnary("1/x", v -> 1.0 / v); break;
            case "sin":   applyTrig("sin", v -> Math.sin(toRad(v))); break;
            case "cos":   applyTrig("cos", v -> Math.cos(toRad(v))); break;
            case "tan":   applyTrig("tan", v -> Math.tan(toRad(v))); break;
            case "sin⁻¹": applyTrig("sin⁻¹", v -> fromRad(Math.asin(v))); break;
            case "cos⁻¹": applyTrig("cos⁻¹", v -> fromRad(Math.acos(v))); break;
            case "tan⁻¹": applyTrig("tan⁻¹", v -> fromRad(Math.atan(v))); break;
            default:
                if (newInput) { currentInput = cmd; newInput = false; }
                else currentInput += cmd;
                display.setText(currentInput);
        }
        animateDisplay();
    }

    private void applyUnary(String name, java.util.function.DoubleUnaryOperator fn) {
        if (!currentInput.isEmpty()) {
            double v = Double.parseDouble(currentInput);
            double r = fn.applyAsDouble(v);
            historyLabel.setText(name + "(" + formatResult(v) + ")");
            display.setText(formatResult(r)); currentInput = String.valueOf(r);
        }
    }

    private void applyTrig(String name, java.util.function.DoubleUnaryOperator fn) {
        applyUnary(name, fn);
    }

    private double toRad(double v)   { return isRadians ? v : Math.toRadians(v); }
    private double fromRad(double v) { return isRadians ? v : Math.toDegrees(v); }

    private double compute(double a, double b, String op) {
        switch (op) {
            case "+": return a + b;
            case "−": return a - b;
            case "×": return a * b;
            case "÷": return b != 0 ? a / b : Double.NaN;
            default:  return b;
        }
    }

    private String formatResult(double v) {
        if (Double.isNaN(v)) return "Error";
        if (Double.isInfinite(v)) return v > 0 ? "∞" : "-∞";
        if (v == Math.floor(v) && Math.abs(v) < 1e12) return String.valueOf((long) v);
        String s = String.format("%.8f", v).replaceAll("0+$", "").replaceAll("\\.$", "");
        return s;
    }

    private void animateDisplay() {
        display.setForeground(ACCENT);
        Timer t = new Timer(180, e -> display.setForeground(TEXT_MAIN));
        t.setRepeats(false); t.start();
    }

    static class CalcButton extends JButton {
        private final Color bg, hoverBg, fg;
        private boolean hovered = false;
        private float glowAlpha = 0f;
        private Timer glowTimer;

        CalcButton(String text, Color bg, Color hoverBg, Color fg) {
            super(text);
            this.bg = bg; this.hoverBg = hoverBg; this.fg = fg;
            setOpaque(false); setContentAreaFilled(false); setBorderPainted(false);
            setFocusPainted(false); setForeground(fg);
            setFont(new Font("Courier New", Font.BOLD, 14));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(80, 52));

            addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { hovered = true; startGlow(true); }
                public void mouseExited(MouseEvent e)  { hovered = false; startGlow(false); }
                public void mousePressed(MouseEvent e) { glowAlpha = 1f; repaint(); }
            });
        }

        private void startGlow(boolean in) {
            if (glowTimer != null) glowTimer.stop();
            glowTimer = new Timer(16, null);
            glowTimer.addActionListener(e -> {
                glowAlpha += in ? 0.12f : -0.12f;
                glowAlpha = Math.max(0f, Math.min(1f, glowAlpha));
                repaint();
                if ((in && glowAlpha >= 1f) || (!in && glowAlpha <= 0f)) glowTimer.stop();
            });
            glowTimer.start();
        }

        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Color current = hovered ? hoverBg : bg;
            g2.setColor(current);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);

            if (glowAlpha > 0) {
                Color glowColor = new Color(fg.getRed(), fg.getGreen(), fg.getBlue(), (int)(60 * glowAlpha));
                g2.setColor(glowColor);
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 12, 12);
            }

            g2.setColor(new Color(255, 255, 255, 12));
            g2.fillRoundRect(3, 3, getWidth() - 6, getHeight() / 2 - 4, 10, 10);

            g2.setFont(getFont());
            g2.setColor(fg);
            FontMetrics fm = g2.getFontMetrics();
            int tx = (getWidth() - fm.stringWidth(getText())) / 2;
            int ty = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
            g2.drawString(getText(), tx, ty);
        }
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}
        SwingUtilities.invokeLater(Calculator::new);
    }
}

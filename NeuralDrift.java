import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

public class NeuralDrift extends JFrame {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            NeuralDrift app = new NeuralDrift();
            app.setVisible(true);
        });
    }

    public NeuralDrift() {
        setTitle("Neural Drift — Generative Synaptic Art");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setUndecorated(true);

        NeuralCanvas canvas = new NeuralCanvas();
        add(canvas);

        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gd = ge.getDefaultScreenDevice();
        if (gd.isFullScreenSupported()) {
            gd.setFullScreenWindow(this);
        } else {
            setSize(1280, 800);
            setLocationRelativeTo(null);
        }

        canvas.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    gd.setFullScreenWindow(null);
                    dispose();
                } else if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                    canvas.togglePause();
                } else if (e.getKeyCode() == KeyEvent.VK_R) {
                    canvas.reset();
                } else if (e.getKeyCode() == KeyEvent.VK_UP) {
                    canvas.increaseNodes();
                } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                    canvas.decreaseNodes();
                }
            }
        });
        canvas.setFocusable(true);
        canvas.requestFocusInWindow();
    }
}

class NeuralCanvas extends JPanel {

    private static final int MAX_NODES = 120;
    private static final int MIN_NODES = 20;
    private static final double CONNECTION_RADIUS = 180.0;
    private static final int PULSE_POOL = 60;

    private List<NeuralNode> nodes = new ArrayList<>();
    private List<SynapticPulse> pulses = new ArrayList<>();
    private List<RippleEffect> ripples = new ArrayList<>();
    private BufferedImage offscreen;
    private Graphics2D og;
    private javax.swing.Timer animTimer;
    private boolean paused = false;
    private int targetNodeCount = 70;
    private long tick = 0;
    private Random rng = new Random();

    private Color[] palette = {
        new Color(0, 255, 180),
        new Color(100, 80, 255),
        new Color(255, 60, 120),
        new Color(255, 200, 50),
        new Color(50, 200, 255)
    };

    public NeuralCanvas() {
        setBackground(Color.BLACK);
        setDoubleBuffered(true);
        initNodes();

        animTimer = new javax.swing.Timer(16, e -> {
            if (!paused) {
                tick++;
                updateAll();
                repaint();
            }
        });
        animTimer.start();

        addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                spawnRipple(e.getX(), e.getY());
                if (SwingUtilities.isRightMouseButton(e)) {
                    addNodeAt(e.getX(), e.getY());
                }
            }
        });
        addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseMoved(MouseEvent e) {
                attractNodes(e.getX(), e.getY(), 0.3);
            }
        });
    }

    private void initNodes() {
        nodes.clear();
        pulses.clear();
        ripples.clear();
        int w = getWidth() == 0 ? 1280 : getWidth();
        int h = getHeight() == 0 ? 800 : getHeight();
        for (int i = 0; i < targetNodeCount; i++) {
            nodes.add(new NeuralNode(rng.nextDouble() * w, rng.nextDouble() * h, rng, palette));
        }
    }

    public void reset() {
        initNodes();
        tick = 0;
    }

    public void togglePause() { paused = !paused; }
    public void increaseNodes() { targetNodeCount = Math.min(targetNodeCount + 10, MAX_NODES); }
    public void decreaseNodes() { targetNodeCount = Math.max(targetNodeCount - 10, MIN_NODES); }

    private void addNodeAt(double x, double y) {
        if (nodes.size() < MAX_NODES) {
            nodes.add(new NeuralNode(x, y, rng, palette));
        }
    }

    private void spawnRipple(double x, double y) {
        ripples.add(new RippleEffect(x, y, palette[rng.nextInt(palette.length)]));
    }

    private void attractNodes(double mx, double my, double strength) {
        for (NeuralNode n : nodes) {
            double dx = mx - n.x, dy = my - n.y;
            double dist = Math.sqrt(dx * dx + dy * dy);
            if (dist < 200 && dist > 0) {
                n.vx += (dx / dist) * strength * (1 - dist / 200.0);
                n.vy += (dy / dist) * strength * (1 - dist / 200.0);
            }
        }
    }

    private void updateAll() {
        int w = getWidth(), h = getHeight();

        // Adjust node count
        while (nodes.size() < targetNodeCount)
            nodes.add(new NeuralNode(rng.nextDouble() * w, rng.nextDouble() * h, rng, palette));
        while (nodes.size() > targetNodeCount)
            nodes.remove(nodes.size() - 1);

        for (NeuralNode n : nodes) n.update(w, h);

        // Randomly fire synaptic pulses
        if (pulses.size() < PULSE_POOL && rng.nextInt(100) < 8) {
            NeuralNode src = nodes.get(rng.nextInt(nodes.size()));
            NeuralNode tgt = findClosestNeighbor(src);
            if (tgt != null) {
                Color c = palette[rng.nextInt(palette.length)];
                pulses.add(new SynapticPulse(src, tgt, c));
            }
        }

        pulses.removeIf(p -> { p.update(); return p.isDead(); });
        ripples.removeIf(r -> { r.update(); return r.isDead(); });
    }

    private NeuralNode findClosestNeighbor(NeuralNode src) {
        NeuralNode best = null;
        double bestDist = CONNECTION_RADIUS;
        for (NeuralNode n : nodes) {
            if (n == src) continue;
            double dx = n.x - src.x, dy = n.y - src.y;
            double d = Math.sqrt(dx * dx + dy * dy);
            if (d < bestDist) { bestDist = d; best = n; }
        }
        return best;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int w = getWidth(), h = getHeight();
        if (offscreen == null || offscreen.getWidth() != w || offscreen.getHeight() != h) {
            offscreen = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            og = offscreen.createGraphics();
            og.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            og.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        }

        // Fade trail
        og.setColor(new Color(0, 0, 0, 30));
        og.fillRect(0, 0, w, h);

        // Draw connections
        for (int i = 0; i < nodes.size(); i++) {
            NeuralNode a = nodes.get(i);
            for (int j = i + 1; j < nodes.size(); j++) {
                NeuralNode b = nodes.get(j);
                double dx = b.x - a.x, dy = b.y - a.y;
                double dist = Math.sqrt(dx * dx + dy * dy);
                if (dist < CONNECTION_RADIUS) {
                    float alpha = (float)(1.0 - dist / CONNECTION_RADIUS);
                    Color ca = blendColors(a.color, b.color, 0.5f);
                    og.setColor(new Color(ca.getRed(), ca.getGreen(), ca.getBlue(), (int)(alpha * 60)));
                    og.setStroke(new BasicStroke(0.5f + alpha * 1.5f));
                    og.drawLine((int)a.x, (int)a.y, (int)b.x, (int)b.y);
                }
            }
        }

        // Draw ripples
        for (RippleEffect r : ripples) r.draw(og);

        // Draw pulses
        for (SynapticPulse p : pulses) p.draw(og);

        // Draw nodes
        for (NeuralNode n : nodes) n.draw(og, tick);

        // UI overlay
        drawHUD(og, w, h);

        g.drawImage(offscreen, 0, 0, null);
    }

    private void drawHUD(Graphics2D g2, int w, int h) {
        g2.setFont(new Font("Courier New", Font.PLAIN, 12));
        String[] lines = {
            "NEURAL DRIFT  |  nodes: " + nodes.size() + "  |  pulses: " + pulses.size(),
            "[ESC] quit  [SPACE] pause  [R] reset  [↑↓] nodes  [click] ripple  [right-click] add node"
        };
        int pad = 16;
        for (int i = 0; i < lines.length; i++) {
            FontMetrics fm = g2.getFontMetrics();
            int tw = fm.stringWidth(lines[i]);
            g2.setColor(new Color(0, 0, 0, 140));
            g2.fillRoundRect(w - tw - pad * 2 - 4, h - pad - (lines.length - i) * 18 - 4,
                    tw + pad * 2 + 8, 20, 6, 6);
            g2.setColor(new Color(180, 255, 200, 200));
            g2.drawString(lines[i], w - tw - pad * 2, h - pad - (lines.length - i - 1) * 18 - 5);
        }

        // Pause indicator
        if (paused) {
            g2.setFont(new Font("Courier New", Font.BOLD, 36));
            String ps = "— PAUSED —";
            FontMetrics fm = g2.getFontMetrics();
            int tw = fm.stringWidth(ps);
            g2.setColor(new Color(0, 0, 0, 160));
            g2.fillRoundRect(w / 2 - tw / 2 - 12, h / 2 - 30, tw + 24, 44, 10, 10);
            g2.setColor(new Color(255, 220, 80, 220));
            g2.drawString(ps, w / 2 - tw / 2, h / 2 + 6);
        }
    }

    static Color blendColors(Color a, Color b, float t) {
        return new Color(
            (int)(a.getRed() * (1 - t) + b.getRed() * t),
            (int)(a.getGreen() * (1 - t) + b.getGreen() * t),
            (int)(a.getBlue() * (1 - t) + b.getBlue() * t)
        );
    }
}

class NeuralNode {
    double x, y, vx, vy;
    double phase;
    double size;
    Color color;
    private Random rng;

    NeuralNode(double x, double y, Random rng, Color[] palette) {
        this.x = x; this.y = y;
        this.rng = rng;
        vx = (rng.nextDouble() - 0.5) * 0.8;
        vy = (rng.nextDouble() - 0.5) * 0.8;
        phase = rng.nextDouble() * Math.PI * 2;
        size = 3 + rng.nextDouble() * 5;
        color = palette[rng.nextInt(palette.length)];
    }

    void update(int w, int h) {
        // Brownian drift
        vx += (rng.nextDouble() - 0.5) * 0.1;
        vy += (rng.nextDouble() - 0.5) * 0.1;

        // Dampen velocity
        double speed = Math.sqrt(vx * vx + vy * vy);
        if (speed > 1.5) { vx *= 1.5 / speed; vy *= 1.5 / speed; }

        x += vx; y += vy;

        // Wrap around edges smoothly
        if (x < -20) x = w + 20;
        if (x > w + 20) x = -20;
        if (y < -20) y = h + 20;
        if (y > h + 20) y = -20;

        phase += 0.03;
    }

    void draw(Graphics2D g2, long tick) {
        float pulse = (float)(0.6 + 0.4 * Math.sin(phase));
        int r = color.getRed(), gr = color.getGreen(), b = color.getBlue();
        int radius = (int)(size * pulse);

        // Outer glow
        for (int layer = 4; layer >= 1; layer--) {
            int alpha = (int)(15 * pulse * (5 - layer));
            g2.setColor(new Color(r, gr, b, Math.min(alpha, 255)));
            int lr = radius + layer * 4;
            g2.fillOval((int)x - lr, (int)y - lr, lr * 2, lr * 2);
        }

        // Core
        g2.setColor(new Color(r, gr, b, 220));
        g2.fillOval((int)x - radius, (int)y - radius, radius * 2, radius * 2);

        // Bright center
        g2.setColor(new Color(
            Math.min(r + 80, 255),
            Math.min(gr + 80, 255),
            Math.min(b + 80, 255), 200));
        int cr = Math.max(1, radius / 2);
        g2.fillOval((int)x - cr, (int)y - cr, cr * 2, cr * 2);
    }
}

class SynapticPulse {
    NeuralNode src, tgt;
    float progress = 0f;
    float speed;
    Color color;
    boolean dead = false;

    SynapticPulse(NeuralNode src, NeuralNode tgt, Color color) {
        this.src = src; this.tgt = tgt; this.color = color;
        speed = 0.015f + (float)(Math.random() * 0.02);
    }

    void update() {
        progress += speed;
        if (progress >= 1f) dead = true;
    }

    boolean isDead() { return dead; }

    void draw(Graphics2D g2) {
        double px = src.x + (tgt.x - src.x) * progress;
        double py = src.y + (tgt.y - src.y) * progress;
        int r = color.getRed(), gr = color.getGreen(), b = color.getBlue();

        // Trail
        for (int i = 1; i <= 5; i++) {
            float tp = Math.max(0, progress - i * 0.03f);
            double tx = src.x + (tgt.x - src.x) * tp;
            double ty = src.y + (tgt.y - src.y) * tp;
            int alpha = (int)(80 * (1f - (float)i / 5));
            g2.setColor(new Color(r, gr, b, alpha));
            g2.fillOval((int)tx - 2, (int)ty - 2, 4, 4);
        }

        // Head glow
        for (int layer = 3; layer >= 1; layer--) {
            g2.setColor(new Color(r, gr, b, 40 * (4 - layer)));
            g2.fillOval((int)px - layer * 4, (int)py - layer * 4, layer * 8, layer * 8);
        }
        g2.setColor(new Color(r, gr, b, 255));
        g2.fillOval((int)px - 3, (int)py - 3, 6, 6);
    }
}

class RippleEffect {
    double x, y;
    float radius = 0;
    float maxRadius = 80 + (float)(Math.random() * 80);
    float alpha = 1f;
    Color color;

    RippleEffect(double x, double y, Color color) {
        this.x = x; this.y = y; this.color = color;
    }

    void update() {
        radius += 3f;
        alpha = Math.max(0, 1f - radius / maxRadius);
    }

    boolean isDead() { return alpha <= 0; }

    void draw(Graphics2D g2) {
        int r = color.getRed(), gr = color.getGreen(), b = color.getBlue();
        g2.setColor(new Color(r, gr, b, (int)(alpha * 120)));
        g2.setStroke(new BasicStroke(2f * alpha));
        g2.drawOval((int)(x - radius), (int)(y - radius), (int)(radius * 2), (int)(radius * 2));

        if (radius > 10) {
            g2.setColor(new Color(r, gr, b, (int)(alpha * 60)));
            g2.setStroke(new BasicStroke(1f));
            float r2 = radius * 0.6f;
            g2.drawOval((int)(x - r2), (int)(y - r2), (int)(r2 * 2), (int)(r2 * 2));
        }
    }
}

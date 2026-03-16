import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class SpaceInvaders extends JFrame {

    public SpaceInvaders() {
        setTitle("SPACE INVADERS");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        GamePanel panel = new GamePanel();
        add(panel);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(SpaceInvaders::new);
    }
}

class GamePanel extends JPanel implements ActionListener, KeyListener {

    static final int W = 800, H = 700;
    static final Color NEON_CYAN   = new Color(0, 255, 240);
    static final Color NEON_PINK   = new Color(255, 0, 180);
    static final Color NEON_GREEN  = new Color(57, 255, 20);
    static final Color NEON_YELLOW = new Color(255, 220, 0);
    static final Color BG          = new Color(5, 5, 20);

    Timer timer;
    Player player;
    List<Enemy>    enemies   = new ArrayList<>();
    List<Bullet>   bullets   = new ArrayList<>();
    List<Particle> particles = new ArrayList<>();
    List<Star>     stars     = new ArrayList<>();

    int score = 0, lives = 3, level = 1;
    boolean gameOver = false, gameWon = false, started = false;
    long lastEnemyShot = 0;
    float bgPulse = 0;

    GamePanel() {
        setPreferredSize(new Dimension(W, H));
        setBackground(BG);
        setFocusable(true);
        addKeyListener(this);
        player = new Player(W / 2, H - 80);
        spawnEnemies();
        spawnStars();
        timer = new Timer(16, this);
        timer.start();
    }

    void spawnStars() {
        stars.clear();
        Random r = new Random();
        for (int i = 0; i < 120; i++)
            stars.add(new Star(r.nextInt(W), r.nextInt(H), r.nextFloat() * 2 + 0.3f));
    }

    void spawnEnemies() {
        enemies.clear();
        int rows = Math.min(3 + level, 6), cols = 10;
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                int x = 60 + col * 70;
                int y = 80 + row * 55;
                Color c = row == 0 ? NEON_PINK : row == 1 ? NEON_YELLOW : NEON_CYAN;
                int pts = row == 0 ? 30 : row == 1 ? 20 : 10;
                enemies.add(new Enemy(x, y, c, pts));
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!started || gameOver || gameWon) { repaint(); return; }
        bgPulse += 0.02f;
        updateStars();
        updatePlayer();
        updateEnemies();
        updateBullets();
        updateParticles();
        checkCollisions();
        enemyShoot();
        if (enemies.isEmpty()) { level++; gameWon = true; }
        repaint();
    }

    void updateStars() {
        for (Star s : stars) { s.y += s.speed; if (s.y > H) s.y = 0; }
    }

    void updatePlayer() {
        if (player.left  && player.x > 30)       player.x -= 5;
        if (player.right && player.x < W - 30)   player.x += 5;
        player.shootCooldown = Math.max(0, player.shootCooldown - 1);
        player.thrusterAnim += 0.2f;
    }

    void updateEnemies() {
        boolean hitEdge = false;
        float speed = 0.8f + level * 0.3f + (1f - enemies.size() / 60f) * 1.5f;
        for (Enemy en : enemies) {
            en.x += en.dir * speed;
            en.animTick += 0.05f;
            if (en.x > W - 30 || en.x < 30) hitEdge = true;
        }
        if (hitEdge) {
            for (Enemy en : enemies) { en.dir *= -1; en.y += 20; }
        }
        for (Enemy en : enemies) {
            if (en.y > H - 100) { gameOver = true; }
        }
    }

    void updateBullets() {
        Iterator<Bullet> it = bullets.iterator();
        while (it.hasNext()) {
            Bullet b = it.next();
            b.y += b.speed;
            b.trail.add(new float[]{b.x, b.y});
            if (b.trail.size() > 8) b.trail.remove(0);
            if (b.y < -10 || b.y > H + 10) it.remove();
        }
    }

    void updateParticles() {
        particles.removeIf(p -> {
            p.x += p.vx; p.y += p.vy; p.life -= p.decay;
            p.vy += 0.05f; return p.life <= 0;
        });
    }

    void enemyShoot() {
        long now = System.currentTimeMillis();
        long delay = Math.max(400, 1200 - level * 100L);
        if (now - lastEnemyShot > delay && !enemies.isEmpty()) {
            lastEnemyShot = now;
            Enemy shooter = enemies.get(new Random().nextInt(enemies.size()));
            bullets.add(new Bullet(shooter.x, shooter.y + 15, 4, NEON_PINK, false));
        }
    }

    void checkCollisions() {
        Iterator<Bullet> bi = bullets.iterator();
        while (bi.hasNext()) {
            Bullet b = bi.next();
            if (b.friendly) {
                Iterator<Enemy> ei = enemies.iterator();
                while (ei.hasNext()) {
                    Enemy en = ei.next();
                    if (dist(b.x, b.y, en.x, en.y) < 22) {
                        score += en.points;
                        explode(en.x, en.y, en.color, 18);
                        ei.remove(); bi.remove();
                        break;
                    }
                }
            } else {
                if (dist(b.x, b.y, player.x, player.y) < 20) {
                    lives--;
                    explode(player.x, player.y, NEON_CYAN, 20);
                    bi.remove();
                    if (lives <= 0) gameOver = true;
                }
            }
        }
    }

    void explode(float x, float y, Color c, int count) {
        Random r = new Random();
        for (int i = 0; i < count; i++) {
            float angle = (float)(r.nextFloat() * Math.PI * 2);
            float speed = r.nextFloat() * 4 + 1;
            particles.add(new Particle(x, y,
                (float)Math.cos(angle) * speed, (float)Math.sin(angle) * speed,
                c, r.nextFloat() * 0.02f + 0.01f));
        }
    }

    float dist(float x1, float y1, float x2, float y2) {
        float dx = x1 - x2, dy = y1 - y2;
        return (float)Math.sqrt(dx * dx + dy * dy);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        drawBackground(g2);
        drawStars(g2);

        if (!started) { drawStartScreen(g2); return; }
        if (gameOver)  { drawGameOver(g2);   return; }
        if (gameWon)   { drawLevelClear(g2); return; }

        drawParticles(g2);
        drawBullets(g2);
        drawEnemies(g2);
        drawPlayer(g2);
        drawHUD(g2);
        drawScanlines(g2);
    }

    void drawBackground(Graphics2D g2) {
        float pulse = (float)(Math.sin(bgPulse) * 0.5 + 0.5);
        GradientPaint gp = new GradientPaint(
            0, 0, new Color(5, 5, 30),
            W, H, new Color((int)(10 + pulse * 8), 5, (int)(20 + pulse * 15)));
        g2.setPaint(gp);
        g2.fillRect(0, 0, W, H);
    }

    void drawStars(Graphics2D g2) {
        for (Star s : stars) {
            float alpha = s.speed / 2.3f;
            g2.setColor(new Color(1f, 1f, 1f, alpha));
            int sz = s.speed > 1.5f ? 2 : 1;
            g2.fillOval((int)s.x, (int)s.y, sz, sz);
        }
    }

    void drawPlayer(Graphics2D g2) {
        int x = (int)player.x, y = (int)player.y;
        float t = player.thrusterAnim;

        int[] tx = {x - 6, x, x + 6};
        int[] ty = {y + 18, y + 6 + (int)(Math.abs(Math.sin(t)) * 10), y + 18};
        g2.setColor(new Color(255, 120, 0, 180));
        g2.fillPolygon(tx, ty, 3);

        g2.setColor(new Color(255, 200, 50, 120));
        int[] tx2 = {x - 3, x, x + 3};
        int[] ty2 = {y + 14, y + 4 + (int)(Math.abs(Math.sin(t * 1.3)) * 6), y + 14};
        g2.fillPolygon(tx2, ty2, 3);

        int[] sx = {x, x - 25, x - 20, x - 8, x + 8, x + 20, x + 25};
        int[] sy = {y - 25, y + 15, y + 5, y + 15, y + 15, y + 5, y + 15};
        g2.setColor(new Color(30, 200, 255, 220));
        g2.fillPolygon(sx, sy, 7);

        g2.setColor(NEON_CYAN);
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawPolygon(sx, sy, 7);

        g2.setColor(new Color(180, 240, 255));
        g2.fillOval(x - 6, y - 8, 12, 12);
        g2.setColor(new Color(255, 255, 255, 120));
        g2.fillOval(x - 3, y - 6, 5, 6);

        setGlow(g2, NEON_CYAN, 6);
        g2.setStroke(new BasicStroke(1f));
        g2.drawPolygon(sx, sy, 7);
        clearGlow(g2);
    }

    void drawEnemies(Graphics2D g2) {
        for (Enemy en : enemies) {
            int x = (int)en.x, y = (int)en.y;
            float anim = (float)Math.sin(en.animTick) * 2;

            g2.setColor(en.color);

            int[] bx = {x-14, x-10, x-14, x-6, x+6, x+14, x+10, x+14};
            int[] by = {(int)(y-8+anim), (int)(y-14+anim), (int)(y+8+anim), (int)(y+14+anim),
                        (int)(y+14+anim), (int)(y+8+anim), (int)(y-14+anim), (int)(y-8+anim)};
            g2.fillPolygon(bx, by, 8);

            g2.setColor(BG);
            g2.fillOval(x - 5, (int)(y - 5 + anim), 4, 6);
            g2.fillOval(x + 1, (int)(y - 5 + anim), 4, 6);

            setGlow(g2, en.color, 5);
            g2.setStroke(new BasicStroke(1f));
            g2.drawPolygon(bx, by, 8);
            clearGlow(g2);
        }
    }

    void drawBullets(Graphics2D g2) {
        for (Bullet b : bullets) {
            List<float[]> trail = b.trail;
            for (int i = 0; i < trail.size(); i++) {
                float alpha = (float) i / trail.size() * 0.6f;
                g2.setColor(new Color(b.color.getRed()/255f, b.color.getGreen()/255f,
                    b.color.getBlue()/255f, alpha));
                int sz = i < trail.size() / 2 ? 2 : 3;
                g2.fillOval((int)trail.get(i)[0] - sz/2, (int)trail.get(i)[1] - sz/2, sz, sz);
            }
            setGlow(g2, b.color, 4);
            g2.setColor(b.color);
            g2.fillRoundRect((int)b.x - 3, (int)b.y - 7, 6, 14, 3, 3);
            clearGlow(g2);
            g2.setColor(Color.WHITE);
            g2.fillRoundRect((int)b.x - 1, (int)b.y - 5, 2, 10, 2, 2);
        }
    }

    void drawParticles(Graphics2D g2) {
        for (Particle p : particles) {
            float alpha = Math.min(p.life, 1f);
            g2.setColor(new Color(p.color.getRed()/255f, p.color.getGreen()/255f,
                p.color.getBlue()/255f, alpha));
            int sz = (int)(p.life * 5) + 1;
            g2.fillOval((int)p.x - sz/2, (int)p.y - sz/2, sz, sz);
        }
    }

    void drawHUD(Graphics2D g2) {
        drawNeonText(g2, "SCORE: " + score, 20, 35, NEON_GREEN, 20);
        drawNeonText(g2, "LEVEL: " + level, W/2 - 50, 35, NEON_YELLOW, 20);

        for (int i = 0; i < lives; i++) {
            int lx = W - 40 - i * 28, ly = 18;
            int[] sx = {lx, lx-10, lx-8, lx-3, lx+3, lx+8, lx+10};
            int[] sy = {ly-10, ly+6, ly+2, ly+6, ly+6, ly+2, ly+6};
            g2.setColor(NEON_CYAN);
            g2.fillPolygon(sx, sy, 7);
        }

        g2.setColor(new Color(NEON_CYAN.getRed(), NEON_CYAN.getGreen(), NEON_CYAN.getBlue(), 60));
        g2.setStroke(new BasicStroke(1f));
        g2.drawLine(0, 50, W, 50);
        g2.drawLine(0, H - 50, W, H - 50);
    }

    void drawScanlines(Graphics2D g2) {
        g2.setColor(new Color(0, 0, 0, 18));
        for (int y = 0; y < H; y += 3) g2.drawLine(0, y, W, y);
    }

    void drawStartScreen(Graphics2D g2) {
        drawBackground(g2);
        drawStars(g2);
        drawScanlines(g2);

        float t = bgPulse;
        drawNeonText(g2, "SPACE INVADERS", W/2 - 190, 200, NEON_CYAN, 48);
        drawNeonText(g2, "NEO EDITION", W/2 - 100, 255, NEON_PINK, 28);

        if ((int)(t * 2) % 2 == 0)
            drawNeonText(g2, "PRESS ENTER TO PLAY", W/2 - 150, 380, NEON_GREEN, 22);

        drawNeonText(g2, "← → MOVE    SPACE SHOOT", W/2 - 170, 450, NEON_YELLOW, 16);
    }

    void drawGameOver(Graphics2D g2) {
        drawBackground(g2);
        drawStars(g2);
        drawParticles(g2);
        drawNeonText(g2, "GAME OVER", W/2 - 150, 230, NEON_PINK, 52);
        drawNeonText(g2, "SCORE: " + score, W/2 - 80, 310, NEON_GREEN, 26);
        if ((int)(bgPulse * 2) % 2 == 0)
            drawNeonText(g2, "PRESS ENTER TO RETRY", W/2 - 155, 390, NEON_YELLOW, 20);
        drawScanlines(g2);
    }

    void drawLevelClear(Graphics2D g2) {
        drawBackground(g2);
        drawStars(g2);
        drawParticles(g2);
        drawNeonText(g2, "LEVEL " + (level-1) + " CLEAR!", W/2 - 170, 230, NEON_GREEN, 48);
        drawNeonText(g2, "SCORE: " + score, W/2 - 80, 310, NEON_CYAN, 26);
        if ((int)(bgPulse * 2) % 2 == 0)
            drawNeonText(g2, "PRESS ENTER FOR LEVEL " + level, W/2 - 185, 390, NEON_PINK, 20);
        drawScanlines(g2);
    }

    void drawNeonText(Graphics2D g2, String text, int x, int y, Color c, int size) {
        g2.setFont(new Font("Courier New", Font.BOLD, size));
        setGlow(g2, c, 8);
        g2.setColor(c);
        g2.drawString(text, x, y);
        clearGlow(g2);
        g2.setColor(Color.WHITE);
        g2.drawString(text, x, y);
    }

    void setGlow(Graphics2D g2, Color c, int radius) {
        g2.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 80));
    }

    void clearGlow(Graphics2D g2) {
        g2.setComposite(AlphaComposite.SrcOver);
    }

    @Override public void keyPressed(KeyEvent e) {
        int k = e.getKeyCode();
        if (k == KeyEvent.VK_LEFT)  player.left  = true;
        if (k == KeyEvent.VK_RIGHT) player.right = true;
        if (k == KeyEvent.VK_SPACE && !gameOver && !gameWon && started) {
            if (player.shootCooldown <= 0) {
                bullets.add(new Bullet(player.x, player.y - 20, -9, NEON_GREEN, true));
                player.shootCooldown = 12;
            }
        }
        if (k == KeyEvent.VK_ENTER) {
            if (!started) { started = true; return; }
            if (gameOver) {
                score = 0; lives = 3; level = 1;
                player = new Player(W / 2, H - 80);
                bullets.clear(); particles.clear();
                spawnEnemies(); gameOver = false; return;
            }
            if (gameWon) {
                player = new Player(W / 2, H - 80);
                bullets.clear(); particles.clear();
                spawnEnemies(); gameWon = false;
            }
        }
    }
    @Override public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_LEFT)  player.left  = false;
        if (e.getKeyCode() == KeyEvent.VK_RIGHT) player.right = false;
    }
    @Override public void keyTyped(KeyEvent e) {}
}

class Player {
    float x, y, thrusterAnim;
    boolean left, right;
    int shootCooldown;
    Player(float x, float y) { this.x = x; this.y = y; }
}

class Enemy {
    float x, y, animTick;
    int dir = 1, points;
    Color color;
    Enemy(float x, float y, Color c, int pts) {
        this.x = x; this.y = y; this.color = c; this.points = pts;
    }
}

class Bullet {
    float x, y, speed;
    Color color;
    boolean friendly;
    List<float[]> trail = new ArrayList<>();
    Bullet(float x, float y, float speed, Color c, boolean friendly) {
        this.x = x; this.y = y; this.speed = speed;
        this.color = c; this.friendly = friendly;
    }
}

class Particle {
    float x, y, vx, vy, life, decay;
    Color color;
    Particle(float x, float y, float vx, float vy, Color c, float decay) {
        this.x = x; this.y = y; this.vx = vx; this.vy = vy;
        this.color = c; this.life = 1f; this.decay = decay;
    }
}

class Star {
    float x, y, speed;
    Star(float x, float y, float speed) { this.x = x; this.y = y; this.speed = speed; }
}

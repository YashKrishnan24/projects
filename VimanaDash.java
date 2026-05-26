import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

public class VimanaDash extends JPanel implements ActionListener, KeyListener {

    private static final int WIDTH = 600;
    private static final int HEIGHT = 800;
    private Timer timer;
    private boolean gameOver;
    private boolean gameStarted;
    private int score;
    private double gameSpeed;

    // Player (Vimana) properties
    private int playerX;
    private final int playerY = HEIGHT - 120;
    private final int PLAYER_SIZE = 40;
    private boolean leftPressed, rightPressed;
    private final int PLAYER_SPEED = 8;

    // Entities
    private ArrayList<Entity> obstacles;
    private ArrayList<Entity> collectibles;
    private Random random;

    public VimanaDash() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);

        random = new Random();
        obstacles = new ArrayList<>();
        collectibles = new ArrayList<>();
        
        initGame();

        // 60 FPS Game Loop
        timer = new Timer(1000 / 60, this);
        timer.start();
    }

    private void initGame() {
        playerX = WIDTH / 2 - PLAYER_SIZE / 2;
        score = 0;
        gameSpeed = 4.0;
        gameOver = false;
        gameStarted = false;
        leftPressed = false;
        rightPressed = false;
        obstacles.clear();
        collectibles.clear();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        
        // Enable Antialiasing for smooth graphics
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw Sunset Gradient Background
        GradientPaint skyGradient = new GradientPaint(0, 0, new Color(40, 20, 80), 0, HEIGHT, new Color(220, 100, 50));
        g2d.setPaint(skyGradient);
        g2d.fillRect(0, 0, WIDTH, HEIGHT);

        if (!gameStarted && !gameOver) {
            drawStartScreen(g2d);
        } else if (gameOver) {
            drawGameOverScreen(g2d);
        } else {
            drawGame(g2d);
        }
    }

    private void drawStartScreen(Graphics2D g2d) {
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("SansSerif", Font.BOLD, 50));
        String title = "VIMANA DASH";
        g2d.drawString(title, WIDTH / 2 - g2d.getFontMetrics().stringWidth(title) / 2, HEIGHT / 3);

        g2d.setFont(new Font("SansSerif", Font.PLAIN, 20));
        g2d.setColor(Color.YELLOW);
        String sub = "Pilot the Golden Chariot. Collect Amrit. Dodge Asura Weapons.";
        g2d.drawString(sub, WIDTH / 2 - g2d.getFontMetrics().stringWidth(sub) / 2, HEIGHT / 2);

        g2d.setColor(Color.WHITE);
        String prompt = "Press SPACE to Begin Your Journey";
        g2d.drawString(prompt, WIDTH / 2 - g2d.getFontMetrics().stringWidth(prompt) / 2, HEIGHT / 2 + 60);
    }

    private void drawGame(Graphics2D g2d) {
        // Draw Collectibles (Amrit - Cyan Orbs)
        for (Entity amrit : collectibles) {
            g2d.setColor(new Color(0, 255, 255, 180));
            g2d.fillOval((int)amrit.x, (int)amrit.y, amrit.size, amrit.size);
            // Glowing effect
            g2d.setColor(new Color(0, 255, 255, 80));
            g2d.fillOval((int)amrit.x - 5, (int)amrit.y - 5, amrit.size + 10, amrit.size + 10);
        }

        // Draw Obstacles (Asura Weapons - Red Rectangles/Triangles)
        for (Entity asura : obstacles) {
            g2d.setColor(new Color(220, 20, 60));
            int[] xPoints = {(int)asura.x, (int)asura.x + asura.size, (int)asura.x + asura.size / 2};
            int[] yPoints = {(int)asura.y, (int)asura.y, (int)asura.y + asura.size};
            g2d.fillPolygon(xPoints, yPoints, 3);
        }

        // Draw Player (Vimana - Golden Chariot)
        g2d.setColor(new Color(255, 215, 0)); // Gold
        g2d.fillRoundRect(playerX, playerY, PLAYER_SIZE, PLAYER_SIZE, 15, 15);
        g2d.setColor(Color.WHITE);
        g2d.drawRoundRect(playerX, playerY, PLAYER_SIZE, PLAYER_SIZE, 15, 15);

        // Draw Score (Punya)
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("SansSerif", Font.BOLD, 24));
        g2d.drawString("Punya (Score): " + score, 20, 40);
    }

    private void drawGameOverScreen(Graphics2D g2d) {
        g2d.setColor(new Color(0, 0, 0, 150));
        g2d.fillRect(0, 0, WIDTH, HEIGHT);

        g2d.setColor(Color.RED);
        g2d.setFont(new Font("SansSerif", Font.BOLD, 60));
        String title = "KALA HAS STRUCK";
        g2d.drawString(title, WIDTH / 2 - g2d.getFontMetrics().stringWidth(title) / 2, HEIGHT / 3);

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("SansSerif", Font.PLAIN, 30));
        String scoreText = "Total Punya: " + score;
        g2d.drawString(scoreText, WIDTH / 2 - g2d.getFontMetrics().stringWidth(scoreText) / 2, HEIGHT / 2);

        g2d.setFont(new Font("SansSerif", Font.PLAIN, 20));
        String prompt = "Press SPACE to Reincarnate (Restart)";
        g2d.drawString(prompt, WIDTH / 2 - g2d.getFontMetrics().stringWidth(prompt) / 2, HEIGHT / 2 + 80);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (gameStarted && !gameOver) {
            updatePlayer();
            spawnEntities();
            updateEntities();
            checkCollisions();
            
            // Gradually increase difficulty
            gameSpeed += 0.001; 
        }
        repaint();
    }

    private void updatePlayer() {
        if (leftPressed && playerX > 0) {
            playerX -= PLAYER_SPEED;
        }
        if (rightPressed && playerX < WIDTH - PLAYER_SIZE) {
            playerX += PLAYER_SPEED;
        }
    }

    private void spawnEntities() {
        // Spawn Obstacles
        if (random.nextInt(100) < 4 + (int)(gameSpeed / 2)) {
            obstacles.add(new Entity(random.nextInt(WIDTH - 30), -30, 30 + random.nextInt(20)));
        }
        // Spawn Amrit
        if (random.nextInt(100) < 3) {
            collectibles.add(new Entity(random.nextInt(WIDTH - 20), -20, 20));
        }
    }

    private void updateEntities() {
        Iterator<Entity> obsIter = obstacles.iterator();
        while (obsIter.hasNext()) {
            Entity obs = obsIter.next();
            obs.y += gameSpeed;
            if (obs.y > HEIGHT) obsIter.remove();
        }

        Iterator<Entity> colIter = collectibles.iterator();
        while (colIter.hasNext()) {
            Entity col = colIter.next();
            col.y += gameSpeed * 0.8; // Amrit falls slightly slower
            if (col.y > HEIGHT) colIter.remove();
        }
    }

    private void checkCollisions() {
        Rectangle playerRect = new Rectangle(playerX, playerY, PLAYER_SIZE, PLAYER_SIZE);

        // Check Obstacle collisions
        for (Entity obs : obstacles) {
            Rectangle obsRect = new Rectangle((int)obs.x, (int)obs.y, obs.size, obs.size);
            if (playerRect.intersects(obsRect)) {
                gameOver = true;
            }
        }

        // Check Collectible collisions
        Iterator<Entity> colIter = collectibles.iterator();
        while (colIter.hasNext()) {
            Entity col = colIter.next();
            Rectangle colRect = new Rectangle((int)col.x, (int)col.y, col.size, col.size);
            if (playerRect.intersects(colRect)) {
                score += 10; // Gain Punya
                colIter.remove();
            }
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_LEFT) {
            leftPressed = true;
        }
        if (key == KeyEvent.VK_RIGHT) {
            rightPressed = true;
        }
        if (key == KeyEvent.VK_SPACE) {
            if (!gameStarted) {
                gameStarted = true;
            } else if (gameOver) {
                initGame();
                gameStarted = true;
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_LEFT) {
            leftPressed = false;
        }
        if (key == KeyEvent.VK_RIGHT) {
            rightPressed = false;
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    // Simple inner class to hold entity data
    class Entity {
        double x, y;
        int size;
        Entity(double x, double y, int size) {
            this.x = x;
            this.y = y;
            this.size = size;
        }
    }

    public static void main(String[] args) {
        // Setup the JFrame
        JFrame frame = new JFrame("Vimana Dash");
        VimanaDash gamePanel = new VimanaDash();
        frame.add(gamePanel);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null); // Center on screen
        frame.setResizable(false);
        frame.setVisible(true);
    }
}

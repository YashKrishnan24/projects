import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;

public class SpaceShooter extends JPanel implements ActionListener, KeyListener {
    Timer timer;
    int playerX = 250;
    int playerY = 450;
    int score = 0;
    boolean left, right, shooting;

    ArrayList<Rectangle> bullets = new ArrayList<>();
    ArrayList<Rectangle> enemies = new ArrayList<>();

    Random rand = new Random();

    public SpaceShooter() {
        JFrame frame = new JFrame("🚀 Space Shooter Game");
        frame.setSize(600, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(this);
        frame.setVisible(true);
        frame.setResizable(false);

        timer = new Timer(20, this);
        timer.start();

        frame.addKeyListener(this);
    }

    public void spawnEnemy() {
        int x = rand.nextInt(550);
        enemies.add(new Rectangle(x, 0, 30, 30));
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        setBackground(Color.BLACK);

        g.setColor(Color.GREEN);
        g.fillRect(playerX, playerY, 40, 20);

        g.setColor(Color.YELLOW);
        for (Rectangle bullet : bullets) {
            g.fillRect(bullet.x, bullet.y, bullet.width, bullet.height);
        }

        g.setColor(Color.RED);
        for (Rectangle enemy : enemies) {
            g.fillRect(enemy.x, enemy.y, enemy.width, enemy.height);
        }

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 18));
        g.drawString("Score: " + score, 10, 20);
    }

    public void actionPerformed(ActionEvent e) {
        if (left && playerX > 0) playerX -= 5;
        if (right && playerX < 540) playerX += 5;

        if (shooting) {
            bullets.add(new Rectangle(playerX + 18, playerY, 5, 10));
            shooting = false;
        }

        for (int i = 0; i < bullets.size(); i++) {
            Rectangle b = bullets.get(i);
            b.y -= 10;
            if (b.y < 0) bullets.remove(i);
        }

        if (rand.nextInt(20) == 0) spawnEnemy();

        for (int i = 0; i < enemies.size(); i++) {
            Rectangle en = enemies.get(i);
            en.y += 4;

            if (en.y > 600) enemies.remove(i);
        }

        checkCollisions();

        repaint();
    }

    public void checkCollisions() {
        for (int i = 0; i < bullets.size(); i++) {
            Rectangle b = bullets.get(i);
            for (int j = 0; j < enemies.size(); j++) {
                Rectangle en = enemies.get(j);
                if (b.intersects(en)) {
                    bullets.remove(i);
                    enemies.remove(j);
                    score += 10;
                    return;
                }
            }
        }
    }

    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_LEFT) left = true;
        if (e.getKeyCode() == KeyEvent.VK_RIGHT) right = true;
        if (e.getKeyCode() == KeyEvent.VK_SPACE) shooting = true;
    }

    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_LEFT) left = false;
        if (e.getKeyCode() == KeyEvent.VK_RIGHT) right = false;
    }

    public void keyTyped(KeyEvent e) {}

    public static void main(String[] args) {
        new SpaceShooter();
    }
}

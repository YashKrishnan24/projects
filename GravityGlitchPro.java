import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;

public class GravityGlitchPro extends JPanel implements ActionListener, KeyListener {
    // Game Constants
    private final int WIDTH = 800;
    private final int HEIGHT = 600;
    private final int BASKET_WIDTH = 100; // Constant size now
    private final int BALL_SIZE = 25;
    private final int TIME_LIMIT = 20;

    // Game State
    private int targetGoal;
    private int score = 0;
    private int timeLeft = TIME_LIMIT;
    private int ballSpeed = 4;
    private boolean gameOver = false;
    private int basketX = WIDTH / 2 - (BASKET_WIDTH / 2);
    
    private Timer gameTimer;
    private Timer clockTimer;
    private ArrayList<Point> balls = new ArrayList<>();
    private Random rand = new Random();

    public GravityGlitchPro(int target) {
        this.targetGoal = target;
        
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(new Color(15, 15, 25)); // Deep space vibe
        setFocusable(true);
        addKeyListener(this);

        // Main Game Loop
        gameTimer = new Timer(16, this);
        gameTimer.start();

        // Countdown & Difficulty Scaler
        clockTimer = new Timer(1000, e -> {
            if (timeLeft > 0) {
                timeLeft--;
                // Make it harder every 5 seconds!
                if (timeLeft % 5 == 0) ballSpeed += 2; 
            } else {
                endGame();
            }
        });
        clockTimer.start();
    }

    private void endGame() {
        gameOver = true;
        gameTimer.stop();
        clockTimer.stop();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (!gameOver) {
            // HUD
            g2.setColor(Color.CYAN);
            g2.setFont(new Font("Monospaced", Font.BOLD, 22));
            g2.drawString("CATCH TARGET: " + targetGoal, 20, 40);
            g2.drawString("CURRENT SCORE: " + score, 20, 70);
            
            g2.setColor(timeLeft <= 5 ? Color.RED : Color.WHITE);
            g2.drawString("TIME REMAINING: " + timeLeft + "s", WIDTH - 280, 40);

            // The Basket (Neon Style)
            g2.setColor(Color.MAGENTA);
            g2.setStroke(new BasicStroke(3));
            g2.drawRoundRect(basketX, HEIGHT - 60, BASKET_WIDTH, 25, 15, 15);
            g2.setColor(new Color(255, 0, 255, 50));
            g2.fillRoundRect(basketX, HEIGHT - 60, BASKET_WIDTH, 25, 15, 15);

            // Falling Balls
            g2.setColor(Color.YELLOW);
            for (Point ball : balls) {
                g2.fillOval(ball.x, ball.y, BALL_SIZE, BALL_SIZE);
            }
        } else {
            drawResult(g2);
        }
    }

    private void drawResult(Graphics2D g2) {
        g2.setFont(new Font("Verdana", Font.BOLD, 45));
        if (score >= targetGoal) {
            g2.setColor(Color.GREEN);
            g2.drawString("CONGRATULATIONS!", WIDTH / 2 - 230, HEIGHT / 2 - 40);
            g2.setFont(new Font("Verdana", Font.ITALIC, 25));
            g2.drawString("You won this time, Space Cowboy.", WIDTH / 2 - 200, HEIGHT / 2 + 10);
        } else {
            g2.setColor(Color.RED);
            g2.drawString("HARD LUCK!", WIDTH / 2 - 140, HEIGHT / 2 - 40);
            g2.setFont(new Font("Verdana", Font.PLAIN, 25));
            g2.drawString("Try again... if you dare.", WIDTH / 2 - 140, HEIGHT / 2 + 10);
        }
        
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 20));
        g2.drawString("Final Score: " + score + " / Target: " + targetGoal, WIDTH / 2 - 130, HEIGHT / 2 + 70);
        g2.drawString("Press ESC to quit", WIDTH / 2 - 80, HEIGHT / 2 + 120);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // Spawn rate increases as speed increases
        if (rand.nextInt(100) < (5 + ballSpeed)) {
            balls.add(new Point(rand.nextInt(WIDTH - BALL_SIZE), -20));
        }

        for (int i = 0; i < balls.size(); i++) {
            Point p = balls.get(i);
            p.y += ballSpeed;

            // Collision check
            if (p.y + BALL_SIZE >= HEIGHT - 60 && p.x + BALL_SIZE > basketX && p.x < basketX + BASKET_WIDTH) {
                score++;
                balls.remove(i);
                i--;
            } else if (p.y > HEIGHT) {
                balls.remove(i);
                i--;
            }
        }
        repaint();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int moveAmount = 35;
        if (e.getKeyCode() == KeyEvent.VK_LEFT && basketX > 0) basketX -= moveAmount;
        if (e.getKeyCode() == KeyEvent.VK_RIGHT && basketX < WIDTH - BASKET_WIDTH) basketX += moveAmount;
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) System.exit(0);
    }

    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e) {}

    public static void main(String[] args) {
        String input = JOptionPane.showInputDialog(null, 
            "The Challenge: 20 Seconds. Fixed Basket.\nHow many balls can you catch?");
        
        try {
            int target = Integer.parseInt(input);
            if (target < 1) throw new Exception();

            JFrame frame = new JFrame("Gravity Glitch PRO");
            frame.add(new GravityGlitchPro(target));
            frame.pack();
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Invalid input. The universe doesn't recognize your math.");
        }
    }
}

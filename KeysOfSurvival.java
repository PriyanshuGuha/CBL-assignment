import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

/**
 * .
 */
public class KeysOfSurvival extends JPanel implements ActionListener, KeyListener {
    JPanel panel = this; // Reference to the game panel
    Random random = new Random(); // Used for randomly generating obstacles.

    static final int FRAME_WIDTH = 414;
    static final int FRAME_HEIGHT = 738;

    static final int NUMBER_OF_LANES = 3;

    static final int PLAYER_WIDTH = Math.min(FRAME_WIDTH / NUMBER_OF_LANES, 80);
    static final int PLAYER_HEIGHT = Math.min(FRAME_WIDTH / NUMBER_OF_LANES, 80);
    // Adjust 80 is too big for the player to fit in a lane.

    static final int PLAYER_Y = 600;

    static final int MILLISECONDS_PER_FRAME = 20;
    // This is the length of each interval processed by the game in milliseconds.

    static final int SPEED = 10; 

    // This is the number of different doors.
    static final int NUMBER_OF_COLORS = 4;

    // These are the background colors.
    static final Color COLOR_1 = new Color(156, 156, 156); 
    static final Color COLOR_2 = new Color(221, 221, 221); 

    static int currentLane = NUMBER_OF_LANES / 2;

    int doorSpawnCooldown = 60; // Number of intervals until next door spawns
    int keySpawnCooldown = 30;
    int zombieSpawnCooldown = 45 + 30 * random.nextInt(8);

    static final int SPAWN_FREQUENCY = 1;
    
    Image playerImage;
    Image peopleIcon;
    Image heartIcon;
    Image zombieImage;
    Image[] doorImages = new Image[NUMBER_OF_COLORS];
    Image[] keyImages = new Image[NUMBER_OF_COLORS];
    Image[] keyIcons = new Image[NUMBER_OF_COLORS];

    String[] colorNames = {"Red", "Green", "Blue", "Yellow"}; // Names of colors used
    Color[] colors = {Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW}; // Colors used

    javax.swing.Timer timer;

    ArrayList<Obstacle> obstacles = new ArrayList<Obstacle>();
    Obstacle removed = null;
    int[] currentKeys = new int[NUMBER_OF_COLORS];

    int score = 0;
    int health = 0;

    KeysOfSurvival() {
        setPreferredSize(new Dimension(FRAME_WIDTH, FRAME_HEIGHT));

        JFrame frame = new JFrame("Rescue Runner");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(this); // Connects the JPanel and JFrame
        frame.setResizable(false);
        frame.pack(); // Sets the size of the frame
        frame.setVisible(true);

        frame.addKeyListener(this);

        loadImages();
        timer = new javax.swing.Timer(MILLISECONDS_PER_FRAME, this);
        timer.start();
    }

    void loadImages() {
        playerImage = new ImageIcon("Player.png").getImage();
        peopleIcon = new ImageIcon("PeopleIcon.png").getImage();
        heartIcon = new ImageIcon("HeartIcon.png").getImage();
        zombieImage = new ImageIcon("Zombie.png").getImage();

        for (int i = 0; i < NUMBER_OF_COLORS; i++) {
            doorImages[i] = new ImageIcon(colorNames[i] + "Door.png").getImage();
            keyImages[i] = new ImageIcon(colorNames[i] + "key.png").getImage();
            keyIcons[i] = new ImageIcon(colorNames[i] + "keyIcon.png").getImage();
        }
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g); // Initial painting
        
        g.setColor(COLOR_1);
        g.fillRect(0, 0, FRAME_WIDTH, FRAME_HEIGHT);
        
        g.setColor(COLOR_2); // Color alternating lanes white
        for (int i = 1; i < NUMBER_OF_LANES; i += 2) {
            g.fillRect(i * FRAME_WIDTH / NUMBER_OF_LANES,
                0,
                FRAME_WIDTH / NUMBER_OF_LANES,
                FRAME_HEIGHT);
        }

        g.setColor(Color.GRAY); // Draw lane borders
        for (int i = 1; i < NUMBER_OF_LANES; i++) {
            g.drawLine(i * FRAME_WIDTH / NUMBER_OF_LANES,
                0,
                i * FRAME_WIDTH / NUMBER_OF_LANES,
                FRAME_HEIGHT);
        }

        // Draw obstacles
        for (Obstacle obstacle : obstacles) {
            Image image = zombieImage;
            if (obstacle instanceof Door) {
                image = doorImages[obstacle.color];
            } else if (obstacle instanceof Key) {
                image = keyImages[obstacle.color];
            }
            g.drawImage(image,
                ((FRAME_WIDTH / NUMBER_OF_LANES * (2 * obstacle.lane + 1) - PLAYER_WIDTH)) / 2,
                obstacle.y,
                PLAYER_WIDTH,
                PLAYER_HEIGHT, // For now, obstacles have identical dimensions to player.
                this);
        }

        // Draw player
        g.drawImage(playerImage,
            ((FRAME_WIDTH / NUMBER_OF_LANES * (2 * currentLane + 1) - PLAYER_WIDTH)) / 2,
            PLAYER_Y,
            PLAYER_WIDTH,
            PLAYER_HEIGHT,
            this);
        
        // Write score
        g.setColor(Color.BLACK);
        g.drawImage(peopleIcon, 20, 12, 5, 10, this);
        g.drawString("People saved: " + score, 28, 20);

        // Show health
        if (health > 8) { // If there are too many hearts, show them differently
            g.drawImage(heartIcon, 20, 30, 10, 10, this);
            g.drawString("× " + health, 33, 39);
        } else {
            for (int i = 0; i < health; i++) {
                g.drawImage(heartIcon, 20 + 15 * i, 30, 10, 10, this);
            }
        }

        // Show keys
        for (int i = 0; i < NUMBER_OF_COLORS; i++) {
            if (currentKeys[i] > 0) {
                g.drawImage(keyIcons[i], FRAME_WIDTH - 135 + 30 * i, 10, 20, 10, this);
                if (currentKeys[i] > 1) {
                    g.setColor(colors[i]);
                    g.drawString("" + currentKeys[i], FRAME_WIDTH - 135 + 30 * i, 30);
                }
            }
        }
    }

    void spawnDoor() {
        obstacles.add(new Door(random.nextInt(NUMBER_OF_LANES), -PLAYER_HEIGHT, SPEED));
    }

    void spawnKey() {
        obstacles.add(new Key(random.nextInt(NUMBER_OF_LANES), -PLAYER_HEIGHT, SPEED));
    }

    void spawnZombie() {
        obstacles.add(new Zombie(random.nextInt(NUMBER_OF_LANES), -PLAYER_HEIGHT, SPEED));
    }

    public void actionPerformed(ActionEvent e) {
        doorSpawnCooldown -= SPAWN_FREQUENCY;
        if (doorSpawnCooldown < 1) {
            doorSpawnCooldown = 60;
            spawnDoor();
        }
        
        keySpawnCooldown -= SPAWN_FREQUENCY;
        if (keySpawnCooldown < 1) {
            keySpawnCooldown = 60;
            spawnKey();
        }

        zombieSpawnCooldown -= SPAWN_FREQUENCY;
        if (zombieSpawnCooldown < 1) {
            zombieSpawnCooldown = 30 + 30 * random.nextInt(8);
            spawnZombie();
        }

        for (Obstacle obstacle : obstacles) {
            obstacle.move();
        }
        if (removed != null) {
            obstacles.remove(removed);
        }
        
        repaint();
    }

    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_LEFT && currentLane != 0) {
            currentLane--;
        }
        if (e.getKeyCode() == KeyEvent.VK_RIGHT && currentLane != NUMBER_OF_LANES - 1) {
            currentLane++;
        }
    }

    public void keyReleased(KeyEvent e) {
        // Nothing to do
    }

    public void keyTyped(KeyEvent e) {
        // Nothing to do
    }

    class Obstacle {
        int lane; // The lane in which this obstacle spawns, indexed from 0 from left to right
        int y; // The distance of this obstacle from the top edge of the frame
        int speed;
        int color = 0;
        boolean passed = false; // Whether the player passed this obstacle

        /**
         * Obstacles are any objects other than the player.
         * 
         * @param lane is the lane in which the object must spawn.
         * @param y is its y-position, or how far it is from the top of the frame.
         * @param speed is its speed.
         */
        Obstacle(int lane, int y, int speed) {
            this.lane = lane;
            this.y = y;
            this.speed = speed;
        }

        void move() {
            y += speed;
            if (y > FRAME_HEIGHT) {
                removed = this;
            }
        }
    }

    class Door extends Obstacle {
        Door(int lane, int y, int speed) {
            super(lane, y, speed);
            color = random.nextInt(NUMBER_OF_COLORS);
        }

        void move() {
            super.move();
            if (y > PLAYER_Y && !passed) {
                if (currentLane == lane) {
                    if (currentKeys[color] == 0) {
                        gameOver();
                    } else {
                        currentKeys[color]--;
                        score++;
                        health++;
                    }
                }
                passed = true;
            }
        }
    }

    class Key extends Obstacle {
        Key(int lane, int y, int speed) {
            super(lane, y, speed);
            color = random.nextInt(NUMBER_OF_COLORS);
        }

        void move() {
            super.move();
            if (y > PLAYER_Y && !passed) {
                if (currentLane == lane) {
                    currentKeys[color]++;
                    removed = this;
                }
                passed = true;
            }
        }
    }

    class Zombie extends Obstacle {
        Zombie(int lane, int y, int speed) {
            super(lane, y, speed);
        }

        void move() {
            super.move();
            if (y > PLAYER_Y && !passed) {
                if (currentLane == lane) {
                    gameOver();
                }
                passed = true;
            }
        }
    }

    void gameOver() {
        if (health >= 3) {
            int option = JOptionPane.showConfirmDialog(this,
                "You hit an obstacle.\nDo you want to use three hearts to continue?",
                "Continue?",
                JOptionPane.YES_NO_OPTION);
            if (option == JOptionPane.YES_OPTION) {
                health -= 3;
                return;
            }
        }
        timer.stop();
        JOptionPane.showMessageDialog(panel, "Game Over!\nYour Score: " + score);
        System.exit(0);
    }

    public static void main(String[] args) {
        new KeysOfSurvival();
    }
}
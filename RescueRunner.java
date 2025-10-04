import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

/**
 * .
 */
public class RescueRunner extends JPanel implements ActionListener {
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

    int doorSpawnCooldown = 60; // Number of intervals until next door spawns
    static final int DOOR_SPEED = 11; 

    static int currentLane = NUMBER_OF_LANES / 2;
    
    Image roboImage;
    Image barrelImage;
    Image droneImage;

    javax.swing.Timer timer;

    ArrayList<Obstacle> obstacles = new ArrayList<Obstacle>();

    RescueRunner() {
        JFrame frame = new JFrame("Rescue Runner");
        frame.setSize(FRAME_WIDTH, FRAME_HEIGHT);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(this);
        frame.setResizable(false);
        frame.setVisible(true);

        loadImages();
        timer = new javax.swing.Timer(MILLISECONDS_PER_FRAME, this);
        timer.start();


    }

    void loadImages() {
        roboImage = new ImageIcon("robo.png").getImage();
        barrelImage = new ImageIcon("barrel.png").getImage();
        droneImage = new ImageIcon("drone.png").getImage();
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g); // Initial painting
        
        g.setColor(Color.WHITE); // Color alternating lanes white
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
            g.drawImage(barrelImage,
                ((FRAME_WIDTH / NUMBER_OF_LANES * (2 * obstacle.lane + 1) - PLAYER_WIDTH)) / 2,
                obstacle.y,
                PLAYER_WIDTH,
                PLAYER_HEIGHT, // For now, obstacles have identical dimensions to player.
                this);
        }

        // Draw player
        g.drawImage(roboImage,
            (FRAME_WIDTH - PLAYER_WIDTH) / 2,
            PLAYER_Y,
            PLAYER_WIDTH,
            PLAYER_HEIGHT,
            this);
        
        // Write score
        g.setColor(Color.BLACK);
        g.drawString("Score: 0", FRAME_WIDTH - 100, 20);
    }

    void spawnDoor() {
        obstacles.add(new Door(random.nextInt(NUMBER_OF_LANES), -PLAYER_HEIGHT, DOOR_SPEED));
    }

    public void actionPerformed(ActionEvent e) {
        doorSpawnCooldown--;
        if (doorSpawnCooldown < 0) {
            doorSpawnCooldown = 60;
            spawnDoor();

        }

        for (Obstacle obstacle : obstacles) {
            obstacle.move();
        }
        
        repaint();
    }

    class Obstacle {
        int lane; // The lane in which this obstacle spawns, indexed from 0 from left to right
        int y; // The distance of this obstacle from the top edge of the framel
        int speed;

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
        }
    }

    class Door extends Obstacle {
        Door(int lane, int y, int speed) {
            super(lane, y, speed);
        }

        void move() {
            y += speed;
            if (y > PLAYER_Y + PLAYER_HEIGHT / 2 && currentLane == lane) {
                timer.stop();
                JOptionPane.showMessageDialog(panel, "Game Over!\nYour Score: 0");
                System.exit(0);
            }
        }
    }

    public static void main(String[] args) {
        new RescueRunner();
    }
}
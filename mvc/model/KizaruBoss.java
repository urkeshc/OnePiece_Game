package mvc.model;

import mvc.controller.CommandCenter;
import mvc.controller.Game;
import mvc.controller.GameOp;
import mvc.controller.ImageLoader;

import java.awt.*;
import java.awt.image.BufferedImage;

public class KizaruBoss extends FoeBoss {

    private static final int KIZARU_BASE_HEALTH = 1250;  // Add base health constant

    private int beamCooldown;
    private double movementSpeed;
    private int kizaruHealth;
    private static final int OPTIMAL_DISTANCE = 250;
    private static final int DISTANCE_TOLERANCE = 40;
    private int framesSinceLastBeam = 0;
    private BufferedImage kizaruImage;

    // Add these new constants
    private static final int MIN_BEAM_COOLDOWN_EASY = 80;
    private static final int MAX_BEAM_COOLDOWN_EASY = 100;
    private static final int MIN_BEAM_COOLDOWN_MEDIUM = 50;
    private static final int MAX_BEAM_COOLDOWN_MEDIUM = 70;
    private static final int MIN_BEAM_COOLDOWN_HARD = 35;
    private static final int MAX_BEAM_COOLDOWN_HARD = 55;

    public KizaruBoss() {
        super(KIZARU_BASE_HEALTH);  // Call parent constructor with base health

        // Load Kizaru's image
        kizaruImage = ImageLoader.getImage("foes/kizaru.png");
        if (kizaruImage == null) {
            System.err.println("Could not load Kizaru image");
        }

        // Set initial position at top center of screen
        setCenter(new Point(Game.DIM.width / 2, 100));
        
        // Adjust parameters based on difficulty
        CommandCenter.Difficulty difficulty = CommandCenter.getInstance().getDifficulty();
        switch (difficulty) {
            case EASY:
                kizaruHealth = 1000;
                beamCooldown = Game.R.nextInt(MAX_BEAM_COOLDOWN_EASY - MIN_BEAM_COOLDOWN_EASY) + MIN_BEAM_COOLDOWN_EASY;
                movementSpeed = 3.5;   // Slower movement
                break;
            case HARD:
                kizaruHealth = 1500;
                beamCooldown = Game.R.nextInt(MAX_BEAM_COOLDOWN_HARD - MIN_BEAM_COOLDOWN_HARD) + MIN_BEAM_COOLDOWN_HARD;
                movementSpeed = 5.5;    // Faster movement
                break;
            default: // MEDIUM
                kizaruHealth = 1250;
                beamCooldown = Game.R.nextInt(MAX_BEAM_COOLDOWN_MEDIUM - MIN_BEAM_COOLDOWN_MEDIUM) + MIN_BEAM_COOLDOWN_MEDIUM;
                movementSpeed = 4.5;
                break;
        }

        // Initialize with adjusted health
        this.maxHealth = kizaruHealth;    // Update maxHealth directly
        this.currentHealth = kizaruHealth; // Update currentHealth directly
        
        // Update health after difficulty adjustment
        this.maxHealth = kizaruHealth;  // Update maxHealth from parent
        this.currentHealth = kizaruHealth;  // Update currentHealth from parent

        // Initialize movement
        setDeltaX(0);
        setDeltaY(0);
    }

    @Override
    public void draw(Graphics g) {
        if (kizaruImage != null) {
            Graphics2D g2d = (Graphics2D) g;
            int x = getCenter().x - getRadius();
            int y = getCenter().y - getRadius();

            // Draw Kizaru
            g2d.drawImage(kizaruImage, x, y, getRadius() * 2, getRadius() * 2, null);
            
            // Draw health bar above Kizaru with some offset
            drawHealthBar(g, x, y - 30);
        }
    }

    private void drawHealthBar(Graphics g, int x, int y) {
        int width = getRadius() * 2;  // Match width with Kizaru's width
        int height = 10;  // Height of health bar
        
        // Draw background (empty health)
        g.setColor(Color.RED);
        g.fillRect(x, y, width, height);
        
        // Draw current health
        g.setColor(Color.GREEN);
        int healthWidth = (int)((getCurrentHealth() / (double)kizaruHealth) * width);
        g.fillRect(x, y, healthWidth, height);
        
        // Draw border
        g.setColor(Color.WHITE);
        g.drawRect(x, y, width, height);
        
        // Draw health text
        g.setColor(Color.WHITE);
        String healthText = getCurrentHealth() + "/" + kizaruHealth;
        g.drawString(healthText, x + width/2 - 20, y - 5);
    }

    @Override
    public void move() {
        super.move();
        
        // Get falcon's position
        Point sunnyPosition = CommandCenter.getInstance().getThousandSunny().getCenter();
        Point kizaruPos = getCenter();
        
        // Calculate distance and direction to falcon
        double dx = sunnyPosition.x - kizaruPos.x;
        double dy = sunnyPosition.y - kizaruPos.y;
        double distance = Math.sqrt(dx * dx + dy * dy);
        
        // Only move if we're not at the optimal distance
        if (Math.abs(distance - OPTIMAL_DISTANCE) > DISTANCE_TOLERANCE) {
            dx /= distance;
            dy /= distance;
            double multiplier = (distance < OPTIMAL_DISTANCE) ? -1.0 : 1.0;
            
            // Use dynamic movementSpeed
            setDeltaX(dx * movementSpeed * multiplier);
            setDeltaY(dy * movementSpeed * multiplier);
        } else {
            setDeltaX(getDeltaX() * 0.90);
            setDeltaY(getDeltaY() * 0.90);
        }
        
        // Keep within screen bounds
        Point newCenter = getCenter();
        if (newCenter.x < 0 || newCenter.x > Game.DIM.width) {
            setDeltaX(-getDeltaX());
        }
        if (newCenter.y < 0 || newCenter.y > Game.DIM.height) {
            setDeltaY(-getDeltaY());
        }

        // Handle beam attacks using dynamic cooldown
        framesSinceLastBeam++;
        if (framesSinceLastBeam >= beamCooldown) {
            fireBeams();
            framesSinceLastBeam = 0;
            // Reset to a new random cooldown
            CommandCenter.Difficulty difficulty = CommandCenter.getInstance().getDifficulty();
            switch (difficulty) {
                case EASY:
                    beamCooldown = Game.R.nextInt(MAX_BEAM_COOLDOWN_EASY - MIN_BEAM_COOLDOWN_EASY) + MIN_BEAM_COOLDOWN_EASY;
                    break;
                case HARD:
                    beamCooldown = Game.R.nextInt(MAX_BEAM_COOLDOWN_HARD - MIN_BEAM_COOLDOWN_HARD) + MIN_BEAM_COOLDOWN_HARD;
                    break;
                default:
                    beamCooldown = Game.R.nextInt(MAX_BEAM_COOLDOWN_MEDIUM - MIN_BEAM_COOLDOWN_MEDIUM) + MIN_BEAM_COOLDOWN_MEDIUM;
                    break;
            }
        }
    }

    private void fireBeams() {
        // Adjust number of beams based on difficulty
        int numBeams;
        double spreadAngle;
        CommandCenter.Difficulty difficulty = CommandCenter.getInstance().getDifficulty();
        
        switch (difficulty) {
            case EASY:
                numBeams = 2;      // Fewer beams
                spreadAngle = 40;  // Wider spread (easier to dodge)
                break;
            case HARD:
                numBeams = 4;      // More beams
                spreadAngle = 20;  // Tighter spread (harder to dodge)
                break;
            default:
                numBeams = 3;      // Medium number of beams
                spreadAngle = 30;  // Medium spread
                break;
        }

        // Calculate angles for beam spread
        double angleStep = spreadAngle / (numBeams - 1);
        double startAngle = -(spreadAngle / 2);
        
        // Fix angle conversion
        for (int i = 0; i < numBeams; i++) {
            double angle = startAngle + (angleStep * i);
            KizaruBeam beam = new KizaruBeam(this, (int)angle);  // Cast to int
            CommandCenter.getInstance().getOpsQueue().enqueue(beam, GameOp.Action.ADD);
        }
    }

    @Override
    public void damage(int amount) {
        int oldHealth = getCurrentHealth();
        super.damage(amount);

        if (getCurrentHealth() <= 0) {
            System.out.println("KIZARU HAS BEEN DEFEATED!");
        }
    }
}

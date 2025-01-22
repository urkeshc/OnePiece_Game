package mvc.model;

import mvc.controller.ImageLoader;
import mvc.controller.CommandCenter;
import mvc.controller.Game;  // Add this import
import mvc.controller.GameOp;
import mvc.controller.SoundLoader;
import java.awt.*;
import java.awt.image.BufferedImage;

public class EnelBoss extends FoeBoss {
    
    // Add missing constant
    private static final int ENEL_HEALTH = 2500;  // Default health value
    
    // Add these new constants
    private static final int MIN_THUNDER_COOLDOWN_EASY = 130;
    private static final int MAX_THUNDER_COOLDOWN_EASY = 170;
    private static final int MIN_THUNDER_COOLDOWN_MEDIUM = 100;
    private static final int MAX_THUNDER_COOLDOWN_MEDIUM = 140;
    private static final int MIN_THUNDER_COOLDOWN_HARD = 70;
    private static final int MAX_THUNDER_COOLDOWN_HARD = 110;
    
    private static final int MIN_BIG_THUNDER_COOLDOWN_EASY = 350;
    private static final int MAX_BIG_THUNDER_COOLDOWN_EASY = 450;
    private static final int MIN_BIG_THUNDER_COOLDOWN_MEDIUM = 250;
    private static final int MAX_BIG_THUNDER_COOLDOWN_MEDIUM = 350;
    private static final int MIN_BIG_THUNDER_COOLDOWN_HARD = 200;
    private static final int MAX_BIG_THUNDER_COOLDOWN_HARD = 300;
    
    private int thunderCooldown;
    private int bigThunderCooldown;
    private double movementSpeed;
    private int enelHealth;
    
    private static final int WARNING_FRAMES = 30;
    private int framesSinceLastThunder = 0;
    private int framesSinceBigThunder = 0;
    private boolean isChargingBigThunder = false;
    private int bigThunderWarningCounter = 0;
    private BufferedImage enelImage;
    private static final int OPTIMAL_DISTANCE = 300;   // Distance Enel tries to maintain from player
    private static final int DISTANCE_TOLERANCE = 50;  // How close to optimal distance before stopping

    public EnelBoss() {
        super(ENEL_HEALTH);  // Call parent constructor with base health
        
        // Load Enel's image
        enelImage = ImageLoader.getImage("foes/enel.png");
        if (enelImage == null) {
            System.err.println("Could not load Enel image");
        }

        // Slower than regular enemies but still moves
        setDeltaX(0);
        setDeltaY(0);
        
        // Adjust parameters based on difficulty
        CommandCenter.Difficulty difficulty = CommandCenter.getInstance().getDifficulty();
        switch (difficulty) {
            case EASY:
                enelHealth = 2000;
                thunderCooldown = Game.R.nextInt(MAX_THUNDER_COOLDOWN_EASY - MIN_THUNDER_COOLDOWN_EASY) + MIN_THUNDER_COOLDOWN_EASY;
                bigThunderCooldown = Game.R.nextInt(MAX_BIG_THUNDER_COOLDOWN_EASY - MIN_BIG_THUNDER_COOLDOWN_EASY) + MIN_BIG_THUNDER_COOLDOWN_EASY;
                movementSpeed = 2.5;
                break;
            case HARD:
                enelHealth = 3000;
                thunderCooldown = Game.R.nextInt(MAX_THUNDER_COOLDOWN_HARD - MIN_THUNDER_COOLDOWN_HARD) + MIN_THUNDER_COOLDOWN_HARD;
                bigThunderCooldown = Game.R.nextInt(MAX_BIG_THUNDER_COOLDOWN_HARD - MIN_BIG_THUNDER_COOLDOWN_HARD) + MIN_BIG_THUNDER_COOLDOWN_HARD;
                movementSpeed = 4.0;
                break;
            default:
                enelHealth = 2500;
                thunderCooldown = Game.R.nextInt(MAX_THUNDER_COOLDOWN_MEDIUM - MIN_THUNDER_COOLDOWN_MEDIUM) + MIN_THUNDER_COOLDOWN_MEDIUM;
                bigThunderCooldown = Game.R.nextInt(MAX_BIG_THUNDER_COOLDOWN_MEDIUM - MIN_BIG_THUNDER_COOLDOWN_MEDIUM) + MIN_BIG_THUNDER_COOLDOWN_MEDIUM;
                movementSpeed = 3.25;
                break;
        }
        this.maxHealth = enelHealth;  // Update maxHealth from parent
        this.currentHealth = enelHealth;  // Update currentHealth from parent
    }

    @Override
    public void draw(Graphics g) {
        if (enelImage != null) {
            Graphics2D g2d = (Graphics2D) g;
            int x = getCenter().x - getRadius();
            int y = getCenter().y - getRadius() + 90;
            
            // Draw health bar above Enel
            drawHealthBar(g, x, y - 20);
            
            // Draw Enel
            g2d.drawImage(enelImage, x, y, getRadius() * 2, getRadius() * 2, null);
        }
    }

    private void drawHealthBar(Graphics g, int x, int y) {
        int width = getRadius() * 2;  // Match width with Enel's width
        int height = 10;  // Height of health bar
        
        // Draw background (empty health)
        g.setColor(Color.RED);
        g.fillRect(x, y, width, height);
        
        // Draw current health
        g.setColor(Color.GREEN);
        int healthWidth = (int)((getCurrentHealth() / (double)ENEL_HEALTH) * width);
        g.fillRect(x, y, healthWidth, height);
        
        // Draw border
        g.setColor(Color.WHITE);
        g.drawRect(x, y, width, height);
        
        // Optionally, draw health text
        g.setColor(Color.WHITE);
        String healthText = getCurrentHealth() + "/" + ENEL_HEALTH;
        g.drawString(healthText, x + width/2 - 20, y - 5);
    }

    @Override
    public void damage(int amount) {
        super.damage(amount);
    }

    @Override
    public void move() {
        super.move();
        
        // Get falcon's position
        Point sunnyPosition = CommandCenter.getInstance().getThousandSunny().getCenter();
        Point enelPos = getCenter();
        
        // Calculate distance and direction to falcon
        double dx = sunnyPosition.x - enelPos.x;
        double dy = sunnyPosition.y - enelPos.y;
        double distance = Math.sqrt(dx * dx + dy * dy);
        
        // Only move if we're not at the optimal distance
        if (Math.abs(distance - OPTIMAL_DISTANCE) > DISTANCE_TOLERANCE) {
            // Normalize direction vector
            dx /= distance;
            dy /= distance;
            
            // If we're too close, move away; if too far, move closer
            double multiplier = (distance < OPTIMAL_DISTANCE) ? -1.0 : 1.0;
            
            // Set new velocity
            setDeltaX(dx * movementSpeed * multiplier);
            setDeltaY(dy * movementSpeed * multiplier);
        } else {
            // At optimal distance, slow down
            setDeltaX(getDeltaX() * 0.95);
            setDeltaY(getDeltaY() * 0.95);
        }
        
        // Keep within screen bounds
        Point newCenter = getCenter();
        if (newCenter.x < 0 || newCenter.x > Game.DIM.width) {
            setDeltaX(-getDeltaX());
        }
        if (newCenter.y < 0 || newCenter.y > Game.DIM.height) {
            setDeltaY(-getDeltaY());
        }
        
        // Handle thunder attacks
        framesSinceLastThunder++;
        framesSinceBigThunder++;
        
        // Only attack if at a good distance
        if (Math.abs(distance - OPTIMAL_DISTANCE) < DISTANCE_TOLERANCE * 2) {
            if (framesSinceLastThunder >= thunderCooldown) {
                fireThunderSpread();
                framesSinceLastThunder = 0;
                // Reset to a new random cooldown for regular thunder
                CommandCenter.Difficulty difficulty = CommandCenter.getInstance().getDifficulty();
                switch (difficulty) {
                    case EASY:
                        thunderCooldown = Game.R.nextInt(MAX_THUNDER_COOLDOWN_EASY - MIN_THUNDER_COOLDOWN_EASY) + MIN_THUNDER_COOLDOWN_EASY;
                        break;
                    case HARD:
                        thunderCooldown = Game.R.nextInt(MAX_THUNDER_COOLDOWN_HARD - MIN_THUNDER_COOLDOWN_HARD) + MIN_THUNDER_COOLDOWN_HARD;
                        break;
                    default:
                        thunderCooldown = Game.R.nextInt(MAX_THUNDER_COOLDOWN_MEDIUM - MIN_THUNDER_COOLDOWN_MEDIUM) + MIN_THUNDER_COOLDOWN_MEDIUM;
                        break;
                }
            }
            
            if (framesSinceBigThunder >= bigThunderCooldown) {
                if (!isChargingBigThunder) {
                    isChargingBigThunder = true;
                    bigThunderWarningCounter = WARNING_FRAMES;
                    SoundLoader.playSound("thunder.wav");
                } else if (bigThunderWarningCounter > 0) {
                    bigThunderWarningCounter--;
                } else {
                    fireBigThunder();
                    framesSinceBigThunder = 0;
                    isChargingBigThunder = false;
                    // Reset to a new random cooldown for big thunder
                    CommandCenter.Difficulty difficulty = CommandCenter.getInstance().getDifficulty();
                    switch (difficulty) {
                        case EASY:
                            bigThunderCooldown = Game.R.nextInt(MAX_BIG_THUNDER_COOLDOWN_EASY - MIN_BIG_THUNDER_COOLDOWN_EASY) + MIN_BIG_THUNDER_COOLDOWN_EASY;
                            break;
                        case HARD:
                            bigThunderCooldown = Game.R.nextInt(MAX_BIG_THUNDER_COOLDOWN_HARD - MIN_BIG_THUNDER_COOLDOWN_HARD) + MIN_BIG_THUNDER_COOLDOWN_HARD;
                            break;
                        default:
                            bigThunderCooldown = Game.R.nextInt(MAX_BIG_THUNDER_COOLDOWN_MEDIUM - MIN_BIG_THUNDER_COOLDOWN_MEDIUM) + MIN_BIG_THUNDER_COOLDOWN_MEDIUM;
                            break;
                    }
                }
            }
        }
    }
    
    private void fireThunderSpread() {
        // Fire three thunders with spread
        double[] angles = {-20, 0, 20};  // Spread angles
        for (double angleOffset : angles) {
            CommandCenter.getInstance().getOpsQueue().enqueue(
                new EnelThunder(this, angleOffset), 
                GameOp.Action.ADD
            );
        }
        // Play thunder sound
        // SoundLoader.playSound("thunder.wav");
    }
    
    private void fireBigThunder() {
        CommandCenter.getInstance().getOpsQueue().enqueue(
            new EnelBigThunder(this), 
            GameOp.Action.ADD
        );
    }
}

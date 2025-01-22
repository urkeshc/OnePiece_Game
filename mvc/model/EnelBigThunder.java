package mvc.model;

import mvc.controller.CommandCenter;
import mvc.controller.ImageLoader;
import java.awt.*;
import java.awt.image.BufferedImage;

public class EnelBigThunder extends Sprite {
    private static final int THUNDER_SPEED = 15;  // Increased from 12
    private static final int THUNDER_DAMAGE = 125; // Much more damage
    private static final double WAVE_AMPLITUDE = 5.0; // How far it deviates from straight path
    private static final double WAVE_FREQUENCY = 0.25; // How fast it oscillates
    private double time = 0; // Track time for wave motion
    private double baseVectorX, baseVectorY; // Store original trajectory
    private static BufferedImage thunderImage;
    private boolean hasWarned = false;
    
    static {
        thunderImage = ImageLoader.getImage("foes/enel_big_thunder.png");
    }
    
    public EnelBigThunder(EnelBoss enel) {
        setTeam(Team.FOE);
        setExpiry(60);
        setRadius(25);
        setCenter((Point) enel.getCenter().clone());
        
        // Calculate direction towards falcon
        Point falconPos = CommandCenter.getInstance().getThousandSunny().getCenter();
        double dx = falconPos.x - getCenter().x;
        double dy = falconPos.y - getCenter().y;
        double angleToFalcon = Math.toDegrees(Math.atan2(dy, dx));
        
        setOrientation((int)angleToFalcon);
        
        // Store base velocity components
        baseVectorX = Math.cos(Math.toRadians(getOrientation())) * THUNDER_SPEED;
        baseVectorY = Math.sin(Math.toRadians(getOrientation())) * THUNDER_SPEED;
        
        // Initial velocity set
        setDeltaX(baseVectorX);
        setDeltaY(baseVectorY);
    }
    
    @Override
    public void draw(Graphics g) {
        if (thunderImage != null) {
            Graphics2D g2d = (Graphics2D) g;
            int x = getCenter().x - getRadius();
            int y = getCenter().y - getRadius();
            g2d.drawImage(thunderImage, x, y, getRadius() * 2, getRadius() * 2, null);
        } else {
            g.setColor(Color.YELLOW);
            g.fillOval(getCenter().x - getRadius(), getCenter().y - getRadius(), 
                      getRadius() * 2, getRadius() * 2);
        }
    }
    
    @Override
    public void move() {
        // Calculate wave offset
        double perpX = -baseVectorY; // Perpendicular vector for sideways motion
        double perpY = baseVectorX;
        
        // Normalize perpendicular vector
        double perpLength = Math.sqrt(perpX * perpX + perpY * perpY);
        perpX /= perpLength;
        perpY /= perpLength;
        
        // Calculate wave offset
        double waveOffset = Math.sin(time * WAVE_FREQUENCY) * WAVE_AMPLITUDE;
        
        // Apply wave motion
        setDeltaX(baseVectorX + perpX * waveOffset);
        setDeltaY(baseVectorY + perpY * waveOffset);
        
        time += 1.0;
        
        super.move();
    }
    
    public int getDamage() {
        return THUNDER_DAMAGE;
    }

    public boolean hasWarned() {
        return hasWarned;
    }

    public void setWarned(boolean warned) {
        this.hasWarned = warned;
    }
}

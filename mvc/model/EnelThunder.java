package mvc.model;

import mvc.controller.CommandCenter;
import mvc.controller.ImageLoader;
import java.awt.*;
import java.awt.image.BufferedImage;

public class EnelThunder extends Sprite {
    private static final int THUNDER_SPEED = 18;  // Increased from 15
    private static final int THUNDER_DAMAGE = 15;  // Damage constant
    private static BufferedImage thunderImage;
    
    static {
        thunderImage = ImageLoader.getImage("foes/enel_small_thunder.png");
    }
    
    public EnelThunder(EnelBoss enel, double angleOffset) {
        setTeam(Team.FOE);
        setExpiry(45);  // Increased from 30
        setRadius(10);
        setCenter((Point) enel.getCenter().clone());
        
        // Calculate direction towards falcon
        Point sunnyPosition = CommandCenter.getInstance().getThousandSunny().getCenter();
        double dx = sunnyPosition.x - getCenter().x;
        double dy = sunnyPosition.y - getCenter().y;
        double angleToSunny = Math.toDegrees(Math.atan2(dy, dx));
        
        // Add offset to create spread effect
        setOrientation((int)(angleToSunny + angleOffset));
        
        // Calculate velocity components
        double vectorX = Math.cos(Math.toRadians(getOrientation())) * THUNDER_SPEED;
        double vectorY = Math.sin(Math.toRadians(getOrientation())) * THUNDER_SPEED;
        
        setDeltaX(vectorX);
        setDeltaY(vectorY);
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
    
    public int getDamage() {
        return THUNDER_DAMAGE;
    }
}

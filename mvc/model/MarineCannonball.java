package mvc.model;

import mvc.controller.ImageLoader;
import java.awt.*;
import java.awt.image.BufferedImage;

public class MarineCannonball extends Sprite {
    private static final int FIRE_POWER = 20;
    private static BufferedImage cannonballImage;
    
    static {
        cannonballImage = ImageLoader.getImage("projectiles/marine_cannonball.png");
    }
    
    public MarineCannonball(MarineShip marineShip) {
        setTeam(Team.FOE);
        setExpiry(20);
        setRadius(6);
        setCenter((Point) marineShip.getCenter().clone());
        setOrientation(marineShip.getOrientation());
        
        // Calculate velocity components
        double vectorX = Math.cos(Math.toRadians(getOrientation())) * FIRE_POWER;
        double vectorY = Math.sin(Math.toRadians(getOrientation())) * FIRE_POWER;
        
        setDeltaX(vectorX);
        setDeltaY(vectorY);
    }
    
    @Override
    public void draw(Graphics g) {
        if (cannonballImage != null) {
            Graphics2D g2d = (Graphics2D) g;
            int x = getCenter().x - getRadius();
            int y = getCenter().y - getRadius();
            g2d.drawImage(cannonballImage, x, y, getRadius() * 2, getRadius() * 2, null);
        } else {
            g.setColor(Color.RED);
            g.fillOval(getCenter().x - getRadius(), getCenter().y - getRadius(), 
                      getRadius() * 2, getRadius() * 2);
        }
    }
}

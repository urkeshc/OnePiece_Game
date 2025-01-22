package mvc.model;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.geom.AffineTransform;
import mvc.controller.ImageLoader;

public class CannonBall extends Sprite
{

    private static BufferedImage cannonballImage;

    static {
        cannonballImage = ImageLoader.getImage("projectiles/sunny_cannonball.png");

        if (cannonballImage == null) {
            System.err.println("Error: Cannonball image not found at /imgs/projectiles/sunny_cannonball.png");
        }
    }


    public CannonBall(ThousandSunny thousandSunny) {

        setTeam(Team.FRIEND);

        setExpiry(18);
        setRadius(8);


        //everything is relative to the falcon ship that fired the bullet
        setCenter((Point) thousandSunny.getCenter().clone());

        //set the bullet orientation to the falcon (ship) orientation
        setOrientation(thousandSunny.getOrientation());

        final double FIRE_POWER = 35.0;
        double vectorX =
                Math.cos(Math.toRadians(getOrientation())) * FIRE_POWER;
        double vectorY =
                Math.sin(Math.toRadians(getOrientation())) * FIRE_POWER;

        //fire force: falcon inertia + fire-vector
        setDeltaX(thousandSunny.getDeltaX() + vectorX);
        setDeltaY(thousandSunny.getDeltaY() + vectorY);

        //we have a reference to the falcon passed into the constructor. Let's create some kick-back.
        //fire kick-back on the falcon: inertia - fire-vector / some arbitrary divisor
        final double KICK_BACK_DIVISOR = 36.0;
        thousandSunny.setDeltaX(thousandSunny.getDeltaX() - vectorX / KICK_BACK_DIVISOR);
        thousandSunny.setDeltaY(thousandSunny.getDeltaY() - vectorY / KICK_BACK_DIVISOR);
    }


    @Override
    public void draw(Graphics g) {
        if (cannonballImage != null) {
            Graphics2D g2d = (Graphics2D) g;
    
            // Calculate the top-left corner of the image
            int x = getCenter().x - getRadius();
            int y = getCenter().y - getRadius();
    
            // Save the original transform
            AffineTransform originalTransform = g2d.getTransform();
    
            // Rotate the image to match the bullet's orientation
            g2d.rotate(Math.toRadians(getOrientation()), getCenter().x, getCenter().y);
    
            // Scale the image to match the bullet's size
            g2d.drawImage(cannonballImage, x, y, getRadius() * 2, getRadius() * 2, null);
    
            // Restore the original transform
            g2d.setTransform(originalTransform);
        } else {
            // Fallback to vector rendering if the image is not loaded
            renderVector(g);
        }
    }

    
}

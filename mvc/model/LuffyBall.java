package mvc.model;


import mvc.controller.CommandCenter;
import mvc.controller.SoundLoader;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.awt.*;

import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import mvc.controller.ImageLoader;

import java.util.LinkedList;

@Data
@EqualsAndHashCode(callSuper=false)
public class LuffyBall extends Sprite{

    private static final int EXPIRE = 140;
    private int nukeState = 0;

    // add image
    private BufferedImage luffyBall;

    private boolean dealtDamage = false;  // Track if this nuke has already dealt damage
    
    public boolean hasDealtDamage() {
        return dealtDamage;
    }

    public void setDealtDamage(boolean dealtDamage) {
        this.dealtDamage = dealtDamage;
    }

    public LuffyBall(ThousandSunny thousandSunny) {

        luffyBall = ImageLoader.getImage("powers/luffy_ball.png");

        if (luffyBall == null) {
            System.out.println("Couldn't load image");
        }
        

        setCenter((Point) thousandSunny.getCenter().clone());
        setColor(Color.YELLOW);
        setExpiry(EXPIRE);
        setRadius(0);
        setTeam(Team.FRIEND);

        final double FIRE_POWER = 11.0;
        double vectorX =
                Math.cos(Math.toRadians(thousandSunny.getOrientation())) * FIRE_POWER;
        double vectorY =
                Math.sin(Math.toRadians(thousandSunny.getOrientation())) * FIRE_POWER;

        //fire force: falcon inertia + fire-vector
        setDeltaX(thousandSunny.getDeltaX() + vectorX);
        setDeltaY(thousandSunny.getDeltaY() + vectorY);

    }


    @Override
    public void draw(Graphics g) 
    {
        if (luffyBall != null) {
            Graphics2D g2d = (Graphics2D) g;
    
            // Calculate the top-left corner of the image
            int x = getCenter().x - getRadius();
            int y = getCenter().y - getRadius();
    
            // Optional: Apply rotation if desired
            AffineTransform originalTransform = g2d.getTransform();
            // You can rotate the image if you like
            // g2d.rotate(Math.toRadians(getOrientation()), getCenter().x, getCenter().y);
    
            // Draw the image scaled to the current radius
            g2d.drawImage(luffyBall, x, y, getRadius() * 2, getRadius() * 2, null);
    
            // Reset the transform to avoid affecting other drawings
            g2d.setTransform(originalTransform);
        } else {
            // Fallback if the image is not loaded
            g.setColor(getColor());
            g.drawOval(getCenter().x - getRadius(), getCenter().y - getRadius(),
                       getRadius() * 2, getRadius() * 2);
        }
    }



    @Override
    public void move() {
        super.move();
        if (getExpiry() % (EXPIRE/6) == 0) nukeState++;
        switch (nukeState) {
            //travelling
            case 0:
                setRadius(17);
                break;
            //exploding
            case 1:
            case 2:
            case 3:
                setRadius(getRadius() + 8);
                break;
            //imploding
            case 4:
            case 5:
            default:
                setRadius(getRadius() - 11);
                break;


        }

    }

    @Override
    public void addToGame(LinkedList<Movable> list) 
    {
        list.add(this);
        SoundLoader.playSound("nuke.wav");
    }

    @Override
    public void removeFromGame(LinkedList<Movable> list) {
        if (getExpiry() == 0) list.remove(this);
    }
}

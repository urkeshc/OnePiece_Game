package mvc.model;

import mvc.controller.CommandCenter;
import mvc.controller.Game;
import mvc.controller.ImageLoader;
import mvc.controller.SoundLoader;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.LinkedList;

public class StrawHat extends Floater {


    private boolean collected = false;

    public boolean isCollected() {
        return collected;
    }
    public void setCollected(boolean collected) {
        this.collected = collected;
    }
    
    // Spawn every 12 seconds
    public static final int SPAWN_NUKE_FLOATER = Game.FRAMES_PER_SECOND * 8;

    private BufferedImage strawHatImage;

    public StrawHat() {
        // Load the strawhat image
        strawHatImage = ImageLoader.getImage("powers/strawhat.png");

        // Set properties for the floater
        setColor(Color.YELLOW); // Fallback color if the image fails to load
        setExpiry(145); // Floater will expire after 120 ticks
    }

    @Override
    public void draw(Graphics g) {
        if (strawHatImage != null) {
            Graphics2D g2d = (Graphics2D) g;

            // Calculate the position to draw the image centered on the floater
            int x = getCenter().x - getRadius();
            int y = getCenter().y - getRadius();

            // Draw the image with proper scaling
            g2d.drawImage(strawHatImage, x, y, getRadius() * 2, getRadius() * 2, null);
        } else {
            // Fallback rendering if the image fails to load
            g.setColor(getColor());
            g.drawOval(getCenter().x - getRadius(), getCenter().y - getRadius(), getRadius() * 2, getRadius() * 2);
        }
    }

    @Override
    public void removeFromGame(LinkedList<Movable> list) {
        super.removeFromGame(list);
        if (getExpiry() > 0) {
            SoundLoader.playSound("nuke-up.wav");
            CommandCenter.getInstance().getThousandSunny().incrementNukeCount();
        }
    }
}

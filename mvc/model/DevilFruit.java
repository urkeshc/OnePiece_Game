package mvc.model;

import mvc.controller.CommandCenter;
import mvc.controller.Game;
import mvc.controller.ImageLoader;
import mvc.controller.SoundLoader;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.LinkedList;

public class DevilFruit extends Floater {

    public static final int SPAWN_SHIELD_FLOATER = Game.FRAMES_PER_SECOND * 7;

    private BufferedImage devilFruitImage;

    public DevilFruit() {
        devilFruitImage = ImageLoader.getImage("powers/devil_fruit.png");

        // Set default properties
        setExpiry(210);
    }

    @Override
    public void draw(Graphics g) {
        if (devilFruitImage != null) {
            Graphics2D g2d = (Graphics2D) g;

            // Calculate the position to draw the image centered on the floater
            int x = getCenter().x - getRadius();
            int y = getCenter().y - getRadius();

            // Draw the image with proper scaling
            g2d.drawImage(devilFruitImage, x, y, getRadius() * 2, getRadius() * 2, null);
        } else {
            // Fallback rendering if the image fails to load
            g.setColor(getColor());
            g.drawOval(getCenter().x - getRadius(), getCenter().y - getRadius(), getRadius() * 2, getRadius() * 2);
        }
    }

    @Override
    public void removeFromGame(LinkedList<Movable> list) {
        super.removeFromGame(list);
        // If the floater is collected (not expired), grant the shield
        if (getExpiry() > 0) {
            System.out.println("Devil Fruit collected! Applying shield...");
            System.out.println("Current shield value before: " + CommandCenter.getInstance().getThousandSunny().getShield());
            CommandCenter.getInstance().getThousandSunny().setShield(ThousandSunny.MAX_SHIELD);
            System.out.println("Shield value after: " + CommandCenter.getInstance().getThousandSunny().getShield());
            SoundLoader.playSound("shieldup.wav");
        }
    }
}

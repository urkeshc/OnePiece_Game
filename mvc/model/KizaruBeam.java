package mvc.model;

import mvc.controller.CommandCenter;
import mvc.controller.Game;
import mvc.controller.GameOp;  // Add this import
import mvc.controller.ImageLoader;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

public class KizaruBeam extends Sprite {
    private static final int BEAM_SPEED = 20;
    private static final int BEAM_DAMAGE = 20;
    private static BufferedImage beamImage;

    static {
        beamImage = ImageLoader.getImage("foes/kizaru_beam.png");
    }

    public KizaruBeam(KizaruBoss kizaru, int lateralOffset) {
        setTeam(Team.FOE);
        setExpiry(45);
        setRadius(15);

        // Set initial position with lateral offset
        Point center = (Point) kizaru.getCenter().clone();
        center.translate(lateralOffset, 0);  // Apply horizontal offset
        setCenter(center);

        // Calculate direction towards falcon
        Point sunnyPosition = CommandCenter.getInstance().getThousandSunny().getCenter();
        double dx = sunnyPosition.x - getCenter().x;
        double dy = sunnyPosition.y - getCenter().y;
        double angle = Math.toDegrees(Math.atan2(dy, dx));

        // All beams will travel in the same direction
        setOrientation((int)angle);

        // Set velocity components
        double vectorX = Math.cos(Math.toRadians(getOrientation())) * BEAM_SPEED;
        double vectorY = Math.sin(Math.toRadians(getOrientation())) * BEAM_SPEED;

        setDeltaX(vectorX);
        setDeltaY(vectorY);
    }

    @Override
    public void move() {
        super.move();
        // Remove if off-screen
        if (getCenter().y > Game.DIM.height || getCenter().y < 0 ||
            getCenter().x > Game.DIM.width || getCenter().x < 0) {
            CommandCenter.getInstance().getOpsQueue().enqueue(this, GameOp.Action.REMOVE);
        }
    }

    @Override
    public void draw(Graphics g) {
        if (beamImage != null) {
            Graphics2D g2d = (Graphics2D) g;

            // Save the current transform
            AffineTransform old = g2d.getTransform();

            // Translate to the center point
            g2d.translate(getCenter().x, getCenter().y);

            // Rotate to match the beam's orientation
            g2d.rotate(Math.toRadians(getOrientation()));

            // Draw the beam
            g2d.drawImage(beamImage, 
                         -getRadius(), -getRadius(),
                         getRadius() * 2, getRadius() * 2, 
                         null);

            // Restore the original transform
            g2d.setTransform(old);
        } else {
            // Fallback to simple circle if image fails to load
            g.setColor(Color.YELLOW);
            g.fillOval(getCenter().x - getRadius(), 
                      getCenter().y - getRadius(),
                      getRadius() * 2, getRadius() * 2);
        }
    }

    public int getDamage() {
        return BEAM_DAMAGE;
    }
}

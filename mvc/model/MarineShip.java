package mvc.model;


import java.util.LinkedList;
import java.awt.*;
import java.awt.image.BufferedImage;

import mvc.controller.CommandCenter;
import mvc.controller.Game;
import mvc.controller.ImageLoader;
import mvc.controller.SoundLoader;


// public class MarineShip extends NormalFoe {
public class MarineShip extends Sprite {

// Characteristic of a Marine Ship: 
	private final int LARGE_RADIUS = 32;
    private int health;
    private static final int MAX_SPEED = 8;
    private static final int CHASE_SPEED = 5;
    private static final double LOCK_ON_DISTANCE = 300;
    private static final double PROB_LOCKON = 0.05;
    private static boolean lockedOnMarineShip = false;

    private boolean lockedOn = false;
    private Point lockonPoint;

    // Add these new fields
    private static final int FRAMES_BETWEEN_SHOTS = 50;
    private static final double FIRING_PROB = 0.02;
    private int framesSinceLastShot;
    private boolean firing;


	private BufferedImage rockImage;

	public MarineShip(int size){
		rockImage = ImageLoader.getImage("foes/marine_ship.png");

		if (rockImage == null) {
			System.out.println("Error loading Navy Ship image");
		}

		if (size == 0) setRadius(LARGE_RADIUS);
		else setRadius(LARGE_RADIUS/(size * 2));

		setTeam(Team.FOE);

        // set's the speed of the ships
		//random delta-x
		setDeltaX(somePosNegValue(12));
		//random delta-y
		setDeltaY(somePosNegValue(12));
        health = 2; // MarineShip takes 2 hits to destroy
	}
///
/// 
/// 
    @Override
    public void move() {
        super.move();
        framesSinceLastShot++;
        ThousandSunny thousandSunny = CommandCenter.getInstance().getThousandSunny();
        if (thousandSunny != null) {
            double distanceToSunny = getCenter().distance(thousandSunny.getCenter());
            if (distanceToSunny < LOCK_ON_DISTANCE) {
                if (!lockedOn && !hasLockedOnMarineShip() && Game.R.nextDouble() < PROB_LOCKON) {
                    setLockedOnMarineShip(true);
                    lockedOn = true;
                    int xOffset = Game.R.nextInt(200) - 100;
                    int yOffset = Game.R.nextInt(200) - 100;
                    lockonPoint = new Point(thousandSunny.getCenter().x + xOffset, thousandSunny.getCenter().y + yOffset);
                }
                if (lockedOn && lockonPoint != null) {
                    double dx = lockonPoint.x - getCenter().x;
                    double dy = lockonPoint.y - getCenter().y;
                    double distance = Math.hypot(dx, dy);
                    if (distance > 5) {
                        // Use CHASE_SPEED when locked on
                        setDeltaX(dx / distance * CHASE_SPEED);
                        setDeltaY(dy / distance * CHASE_SPEED);
                    } else {
                        setDeltaX(0);
                        setDeltaY(0);
                        lockedOn = false;
                        setLockedOnMarineShip(false);
                    }
                    // Update orientation to face the lockon point
                    setOrientation((int) Math.toDegrees(Math.atan2(dy, dx)));
                }
                // Add firing logic
                if (Game.R.nextDouble() < FIRING_PROB) {
                    firing = true;
                }
            } else {
                lockonPoint = null;
                lockedOn = false;
                setLockedOnMarineShip(false);
                // Random movement when not locking onto the Falcon
                if (Game.R.nextDouble() < 0.01) {
                    setDeltaX(somePosNegValue(5));
                    setDeltaY(somePosNegValue(5));
                }
            }
        }
    }

	public int getSize(){
		switch (getRadius()) {
			case LARGE_RADIUS:
				return 0;
			case LARGE_RADIUS / 2:
				return 1;
			case LARGE_RADIUS / 4:
				return 2;
			default:
				return 0;
		}
	}

	@Override
	public void draw(Graphics g) {
		// renderVector(g);

		if (rockImage != null) {
			// Cast Graphics to Graphics2D for better control
			Graphics2D g2d = (Graphics2D) g;

			// Calculate the top-left corner of the image
			int x = getCenter().x - getRadius();
			int y = getCenter().y - getRadius();

			g2d.drawImage(rockImage, x, y, getRadius() * 2, getRadius() * 2, null);

		} else {
			// Fallback to vector rendering if the image is not loaded
			renderVector(g);
		}

	}
///
/// 
/// 
/// 
	@Override
	public void removeFromGame(LinkedList<Movable> list) {
        if (health <= 0) {
            super.removeFromGame(list);
            // Play destruction sound
            SoundLoader.playSound("kapow.wav");
            // Award points
            CommandCenter.getInstance().setScore(CommandCenter.getInstance().getScore() + 10L * (getSize() + 1));
        }
	}
///
/// 
/// 
    public static boolean hasLockedOnMarineShip() {
        return lockedOnMarineShip;
    }

    public static void setLockedOnMarineShip(boolean lockedOn) {
        lockedOnMarineShip = lockedOn;
    }

    public void decreaseHealth() {
        SoundLoader.playSound("pillow.wav");
        health--;
    }

    public int getHealth() {
        return health;
    }

    // Add these new methods
    public boolean isFiring() {
        return firing;
    }
    
    public int getFramesSinceLastShot() {
        return framesSinceLastShot;
    }
    
    public void setFramesSinceLastShot(int frames) {
        this.framesSinceLastShot = frames;
    }
///
}

package mvc.model;

import mvc.controller.CommandCenter;
import mvc.controller.Game;
import mvc.controller.ImageLoader;
import mvc.controller.SoundLoader;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;

@Data
@EqualsAndHashCode(callSuper = false)
public class ThousandSunny extends Sprite {

	// ==============================================================
	// FIELDS 
	// ==============================================================

	public final static int TURN_STEP = 11;
	public static final int INITIAL_SPAWN_TIME = 48;
	public static final int MAX_SHIELD = 200;
	public static final int MAX_NUKE = 600;

	public static final int MIN_RADIUS = 32;
	public static final int MAX_HEALTH = 125;  // Add this constant

	private static final double BASE_THRUST = 0.502;
	private static final int BASE_MAX_VELOCITY = 39;
	private double thrust;
	private int maxVelocity;


	//images states
	public enum ImageState {
		SUNNY_INVISIBLE, //for pre-spawning
		SUNNY, //normal ship
		SUNNY_THR, //normal ship thrusting
		SUNNY_SHIELD, //shielded ship (cyan)
		SUNNY_SHIELD_THR, //shielded ship (cyan) thrusting
	}


	//instance fields (getters/setters provided by Lombok @Data above)
	private int shield;

	// private int nukeMeter;
	private int nukeCount; // allow more than one Luffy Ball
	private int invisible;
	private boolean maxSpeedAttained;
	private int showLevel;

	public enum TurnState {IDLE, LEFT, RIGHT}
	private TurnState turnState = TurnState.IDLE;

	private boolean thrusting;
	private int health;

	// ==============================================================
	// CONSTRUCTOR
	// ==============================================================
	
	public ThousandSunny() {

		setTeam(Team.FRIEND);

		setRadius(MIN_RADIUS);

    	Map<ImageState, BufferedImage> rasterMap = new HashMap<>();
		
		// Load and verify each image, with debug output
		BufferedImage normalImg = ImageLoader.getImage("fal/sunny125.png");
		BufferedImage thrustImg = ImageLoader.getImage("fal/sunny125_thr.png");
		

		System.out.println("Loading Thousand Sunny images:");
		System.out.println("Normal image: " + (normalImg != null ? "loaded" : "failed"));
		System.out.println("Thrust image: " + (thrustImg != null ? "loaded" : "failed"));


		// Map the images
		rasterMap.put(ImageState.SUNNY_INVISIBLE, null);  // This one is meant to be null
		rasterMap.put(ImageState.SUNNY, normalImg);
		rasterMap.put(ImageState.SUNNY_THR, thrustImg);
		// For shield states, reuse the normal/thrust images
		rasterMap.put(ImageState.SUNNY_SHIELD, normalImg);
		rasterMap.put(ImageState.SUNNY_SHIELD_THR, thrustImg);

		setRasterMap(rasterMap);
		setHealth(MAX_HEALTH);

		// Initialize speed-related fields directly
		int level = CommandCenter.getInstance().getLevel();
		thrust = BASE_THRUST * (1 + (level * 0.1));
		maxVelocity = (int) (BASE_MAX_VELOCITY * (1 + (level * 0.1)));
		System.out.println("Level " + level + " speeds - Thrust: " + thrust + ", Max Velocity: " + maxVelocity);

	}


	// ==============================================================
	// METHODS 
	// ==============================================================
    public void setShield(int shield) {
        this.shield = shield;
    }

    public void setShowLevel(int showLevel) {
        this.showLevel = showLevel;
    }


	@Override
	public void move() {

		if (!CommandCenter.getInstance().isSunnyPositionFixed()) super.move();

		if (invisible > 0) invisible--;
		if (shield > 0) shield--;
		if (showLevel > 0) showLevel--;


		//apply some thrust vectors using trig.
		if (thrusting) {
			double vectorX = Math.cos(Math.toRadians(getOrientation()))
					* thrust;
			double vectorY = Math.sin(Math.toRadians(getOrientation()))
					* thrust;

			//Absolute velocity is the hypotenuse of deltaX and deltaY
			int absVelocity =
					(int) Math.sqrt(Math.pow(getDeltaX()+ vectorX, 2) + Math.pow(getDeltaY() + vectorY, 2));

			//only accelerate (or adjust radius) if we are below the maximum absVelocity.
			if (absVelocity < maxVelocity){
				//accelerate
				setDeltaX(getDeltaX() + vectorX);
				setDeltaY(getDeltaY() + vectorY);
				setRadius(MIN_RADIUS + absVelocity / 3);
				maxSpeedAttained = false;
			} else {
				maxSpeedAttained = true;
			}

		}


		//adjust the orientation given turnState
		int adjustOr = getOrientation();
		switch (turnState){
			case LEFT:
				adjustOr = getOrientation() <= 0 ? 360 - TURN_STEP : getOrientation() - TURN_STEP;
				break;
			case RIGHT:
				adjustOr = getOrientation() >= 360 ? TURN_STEP : getOrientation() + TURN_STEP;
				break;
			case IDLE:
			default:
				//do nothing
		}
		setOrientation(adjustOr);

	}

	@Override
	public void draw(Graphics g) {

		if (nukeCount > 0) drawNukeHalo(g);

		//set local image-state
		ImageState imageState;
		if (invisible > 0){
			imageState = ImageState.SUNNY_INVISIBLE;
			// Don't try to render anything if invisible
			return;
		}
		else if (shield > 0){
			imageState = thrusting ? ImageState.SUNNY_SHIELD_THR : ImageState.SUNNY_SHIELD;
			drawShieldHalo(g);
		}
		else { //not protected
			imageState = thrusting ? ImageState.SUNNY_THR : ImageState.SUNNY;
		}

		BufferedImage currentImage = getRasterMap().get(imageState);
		if (currentImage == null) {
			System.out.println("WARNING: No image found for state: " + imageState);
			// Draw a placeholder rectangle when image is missing
			g.setColor(Color.RED);
			g.fillRect(getCenter().x - getRadius(), getCenter().y - getRadius(), 
					  getRadius() * 2, getRadius() * 2);
		} else {
			renderRaster((Graphics2D) g, currentImage);
		}
	}


	// shield halo
	private void drawShieldHalo(Graphics g){
		g.setColor(Color.CYAN);
		g.drawOval(getCenter().x - getRadius(), getCenter().y - getRadius(), getRadius() *2, getRadius() *2);
	}

	// nuke halo, not really needed
	private void drawNukeHalo(Graphics g){
		if (invisible > 0) return;
		g.setColor(Color.YELLOW);
		g.drawOval(getCenter().x - getRadius()+10, getCenter().y - getRadius()+10, getRadius() *2 -20,
				getRadius() *2-20);
	}



	@Override
	public void removeFromGame(LinkedList<Movable> list) {
	    // Only execute if both shield is down AND health is zero
	    if (shield == 0 && health == 0) {
	        CommandCenter.getInstance().setNumSunny(CommandCenter.getInstance().getNumSunny() - 1);
	        
	        if (CommandCenter.getInstance().isGameOver()) return;
	        
	        SoundLoader.playSound("shipspawn.wav");
	        setShield(ThousandSunny.INITIAL_SPAWN_TIME);
	        setInvisible(ThousandSunny.INITIAL_SPAWN_TIME / 5);
	        setOrientation(Game.R.nextInt(360 / ThousandSunny.TURN_STEP) * ThousandSunny.TURN_STEP);
	        setDeltaX(0);
	        setDeltaY(0);
	        setRadius(MIN_RADIUS);
	        setMaxSpeedAttained(false);
	        nukeCount = 0;
	        setHealth(MAX_HEALTH);
	    }
	}


	public void decrementSunnyNumAndSpawn() {
		CommandCenter.getInstance().setNumSunny(CommandCenter.getInstance().getNumSunny() - 1);
		if (CommandCenter.getInstance().isGameOver()) return;
		
		if (CommandCenter.getInstance().isGameOver()) return;
		SoundLoader.playSound("shipspawn.wav");
		setShield(ThousandSunny.INITIAL_SPAWN_TIME);
		setInvisible(ThousandSunny.INITIAL_SPAWN_TIME / 5);
		setOrientation(Game.R.nextInt(360 / ThousandSunny.TURN_STEP) * ThousandSunny.TURN_STEP);
		setDeltaX(0);
		setDeltaY(0);
		setRadius(MIN_RADIUS); // Use the updated radius
		setMaxSpeedAttained(false);
		nukeCount = 0;
		setHealth(MAX_HEALTH);  // Reset health when respawning
	
	}


	// handle the nukes of the Thousand Sunny
	public void incrementNukeCount() {
		if (nukeCount < 3) { // Assurez-vous de ne pas dépasser le maximum
			nukeCount++;
			System.out.println("Nuke count increased to: " + nukeCount);
		} else {
			System.out.println("Attempted to increase nukeCount beyond max: " + nukeCount);
		}
	}
	
	public void decrementNukeCount() {
		if (nukeCount > 0) {
			nukeCount -= 1;
			System.out.println("Nuke count decreased: " + nukeCount);
		} 
	}
		
	public int getNukeCount() {
		return nukeCount;
	}	
	
	// Add these methods
    public int getHealth() {
        return health;
    }
    
    public void setHealth(int health) {
        this.health = Math.min(Math.max(0, health), MAX_HEALTH);  // Clamp between 0 and MAX_HEALTH
    }
    
    public void damage(int amount) {
        int oldHealth = health;
        health = Math.max(0, health - amount);
        
        // Only print death message and remove if health just reached 0
        if (oldHealth > 0 && health == 0) {
            System.out.println("Falcon dead, remaining lives: " + (CommandCenter.getInstance().getNumSunny() - 1));
            removeFromGame(null);
        }
    }

    public void bounceOff(Movable other) {
        // Calculate bounce vector
        double dx = getCenter().x - other.getCenter().x;
        double dy = getCenter().y - other.getCenter().y;
        double distance = Math.hypot(dx, dy);

        // Normalize the vector
        dx /= distance;
        dy /= distance;

        // Set the bounce velocity
        final double BOUNCE_SPEED = 15.0;
        setDeltaX(dx * BOUNCE_SPEED);
        setDeltaY(dy * BOUNCE_SPEED);
	}

	public void updateSpeedForLevel() {
		int level = CommandCenter.getInstance().getLevel();
		// Increase speed by 10% each level
		thrust = BASE_THRUST * (1 + (level * 0.15));
		// maxVelocity = (int) (BASE_MAX_VELOCITY * (1 + (level * 0.15)));
		System.out.println("Level " + level + " speeds - Thrust: " + thrust + ", Max Velocity: " + maxVelocity);
	}

} // end class

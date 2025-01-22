package mvc.model;


import java.util.LinkedList;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.geom.AffineTransform;

import mvc.controller.CommandCenter;
import mvc.controller.GameOp;
import mvc.controller.ImageLoader;
import mvc.controller.SoundLoader;


public class Rocks extends Sprite {

	private final int LARGE_RADIUS = 110;

	private BufferedImage rockImage;

	public Rocks(int size){

		rockImage = ImageLoader.getImage("foes/rock.png");

		if (rockImage == null) {
			System.out.println("Error loading rock image");
		}

		if (size == 0) setRadius(LARGE_RADIUS);
		else setRadius(LARGE_RADIUS/(size * 2));

		setTeam(Team.FOE);

		//the spin will be either plus or minus 0-9
		setSpin(somePosNegValue(10));
		//random delta-x
		setDeltaX(somePosNegValue(10));
		//random delta-y
		setDeltaY(somePosNegValue(10));
	}

	public Rocks(Rocks astExploded){
		this(astExploded.getSize() + 1);
		setCenter((Point)astExploded.getCenter().clone());
		int newSmallerSize = astExploded.getSize() + 1;
		//random delta-x : inertia + the smaller the asteroid, the faster its possible speed
		setDeltaX(astExploded.getDeltaX() / 1.5 + somePosNegValue( 5 + newSmallerSize * 2));
		//random delta-y : inertia + the smaller the asteroid, the faster its possible speed
		setDeltaY(astExploded.getDeltaY() / 1.5 + somePosNegValue( 5 + newSmallerSize * 2));

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

			AffineTransform originalTransform = g2d.getTransform();
			if (getSpin() != 0) {
				g2d.rotate(Math.toRadians(getOrientation()), getCenter().x, getCenter().y);
			}

			g2d.drawImage(rockImage, x, y, getRadius() * 2, getRadius() * 2, null);

			// Reset the transform to avoid affecting other drawings
			g2d.setTransform(originalTransform);
		} else {
			// Fallback to vector rendering if the image is not loaded
			renderVector(g);
		}

	}

	@Override
	public void removeFromGame(LinkedList<Movable> list) {
		super.removeFromGame(list);
		spawnSmallerAsteroidsOrDebris();
		CommandCenter.getInstance().setScore(CommandCenter.getInstance().getScore() + 10L * (getSize() + 1));

		if (getSize() > 1)
			SoundLoader.playSound("pillow.wav");
		else
			SoundLoader.playSound("kapow.wav");

	}

	private void spawnSmallerAsteroidsOrDebris() {

		int size = getSize();
		if (size > 1) {
			CommandCenter.getInstance().getOpsQueue().enqueue(new WhiteCloudDebris(this), GameOp.Action.ADD);
		}
		else {
			size += 2;
			while (size-- > 0) {
				CommandCenter.getInstance().getOpsQueue().enqueue(new Rocks(this), GameOp.Action.ADD);
			}
		}

	}
}

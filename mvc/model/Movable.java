package mvc.model;

import java.awt.*;
import java.util.LinkedList;


public interface Movable {

	public enum Team {FRIEND, FOE, FLOATER, DEBRIS}

	//for the game to move and draw movable objects. See the GamePanel class.
	void move();
	void draw(Graphics g);

	//for collision detection
	Point getCenter();
	int getRadius();
	Team getTeam();

	void addToGame(LinkedList<Movable> list);
	void removeFromGame(LinkedList<Movable> list);


} //end Movable

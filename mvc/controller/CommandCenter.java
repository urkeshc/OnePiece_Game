package mvc.controller;


import java.awt.*;

import mvc.model.*;
import lombok.Data;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;


//The CommandCenter is a singleton that manages the state of the game.
//the lombok @Data gives us automatic getters and setters on all members
@Data
public class CommandCenter {

	public enum Universe {
		FREE_FLY,
		CENTER,
		BIG,
		HORIZONTAL,
		VERTICAL,
		DARK

	}

    public enum Difficulty {
        EASY,
        MEDIUM,
        HARD
    }

	private Universe universe;
	private  int numSunny;
	private  int level;
	private  long score;
	private  boolean paused;
	private  boolean themeMusic; // This field defaults to false in Java
	private boolean radar; //to toggle on/off the mini-map
	//this value is used to count the number of frames (full animation cycles) in the game
	private long frame;

	private String soundPath;
	private final ThousandSunny thousandSunny;
	//miniDimHash associates dimension with the Universe.
	private final Map<Universe, Dimension> miniDimHash = new HashMap<>();
	private final MiniMap miniMap = new MiniMap();

	private final LinkedList<Movable> movDebris = new LinkedList<>();
	private final LinkedList<Movable> movFriends = new LinkedList<>();
	private final LinkedList<Movable> movFoes = new LinkedList<>();
	private final LinkedList<Movable> movFloaters = new LinkedList<>();

	private final GameOpsQueue opsQueue = new GameOpsQueue();

    private Difficulty difficulty = Difficulty.MEDIUM;

	private static CommandCenter instance = null;

	// Constructor made private
	private CommandCenter() {
		instance = this;
		thousandSunny = new ThousandSunny();
	}

	public static CommandCenter getInstance(){
		if (instance == null){
			instance = new CommandCenter();
		}
		return instance;
	}

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }

	public void initGame(){
		clearAll();
		generateStarField();
		setDimHash();
		setLevel(0);
		setScore(0);
		setPaused(false);
        setThemeMusic(true);
		SoundLoader.playSound("dr_loop.wav");  // Start playing music immediately
		setNumSunny(4);  // Sets initial number of lives to 4
		thousandSunny.decrementSunnyNumAndSpawn();
		opsQueue.enqueue(thousandSunny, GameOp.Action.ADD);
		opsQueue.enqueue(miniMap, GameOp.Action.ADD);


	}

	private void setDimHash(){
		//initialize with values that define the aspect ratio of the Universe. See checkNewLevel() of Game class.
		miniDimHash.put(Universe.FREE_FLY, new Dimension(1,1));
		miniDimHash.put(Universe.CENTER, new Dimension(1,1));
		miniDimHash.put(Universe.BIG, new Dimension(2,2));
		miniDimHash.put(Universe.HORIZONTAL, new Dimension(3,1));
		miniDimHash.put(Universe.VERTICAL, new Dimension(1,3));
		miniDimHash.put(Universe.DARK, new Dimension(4,4));
	}


	private void generateStarField(){
		System.out.println("=== Generating initial bakground field ===");
		System.out.println("Current level: " + level);
		
		int count = 20;
		while (count-- > 0){
			opsQueue.enqueue(new BackgroundObjects(), GameOp.Action.ADD);
		}
	}

	public void incrementFrame(){
		frame = frame < Long.MAX_VALUE ? frame + 1 : 0;

		int framesPerSecond = Game.FRAMES_PER_SECOND;
		int secondsInterval = 5; // Print every 5 seconds
		if (frame % (framesPerSecond * secondsInterval) == 0) {
			long activeNukes = movFriends.stream().filter(m -> m instanceof LuffyBall).count();
			System.out.println("Active Nukes: " + activeNukes);
    	}
	}

	private void clearAll(){

		movDebris.clear();
		movFriends.clear();
		movFoes.clear();
		movFloaters.clear();
		
		// Extra safety: specific bg removal
		movDebris.removeIf(mov -> mov instanceof BackgroundObjects);
	}

	private boolean gameOver = false; // Add this field

	public boolean isGameOver() {		//if the number of falcons is zero, then game over
		// Modify this method to include the gameOver flag
		return numSunny < 1 || gameOver;  // Game ends when lives reach 0
	}

	public void setGameOver(boolean gameOver) {
		this.gameOver = gameOver;
	}

	public Dimension getUniDim(){
		return miniDimHash.get(universe);
	}

	public boolean isSunnyPositionFixed(){
		return universe != Universe.FREE_FLY;
	}

	public String getSoundPath() {
        return soundPath;
    }

    public void setSoundPath(String soundPath) {
        this.soundPath = soundPath;
    }
	






}

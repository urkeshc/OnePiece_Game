package mvc.controller;

import mvc.model.*;
import mvc.view.GamePanel;


import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.LinkedList;
import java.util.Random;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;


// ===============================================
// == This Game class is the CONTROLLER
// ===============================================

public class Game implements Runnable, KeyListener { 
    /// the game class agrees to implement the contract of the Runnable and KeyListener interfaces

    // ===============================================
    // FIELDS
    // ===============================================

    public static Dimension DIM; //the dimension of the game-screen.



    private final GamePanel gamePanel;
    //this is used throughout many classes.
    public static final Random R = new Random();

    public final static int ANIMATION_DELAY = 33; // milliseconds between frames

    public final static int FRAMES_PER_SECOND = 1000 / ANIMATION_DELAY;

    private final Thread animationThread;


    //key-codes
    private static final int
            PAUSE = 80, // p key
            QUIT = 81, // q key
            LEFT = 37, // rotate left; left arrow
            RIGHT = 39, // rotate right; right arrow
            UP = 38, // thrust; up arrow
            START = 83, // s key
            FIRE = 32, // space key
            MUTE = 77, // m-key mute
            NUKE = 70, // f-key
            RADAR = 65; // a-key


    // ===============================================
    // ==CONSTRUCTOR
    // ===============================================

    public Game() {
        DIM = setDimFromEnv();
        gamePanel = new GamePanel(DIM);
        gamePanel.addKeyListener(this);
        
        // Initialize background music
        CommandCenter.getInstance().setThemeMusic(true);
        SoundLoader.playSound("dr_loop.wav");
        
        // Start animation thread
        animationThread = new Thread(this);
        animationThread.setDaemon(true);
        animationThread.start();

        // Prompt the player to select difficulty level
        SwingUtilities.invokeLater(() -> {
            String[] options = {"Easy", "Medium", "Hard"};
            int choice = JOptionPane.showOptionDialog(
                null,
                "Select Difficulty Level:",
                "Difficulty Selection",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[1]
            );
            CommandCenter.Difficulty difficulty = CommandCenter.Difficulty.MEDIUM;
            if (choice == 0) {
                difficulty = CommandCenter.Difficulty.EASY;
            } else if (choice == 2) {
                difficulty = CommandCenter.Difficulty.HARD;
            }
            CommandCenter.getInstance().setDifficulty(difficulty);
            // Initialize the game after selecting difficulty
            CommandCenter.getInstance().initGame();
        });
    }

    // ===============================================
    // ==METHODS
    // ===============================================

    public static void main(String[] args) {


        //typical Swing application start; we pass EventQueue a Runnable object.
        EventQueue.invokeLater(Game::new);
    }

    /*
    in the Environmental Variables of the runtime configuration:
    WIDTH=980;HEIGHT=600 or something like this.
     */
    private Dimension setDimFromEnv(){

        Dimension dimension;
        try {
            String width = System.getenv("WIDTH");
            String height = System.getenv("HEIGHT");
            dimension = new Dimension(Integer.parseInt(width), Integer.parseInt(height));
        } catch (NumberFormatException e) {
            //some default value
            dimension =  new Dimension(1500, 950);
        }
        return dimension;
    }

    // Game implements runnable, and must have run method
    @Override
    public void run() {

        // lower animation thread's priority, thereby yielding to the 'Event Dispatch Thread' or EDT
        // thread which listens to keystrokes
        animationThread.setPriority(Thread.MIN_PRIORITY);

        // and get the current time
        long startTime = System.currentTimeMillis();

        // this thread animates the scene
        while (Thread.currentThread() == animationThread) {


            //this call will cause all movables to move() and draw() themselves every ~40ms
            // see GamePanel class for details
            gamePanel.update(gamePanel.getGraphics());

            checkCollisions();
            checkNewLevel();

            checkFloaters();
            fireMarineShips(); // Add this line
            //this method will execute addToGame() and removeFromGame() callbacks on Movable objects
            processGameOpsQueue();
            //keep track of the frame for development purposes
            CommandCenter.getInstance().incrementFrame();

            // surround the sleep() in a try/catch block
            // this simply controls delay time between
            // the frames of the animation
            try {
                // The total amount of time is guaranteed to be at least ANIMATION_DELAY long.  If processing (update)
                // between frames takes longer than ANIMATION_DELAY, then the difference between startTime - 
                // System.currentTimeMillis() will be negative, then zero will be the sleep time
                startTime += ANIMATION_DELAY;

                Thread.sleep(Math.max(0,
                        startTime - System.currentTimeMillis()));
            } catch (InterruptedException e) {
                // do nothing (bury the exception), and just continue, e.g. skip this frame -- no big deal
            }
        } // end while
    } // end run

    private void checkFloaters() {

        spawnDevilFruits();
        spawnStrawHats();
    }

    private void checkCollisions() {
        // ...existing code...
        
        
        //This has order-of-growth of O(FOES * FRIENDS)
        Point pntFriendCenter, pntFoeCenter;
        int radFriend, radFoe;
        for (Movable movFriend : CommandCenter.getInstance().getMovFriends()) {
            // Skip if the friend has already been marked for removal
            if (CommandCenter.getInstance().getOpsQueue().contains(movFriend)) continue;

            for (Movable movFoe : CommandCenter.getInstance().getMovFoes()) {
                // Skip if the foe has already been marked for removal
                if (CommandCenter.getInstance().getOpsQueue().contains(movFoe)) continue;

                pntFriendCenter = movFriend.getCenter();
                pntFoeCenter = movFoe.getCenter();
                radFriend = movFriend.getRadius();
                radFoe = movFoe.getRadius();

                //detect collision
                if (pntFriendCenter.distance(pntFoeCenter) < (radFriend + radFoe)) {
                    if (movFoe instanceof EnelBoss) {
                        EnelBoss enel = (EnelBoss) movFoe;
                        if (movFriend instanceof CannonBall) {
                            System.out.println("Bullet hit Enel! Health before: " + enel.getCurrentHealth());
                            enel.damage(35);
                            System.out.println("Health after bullet: " + enel.getCurrentHealth());
                            CommandCenter.getInstance().getOpsQueue().enqueue(movFriend, GameOp.Action.REMOVE);

                        } else if (movFriend instanceof LuffyBall && !((LuffyBall)movFriend).hasDealtDamage()) {
                            System.out.println("Nuke hit Enel! Health before: " + enel.getCurrentHealth());
                            enel.damage(200);
                            System.out.println("Health after nuke: " + enel.getCurrentHealth());
                            ((LuffyBall)movFriend).setDealtDamage(true);  // Mark this nuke as having dealt its damage
                        }
                        
                        // Only remove Enel if health drops to zero
                        if (enel.getCurrentHealth() <= 0) {
                            CommandCenter.getInstance().getOpsQueue().enqueue(enel, GameOp.Action.REMOVE);
                            System.out.println("Enel defeated!");
                        }
                        break;

                    } else if (movFoe instanceof MarineShip) {
                        MarineShip marineShip = (MarineShip) movFoe;
                        marineShip.decreaseHealth();
                        // Remove the friend (e.g., bullet)
                        CommandCenter.getInstance().getOpsQueue().enqueue(movFriend, GameOp.Action.REMOVE);
                        if (marineShip.getHealth() <= 0) {
                            CommandCenter.getInstance().getOpsQueue().enqueue(marineShip, GameOp.Action.REMOVE);
                        }
                    } else if (movFoe instanceof KizaruBoss) {
                        KizaruBoss kizaru = (KizaruBoss) movFoe;
                        if (movFriend instanceof CannonBall) {
                            System.out.println("Direct hit on Kizaru with bullet!");
                            kizaru.damage(35);
                            CommandCenter.getInstance().getOpsQueue().enqueue(movFriend, GameOp.Action.REMOVE);

                        } else if (movFriend instanceof LuffyBall && !((LuffyBall)movFriend).hasDealtDamage()) {
                            System.out.println("Kizaru hit by Nuke!");
                            kizaru.damage(200);
                            ((LuffyBall)movFriend).setDealtDamage(true);
                        }
                        
                        if (kizaru.getCurrentHealth() <= 0) {
                            CommandCenter.getInstance().getOpsQueue().enqueue(kizaru, GameOp.Action.REMOVE);
                        }
                        break;
                    } else {
                        //enqueue the friend
                        CommandCenter.getInstance().getOpsQueue().enqueue(movFriend, GameOp.Action.REMOVE);
                        //enqueue the foe
                        CommandCenter.getInstance().getOpsQueue().enqueue(movFoe, GameOp.Action.REMOVE);
                    }
                }
            }//end inner for
        }//end outer for


        //check for collisions between falcon and floaters. Order of growth of O(FLOATERS)
        Point pntFalCenter = CommandCenter.getInstance().getThousandSunny().getCenter();
        int radFalcon = CommandCenter.getInstance().getThousandSunny().getRadius();

        Point pntFloaterCenter;
        int radFloater;
        for (Movable movFloater : CommandCenter.getInstance().getMovFloaters()) {
            pntFloaterCenter = movFloater.getCenter();
            radFloater = movFloater.getRadius();

            //detect collision
            if (pntFalCenter.distance(pntFloaterCenter) < (radFalcon + radFloater)) {

                // handle the Luffy Ball increments
                if (movFloater instanceof StrawHat) {

                    StrawHat strawHat = (StrawHat) movFloater;
                    if (!strawHat.isCollected() && CommandCenter.getInstance().getThousandSunny().getNukeCount() < 3) {
                        strawHat.setCollected(true);
                        System.out.println("Strawhat collected.");
                    }
                }

                //enqueue the floater
                CommandCenter.getInstance().getOpsQueue().enqueue(movFloater, GameOp.Action.REMOVE);
            }//end if
        }//end for


        // Single consolidated check for Falcon vs all Foes
        ThousandSunny thousandSunny = CommandCenter.getInstance().getThousandSunny();
        for (Movable movFoe : CommandCenter.getInstance().getMovFoes()) {
            if (thousandSunny.getCenter().distance(movFoe.getCenter()) < (thousandSunny.getRadius() + movFoe.getRadius())) {
                if (thousandSunny.getShield() <= 0) {  // Only take damage if not shielded
                    if (movFoe instanceof EnelBigThunder) {
                        int currentHealth = thousandSunny.getHealth();
                        System.out.println("Falcon hit by Big Thunder, health before: " + currentHealth);
                        thousandSunny.damage(((EnelBigThunder) movFoe).getDamage());
                        System.out.println("Big Thunder hit! New health: " + thousandSunny.getHealth());
                        CommandCenter.getInstance().getOpsQueue().enqueue(movFoe, GameOp.Action.REMOVE);
                        SoundLoader.playSound("kapow.wav");

                    } else if (movFoe instanceof EnelThunder) {
                        int currentHealth = thousandSunny.getHealth();
                        System.out.println("Falcon hit by Thunder, health before: " + currentHealth);
                        thousandSunny.damage(((EnelThunder) movFoe).getDamage());
                        System.out.println("Thunder hit! New health: " + thousandSunny.getHealth());
                        CommandCenter.getInstance().getOpsQueue().enqueue(movFoe, GameOp.Action.REMOVE);
                        SoundLoader.playSound("kapow.wav");

                    } else if (movFoe instanceof KizaruBeam) {
                        int currentHealth = thousandSunny.getHealth();
                        System.out.println("Falcon hit by Kizaru's beam! Health before: " + currentHealth);
                        thousandSunny.damage(((KizaruBeam) movFoe).getDamage());
                        System.out.println("Beam hit! New health: " + thousandSunny.getHealth());
                        CommandCenter.getInstance().getOpsQueue().enqueue(movFoe, GameOp.Action.REMOVE);
                        SoundLoader.playSound("kapow.wav");

                    } else if (movFoe instanceof EnelBoss) {
                        // First check shield and apply damage if no shield
                        if (thousandSunny.getShield() <= 0) {
                            thousandSunny.damage(10);  // Apply damage first
                            System.out.println("Falcon took damage from Enel's electric aura!");
                        }
                        
                        // Then bounce and play sound
                        thousandSunny.bounceOff(movFoe);
                        SoundLoader.playSound("bounce.wav");
                        
                        // Finally, give a brief immunity to prevent multiple rapid hits
                        thousandSunny.setShield(20); // Brief immunity frame after hit
                    } else if (movFoe instanceof MarineCannonball) {
                        thousandSunny.damage(25);  // Projectiles do 25 damage
                        CommandCenter.getInstance().getOpsQueue().enqueue(movFoe, GameOp.Action.REMOVE);
                        SoundLoader.playSound("kapow.wav");
                    } else if (movFoe instanceof MarineShip) {
                        thousandSunny.damage(50);  // Direct ship collision does 50 damage
                        SoundLoader.playSound("kapow.wav");
                    } else if (movFoe instanceof Rocks) {
                        thousandSunny.damage(70);  // Asteroids do 75 damage
                        SoundLoader.playSound("kapow.wav");
                    } else if (movFoe instanceof EnelBoss) { // here we will add the Enel thunder, as in the MarineCannonball
                        // For now, just print debug info with no damage to either
                        System.out.println("Collision with Enel - no damage applied yet");
                        // We'll add bounce effect in next step
                    } else if (movFoe instanceof KizaruBoss) {
                        // First check shield and apply damage if no shield
                        if (thousandSunny.getShield() <= 0) {
                            thousandSunny.damage(10);  // Apply damage first
                            System.out.println("Falcon took damage from Kizaru's aura!");
                        }
                        
                        // Then bounce and play sound
                        thousandSunny.bounceOff(movFoe);
                        SoundLoader.playSound("bounce.wav");
                        
                        // Brief immunity after hit
                        thousandSunny.setShield(20);
                    }
                }
            }
        }

    }//end meth



// 
    //This method adds and removes movables to/from their respective linked-lists.
    private void processGameOpsQueue() {

        //deferred mutation: these operations are done AFTER we have completed our collision detection to avoid
        // mutating the movable linkedlists while iterating them above.
        while (!CommandCenter.getInstance().getOpsQueue().isEmpty()) {

            GameOp gameOp = CommandCenter.getInstance().getOpsQueue().dequeue();

            //given team, determine which linked-list this object will be added-to or removed-from
            LinkedList<Movable> list;
            Movable mov = gameOp.getMovable();
            switch (mov.getTeam()) {
                case FOE:
                    list = CommandCenter.getInstance().getMovFoes();
                    break;
                case FRIEND:
                    list = CommandCenter.getInstance().getMovFriends();
                    break;
                case FLOATER:
                    list = CommandCenter.getInstance().getMovFloaters();
                    break;
                case DEBRIS:
                default:
                    list = CommandCenter.getInstance().getMovDebris();
            }

            //pass the appropriate linked-list from above
            //this block will execute the addToGame() or removeFromGame() callbacks in the Movable models.
            GameOp.Action action = gameOp.getAction();
            if (action == GameOp.Action.ADD)
                mov.addToGame(list);
            else //REMOVE
                mov.removeFromGame(list);

        }//end while
    }



/// SPAWN DEMON FRUIT AND STRAWHATS
    private void spawnDevilFruits() {

        if (CommandCenter.getInstance().getFrame() % DevilFruit.SPAWN_SHIELD_FLOATER == 0) {
            CommandCenter.getInstance().getOpsQueue().enqueue(new DevilFruit(), GameOp.Action.ADD);
        }
    }
    private void spawnStrawHats() {

        if (CommandCenter.getInstance().getFrame() % StrawHat.SPAWN_NUKE_FLOATER == 0) {
            CommandCenter.getInstance().getOpsQueue().enqueue(new StrawHat(), GameOp.Action.ADD);
        }
    }



// SPAWN FOES (ROCKS, MARINES, BOSSES)
    private void spawnRocks(int num) {

        while (num-- > 0) {
            CommandCenter.getInstance().getOpsQueue().enqueue(new Rocks(0), GameOp.Action.ADD);

        }
    }
    private void spawnBigMarineShip(int num) {

        while (num-- > 0) {
            CommandCenter.getInstance().getOpsQueue().enqueue(new MarineShip(0), GameOp.Action.ADD);

        }
    }


// LEVEL CHECKS, CINEMATICS AND UPDATES
    private static final int MAX_LEVEL = 3; // Define maximum level
    private boolean isTransitioning = false;
    private boolean isVictorySequenceStarted = false; // Add this field at class level

    private boolean isLevelClear() {
        // Don't check if already transitioning or victory sequence started
        if (isTransitioning || isVictorySequenceStarted) return false;
        
        // Only check every few frames to reduce spam
        if (CommandCenter.getInstance().getFrame() % 10 != 0) return false;
        
        int currentLevel = CommandCenter.getInstance().getLevel();
        
        // Check appropriate enemies based on level
        for (Movable movFoe : CommandCenter.getInstance().getMovFoes()) {
            if (currentLevel == 2) {
                if (movFoe instanceof KizaruBoss) return false;
            }
            else if (currentLevel == 3) {
                if (movFoe instanceof EnelBoss) return false;
            }
            else {
                if (movFoe instanceof Rocks || movFoe instanceof MarineShip) return false;
            }
        }
        
        // Level is clear - log it once
        System.out.println("Level " + currentLevel + " cleared!");
        
        // If we just cleared level 3, trigger victory sequence only once
        if (currentLevel == 3 && !isVictorySequenceStarted) {
            isVictorySequenceStarted = true;
            showGameVictory();
            return false;
        }
        
        return true;
    }

    private void showGameVictory() {
        isTransitioning = true;
        CommandCenter.getInstance().setPaused(true);
        
        String victoryMessage = "Congratulations! You've defeated both bosses!";
        String victoryImage = "/imgs/level_cleared/luffy_endgame.png";
        
        System.out.println("Starting victory sequence");
        gamePanel.showCinematic(victoryImage, victoryMessage);
        
        // Show victory screen for 5 seconds, then exit game
        javax.swing.Timer timer = new javax.swing.Timer(5000, e -> {
            ((javax.swing.Timer)e.getSource()).stop();
            gamePanel.hideCinematic();
            System.out.println("Victory sequence complete - Exiting game");
            System.exit(0);  // Force exit the game
        });
        timer.setRepeats(false);
        timer.start();
    }

    private void checkNewLevel() {
        // Don't check for new level if we're transitioning or level isn't clear
        if (!isLevelClear() || isTransitioning) return;

        int currentLevel = CommandCenter.getInstance().getLevel();
        int nextLevel = currentLevel + 1;

        // Don't proceed past MAX_LEVEL
        if (nextLevel > MAX_LEVEL) return;

        // Rest of level transition logic
        // ...existing code...
        isTransitioning = true;
        CommandCenter.getInstance().setPaused(true);

        // Setup the next level first
        setupNewLevel(nextLevel);

        // Only show cinematics for levels 2 and 3
        if (nextLevel == 2 || nextLevel == 3) {
            String message = "Welcome to Level " + nextLevel + ": " +
                            (nextLevel == 2 ? "Sabaody" : "Skypiea");
            String imagePath = "/imgs/level_cleared/" +
                            (nextLevel == 2 ? "kizaru_lvl2.png" : "enels_lvl3.png");

            System.out.println("Showing cinematic for level " + nextLevel);
            gamePanel.showCinematic(imagePath, message);

            // Create a timer to hide the cinematic and resume the game
            javax.swing.Timer timer = new javax.swing.Timer(3000, e -> {
                ((javax.swing.Timer)e.getSource()).stop();
                gamePanel.hideCinematic();
                CommandCenter.getInstance().setPaused(false);
                isTransitioning = false;
                System.out.println("Level " + nextLevel + " started");
            });
            timer.setRepeats(false);
            timer.start();
        } else {
            // For level 1, just unpause and continue
            CommandCenter.getInstance().setPaused(false);
            isTransitioning = false;
            System.out.println("Level " + nextLevel + " started (no cinematic)");
        }
    }

    private void handleGameCompletion() {
        isTransitioning = true;
        CommandCenter.getInstance().setPaused(true);

        // Show victory cinematic
        String victoryMessage = "Congratulations! You've defeated both admirals!";
        String victoryImage = "/imgs/level_cleared/luffy_victory.png";
        gamePanel.showCinematic(victoryImage, victoryMessage);

        // Create a timer to transition to game over state
        javax.swing.Timer timer = new javax.swing.Timer(5000, e -> {
            ((javax.swing.Timer)e.getSource()).stop();
            gamePanel.hideCinematic();
            
            // Set game over state after showing victory screen
            isTransitioning = false;
            CommandCenter.getInstance().setGameOver(true);
            CommandCenter.getInstance().setPaused(false);
        });
        timer.setRepeats(false);
        timer.start();
    }

    private void setupNewLevel(int level) {
        // Ensure the level does not exceed MAX_LEVEL
        if (level > MAX_LEVEL) {
            level = MAX_LEVEL;
        }

        // Set level and score
        CommandCenter.getInstance().setLevel(level);
        CommandCenter.getInstance().setScore(CommandCenter.getInstance().getScore() + (10_000L * level));
        
        // Reset game state
        CommandCenter.getInstance().setUniverse(CommandCenter.Universe.values()[0]);
        CommandCenter.getInstance().setRadar(false);
        
        ThousandSunny thousandSunny = CommandCenter.getInstance().getThousandSunny();
        thousandSunny.setCenter(new Point(Game.DIM.width / 2, Game.DIM.height / 2));
        thousandSunny.setShield(ThousandSunny.INITIAL_SPAWN_TIME);
        thousandSunny.setShowLevel(ThousandSunny.INITIAL_SPAWN_TIME);
        
        // Clear and regenerate environment
        CommandCenter.getInstance().getMovDebris().clear();
        CommandCenter.getInstance().getMovFoes().clear();
        generateNewBgField(8);
        
        // Setup level content
        switch (level) {
            case 2:
                gamePanel.setBackgroundImage("/imgs/backgrounds/sabaody_background.jpg");
                spawnRocks(level);
                spawnBigMarineShip(level);
                CommandCenter.getInstance().getOpsQueue().enqueue(new KizaruBoss(), GameOp.Action.ADD);
                SoundLoader.playSound("kizaru.wav");
                break;
            case 3:
                spawnRocks(1);
                spawnBigMarineShip(1);
                gamePanel.setBackgroundImage("/imgs/backgrounds/skypiea_background.jpg");
                CommandCenter.getInstance().getOpsQueue().enqueue(new EnelBoss(), GameOp.Action.ADD);
                SoundLoader.playSound("enel.wav");
                break;
            default:
                gamePanel.setBackgroundImage("/imgs/backgrounds/ocean_background.jpg");
                spawnRocks(level + 2);
                spawnBigMarineShip(level + 2);
        }
        
        CommandCenter.getInstance().setPaused(false);
    }

    private void generateNewBgField(int count) {
        long currentStars = CommandCenter.getInstance().getMovDebris()
            .stream()
            .filter(mov -> mov instanceof BackgroundObjects)
            .count();

        CommandCenter.getInstance().getMovDebris().removeIf(mov -> mov instanceof BackgroundObjects);
        
        while (count-- > 0) {
            CommandCenter.getInstance().getOpsQueue().enqueue(new BackgroundObjects(), GameOp.Action.ADD);
        }

        // Debug final count
        currentStars = CommandCenter.getInstance().getMovDebris()
            .stream()
            .filter(mov -> mov instanceof BackgroundObjects)
            .count();
    }


    // ===============================================
    // KEYLISTENER METHODS
    // ===============================================

    @Override
    public void keyPressed(KeyEvent e) {
        ThousandSunny thousandSunny = CommandCenter.getInstance().getThousandSunny();
        int keyCode = e.getKeyCode();

        switch (keyCode) {
            case FIRE:
                CommandCenter.getInstance().getOpsQueue().enqueue(new CannonBall(thousandSunny), GameOp.Action.ADD);
                break;
            case NUKE:
                if (thousandSunny.getNukeCount() > 0)
                {
                    CommandCenter.getInstance().getOpsQueue().enqueue(new LuffyBall(thousandSunny), GameOp.Action.ADD);
                    thousandSunny.decrementNukeCount();
                }
                break;
            case UP:
                thousandSunny.setThrusting(true);
                SoundLoader.playSound("whitenoise_loop.wav");
                break;
            case LEFT:
                thousandSunny.setTurnState(ThousandSunny.TurnState.LEFT);
                break;
            case RIGHT:
                thousandSunny.setTurnState(ThousandSunny.TurnState.RIGHT);
                break;
            default:
                break;
        }

    }

    @Override
    public void keyReleased(KeyEvent e) {
        ThousandSunny thousandSunny = CommandCenter.getInstance().getThousandSunny();
        int keyCode = e.getKeyCode();
        //show the key-code in the console
        System.out.println(keyCode);

        if (keyCode == START && CommandCenter.getInstance().isGameOver()) {
            CommandCenter.getInstance().initGame();
            return;
        }

        switch (keyCode) {

            //releasing either the LEFT or RIGHT arrow key will set the TurnState to IDLE
            case LEFT:
            case RIGHT:
                thousandSunny.setTurnState(ThousandSunny.TurnState.IDLE);
                break;
            case UP:
                // Replace System.exit(0); with stopping thrust
                thousandSunny.setThrusting(false);
                SoundLoader.stopSound("whitenoise_loop.wav"); // Optional: Stop thrust sound
                break;
            case RADAR:
                //toggle the boolean switch
                CommandCenter.getInstance().setRadar(!CommandCenter.getInstance().isRadar());
                break;
            case MUTE:
                //if music is currently playing, then stop
                if (CommandCenter.getInstance().isThemeMusic()) {
                    SoundLoader.stopSound("dr_loop.wav");
                } else { //else not playing, then play
                    SoundLoader.playSound("dr_loop.wav");
                }
                //toggle the boolean switch
                CommandCenter.getInstance().setThemeMusic(!CommandCenter.getInstance().isThemeMusic());
                break;
            default:
                break;

        }

    }

    @Override
    // does nothing, but we need it b/c of KeyListener contract
    public void keyTyped(KeyEvent e) {
    }

    private void fireMarineShips() {
        for (Movable movFoe : CommandCenter.getInstance().getMovFoes()) {
            if (movFoe instanceof MarineShip) {
                MarineShip marineShip = (MarineShip) movFoe;
                if (marineShip.isFiring() && marineShip.getFramesSinceLastShot() >= 50) {
                    CommandCenter.getInstance().getOpsQueue().enqueue(
                        new MarineCannonball(marineShip), GameOp.Action.ADD);
                    marineShip.setFramesSinceLastShot(0);
                }
            }
        }
    }

    private void showVictoryScreen() {
        isTransitioning = true;
        CommandCenter.getInstance().setPaused(true);

        String victoryMessage = "Congratulations! You've defeated both admirals!";
        String victoryImage = "/imgs/level_cleared/luffy_victory.png";
        
        gamePanel.showCinematic(victoryImage, victoryMessage);

        // Create a timer to show victory screen for 5 seconds before game over
        javax.swing.Timer timer = new javax.swing.Timer(5000, e -> {
            ((javax.swing.Timer)e.getSource()).stop();
            gamePanel.hideCinematic();
            isTransitioning = false;
            CommandCenter.getInstance().setGameOver(true);
        });
        timer.setRepeats(false);
        timer.start();
    }
    
    // Debug helper method
    private void debugStarCount(String location) {
        long starCount = CommandCenter.getInstance().getMovDebris()
            .stream()
            .filter(mov -> mov instanceof BackgroundObjects)
            .count();
        System.out.println("Star count at " + location + ": " + starCount);
    }

} // end of Game class





package mvc.view;

import mvc.controller.CommandCenter;
import mvc.controller.Game;
import mvc.controller.ImageLoader;
import mvc.model.*;

import java.awt.*;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.awt.Image;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JPanel;  // Add this import


public class GamePanel extends JPanel {

    // ==============================================================
    // FIELDS
    // ==============================================================
    private final Font fontNormal = new Font("SansSerif", Font.BOLD, 12);
    private final Font fontBig = new Font("SansSerif", Font.BOLD + Font.ITALIC, 36);
    private final DecimalFormat decimalFormat = new DecimalFormat("#,###");
    private FontMetrics fontMetrics;
    private int fontWidth;
    private int fontHeight;

    //
    private BufferedImage luffyBallImage;
    private BufferedImage opeOpeImage;


    //used to draw number of ships remaining
    private final Point[] pntShipsRemaining;

    //used for double-buffering
    private Image imgOff;
    private Graphics grpOff;

    private Image backgroundImage;

    private ImageIcon cinematicImage;
    private String cinematicText;
    private boolean showCinematic = false;

    // ==============================================================
    // CONSTRUCTOR
    // ==============================================================


    public GamePanel(Dimension dim) {

        GameFrame gameFrame = new GameFrame();

        gameFrame.getContentPane().add(this);


        // background, based on level
        setBackgroundImage("/imgs/backgrounds/ocean_background.jpg");


        // luffyball for top right corner tracker
        try {
            luffyBallImage = ImageLoader.getImage("powers/luffy_ball.png");
            if (luffyBallImage == null) {
                System.err.println("Could not load Luffy Ball image.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Load the Ope Ope no Mi heart image
        try {
            opeOpeImage = ImageLoader.getImage("fal/opeope_nomi.png");
            if (opeOpeImage == null) {
                System.err.println("Could not load Ope Ope no Mi image.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        // Robert Alef's awesome falcon design
        List<Point> listShip = new ArrayList<>();
        listShip.add(new Point(0,9));
        listShip.add(new Point(-1, 6));
        listShip.add(new Point(-1,3));
        listShip.add(new Point(-4, 1));
        listShip.add(new Point(4,1));
        listShip.add(new Point(-4,1));
        listShip.add(new Point(-4, -2));
        listShip.add(new Point(-1, -2));
        listShip.add(new Point(-1, -9));
        listShip.add(new Point(-1, -2));
        listShip.add(new Point(-4, -2));
        listShip.add(new Point(-10, -8));
        listShip.add(new Point(-5, -9));
        listShip.add(new Point(-7, -11));
        listShip.add(new Point(-4, -11));
        listShip.add(new Point(-2, -9));
        listShip.add(new Point(-2, -10));
        listShip.add(new Point(-1, -10));
        listShip.add(new Point(-1, -9));
        listShip.add(new Point(1, -9));
        listShip.add(new Point(1, -10));
        listShip.add(new Point(2, -10));
        listShip.add(new Point(2, -9));
        listShip.add(new Point(4, -11));
        listShip.add(new Point(7, -11));
        listShip.add(new Point(5, -9));
        listShip.add(new Point(10, -8));
        listShip.add(new Point(4, -2));
        listShip.add(new Point(1, -2));
        listShip.add(new Point(1, -9));
        listShip.add(new Point(1, -2));
        listShip.add(new Point(4,-2));
        listShip.add(new Point(4, 1));
        listShip.add(new Point(1, 3));
        listShip.add(new Point(1,6));
        listShip.add(new Point(0,9));


        pntShipsRemaining = listShip.toArray(new Point[0]);

        gameFrame.pack();
        initFontInfo();
        gameFrame.setSize(dim);
        //change the name of the game-frame to your game name
        gameFrame.setTitle("Game Base");
        gameFrame.setResizable(false);
        gameFrame.setVisible(true);
        setFocusable(true);
    }


    // ==============================================================
    // METHODS
    // ==============================================================

    private void drawSunnyStatus(final Graphics graphics){

        graphics.setColor(Color.white);
        graphics.setFont(fontNormal);
        final int OFFSET_LEFT = 220;


        //draw the level upper-right corner
        String levelText = "Level : [" + CommandCenter.getInstance().getLevel() + "]  " +
        CommandCenter.getInstance().getUniverse().toString().replace('_', ' ');
        graphics.drawString(levelText, Game.DIM.width - OFFSET_LEFT, fontHeight); //upper-right corner
        graphics.drawString("Score : " + decimalFormat.format(CommandCenter.getInstance().getScore()),
                Game.DIM.width - OFFSET_LEFT,
                fontHeight * 2);

        //build the status string array with possible messages in middle of screen
        List<String> statusArray = new ArrayList<>();
        if (CommandCenter.getInstance().getThousandSunny().getShowLevel() > 0) statusArray.add(levelText);
        if (CommandCenter.getInstance().getThousandSunny().isMaxSpeedAttained()) statusArray.add("WARNING - SLOW DOWN");
        if (CommandCenter.getInstance().getThousandSunny().getNukeCount() > 0)

            //draw the statusArray strings to middle of screen
        if (!statusArray.isEmpty())
            displayTextOnScreen(graphics, statusArray.toArray(new String[0]));
    }

    //this is used for development, you can remove it from your final game
    private void drawNumFrame(Graphics g) {
        g.setColor(Color.white);
        g.setFont(fontNormal);
        g.drawString("FRAME[JAVA]:" + CommandCenter.getInstance().getFrame(), fontWidth,
                Game.DIM.height  - (fontHeight + 22));

    }

    private void drawMeters(Graphics g) {
        //will be a number between 0-100 inclusive
        int shieldValue = CommandCenter.getInstance().getThousandSunny().getShield() / 2;
        int nukeValue = CommandCenter.getInstance().getThousandSunny().getNukeCount() * 33; // Scale to make it visible
        int healthValue = CommandCenter.getInstance().getThousandSunny().getHealth();

        // Draw health bar with larger size
        drawHealthMeter(g, healthValue);
        
        // Draw other meters normally
        drawOneMeter(g, Color.CYAN, 1, shieldValue);
        drawOneMeter(g, Color.YELLOW, 2, nukeValue);
    }


// ===============================================================
/// Draw the health bar
    private void drawHealthMeter(Graphics g, int currentHealth) {
        int width = 200;  // Bar width
        int height = 20;  // Bar height
        
        // Center horizontally, place at top with some padding
        int xVal = (Game.DIM.width - width) / 2;
        int yVal = 20;
        
        // Draw background
        g.setColor(Color.DARK_GRAY);
        g.fillRect(xVal, yVal, width, height);

        // Calculate health percentage based on MAX_HEALTH
        double healthPercentage = (double) currentHealth / ThousandSunny.MAX_HEALTH;
        int fillWidth = (int) (width * healthPercentage);
        
        // Draw the filled portion (health)
        g.setColor(new Color(34, 177, 76));  // Green color
        g.fillRect(xVal, yVal, fillWidth, height);

        // Draw the border
        g.setColor(Color.WHITE);
        g.drawRect(xVal, yVal, width, height);

        // Draw the health text (show actual values instead of percentage)
        g.setColor(Color.WHITE);
        String healthText = currentHealth + "/" + ThousandSunny.MAX_HEALTH;
        int textX = xVal + width/2 - 20;  // Adjusted for potentially longer text
        int textY = yVal + height - 5;
        g.drawString(healthText, textX, textY);
    }



/// ===============================================================
/// 
    private void drawOneMeter(Graphics g, Color color, int offSet, int percent) {

        int xVal = Game.DIM.width - (100 + 120 * offSet);
        int yVal = Game.DIM.height - 45;

        //draw meter
        g.setColor(color);
        g.fillRect(xVal, yVal, percent, 10);

        //draw gray box
        g.setColor(Color.DARK_GRAY);
        g.drawRect(xVal, yVal, 100, 10);
    }


    @Override
    public void update(Graphics g) {
        // Initialize off-screen buffer
        imgOff = createImage(Game.DIM.width, Game.DIM.height);
        grpOff = imgOff.getGraphics();
    
        // Draw background
        if (backgroundImage != null) {
            grpOff.drawImage(backgroundImage, 0, 0, Game.DIM.width, Game.DIM.height, this);
        } else {
            System.err.println("Failed to load");
            grpOff.setColor(Color.BLACK);
            grpOff.fillRect(0, 0, Game.DIM.width, Game.DIM.height);
        }        
    
        // Handle cinematic display
        if (showCinematic) {
            drawCinematic(grpOff);
            g.drawImage(imgOff, 0, 0, this);
            return;
        }
    
        // Handle game states
        if (CommandCenter.getInstance().isGameOver()) {
            displayTextOnScreen(grpOff,
                    "WELCOME TO ONE PIECE: THOUSAND SUNNY",
                    "use the arrow keys to turn and thrust",
                    "use the space bar to fire",
                    "'S' to Start",
                    "'P' to Pause",
                    "'Q' to Quit",
                    "'M' to toggle music",
                    "'A' to toggle radar"
            );
        } else if (CommandCenter.getInstance().isPaused()) {
            displayTextOnScreen(grpOff, "Game Paused");
        } else {
            // Normal gameplay
            moveDrawMovables(grpOff,
                    CommandCenter.getInstance().getMovDebris(),
                    CommandCenter.getInstance().getMovFloaters(),
                    CommandCenter.getInstance().getMovFoes(),
                    CommandCenter.getInstance().getMovFriends());
    
            drawNumberShipsRemaining(grpOff);
            drawMeters(grpOff);
            drawSunnyStatus(grpOff);
        }

        // Draw nuke indicator
        drawNukeIndicator(grpOff);

    
        // Copy the offscreen image to the screen
        g.drawImage(imgOff, 0, 0, this);
    }


    //this method causes all sprites to move and draw themselves. This method takes a variable number of teams.
    @SafeVarargs
    private final void moveDrawMovables(final Graphics g, LinkedList<Movable>... teams) {

        for (LinkedList<Movable> team : teams) {
            for (Movable mov : team) {
                mov.move();
                mov.draw(g);
            }
        }

    }


// ===============================================================
/// DRAW THE NUMBER OF LIFES REMAINING
    private void drawNumberShipsRemaining(Graphics g) {
        int numSunny = CommandCenter.getInstance().getNumSunny();
        final int HEART_SIZE = 35;  // Size of each heart
        final int SPACING = 35;     // Space between hearts
        final int Y_POS = Game.DIM.height - 75;
        
        // Draw hearts for remaining lives (subtract 1 because current life isn't counted)
        for (int i = 1; i < numSunny; i++) {
            int x = Game.DIM.width - (SPACING * i);
            if (opeOpeImage != null) {
                g.drawImage(opeOpeImage, x - HEART_SIZE, Y_POS, HEART_SIZE, HEART_SIZE, null);
            }
        }
    }




    private void initFontInfo() {
        Graphics g = getGraphics();            // get the graphics context for the panel
        g.setFont(fontNormal);                        // take care of some simple font stuff
        fontMetrics = g.getFontMetrics();
        fontWidth = fontMetrics.getMaxAdvance();
        fontHeight = fontMetrics.getHeight();
        g.setFont(fontBig);                    // set font info
    }




    private void displayTextOnScreen(final Graphics graphics, String... lines) {

        //AtomicInteger is safe to pass into a stream
        final AtomicInteger spacer = new AtomicInteger(0);
        Arrays.stream(lines)
                .forEach(str ->
                            graphics.drawString(str, (Game.DIM.width - fontMetrics.stringWidth(str)) / 2,
                                    Game.DIM.height / 4 + fontHeight + spacer.getAndAdd(40))

                );


    }


// ===============================================================
// PASS TO A NEW LEVEL: CHANGE BG
    public void setBackgroundImage(String imagePath) {
        try {
            // Try to load the image using getResourceAsStream first
            var imageStream = getClass().getResourceAsStream(imagePath);
            if (imageStream != null) {
                backgroundImage = ImageIO.read(imageStream);
                System.out.println("Background image loaded successfully: " + imagePath);
            } else {
                System.err.println("Could not find background image: " + imagePath);
                // Set a default color background as fallback
                backgroundImage = new BufferedImage(Game.DIM.width, Game.DIM.height, BufferedImage.TYPE_INT_RGB);
                Graphics2D g2d = (Graphics2D) backgroundImage.getGraphics();
                g2d.setColor(Color.BLACK);
                g2d.fillRect(0, 0, Game.DIM.width, Game.DIM.height);
                g2d.dispose();
            }
        } catch (IOException e) {
            System.err.println("Error loading background image: " + imagePath);
            e.printStackTrace();
            // Create a default black background
            backgroundImage = new BufferedImage(Game.DIM.width, Game.DIM.height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = (Graphics2D) backgroundImage.getGraphics();
            g2d.setColor(Color.BLACK);
            g2d.fillRect(0, 0, Game.DIM.width, Game.DIM.height);
            g2d.dispose();
        }
    }


// ===============================================================
// Luffy ball indicators (top-right corner)
    private void drawNukeIndicator(Graphics g) {
        int nukeCount = CommandCenter.getInstance().getThousandSunny().getNukeCount();
    
        // Positioning variables
        final int SQUARE_SIZE = 40;
        final int SPACING = 20;
        final int START_X = Game.DIM.width - ((SQUARE_SIZE + SPACING) + 140) - SPACING; // dinstance form the right edge
        final int Y_POS = 40; // Distance from the top edge
    
        // Loop to draw three squares
        for (int i = 0; i < 3; i++) {
            int x = START_X + i * (SQUARE_SIZE + SPACING);
    
            // Draw white square with transparent interior
            g.setColor(Color.WHITE);
            g.drawRect(x, Y_POS, SQUARE_SIZE, SQUARE_SIZE);
    
            // If we have a nuke at this slot, draw the Luffy Ball image
            if (i < nukeCount && luffyBallImage != null) {
                g.drawImage(luffyBallImage, x + 2, Y_POS + 2, SQUARE_SIZE - 4, SQUARE_SIZE - 4, null);
            }
        }
    }


    // ===============================================================
    // CINEMATIC DISPLAY

    // showing image for new level (allowing for gif)
    public void showCinematic(String imagePath, String message) {
        try {
            // Use getResourceAsStream instead of ImageLoader
            var imageStream = getClass().getResourceAsStream(imagePath);
            if (imageStream != null) {
                Image image = ImageIO.read(imageStream);
                cinematicImage = new ImageIcon(image);
                cinematicText = message;
                showCinematic = true;
                System.out.println("Cinematic image loaded successfully: " + imagePath);
            } else {
                System.err.println("Cinematic image not found: " + imagePath);
                showCinematic = false;
            }
        } catch (Exception e) {
            System.err.println("Error loading cinematic image: " + imagePath);
            e.printStackTrace();
            showCinematic = false;
        }
    }

    // reapint only when the cinematic is shown
    public void hideCinematic() {
        showCinematic = false;
        cinematicImage = null;  // Clear the cinematic image
        cinematicText = null;  // Clear the message
        repaint();  // Trigger re-rendering
    }
    

    private void drawCinematic(Graphics g) {
        if (!showCinematic || cinematicImage == null) {
            showCinematic = false;
            return;
        }
    
        try {
            // Draw semi-transparent black background
            g.setColor(new Color(0, 0, 0, 120));
            g.fillRect(0, 0, Game.DIM.width, Game.DIM.height);
    
            // Get image dimensions safely
            int originalWidth = cinematicImage.getIconWidth();
            int originalHeight = cinematicImage.getIconHeight();
    
            if (originalWidth <= 0 || originalHeight <= 0) {
                System.err.println("Invalid cinematic image dimensions");
                showCinematic = false;
                return;
            }
    
            // Calculate scaled dimensions
            int maxWidth = (int)(Game.DIM.width * 0.4);
            int maxHeight = (int)(Game.DIM.height * 0.4);
            double scale = Math.min((double)maxWidth/originalWidth, (double)maxHeight/originalHeight);
            int scaledWidth = (int)(originalWidth * scale);
            int scaledHeight = (int)(originalHeight * scale);
    
            // Center the image
            int x = (Game.DIM.width - scaledWidth) / 2;
            int y = (Game.DIM.height - scaledHeight) / 3;
    
            // Draw scaled image
            g.drawImage(cinematicImage.getImage(), x, y, scaledWidth, scaledHeight, null);
    
            // Draw message if available
            if (cinematicText != null) {
                g.setFont(fontBig);
                FontMetrics fm = g.getFontMetrics();
                int msgWidth = fm.stringWidth(cinematicText);
                int msgX = (Game.DIM.width - msgWidth) / 2;
                int msgY = y + scaledHeight + 50;
    
                g.setColor(Color.BLACK);
                g.drawString(cinematicText, msgX - 1, msgY - 1);
                g.drawString(cinematicText, msgX - 1, msgY + 1);
                g.drawString(cinematicText, msgX + 1, msgY - 1);
                g.drawString(cinematicText, msgX + 1, msgY + 1);
    
                g.setColor(Color.YELLOW);
                g.drawString(cinematicText, msgX, msgY);
            }
        } catch (Exception e) {
            System.err.println("Error drawing cinematic: " + e.getMessage());
            showCinematic = false;
        }
    }
}

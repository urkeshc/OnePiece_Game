package mvc.model;

import mvc.controller.Game;
import java.awt.*;

public abstract class FoeBoss extends Sprite {
    
    protected int maxHealth;
    protected int currentHealth;
    protected boolean isActive;
    protected final int BOSS_RADIUS = 80;  // Larger than regular enemies

    public FoeBoss(int health) {
        setTeam(Team.FOE);
        this.maxHealth = health;
        this.currentHealth = health;
        this.isActive = true;
        setRadius(BOSS_RADIUS);
        
        // Start at top of screen, random x position
        setCenter(new Point(
            Game.R.nextInt(Game.DIM.width - BOSS_RADIUS * 2) + BOSS_RADIUS,
            BOSS_RADIUS
        ));
    }

    // Add getter for currentHealth
    public int getCurrentHealth() {
        return currentHealth;
    }

    public void damage(int amount) {
        currentHealth -= amount;
        if (currentHealth <= 0) {
            isActive = false;
        }
    }

    // Draw health bar above boss
    protected void drawHealthBar(Graphics g) {
        int barWidth = 100;
        int barHeight = 10;
        int x = getCenter().x - barWidth/2;
        int y = getCenter().y - getRadius() - 20;  // 20 pixels above boss

        // Background (empty health)
        g.setColor(Color.RED);
        g.fillRect(x, y, barWidth, barHeight);

        // Foreground (current health)
        g.setColor(Color.GREEN);
        int currentWidth = (int)((currentHealth / (double)maxHealth) * barWidth);
        g.fillRect(x, y, currentWidth, barHeight);

        // Border
        g.setColor(Color.WHITE);
        g.drawRect(x, y, barWidth, barHeight);
    }

    // Override draw from Sprite, not calling super.draw() directly
    @Override
    public void draw(Graphics g) {
        // Draw the boss
        renderVector(g);  // This is from Sprite class
        // Draw the health bar on top
        drawHealthBar(g);
    }
}

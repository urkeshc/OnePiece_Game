package mvc.model;

import mvc.controller.CommandCenter;
import mvc.controller.Game;
import mvc.controller.ImageLoader;
import lombok.Data;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.LinkedList;

//Sprite has a lot of bloat that we don't need to simply render a star field.
//This class demonstrates how we can use the Movable interface without extending Sprite.
@Data
public class BackgroundObjects implements Movable {

    private Point center;
    private BufferedImage bubbleImage;
    private int size;

    public BackgroundObjects() {
        // Position aléatoire dans l'espace du jeu
        center = new Point(Game.R.nextInt(Game.DIM.width), Game.R.nextInt(Game.DIM.height));

        // Get the current level from CommandCenter
        int level = CommandCenter.getInstance().getLevel();

        // Load different images based on the level
        switch (level) {
            case 1:
                bubbleImage = ImageLoader.getImage("backgrounds/grand_line_island.png");
                break;
            case 3:
                bubbleImage = ImageLoader.getImage("backgrounds/skypiea_cloud.png");
                break;
            default:
                bubbleImage = ImageLoader.getImage("backgrounds/sabaody_bubbles.png");
                break;
        }

        // Vérifier si l'image est chargée correctement
        if (bubbleImage == null) {
            System.err.println("Échec du chargement de l'image des bulles.");
        }

        size = 15;
    }

    //The following methods are contract methods from Movable. We need all of them to satisfy the contract.
    @Override
    public void draw(Graphics g) {
        if (bubbleImage == null) return;

        // Redimensionner l'image si nécessaire
        Image scaledImage = bubbleImage.getScaledInstance(size, size, Image.SCALE_SMOOTH);

        // Dessiner l'image de la bulle centrée sur le point
        int drawX = center.x - size / 2;
        int drawY = center.y - size / 2;
        g.drawImage(scaledImage, drawX, drawY, null);
    }

    @Override
    public Point getCenter() {
        return center;
    }

    @Override
    public int getRadius() {
        return size / 2;
    }

    @Override
    public Team getTeam() {
        return Team.DEBRIS;
    }




    @Override
    public void move() {
        // Si la position du falcon n'est pas fixée, ne rien faire
        if (!CommandCenter.getInstance().isSunnyPositionFixed()) return;

        // Déplacer la bulle en fonction du mouvement du falcon
        center.x = (int) Math.round(center.x - CommandCenter.getInstance().getThousandSunny().getDeltaX());
        center.y = (int) Math.round(center.y - CommandCenter.getInstance().getThousandSunny().getDeltaY());

        // Gestion des limites de l'écran avec réapparition de l'autre côté
        if (center.x > Game.DIM.width) {
            center.x = 0;
        } else if (center.x < 0) {
            center.x = Game.DIM.width;
        }

        if (center.y > Game.DIM.height) {
            center.y = 0;
        } else if (center.y < 0) {
            center.y = Game.DIM.height;
        }

    }


    @Override
    public void addToGame(LinkedList<Movable> list) {
        list.add(this);
    }

    @Override
    public void removeFromGame(LinkedList<Movable> list) {
       list.remove(this);
    }



}

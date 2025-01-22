package mvc.model;



import mvc.controller.CommandCenter;
import mvc.controller.Game;
import mvc.model.prime.AspectRatio;

import java.awt.*;

public class MiniMap extends Sprite {


    //size of mini-map as percentage of screen (game dimension)
    private final double MINI_MAP_PERCENT = 0.31;

    //used to adjust non-square universes. Set in draw()
    private AspectRatio aspectRatio;

    private final Color PUMPKIN = new Color(200, 100, 50);
    private final Color LIGHT_GRAY = new Color(200, 200, 200);

    public MiniMap() {
        setTeam(Team.DEBRIS);
        setCenter(new Point(0,0));
    }

    @Override
    public void move() {}


    @Override
    public void draw(Graphics g) {

        //controlled by the A-key
        if (!CommandCenter.getInstance().isRadar()) return;

        //get the aspect-ratio which is used to adjust for non-square universes
        aspectRatio = aspectAdjustedRatio(CommandCenter.getInstance().getUniDim());

        //scale to some percent of game-dim
        int miniWidth = (int) Math.round( MINI_MAP_PERCENT * Game.DIM.width * aspectRatio.getWidth());
        int miniHeight = (int) Math.round(MINI_MAP_PERCENT * Game.DIM.height * aspectRatio.getHeight());

        //gray bounding box (entire universe)
        g.setColor(Color.DARK_GRAY);
        g.drawRect(
                0,
                0,
                miniWidth,
                miniHeight
        );


        //draw the view-portal box
        g.setColor(Color.DARK_GRAY);
        int miniViewPortWidth = miniWidth / CommandCenter.getInstance().getUniDim().width;
        int miniViewPortHeight = miniHeight / CommandCenter.getInstance().getUniDim().height;
        g.drawRect(
                0 ,
                0,
                miniViewPortWidth,
                miniViewPortHeight

        );


        //draw debris radar-blips.
        CommandCenter.getInstance().getMovDebris().forEach( mov -> {
                    g.setColor(Color.DARK_GRAY);
                    Point translatedPoint = translatePoint(mov.getCenter());
                    g.fillOval(translatedPoint.x - 1, translatedPoint.y - 1, 2, 2);
                }
        );

        CommandCenter.getInstance().getMovFoes().forEach( mov -> {
                    if (!(mov instanceof Rocks)) return;
                    Rocks rocks = (Rocks) mov;
                    g.setColor(LIGHT_GRAY);
                    Point translatedPoint = translatePoint(rocks.getCenter());
                    switch (rocks.getSize()){
                        //large
                        case 0:
                            g.fillOval(translatedPoint.x - 3, translatedPoint.y - 3, 6, 6);
                            break;
                        //med
                        case 1:
                            g.drawOval(translatedPoint.x - 3, translatedPoint.y - 3, 6, 6);
                            break;
                        //small
                        case 2:
                        default:
                            g.drawOval(translatedPoint.x - 2, translatedPoint.y - 2, 4, 4);
                    }
                }
        );


        //draw floater radar-blips
        CommandCenter.getInstance().getMovFloaters().forEach( mov -> {
                    g.setColor(mov instanceof StrawHat ? Color.YELLOW : Color.CYAN);
                    Point translatedPoint = translatePoint(mov.getCenter());
                    g.fillRect(translatedPoint.x - 2, translatedPoint.y - 2, 4, 4);
                }
        );



        //draw friend radar-blips
        CommandCenter.getInstance().getMovFriends().forEach( mov -> {
                    Color color;
                    if (mov instanceof ThousandSunny && CommandCenter.getInstance().getThousandSunny().getShield() > 0)
                        color = Color.CYAN;
                    else if (mov instanceof LuffyBall)
                        color = Color.YELLOW;
                    else
                        color = PUMPKIN;
                    g.setColor(color);
                    Point translatedPoint = translatePoint(mov.getCenter());
                    g.fillOval(translatedPoint.x - 2, translatedPoint.y - 2, 4, 4);
                }
        );


    }

    private Point translatePoint(Point point){
        return new Point(
                (int) Math.round( MINI_MAP_PERCENT  * point.x / CommandCenter.getInstance().getUniDim().width * aspectRatio.getWidth()),
                (int) Math.round( MINI_MAP_PERCENT  * point.y / CommandCenter.getInstance().getUniDim().height * aspectRatio.getHeight())
        );
    }


    //the purpose of this method is to adjust the aspect of non-square universes
    private AspectRatio aspectAdjustedRatio(Dimension universeDim){
        if (universeDim.width == universeDim.height){
            return new AspectRatio(1.0, 1.0);
        }
        else if (universeDim.width > universeDim.height){
            double wMultiple = (double) universeDim.width / universeDim.height;
            return new AspectRatio(wMultiple, 1.0).scale(0.5);
        }
        //universeDim.width < universeDim.height
        else {
            double hMultiple = (double) universeDim.height / universeDim.width;
            return new AspectRatio(1.0, hMultiple).scale(0.5);
        }

    }



}

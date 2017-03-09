package hil;
import java.awt.Color;  
import java.awt.Graphics;  
import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import java.util.HashMap;  
  
public class AnoHilbert {  
      
    public static final int WHEELSIZE = 1024;  
      
    // four edges  
    public static final int NORTH = 0;  
    public static final int EAST = 90;  
    public static final int SOUTH = 180;  
    public static final int WEST = 270;  
  
    // four corners  
    public static final int NE = 45;  
    public static final int SE = 135;  
    public static final int SW = 225;  
    public static final int NW = 315;  
      
    // attributes  
    private Point2D.Double location;  
    private int count;
    private static double mapWidth;
    private static double mapHeight;
    private HashMap<Integer,Point2D.Double> map;
  
    public AnoHilbert() {  
        count = 0;
        
    }  
  
    public void process( int level, double width, double height) {  
        this.location = null;  
        map = new HashMap<Integer,Point2D.Double>();
        if(level > 32 )   
        {  
            System.out.println("could get max depth is 32!");
            return;  
        }  
        mapWidth = width;
        mapHeight = height;
        hilbert( level, 0, 0, width, height, 0, 225);  
    }  
  
    public void hilbert( int depth, double startx, double starty, double width, double height, int startgray, int endgray) {  
        double centerX = width / 2;  
        double centerY = height / 2;  
        if (depth == 0) {   
            this.location = new Point2D.Double(startx + centerX, mapHeight-(starty + centerY));  
            map.put(count, location);
            count++;
            return;  
        }  
        switch (startgray) {  
        case 0:  
            if (endgray == 225) {  
                hilbert( depth - 1, startx, starty + centerY, centerX, centerY, 90, 225); // bottom-left  
                hilbert( depth - 1, startx, starty, centerX, centerY, 0, 225); // upper-left  
                hilbert( depth - 1, startx + centerX, starty, centerX, centerY, 0, 225); // upper-right  
                hilbert( depth - 1, startx + centerX, starty + centerY, centerX, centerY, 270, 45); // bottom-right  
                return;  
            }  
  
            if (endgray != 135)  
                return;  
            hilbert( depth - 1, startx + centerX, starty + centerY, centerX, centerY, 270, 135);  
            hilbert( depth - 1, startx + centerX, starty, centerX, centerY, 0, 135);  
            hilbert( depth - 1, startx, starty, centerX, centerY, 0, 135);  
            hilbert( depth - 1, startx, starty + centerY, centerX, centerY, 90, 315);  
  
            return;  
        case 90:  
            if (endgray == 315) {  
                hilbert( depth - 1, startx, starty, centerX, centerY, 180, 315);  
                hilbert( depth - 1, startx + centerX, starty,centerX, centerY, 90, 315);  
                hilbert( depth - 1, startx + centerX, starty + centerY, centerX, centerY, 90, 315);  
                hilbert( depth - 1, startx, starty + centerY,centerX, centerY, 0, 135);  
  
                return;  
            }  
  
            if (endgray != 225)  
                return;  
            hilbert( depth - 1, startx, starty + centerY, centerX, centerY, 0, 225);  
            hilbert( depth - 1, startx + centerX, starty + centerY, centerX, centerY, 90, 225);  
            hilbert( depth - 1, startx + centerX, starty, centerX,  centerY, 90, 225);  
            hilbert( depth - 1, startx, starty, centerX, centerY, 180, 45);  
  
            return;  
        case 180:  
            if (endgray == 45) {  
                hilbert( depth - 1, startx + centerX, starty, centerX, centerY, 270, 45);  
                hilbert( depth - 1, startx + centerX, starty  + centerY, centerX, centerY, 180, 45);  
                hilbert( depth - 1, startx, starty + centerY,centerX, centerY, 180, 45);  
                hilbert( depth - 1, startx, starty, centerX,  centerY, 90, 225);  
  
                return;  
            }  
  
            if (endgray != 315)  
                return;  
            hilbert( depth - 1, startx, starty, centerX, centerY, 90, 315);  
            hilbert( depth - 1, startx, starty + centerY, centerX, centerY, 180, 315);  
            hilbert( depth - 1, startx + centerX, starty + centerY,centerX, centerY, 180, 315);  
            hilbert( depth - 1, startx + centerX, starty, centerX, centerY, 270, 135);  
            return;  
        case 270:  
            if (endgray == 45) {  
                hilbert( depth - 1, startx + centerX, starty, centerX, centerY, 180, 45);  
                hilbert( depth - 1, startx, starty, centerX, centerY, 270, 45);  
                hilbert( depth - 1, startx, starty + centerY, centerX, centerY, 270, 45);  
                hilbert( depth - 1, startx + centerX, starty + centerY, centerX, centerY, 0, 225);  
                return;  
            }  
  
            if (endgray != 135)  
                return;  
            hilbert( depth - 1, startx + centerX, starty + centerY,centerX, centerY, 0, 135);  
            hilbert( depth - 1, startx, starty + centerY, centerX, centerY, 270, 135);  
            hilbert( depth - 1, startx, starty, centerX, centerY, 270, 135);  
            hilbert( depth - 1, startx + centerX, starty, centerX, centerY, 180, 315);  
  
            return;  
        }    
    }

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public HashMap<Integer, Double> getMap() {
		return map;
	}


}  
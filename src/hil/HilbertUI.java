package hil;
import java.awt.BorderLayout;  
import java.awt.Color;  
import java.awt.Dimension;  
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseEvent;  
import java.awt.event.MouseListener;
import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.JComponent;  
import javax.swing.JFrame;  
  
public class HilbertUI {  

    public static void main(String args[]){
    	double width=1,height=1;
    	int level;
    	int N = 2;
    	level =(int)( Math.log(N)/Math.log(2));
    	System.out.println(level);
    	AnoHilbert hilbert = new AnoHilbert();
    	hilbert.process(level, width, height);  
    	
    	//test
    	HashMap<Integer,Point2D.Double> map = hilbert.getMap();
        Iterator it = map.entrySet().iterator();
        while(it.hasNext()){
        	Map.Entry<Integer, Point2D.Double> entry = (Map.Entry<Integer, Point2D.Double>)it.next();
        	int hil = entry.getKey();
        	Point2D.Double p = entry.getValue();
        	System.out.println(hil+" "+p);
        }
    }
    public static HashMap<Integer,Point2D.Double> fillWithHilCur(int N,double width,double height){
    	int level;
    	level =(int)( Math.log(N)/Math.log(2));
    	System.out.println(level);
    	AnoHilbert hilbert = new AnoHilbert();
    	hilbert.process(level, width, height);  
        return hilbert.getMap();
    }
}  

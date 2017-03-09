package hil;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.util.HashMap;

public class Hilbert {

	/**
	 * P(x,y) to hilbert value.
	 * @param x is in(0,1)
	 * @param y is in(0,1)
	 * @param n the num of hilbert region in x direction or y direction (must be the power of 2)
	 * @return
	 */
	public static int coordToHil(double x,double y,int n){
		int xtoInt,ytoInt;
		int s;
		int d=0;
		xtoInt = (int)((x-0.5/n)*n);
		ytoInt = (int)((y-0.5/n)*n);
		long rx,ry;
		for(s=n/2;s>0;s/=2){
			rx = (xtoInt & s)>0?1:0;
			ry = (ytoInt & s)>0?1:0;
			d += s*s*((3*rx)^ry);
			 if(ry ==0 ){
				 if(rx==1){
					 xtoInt = s-1-xtoInt;
					 ytoInt = s-1-ytoInt;
				 }
				 int temp = xtoInt;
				 xtoInt = ytoInt;
				 ytoInt = temp;
				 
			 }
		}
		return d;
	}
	
	//convert hilbert value to coordinates (x,y) x,y is in [0,1]
	public static double[] hilToCoord(long hil,int n){
		int s;
		long rx,ry,t=hil;
		int x=0,y=0;
		double coord[] = new double[2];
		for(s=1;s<n;s*=2){
			rx = 1&(t/2);
			ry = 1&(t^rx);
			if(ry ==0 ){
				 if(rx==1){
					 x = s-1-x;
					 y = s-1-y;
				 }
				 int temp = x;
				 x = y;
				 y = temp;
				 
			 }
			x += s*rx;
			y += s*ry;
			t/=4;
		}
		coord[0] = x/(n*1.0)+0.5/n;
		coord[1] = y/(n*1.0)+0.5/n;
		return coord;
	}

}

import java.awt.Polygon;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class IndexofVoronoi {
	private static Point site;
	private static ArrayList<String> vertices; //the vertices of voronoi sited on site
	private Point minBR[];//存储左下和右上点
	private HashSet<String> oneNN;//存储泰森多边形的1近邻
	private Polygon voronoi;
	private int[] xpoints;  //x axis of vertex on voronoi diagram,(int) initialValue*10000
	private int[] ypoints; //y axis of vertex on voronoi diagram
	static int verNum;
	public IndexofVoronoi(Point p){
		site = p;
		vertices = new ArrayList<String>();
		oneNN = new HashSet<String>();
		minBR = new Point[2];
	}
	public void addVertex(ArrayList<String> para){
		vertices.addAll(para);
		verNum = vertices.size();
		xpoints = new int[verNum];
		ypoints = new int[verNum];
		setBoundaryRectangle();
		generatePolygon();
//		StdDraw.setPenRadius(.001);
//		StdDraw.setPenColor(StdDraw.GREEN);
//		StdDraw.line(minBR[0].x,minBR[0].y,minBR[0].x,minBR[1].y);
//		StdDraw.line(minBR[0].x,minBR[0].y,minBR[1].x,minBR[0].y);
//		StdDraw.line(minBR[0].x,minBR[1].y,minBR[1].x,minBR[1].y);
//		StdDraw.line(minBR[1].x,minBR[1].y,minBR[1].x,minBR[0].y);
		
	}
	public void setBoundaryRectangle(){
		double xmin,ymin;
		double xmax,ymax;
		int size = vertices.size();
		xmin = xmax = Double.parseDouble(vertices.get(0).split(" ")[0]);
		ymin = ymax = Double.parseDouble(vertices.get(0).split(" ")[1]);
		for(int i=1;i<size;i++){
			double x_axis = Double.parseDouble(vertices.get(i).split(" ")[0]);
			double y_axis = Double.parseDouble(vertices.get(i).split(" ")[1]);
			if(xmin>x_axis){
				xmin = x_axis;
			}
			if(xmax<x_axis){
				xmax = x_axis;
			}
			if(ymin>y_axis){
				ymin = y_axis;
			}
			if(ymax<y_axis){
				ymax = y_axis;
			}
		}
		this.minBR[0] = new Point(xmin,ymin);
		this.minBR[1] = new Point(xmax,ymax);
	}
	
	public Point[] getMinBR() {
		return minBR;
	}
	public HashSet<String> getOneNN() {
		return oneNN;
	}
	public void addOneNearNeighbor(HashSet<String> para){
		oneNN .addAll(para) ;
	}
	
	//generate a java.awt.Polygon object
	private  void generatePolygon(){
		for(int i=0;i<verNum;i++){
			int x =(int)(Double.parseDouble(vertices.get(i).split(" ")[0])*10000);
			int y = (int)(Double.parseDouble(vertices.get(i).split(" ")[1])*10000);
			xpoints[i] = x;
			ypoints[i] = y;
		}
		voronoi = new Polygon(xpoints,ypoints,verNum);
	}
	
	//decide if a point is in voronoi boundary
	public boolean containsPoint(double x,double y){
			x = x*10000;
			y = y*10000;
			boolean found = voronoi.contains(x, y);
			return found;
	}
	
	//decide if a point P(x,y) is in minBR of voronoi
	public boolean mBRContains(double x,double y){
		boolean found = false;
		double xmin,xmax,ymin,ymax;
		xmin = minBR[0].x;
		ymin = minBR[0].y;
		xmax = minBR[1].x;
		ymax = minBR[1].y;
		if(x>=xmin&&x<=xmax&&y>=ymin&&y<=ymax){
			found = true;
		}
		return found;
	}
	
	public void drawVoronoi(){
		StdDraw.setPenRadius(.01);
		StdDraw.setPenColor(StdDraw.BLUE);
		StdDraw.point(site.x, site.y);
		int i=0;
		for(String p:vertices){
			StdDraw.setPenRadius(.03);
			StdDraw.setPenColor(StdDraw.RED);
			StdDraw.point(Double.parseDouble(p.split(" ")[0]), Double.parseDouble(p.split(" ")[1]));
		}
	} 
	public String toString(){
		String str="";
		str = "site:"+"("+site.x+","+site.y+")"+vertices.size()+vertices;
		return str;
	}
}

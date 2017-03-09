// the voronoi diagram (a set of edges) for a set of points (sites)

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Scanner;

public class Voronoi {
	
	List <Point> sites;
	List <Edge> edges; // edges on Voronoi diagram
	PriorityQueue<Event> events; // priority queue represents sweep line
	Parabola root; // binary search tree represents beach line
	static HashMap<String,ArrayList<Edge>> diagrams;
	static HashMap<String,HashSet<String>> verticesToSite;
	
	// size of StdDraw window
	double width = 1;
	double height = 1;
	
	double ycurr; // current y-coord of sweep line
	
	public Voronoi (List <Point> sites) {
		this.sites = sites;
		edges = new ArrayList<Edge>();
		diagrams = new HashMap<String,ArrayList<Edge>>();
		verticesToSite = new HashMap<String,HashSet<String>>();
		generateVoronoi();
	}
	
	private void generateVoronoi() {
		
		events = new PriorityQueue <Event>();
		for (Point p : sites) {
			events.add(new Event(p, Event.SITE_EVENT));
		}
		
		// process events (sweep line)
		int count = 0;
		while (!events.isEmpty()) {
			//System.out.println();
			Event e = events.remove();
			ycurr = e.p.y;
			count++;
			if (e.type == Event.SITE_EVENT) {
				//System.out.println(count + ". SITE_EVENT " + e.p);
				handleSite(e.p);
			}
			else {
				//System.out.println(count + ". CIRCLE_EVENT " + e.p);
				handleCircle(e);
			}
		}
		
		ycurr = width+height;
		
		endEdges(root); // close off any dangling edges
		
		// get rid of those crazy infinite lines
		for (Edge e: edges){
			if (e.neighbor != null) {
				e.start = e.neighbor.end;
				e.neighbor = null;
			}
		}
	}

	// end all unfinished edges
	private void endEdges(Parabola p) {
		if (p.type == Parabola.IS_FOCUS) {
			p = null;
			return;
		}

		double x = getXofEdge(p);
		p.edge.end = new Point (x, p.edge.slope*x+p.edge.yint);
		edges.add(p.edge);
		
		//add edges to correlate to site
		addEdgetoSite(p.edge.site_left, p.edge);
		addEdgetoSite(p.edge.site_right,p.edge);
		
		endEdges(p.child_left);
		endEdges(p.child_right);
		
		p = null;
	}

	// processes site event
	private void handleSite(Point p) {
		// base case
		if (root == null) {
			root = new Parabola(p);
			return;
		}
		
		// find parabola on beach line right above p
		Parabola par = getParabolaByX(p.x);
		if (par.event != null) {
			events.remove(par.event);
			par.event = null;
		}

		// create new dangling edge; bisects parabola focus and p
		Point start = new Point(p.x, getY(par.point, p.x));
		Edge el = new Edge(start, par.point, p);
		Edge er = new Edge(start, p, par.point);
		el.neighbor = er;
		er.neighbor = el;
		par.edge = el;
		par.type = Parabola.IS_VERTEX;
		
		// replace original parabola par with p0, p1, p2
		Parabola p0 = new Parabola (par.point);
		Parabola p1 = new Parabola (p);
		Parabola p2 = new Parabola (par.point);

		par.setLeftChild(p0);
		par.setRightChild(new Parabola());
		par.child_right.edge = er;
		par.child_right.setLeftChild(p1);
		par.child_right.setRightChild(p2);

		checkCircleEvent(p0);
		checkCircleEvent(p2);
	}
	
	// process circle event
	private void handleCircle(Event e) {
		
		// find p0, p1, p2 that generate this event from left to right
		Parabola p1 = e.arc;
		Parabola xl = Parabola.getLeftParent(p1);
		Parabola xr = Parabola.getRightParent(p1);
		Parabola p0 = Parabola.getLeftChild(xl);
		Parabola p2 = Parabola.getRightChild(xr);
		
		// remove associated events since the points will be altered
		if (p0.event != null) {
			events.remove(p0.event);
			p0.event = null;
		}
		if (p2.event != null) {
			events.remove(p2.event);
			p2.event = null;
		}
		
		Point p = new Point(e.p.x, getY(p1.point, e.p.x)); // new vertex
	
		// end edges!
		xl.edge.end = p;
		xr.edge.end = p;
		edges.add(xl.edge);
		edges.add(xr.edge);
		//attach edge to site
		addEdgetoSite(p0.point,xl.edge);
		addEdgetoSite(p1.point,xl.edge);
		addEdgetoSite(p1.point,xr.edge);
		addEdgetoSite(p2.point,xr.edge);		

		// start new bisector (edge) from this vertex on which ever original edge is higher in tree
		Parabola higher = new Parabola();
		Parabola par = p1;
		while (par != root) {
			par = par.parent;
			if (par == xl) higher = xl;
			if (par == xr) higher = xr;
		}
		higher.edge = new Edge(p, p0.point, p2.point);
		
		// delete p1 and parent (boundary edge) from beach line
		Parabola gparent = p1.parent.parent;
		if (p1.parent.child_left == p1) {
			if(gparent.child_left  == p1.parent) gparent.setLeftChild(p1.parent.child_right);
			if(gparent.child_right == p1.parent) gparent.setRightChild(p1.parent.child_right);
		}
		else {
			if(gparent.child_left  == p1.parent) gparent.setLeftChild(p1.parent.child_left);
			if(gparent.child_right == p1.parent) gparent.setRightChild(p1.parent.child_left);
		}

		Point op = p1.point;
		p1.parent = null;
		p1 = null;
		
		checkCircleEvent(p0);
		checkCircleEvent(p2);
	}
	
	// adds circle event if foci a, b, c lie on the same circle
	private void checkCircleEvent(Parabola b) {

		Parabola lp = Parabola.getLeftParent(b);
		Parabola rp = Parabola.getRightParent(b);

		if (lp == null || rp == null) return;
		
		Parabola a = Parabola.getLeftChild(lp);
		Parabola c = Parabola.getRightChild(rp);
	
		if (a == null || c == null || a.point == c.point) return;

		if (ccw(a.point,b.point,c.point) != 1) return;
		
		// edges will intersect to form a vertex for a circle event
		Point start = getEdgeIntersection(lp.edge, rp.edge);
		if (start == null) return;
		
		// compute radius
		double dx = b.point.x - start.x;
		double dy = b.point.y - start.y;
		double d = Math.sqrt((dx*dx) + (dy*dy));
		if (start.y + d < ycurr) return; // must be after sweep line

		Point ep = new Point(start.x, start.y + d);
		//System.out.println("added circle event "+ ep);

		// add circle event
		Event e = new Event (ep, Event.CIRCLE_EVENT);
		e.arc = b;
		b.event = e;
		events.add(e);
	}

	// first thing we learned in this class :P
	public int ccw(Point a, Point b, Point c) {
        double area2 = (b.x-a.x)*(c.y-a.y) - (b.y-a.y)*(c.x-a.x);
        if (area2 < 0) return -1;
        else if (area2 > 0) return 1;
        else return  0;
    }

	// returns intersection of the lines of with vectors a and b
	private Point getEdgeIntersection(Edge a, Edge b) {

		if (b.slope == a.slope && b.yint != a.yint) return null;

		double x = (b.yint - a.yint)/(a.slope - b.slope);
		double y = a.slope*x + a.yint;

		return new Point(x, y);
	} 
	
	// returns current x-coordinate of an unfinished edge
	private double getXofEdge (Parabola par) {
		//find intersection of two parabolas
		
		Parabola left = Parabola.getLeftChild(par);
		Parabola right = Parabola.getRightChild(par);
		
		Point p = left.point;
		Point r = right.point;
		
		double dp = 2*(p.y - ycurr);
		double a1 = 1/dp;
		double b1 = -2*p.x/dp;
		double c1 = (p.x*p.x + p.y*p.y - ycurr*ycurr)/dp;
		
		double dp2 = 2*(r.y - ycurr);
		double a2 = 1/dp2;
		double b2 = -2*r.x/dp2;
		double c2 = (r.x*r.x + r.y*r.y - ycurr*ycurr)/dp2;
		
		double a = a1-a2;
		double b = b1-b2;
		double c = c1-c2;
		
		double disc = b*b - 4*a*c;
		double x1 = (-b + Math.sqrt(disc))/(2*a);
		double x2 = (-b - Math.sqrt(disc))/(2*a);
		
		double ry;
		if (p.y > r.y) ry = Math.max(x1, x2);
		else ry = Math.min(x1, x2);
		
		return ry;
	}
	
	// returns parabola above this x coordinate in the beach line
	private Parabola getParabolaByX (double xx) {
		Parabola par = root;
		double x = 0;
		while (par.type == Parabola.IS_VERTEX) {
			x = getXofEdge(par);
			if (x>xx) par = par.child_left;
			else par = par.child_right;
		}
		return par;
	}
	
	// find corresponding y-coordinate to x on parabola with focus p
	private double getY(Point p, double x) {
		// determine equation for parabola around focus p
		double dp = 2*(p.y - ycurr);
		double a1 = 1/dp;
		double b1 = -2*p.x/dp;
		double c1 = (p.x*p.x + p.y*p.y - ycurr*ycurr)/dp;
		return (a1*x*x + b1*x + c1);
	}
	
	
	public static void addEdgetoSite(Point site,Edge e){
		String site_str = site.toString();
		boolean found = true;
		//存储voronoi的边
		if(diagrams.containsKey(site_str)){
			ArrayList<Edge> edges = diagrams.get(site_str);
			edges.add(e);
			
		}
		else{
			ArrayList<Edge> edges = new ArrayList<Edge>();
			edges.add(e);
			diagrams.put(site_str, edges);
		}
	}
	
	//draw point
	public static void drawPoint(Point p){
		StdDraw.setPenColor(StdDraw.MAGENTA);
		StdDraw.setPenRadius(.008);
		StdDraw.point(p.x, p.y);
	}
	//draw point
	public static void drawPoint(String x,String y){
		StdDraw.setPenColor(StdDraw.GREEN);
		StdDraw.setPenRadius(.02);
		StdDraw.point(Double.parseDouble(x), Double.parseDouble(y));
	}
	//draw Edge
	public static void drawEdge(Edge e){
		StdDraw.setPenColor(StdDraw.GREEN);
		StdDraw.setPenRadius(.001);
		StdDraw.line(e.start.x, e.start.y,e.end.x,e.end.y);
	}
	
	public static void drawPolygon(ArrayList<String> points){
		int size = points.size();
		double[] x = new double[size];
		double[] y = new double[size];
		StdDraw.setPenColor(StdDraw.BLUE);
		StdDraw.setPenRadius(.02);
		for(int i=0;i<size;i++){
			String str1 = points.get(i).split(" ")[0];
			String str2 = points.get(i).split(" ")[1];
			double xaxis = Double.parseDouble(str1);
			double yaxis = Double.parseDouble(str2);
			x[i] = xaxis;
			y[i] = yaxis;
			StdDraw.point(xaxis,yaxis );
		}
		StdDraw.setPenColor(StdDraw.GREEN);
		StdDraw.setPenRadius(.002);
		StdDraw.polygon(x, y);
	}
	
	//将泰森多边形的顶点按照圆周顺序存储
	private static ArrayList<String> generateVerClockwise(ArrayList<Edge> modifyResult){
		ArrayList<String> verArr = new ArrayList<String>();
		//add the vertex to ver_str in an order
		
		//decide the start point of  voronoi diagram (针对不闭合多边形)
		HashSet<String> middleVarforCount = new HashSet<String>();
		for(Edge e:modifyResult){
			String side1 = e.start.toString();
			String side2 = e.end.toString();
			if(middleVarforCount.contains(side1)){
				middleVarforCount.remove(side1);
			}
			else{
				middleVarforCount.add(side1);
			}
			if(middleVarforCount.contains(side2)){
				middleVarforCount.remove(side2);
			}
			else{
				middleVarforCount.add(side2);
			}
		}
		String startPoint;
		if(middleVarforCount.isEmpty()){
			startPoint = modifyResult.get(0).start.toString();
			verArr.add(startPoint);
//			drawEdge(modifyResult.get(0));
			modifyResult.remove(0);
		}
		else{
			startPoint = (String)middleVarforCount.toArray()[0];
			verArr.add(startPoint);
		}
		int num = modifyResult.size();
		boolean whetherChangePoint = true;
		for(int k=0;k<num;k++){
			Edge temp = modifyResult.get(k);
//			drawEdge(temp);
			String tempS = temp.start.toString();
			String tempE = temp.end.toString();
			if(tempS.equals(startPoint)){
				verArr.add(tempE);
				startPoint = tempE;
				modifyResult.remove(k);
				k = -1;
				num = modifyResult.size();
				whetherChangePoint = false;
			}
			else if(tempE.equals(startPoint)){
				verArr.add(tempS);
				startPoint = tempS;
				modifyResult.remove(k);
				k = -1;
				num = modifyResult.size();
				whetherChangePoint = false;
			}
		}
//		drawPolygon(verArr);
		return verArr;
	}
	
	//generate site and its voronoi diagram with minBR
	public static HashMap<String,IndexofVoronoi> generateVorIndex(String path) throws FileNotFoundException{
		HashMap<String,IndexofVoronoi> allVorDiag = new HashMap<String,IndexofVoronoi>();
		//read points from file path
		ArrayList<Point> points = new ArrayList<Point>();
		//read coordinates of sites from filepath
		Scanner scan = new Scanner(new File(path));
		while(scan.hasNextLine()){
			String str = scan.nextLine();
			double x = Double.parseDouble(str.split(",")[0]);
			double y = Double.parseDouble(str.split(",")[1]);
			points.add(new Point(x, y));
		}
		//run main program,initialize the edges of voronoi diagram
		Voronoi diagram = new Voronoi (points);
		
		//output the edges of each voronoi at site
		Iterator<Entry<String, ArrayList<Edge>>> it = diagrams.entrySet().iterator();
		int i=0;
		while(it.hasNext()){
			Map.Entry<String, ArrayList<Edge>> entry = (Map.Entry<String, ArrayList<Edge>>)it.next();
			String key = entry.getKey();
			ArrayList<Edge> value = entry.getValue();
			ArrayList<Edge> modifyResult = new ArrayList<Edge>();
			ArrayList<String> verArr = new ArrayList<String>();
			HashSet<String> neighbors = new HashSet<String>();
//			//for test
//			if(i==7){
//				drawPoint(key.split(" ")[0], key.split(" ")[1]);
//				System.out.println(value.size()+" "+value);
//			}
			for(Edge e:value){
				
//				//test
//				if(i==7){
//					StdDraw.setPenColor(StdDraw.BLACK);
//					StdDraw.setPenRadius(.002);
//					StdDraw.line(e.start.x, e.start.y, e.end.x, e.end.y);
//				}
				
				//store the 1-near-neighbor sites of site 'key'
				if(!e.site_left.toString().equals(key)){
					neighbors.add(e.site_left.toString());
				}
				if(!e.site_right.toString().equals(key)){
					neighbors.add(e.site_right.toString());
				}
				
				//modify edges ,delete same edge with opposite direction
				boolean found = true;
				
				for(int j=0;j<modifyResult.size();j++){
					if(modifyResult.get(j).isNeighbor(e)){
						found = false;
					
						break;
					}
				}
				if(found){
					modifyResult.add(e);
				}
			}
			//construct IndexofVoronoi object and push to allVorDiag
			verArr = generateVerClockwise(modifyResult);
			IndexofVoronoi polygon = new IndexofVoronoi(new Point(Double.parseDouble(key.split(" ")[0]),Double.parseDouble(key.split(" ")[1])));
			polygon.addVertex(verArr);
			polygon.addOneNearNeighbor(neighbors);
			allVorDiag.put(key, polygon);
//			//for test
//			i++;
		}	
		
		
		//draw all sites
		StdDraw.setPenColor(StdDraw.BLUE);
		StdDraw.setPenRadius(.015);
		for (Point p: points) {
			StdDraw.point(p.x, p.y);
		}
		
		//draw all edges
		StdDraw.setPenColor(StdDraw.BLACK);
		StdDraw.setPenRadius(.0009);
		for (Edge e: diagram.edges) {
			StdDraw.line(e.start.x, e.start.y, e.end.x, e.end.y);
		}

		return allVorDiag;
	}
	public static void main(String[] args) throws FileNotFoundException{
		
		//construct edges neighbors and vertices in a order in all sites
		HashMap<String, IndexofVoronoi> allVorDiag = generateVorIndex("src/sites3");

	}
}

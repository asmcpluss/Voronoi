import java.awt.Graphics;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import BPlusTree.*;
import hil.AnoHilbert;
import hil.Hilbert;

public class RunServer {
	
	private static HashMap<String,IndexofVoronoi> diagrams;
	private static int level = 4; //hilbert的阶，2的幂
	
	public static void main(String args[]) throws FileNotFoundException{
		BPlusTree<Leaf> tree = constructTree("src/sites1",3);
		System.out.println(tree);
		double coord[] = {0.2,0.8};
		int k = 6;
		//test
		System.out.println("adf "+Hilbert.coordToHil(coord[0], coord[1], 4));
		test(tree);
	}
	
	public static void test(BPlusTree<Leaf> tree){
		StdDraw.setPenColor(StdDraw.MAGENTA);
		StdDraw.setPenRadius(.018);
		for(int i=0;i<level*level;i++){
			double coord[] = Hilbert.hilToCoord(i, level);
//			kNNQuery(i, 6, tree);
		}
	}
	
	/**
	 * user send a query
	 * @param usrHil
	 * @param path
	 * @param k
	 * @return
	 * @throws FileNotFoundException
	 */
	public static ArrayList<Integer> usrQuery(int usrHil,String path,int k) throws FileNotFoundException{
		BPlusTree<Leaf> tree = constructTree(path,3);
		long start = System.nanoTime();
		System.out.println(start);
		ArrayList<Integer> result = kNNQuery(usrHil,k,tree);
		long end = System.nanoTime();
		System.out.println(end);
		System.out.println("服务器端运行时间为 "+(end-start)/1000000.0+" ms");
		return result;
	}
	
	/**
	 * create a b plus tree
	 * @param path
	 * @param nodeBranches
	 * @return
	 * @throws FileNotFoundException
	 */
	public static BPlusTree<Leaf> constructTree(String path,int nodeBranches) throws FileNotFoundException{
		diagrams = new HashMap<String,IndexofVoronoi>();
		diagrams = Voronoi.generateVorIndex(path);
		ArrayList<Leaf> leaves = generateLeafNode(1,1);
		BPlusTree<Leaf> tree = makeTree(leaves,nodeBranches);
		return tree;
	}
	
	/**
	 * k nearest neighbor query
	 * @param hilVal
	 * @param k
	 * @param tree
	 * @return
	 */
	public static ArrayList<Integer> kNNQuery(int hilVal,int k,BPlusTree<Leaf> tree){
		ArrayList<Integer> result = new ArrayList<Integer>();
		HashSet<Integer> resultTemp = new HashSet<Integer>();
		Leaf node = tree.search(hilVal);
		if(node==null){
			System.out.println("没有查询到结果");
			return null;
		}
		String vorSite = node.getVorSite();
		int count = 0;
		HashSet<String> neighbors = new HashSet<String>();
		HashSet<String> candidate = new HashSet<String>();
		HashSet<String> temp = new HashSet<String>();
		candidate.add(vorSite);
		neighbors.add(vorSite);
		while(count<k){
			for(String site:candidate){
				HashSet<String> nextNeighbors = diagrams.get(site).getOneNN();
				neighbors.addAll(nextNeighbors);
				temp.addAll(nextNeighbors);
			}
			candidate.clear();
			candidate.addAll(temp);
			temp.clear();
			count +=1;
		}
		Hilbert hilbert = new Hilbert();
		for(String str:neighbors){
			int val = hilbert.coordToHil(Double.parseDouble(str.split(" ")[0]), Double.parseDouble(str.split(" ")[1]), level);
			resultTemp.add(val);
		}
//		System.out.println(result.size());
//		drawKNNRS(result, level);
		result.addAll(resultTemp);
		return result;
	}
	
	
	/**
	 * construct a tree ,insert all Leaf objects to this tree
	 * @param leaves
	 * @param treeParam node branches
	 * @return
	 */
	public static BPlusTree<Leaf> makeTree(ArrayList<Leaf> leaves, int treeParam) {
		// create a new b+tree based on the elements amount
		BPlusTree<Leaf> tree = new BPlusTree<Leaf>(treeParam, leaves.size());
		// insert each element to the tree
		int key;
		for(Leaf element:leaves){
			key = element.getHilLowerBound();
			tree.insert(key, element);
		}
		return tree;
	}
	/**
	 * generate all leafNodes 
	 * @param level must be the power of 2
	 */
	public static  ArrayList<Leaf> generateLeafNode(double width,double height){
        int n = level;
        ArrayList<Leaf> nodes = new ArrayList<Leaf>();
        String lastVor = "";
        
        HashMap<Integer,Point2D.Double> hilMap = fillWithHilCur(n,width,height);
        Iterator it = hilMap.entrySet().iterator();
        Point2D.Double lastP = new Point2D.Double();
        Leaf lastNode = null;
        while(it.hasNext()){
        	Map.Entry<Integer, Point2D.Double> entry = (Map.Entry<Integer, Point2D.Double>)it.next();
        	int hil = entry.getKey();
        	double coord[] = new double[2];
        	Point2D.Double p = entry.getValue();
        	//show hilbert curve
        	StdDraw.setPenColor(StdDraw.RED);
        	if(hil>0){
        		StdDraw.line(lastP.getX(), lastP.getY(), p.getX(), p.getY());
        	}
        	coord[0] = p.getX();
        	coord[1] = p.getY();
        	//find the voronoi polygon correspond to hilbert by coordinate
        	String vorSite="";
        	vorSite = getVorSiteByCoord(coord);
        	if(!vorSite.equals(lastVor)){
        		if(hil>0){
        			lastNode.setNextHil(hil);
        		}
        		Leaf leaf = new Leaf(hil,vorSite);
        		lastNode = leaf;
        		nodes.add(leaf);
        	}
        	lastVor = vorSite;
        	lastP = p;
        }
        return nodes;
	}
	
	/**
	 * get the site of voronoi the point(coord) locates in
	 * @param coord
	 * @return
	 */
	public static String getVorSiteByCoord(double[] coord){
		String result = null;
		ArrayList<String> candidate = new ArrayList<String>();
		Iterator iter  = diagrams.entrySet().iterator();
		while(iter.hasNext()){
			Map.Entry<String, IndexofVoronoi> entry = (Map.Entry<String, IndexofVoronoi>)iter.next();
			String site = entry.getKey();
			IndexofVoronoi vor = entry.getValue();
			if(vor.mBRContains(coord[0], coord[1])){
				candidate.add(site);
				if(vor.containsPoint(coord[0], coord[1])){
					result = site;
					break;
				}
			}
		}
		//deal with imprecise result due to convertion from double to int
		if(result==null){
			String str = "";
			double siteX,siteY,distance = Double.MAX_VALUE,realD;
			for(int i=0;i<candidate.size();i++){
				str = candidate.get(i);
				siteX = Double.parseDouble(str.split(" ")[0]);
				siteY = Double.parseDouble(str.split(" ")[1]);
				realD = (siteX-coord[0])*(siteX-coord[0])+(siteY-coord[1])*(siteY-coord[1]);
				if(realD<distance){
					distance = realD;
					result = str;
				}
			}
		}
		return result;
	}
	
	//generate hilbert curve to fill the region
    public static HashMap<Integer,Point2D.Double> fillWithHilCur(int N,double width,double height){
    	int level;
    	level =(int)( Math.log(N)/Math.log(2));
    	AnoHilbert hilbert = new AnoHilbert();
    	hilbert.process(level, width, height);  
        return hilbert.getMap();
    }
	
	//draw knn result
	public static void drawKNNRS(ArrayList<Integer> vals,int n){
		StdDraw.setPenRadius(0.02);
		StdDraw.setPenColor(StdDraw.GREEN);
		StdDraw.point(0.01, 0.01);
		StdDraw.setPenRadius(0.02);
		StdDraw.setPenColor(StdDraw.RED);
		for(Integer e : vals){
			double[] coord = Hilbert.hilToCoord(e, n);
			StdDraw.point(coord[0], coord[1]);
		}
	}
}

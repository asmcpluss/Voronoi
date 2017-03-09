import java.awt.Graphics;
import java.awt.Point;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import BPlusTree.*;
import hil.Hilbert;

public class RunApp {
	private static HashMap<String,IndexofVoronoi> diagrams;
	
	public static void main(String args[]) throws FileNotFoundException{
		diagrams = new HashMap<String,IndexofVoronoi>();
		diagrams = Voronoi.generateVorIndex("src/sites8");
		int level = 64;
		ArrayList<Leaf> leaves = generateLeafNode(level);
		BPlusTree<Leaf> tree = makeTree(leaves,5);
		int hilVal = 3624;
		int k = 3;
		ArrayList<Integer> result = kNNQuery(hilVal,k,tree,level);
		System.out.println("服务器端返回的k近邻个数"+result.size());
		drawKNNRS(result, level);
	}
	
	public static ArrayList<Integer> kNNQuery(int hilVal,int k,BPlusTree<Leaf> tree,int n){
		ArrayList<Integer> result = new ArrayList<Integer>();
		//test
		System.out.println("运行的分界线");
		Leaf node = tree.search(hilVal);
		if(node==null){
			System.out.println("没有查询到结果");
			return null;
		}
		String vorSite = node.getVorSite();
		Voronoi.drawPoint(vorSite.split(" ")[0], vorSite.split(" ")[1]);
		int count = 0;
		HashSet<String> neighbors = new HashSet<String>();
		HashSet<String> candidate = new HashSet<String>();
		HashSet<String> temp = new HashSet<String>();
		candidate.add(vorSite);
		while(count<k-1){
			for(String site:candidate){
				HashSet<String> nextNeighbors = diagrams.get(site).getOneNN();
				neighbors.addAll(nextNeighbors);
				temp.addAll(nextNeighbors);
			}
			candidate.clear();
			candidate.addAll(temp);
			temp.clear();
//			count = neighbors.size();
			count++;
		}
		Hilbert hilbert = new Hilbert();
		for(String str:neighbors){
			int val = hilbert.coordToHil(Double.parseDouble(str.split(" ")[0]), Double.parseDouble(str.split(" ")[1]), n);
			result.add(val);
		}
		System.out.println(result.size());
		return result;
	}
	
	
	//construct a tree ,insert all Leaf objects to this tree
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
	public static  ArrayList<Leaf> generateLeafNode(int level){
		Hilbert hilbert = new Hilbert();
        int n = level;
        ArrayList<Leaf> nodes = new ArrayList<Leaf>();
        StdDraw.setPenColor(StdDraw.RED);
        StdDraw.setPenRadius(.002);
        String lastVor = "";
        Leaf lastNode = null;
        for(int i=0;i<n*n;i++){
        	double[] coord = hilbert.hilToCoord(i, n);
        	//draw point to see if the point is in voronoi x
        	if(i<n*n-1){
        		StdDraw.line(coord[0], coord[1],hilbert.hilToCoord(i+1,n)[0],hilbert.hilToCoord(i+1, n)[1]);
        	}
        	//create new leaf node
        	String vorSite;
        	vorSite = getVorSiteByCoord(coord);
        	if(!vorSite.equals(lastVor)){
        		if(i>0){
        			lastNode.setNextHil(i);
        		}
        		Leaf leaf = new Leaf(i,vorSite);
        		lastNode = leaf;
        		nodes.add(leaf);
        	}
        	lastVor = vorSite;
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
		String vorSite;
		Iterator iter  = diagrams.entrySet().iterator();
		while(iter.hasNext()){
			Map.Entry<String, IndexofVoronoi> entry = (Map.Entry<String, IndexofVoronoi>)iter.next();
			String site = entry.getKey();
			IndexofVoronoi vor = entry.getValue();
//			if(coord[0]==0.0625&&coord[1]==0.8125){
//				System.out.println(vor.getMinBR()[0]+" "+vor.getMinBR()[1]);
//			}
			if(vor.mBRContains(coord[0], coord[1])){
				candidate.add(site);
				if(vor.containsPoint(coord[0], coord[1])){
					result = site;
					break;
				}
			}
		}
		
		//test
		if(candidate.isEmpty()){
			StdDraw.setPenColor(StdDraw.RED);
			StdDraw.setPenRadius(.01);
			StdDraw.point(coord[0], coord[1]);
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
	
	//draw knn result
	public static void drawKNNRS(ArrayList<Integer> vals,int n){
		StdDraw.setPenRadius(0.02);
		StdDraw.setPenColor(StdDraw.RED);
		for(Integer e : vals){
			double[] coord = Hilbert.hilToCoord(e, n);
			StdDraw.point(coord[0], coord[1]);
		}
	}
}

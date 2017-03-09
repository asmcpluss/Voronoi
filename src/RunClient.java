import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

import hil.Hilbert;

public class RunClient {
	private static int level = 4;
	
	public static void main(String args[]) throws FileNotFoundException{
		int k = 3; //k nearest neighbors
		double[] coord = new double[2];
		coord[0] = 0.2;
		coord[1] = 0.6;
		int hilVal = Hilbert.coordToHil(coord[0]+1.0/level*0.5, coord[1]+1.0/level*0.5, level);
		String path = "src/sites1";
		ArrayList<Integer> knnResult = RunServer.usrQuery(hilVal, path, k);
		System.out.println(knnResult);
		System.out.println("communication cost: "+knnResult.size()+" "+knnResult.size()*(Integer.SIZE/8)+" Bytes");
		long clientStart = System.nanoTime();
		ArrayList<String> kNNPOI = responseRetrieval(coord,knnResult,k);
		long clientEnd = System.nanoTime();
		System.out.println("客户端运行时间："+(clientEnd-clientStart)/1000000.0+" ms");
		
		System.out.println(kNNPOI);
		StdDraw.setPenColor(StdDraw.MAGENTA);
		StdDraw.point(coord[0], coord[1]);
		System.out.println(hilVal);
	}
	
	/**
	 * return k nearest neighbors
	 * @param usrLocation
	 * @param rsFromServer
	 * @param k
	 * @return
	 */
	public static ArrayList<String> responseRetrieval(double[] usrLocation,ArrayList<Integer> rsFromServer,int k){
		ArrayList<String> result = new ArrayList<String>();
		HashMap<Double,String> map = new HashMap<Double,String>();
		ArrayList<Double> dists = new ArrayList<Double>();
		for(Integer hil:rsFromServer){
			double coord[] = Hilbert.hilToCoord(hil,level);
			double x = coord[0];
			double y = coord[1];
			double distance = Math.sqrt((usrLocation[0]-x)*(usrLocation[0]-x)+(usrLocation[1]-y)*(usrLocation[1]-y));
			map.put(distance, x+" "+y);
		}
		dists.addAll(map.keySet());
		Collections.sort(dists);
		
		//test
		StdDraw.setPenRadius(.02);
		StdDraw.setPenColor(StdDraw.BLUE);
		StdDraw.point(usrLocation[0], usrLocation[1]);
		
		StdDraw.setPenColor(StdDraw.RED);
		
		for(int i=0;i<k;i++){
			String str = map.get(dists.get(i));
			result.add(map.get(dists.get(i)));
			double x =Double.parseDouble(str.split(" ")[0]);
			double y = Double.parseDouble(str.split(" ")[1]);
			StdDraw.point(x, y);
		}
		return result;
	}

}


public class Leaf {
	private int hilLowerBound;
	private String vorSite;
	private int nextHil; //the value of the right leaf node
	
	public Leaf(int hilLBound,String site){
		hilLowerBound =hilLBound;
		vorSite = site;
	}

	public int getHilLowerBound() {
		return hilLowerBound;
	}

	public void setHilLowerBound(int hilLowerBound) {
		this.hilLowerBound = hilLowerBound;
	}

	public String getVorSite() {
		return vorSite;
	}

	public void setVorSite(String vorSite) {
		this.vorSite = vorSite;
	}

	public int getNextHil() {
		return nextHil;
	}

	public void setNextHil(int nextHil) {
		this.nextHil = nextHil;
	}

}

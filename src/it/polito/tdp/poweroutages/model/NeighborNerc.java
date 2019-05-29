package it.polito.tdp.poweroutages.model;

public class NeighborNerc implements Comparable<NeighborNerc>{
	Nerc nerc;
	int correlation;
	
	public NeighborNerc(Nerc nerc, int correlation){
		this.nerc = nerc;
		this.correlation = correlation;
	}

	@Override
	public String toString() {
		return "NeighborNerc [nerc=" + nerc + ", correlation=" + correlation + "]";
	}

	@Override
	public int compareTo(NeighborNerc o) {
		return Integer.compare(o.correlation,this.correlation);
	}

}
package mmc;

public class Edge {
	public int from;
	public int to;
	public double weight;

	public Edge(int from, int to){
		this.from=from;
		this.to=to;
		this.weight=1;
	}
	
	public Edge(int from, int to, double weight) {
		this.from=from;
		this.to=to;
		this.weight=weight;
	}

	Edge(){

	}

	public boolean equals(Object e){
		if(e == null)
			return false;
		if(!(e instanceof Edge))
			return false;
		if (e == this)
			return true;
		return from == ((Edge) e).from && to == ((Edge) e).to;
	}

	private int compareTo(Edge e){
		if(e==null)
			return 1;
		if (equals(e))
			return 0;
		if (from < e.from || (from == e.from && to < e.to))
			return -1;
		return 1;
	}

	public boolean isLess(Edge e1, Edge e2){
		return !e1.equals(e2) && e1.compareTo(e2)==-1;
	}

	public int hashCode(){
		final int prime = 31;
		int result = 1;
		result = prime * result + from;
		result = prime * result + to;
		return result;
	}

}



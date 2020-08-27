package mmc;

import org.apache.log4j.Logger;

import java.util.HashSet;
import java.util.Vector;

public class Graph {
	private static Logger logger = Logger.getLogger(Graph.class);
	//Number of nodes and edges
	public int nodeNum;
	public int edgeNum;
	
	// The adjacency list of the graph; adjList[i] stores the neighbor set of node i
	public Vector<HashSet<Integer>> adjList = new Vector<HashSet<Integer>>();
	
	// The edge list
	public Vector<Edge> edges = new Vector<Edge>();
	public Vector<Edge> originalEdges = new Vector<Edge>();
	
	// labels of nodes. Node id {0, ..., nodeNum-1}
	public Vector<Integer> labels = new Vector<Integer>();
	
	public Graph(){};
	
	/*
     * Read the graph from the given path
     * Graph file format [all values are integers][undirected]:
     * Line1: n(nodeNum) m(edgeNum)
     * Line2: from_1 to_1
     * Line3: from_2 to_2
     * ...
     * Line_(m+1): from_m to_m
     * Line_(m+2): label of node_1
     * ...
     * Line_(1+m+n): label of node_n
     */
    void read(String path){
    	logger.info("Start to load undirected graph from " + path + " for the basic subgraph matching algorithm.");
    	
    };

    /*
     * print out the whole graph. [For debug]
     */
    public void print(){
    	 logger.info("nid\tlabel\tNeighbors");
    	    for (int i = 0; i < nodeNum; i++) {
    	        System.out.print(i + "\t" + labels.get(i));
    	        for(Integer e : adjList.get(i)) {
    	        	System.out.print("\t" + e);
    	        }
    	        logger.info("");
    	    }
    };
//
//    void Print(ostream &ofile){};

    public void clear(){
    	adjList.clear();
    	edges.clear();
    	labels.clear();
    };
}

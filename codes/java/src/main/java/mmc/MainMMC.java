package mmc;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;

//import main.Utilities;

public class MainMMC {
	
	public MainUtilities Utilities = new MainUtilities();
	
	private static Logger logger = Logger.getLogger(MainMMC.class);

	public MaximalMotifClique mmc = new MaximalMotifClique(this);
	public SubGraphMatch sgm = new SubGraphMatch(this);

	private int mustContainNodeId = -1;

	public int subgraphLimit = -1;	//default value -1
	public int MMCLimit = -1;		//default value -1

	public int globalHashId = 0;
	public int maxCliqueSize = 0;

	public SetTrieNode root = null;
	public Graph tempGraph = null;

	//Structures used in IsoCheck
	public int currentU = -1;

	public Vector< Vector<Integer> > tempMatchedSubgraphs = new Vector< Vector<Integer> >();
	public HashMap<Integer, HashSet<Integer> > candLabel2Nodes = new HashMap<Integer, HashSet<Integer> >();
	public HashMap<Integer, HashSet<Integer> > cands = new HashMap<Integer, HashSet<Integer> >();

	//public Vector<Boolean> isVisited = new Vector<Boolean>();
        public Boolean[] isVisited = null;
	public HashMap<Integer, String> id2Name = new HashMap<Integer, String>();		// map node id to real info.
	public Vector< Vector<Integer> > subgraphs = new Vector< Vector<Integer> >();	// matched subgraphs

	public HashMap<Integer, HashSet<Integer> > motif_label2Nodes = new HashMap<Integer, HashSet<Integer> >(); 
	public HashMap<Integer, HashSet<Integer> > motif_labelAdj = new HashMap<Integer, HashSet<Integer> >();

	public HashSet<Integer> pre_isoCheckSet = new HashSet<Integer>();

	public int motifNodeSize;
	public HashSet<Integer> labelSet = new HashSet<Integer>();

	public Vector<HashSet<Integer>> maxMotifCliques = new Vector<HashSet<Integer>>(); // the result set

	public Graph motif=new Graph();
	public Graph graph=new Graph();

	public MainMMC(String s){
		initialize(s);
	}
	
	//for testing purpose
	public Graph setDummyMotif1() {
		Graph k = new Graph();
		k.edgeNum=8;
		k.nodeNum=4;
		
		for(int i=0; i< k.nodeNum;i++){
			k.adjList.add(new HashSet<Integer>());
		}
		k.labels.add(0);
		k.labels.add(0);
		k.labels.add(1);
		k.labels.add(1);
		
		k.edges.add(new Edge(0,1));
		k.edges.add(new Edge(0,2));
		k.edges.add(new Edge(0,3));
		k.edges.add(new Edge(1,3));
		
		k.edges.add(new Edge(1,0));
		k.edges.add(new Edge(2,0));
		k.edges.add(new Edge(3,0));
		k.edges.add(new Edge(3,1));

		for(int i=0; i<k.edgeNum; i++){
			int a=k.edges.get(i).from;
			int b=k.edges.get(i).to;
			k.adjList.get(a).add(b);
		}
		return k;
	}
	
	public Graph setDummyMotif2() {
		Graph k = new Graph();
		k.edgeNum=2;
		k.nodeNum=2;
		for(int i=0; i<k.nodeNum;i++){
			k.adjList.add(new HashSet<Integer>());
		}
		k.labels.add(0);
		k.labels.add(1);
		
		k.edges.add(new Edge(0,1));
		k.edges.add(new Edge(1,0));


		for(int i=0; i<k.edgeNum; i++){
			int a=k.edges.get(i).from;
			int b=k.edges.get(i).to;
			k.adjList.get(a).add(b);
		}
		return k;
	}
	
	public Graph setDummyMotif3() {
		Graph k = new Graph();
		k.edgeNum=6;
		k.nodeNum=3;
		for(int i=0; i<k.nodeNum;i++){
			k.adjList.add(new HashSet<Integer>());
		}
		k.labels.add(1);
		k.labels.add(0);
		k.labels.add(0);

		k.edges.add(new Edge(0,1));
		k.edges.add(new Edge(1,0));
		k.edges.add(new Edge(0,2));
		k.edges.add(new Edge(2,0));
		k.edges.add(new Edge(1,2));
		k.edges.add(new Edge(2,1));


		for(int i=0; i<k.edgeNum; i++){
			int a=k.edges.get(i).from;
			int b=k.edges.get(i).to;
			k.adjList.get(a).add(b);
		}
		return k;
	}
	
	private void initialize(String s){
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(s));
			String st;
			st = br.readLine();
			String[] v = st.split("\t", -1);
			int numberOfNode = (int) Integer.parseInt(v[0]);
			int numberOfEdge = (int) Integer.parseInt(v[1]);
			int numberOfType = (int) Integer.parseInt(v[2]);
			
			graph.edgeNum = 2 * numberOfEdge;
			graph.nodeNum = numberOfNode;
			
			//read for id2Name
			for(int i = 0; i < numberOfNode; i++) {
				st = br.readLine();
				v =  st.split("\t",-1);
				id2Name.put(Integer.parseInt(v[0]), v[1]);
				graph.adjList.add(new HashSet<Integer>());
			}
			
			//read for edges
			for(int i = 0; i < numberOfEdge; i++) {
				st = br.readLine();
				v =  st.split("\t",-1);
				int a = Integer.parseInt(v[0]);
				int b = Integer.parseInt(v[1]);
				double w = 1;
				if (v.length > 2) {
					w = Double.parseDouble(v[2]);
				}
				// TODO: need to update adjList if lower weighted edges are filtered out
				// adjList is used by BK algorithm (for traditional cliques)
				graph.adjList.get(a).add(b);
				graph.adjList.get(b).add(a);
				graph.originalEdges.add(new Edge(a,b,w));
				graph.originalEdges.add(new Edge(b,a,w));
			}
			
			//read for labels translation, but not used here
			for(int i = 0; i < numberOfType; i++) {
				st = br.readLine();
			}
			
			//read for labels
			for(int i = 0; i < numberOfNode; i++) {
				st = br.readLine();
				graph.labels.add(Integer.parseInt(st));
			}
			
			logger.info("done initializing");
		} catch (FileNotFoundException e) {
			logger.info("initialize: File not found");
			e.printStackTrace();
		} catch (IOException e) {
			logger.info("initialize: IO exception");
			e.printStackTrace();
		} finally{
			if(br!=null)
				try {
					br.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
	}
	
	public synchronized void doMCC(Graph m, int mustContain) throws InterruptedException{
		// set it to unlimited
		// let frontend control when to stop searching
		MMCLimit = -1;
		motif=m;
		mustContainNodeId = mustContain;
		
		// filter graph.originalEdges and generate graph.adjList
		graph.edges.clear();
		graph.adjList.stream().forEach((adjList) -> adjList.clear());
		graph.originalEdges.stream().forEach((edge) -> {
			if (edge.weight >= Utilities.weightLowerBound) {
				// logger.info("edge weight = " + edge.weight + " weight lower bound = " + Utilities.weightLowerBound);
				graph.edges.add(edge);
				graph.adjList.get(edge.from).add(edge.to);
				graph.adjList.get(edge.to).add(edge.from);
			}
		});
		graph.edgeNum = graph.edges.size();
		// TODO: should we also update graph.nodeNum?
		
		// maxMotifCliques.clear();
		Utilities.cliques = new Vector<HashSet<Integer>>();
		subgraphs.clear();		

		mmc.GetLabel2NodesMap(motif, motif_label2Nodes);
		
		mmc.GetLabelAdj(motif, motif_labelAdj);
		
		motifNodeSize = motif.nodeNum;
		
		if (motif.edgeNum == 0){
			logger.info("There's no edge in the inputted motif. Please input a valid motif!");
			Utilities.cliques = null;
		}
                
		globalHashId = 0;
		root = new SetTrieNode(-1, false, 0, globalHashId++);
		
		if (mustContainNodeId != -1)
			try {
				sgm.GetMatchedSubgraph(motif, graph, subgraphs, mustContainNodeId);
			} catch (InstantiationException | IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		else
			sgm.GetMatchedSubgraph(motif, graph, subgraphs, false);

		logger.info(subgraphs.size() + " subgraphs have been found!");
		// logger.info(maxMotifCliques.size() + " maximal motif cliques have been found!");
		logger.info(Utilities.cliques.size() + " maximal motif cliques have been found!");
		logger.info(Utilities.cliques.toString());
		Utilities.searchFinished = true;
		// maxMotifCliques.sort((s1,s2) -> s2.size() - s1.size());
	}

	public synchronized Vector<HashSet<Integer>> doBK(int mustContain) {

        mustContainNodeId = mustContain;
        HashSet<Integer> current_node_set = new HashSet<>();
        HashSet<Integer> candidates = new HashSet<>();
        HashSet<Integer> NOT = new HashSet<>();

        maxMotifCliques.clear();

        if (mustContainNodeId != -1) {
            current_node_set.add(mustContainNodeId);
            for (Integer e: graph.adjList.get(mustContainNodeId)) {
                candidates.add(e);
            }
        } else {
            for (int i = 0; i < graph.nodeNum; i++) {
                candidates.add(i);
            }
        }

        logger.debug("candidate node set for BK has size = " + candidates.size());
		logger.debug("current node set for BK has size = " + current_node_set.size());
        mmc.BK(graph, current_node_set, candidates, NOT, maxMotifCliques);

        logger.info("BK search finishes, max cliques size = " + maxMotifCliques.size());
        maxMotifCliques.sort((s1,s2) -> s2.size() - s1.size());
        
        return maxMotifCliques;
    }

//	private int split(String txt, Vector<String> strs, char ch) {
//		//this is the general case
//		int pos = txt.indexOf(ch);
//		int initialPos = 0;
//		strs.clear();
//		// Decompose statement
//		while (pos != Integer.MAX_VALUE) {
//			strs.add(txt.substring(initialPos, pos - initialPos));
//			initialPos = pos + 1;
//			pos = txt.indexOf(ch, pos);
//		}
//		// Add the last one
//		strs.add(txt.substring(initialPos, Math.min(pos, txt.length()) - initialPos));
//		//return the size of the vector
//		return strs.size();
//	}

	private void ReadId2Name(String name2id_path) {	
		File file = new File(name2id_path);

		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(file));
			String st;
			while ((st = br.readLine()) != null)
			{
				String[] v =  st.split("\t",-1); 
				id2Name.put(Integer.parseInt(v[1]), v[0]);	
			}


		} catch (FileNotFoundException e) {
			logger.info("Utilities.ReadId2Name(): File not found");
			e.printStackTrace();
		} catch (IOException e) {
			logger.info("Utilities.ReadId2Name(): IO exception");
			e.printStackTrace();
		} finally{
			if(br!=null)
				try {
					br.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
	}

	private void PrintNodeMeaning( HashSet<Integer> p,  Graph graph,  String name2id_path) {
		// If it is the first time to invoke this func., we need to load the name2id file of the DBLP dataHashSet.
		if (id2Name.isEmpty()) {
			ReadId2Name(name2id_path);
		}
		// Output the name of authors
		for ( Integer e : p) {
			logger.info ("id=" + e + ": " + graph.labels.get(e) +  "\t" + id2Name.get(e));
		}
	}

	private void PrintResults( Vector<HashSet<Integer> > result,  String output_path,  Graph graph,
			String name2id_path) {

		if (name2id_path.isEmpty()) return;


		Writer fw = null;
		BufferedWriter bw = null;
		try {

			fw = new FileWriter(output_path);
			bw = new BufferedWriter(fw);

			int index = 0;

			double avg_size = 0;
			double avg_edge = 0;
			HashMap<Integer, Integer> nodeSizeToFreq = new HashMap<Integer, Integer>();
			HashMap<Integer, Integer> edgeSizeToFreq = new HashMap<Integer, Integer>();

			for ( HashSet<Integer> e : result) {
				int e_size = e.size();
				avg_size += e_size;

				if (nodeSizeToFreq.containsKey(e_size)){
					//nodeSizeToFreq[e_size]++;
					int s=nodeSizeToFreq.get(e_size);
					nodeSizeToFreq.put(e_size, s+1);
				}
				else
					nodeSizeToFreq.put(e_size, 1);

				index++;

				HashMap<Integer, Integer> label_count = new HashMap<Integer, Integer>();
				for (Integer u : e) {
					if (!label_count.containsKey(graph.labels.get(u)) ) {
						label_count.put(graph.labels.get(u), 1);
					} else{
						int t = label_count.get(graph.labels.get(u));
						label_count.put(graph.labels.get(u), t + 1);
					}
				}
				bw.write(String.format("The %d th MMC:", index));
				bw.write(String.format("num_nodes: %d", e.size()));
				for ( Integer u : e) {
					bw.write(String.format("\t%d", u));
				}
				bw.write("\n");
				if (id2Name.isEmpty()) {
					ReadId2Name(name2id_path);
				}

				bw.write("id\tproduct-name\ttype\n");
				for ( Integer u : e) {
					bw.write(String.format("%d\t%s\t%d\n", u, id2Name.get(u), graph.labels.get(u)));
				}
				bw.write("Print the graph:\n");
				int edge_size = 0;
				for ( Integer u: e) {
					for ( Integer w:e) {
						if (u != w && u < w) {

							if (graph.adjList.get(u).contains(w)) {
								avg_edge++;
								edge_size++;
								bw.write(String.format("%d\t%d\n", u, w));
							}

						}
					}
				}

				if (edgeSizeToFreq.containsKey(edge_size)){
					int size = edgeSizeToFreq.get(edge_size);
					edgeSizeToFreq.put(edge_size, size+1);
				}

				else
					edgeSizeToFreq.put(edge_size, 1);

			}

			if (result.size() != 0) {
				logger.info("avg node size: " + avg_size / result.size());
				logger.info("avg edge size: " + avg_edge / result.size());
			}

			logger.info("node size  freq info.");
			for (Map.Entry<Integer, Integer> e : nodeSizeToFreq.entrySet()) {
				logger.info(e.getKey() + " " + e.getValue());
			}

			logger.info(  "edge size  freq info.");
			for (Map.Entry<Integer, Integer> e : edgeSizeToFreq.entrySet()) {
				logger.info(e.getKey() + " " + e.getValue());
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if(bw!=null)
					bw.close(); 
				if(fw!=null)
					fw.close(); 
			}
			catch (IOException ex) {
				ex.printStackTrace();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

	}



}

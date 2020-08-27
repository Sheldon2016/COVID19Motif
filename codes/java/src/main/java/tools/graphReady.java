package tools;

import java.awt.List;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class graphReady {
	/*
	 * Note: the graph IDs starts from 1 in ArrayList<Integer>graph[] and
	 * ArrayList<Double>w[]. ReadGraph - read the graph without nodeNum (read_path,
	 * separator, left, right, weight line, uselessLines, nodeNum, ifWeighted)
	 * readGraph - read the graph (read_path, separator, left, right, weight line,
	 * uselessLines, edgeNum, nodeNum, ifWeighted) graph - read the graph (
	 * normalize - make the IDs continues (read_path, separator, left, right, weight
	 * line, uselessLines, edgeNum, nodeNum, ifWeighted) getNodeNum - return nodeNum
	 * (read_path, separator, left, right, uselessLines, edgeNum) addRandomWeight -
	 * add random weights on the edges writeGraph - write graph into writeDir
	 * (writeDir, separator, ifWeighted) diameter() - return the diameter of the
	 * graph
	 * 
	 */

	public ArrayList<Integer> graph[]; // to store the uncertain graph into a list
	public ArrayList<Double> w[]; // Similar structure as graph, but a UGDiameter-size vector serve as the element
									// on each edge
	public int edgeNum;
	public int nodeNum;

	public void readGraph_root(String s, String sp, int l, int r, int wl, int startLine, int edgeNum, int nodeNum,
			boolean ifWeighted, boolean ifAdd1) throws IOException {

		if (nodeNum != -1 && edgeNum != -1) {
			this.nodeNum = nodeNum;
			this.edgeNum = edgeNum;
		} else {
			this.nodeNum = getNodeNum(s, sp, l, r, startLine);
			edgeNum = this.edgeNum;
			nodeNum = this.nodeNum;
		}

		graph = new ArrayList[nodeNum + 1];
		w = new ArrayList[nodeNum + 1];

		for (int i = 1; i < graph.length; i++) {
			graph[i] = new ArrayList<Integer>();
			w[i] = new ArrayList<Double>();
		}
		BufferedReader a = new BufferedReader(new FileReader(s));
		for (int i = 0; i < startLine; i++) {
			a.readLine();
		}
		for (int i = 0; i < edgeNum; i++) {
			String str = a.readLine();
			String[] tem = str.split(sp);
				
			int left = Integer.parseInt(tem[l]), right = Integer.parseInt(tem[r]);
			if(left > graph.length || right > graph.length) continue;
			if (ifAdd1) {
				left++;
				right++;
			}
			double weight = -1;

			if (!graph[left].contains(right)) {
				graph[left].add(right);
				if (ifWeighted) {
					weight = Double.parseDouble(tem[wl]);
					w[left].add(weight);
				} else {
					weight = 1.0;
					w[left].add(weight);
				}
			}
			if (!graph[right].contains(left)) {
				graph[right].add(left);
				if (ifWeighted) {
					weight = Double.parseDouble(tem[wl]);
					w[right].add(weight);
				} else {
					weight = 1.0;
					w[right].add(weight);
				}
			}

		}

		this.match = new String[nodeNum + 1];
		this.map = new HashMap();
		for (int i = 1; i <= nodeNum; i++) {
			this.match[i] = i + "";
			this.map.put(i + "", i);
		}

	}

	public void readGraph(String s, String sp, int l, int r, int wl, int startLine, boolean ifWeighted)
			throws IOException {
		int nodeNum = -1, edgeNum = -1;
		boolean ifAdd1 = false;
		readGraph_root(s, sp, l, r, wl, startLine, edgeNum, nodeNum, ifWeighted, ifAdd1);
		/*
		 * this.nodeNum=getNodeNum(s,sp,l,r,startLine); graph = new
		 * ArrayList[nodeNum+1]; w = new ArrayList[nodeNum+1];
		 * 
		 * for(int i=1;i<graph.length;i++){ graph[i]=new ArrayList<Integer>();
		 * if(ifWeighted)w[i]= new ArrayList<Double>(); } BufferedReader a = new
		 * BufferedReader(new FileReader(s)); for(int i=0;i<startLine;i++){
		 * a.readLine(); } for(int i=0;i<edgeNum;i++){ String[]tem =
		 * a.readLine().split(sp); int left = Integer.parseInt(tem[l]), right =
		 * Integer.parseInt(tem[r]); double weight=-1;
		 * 
		 * if(!graph[left].contains(right)){ graph[left].add(right); if(ifWeighted){
		 * weight=Double.parseDouble(tem[wl]); w[left].add(weight); } else{ weight=1.0;
		 * w[left].add(weight); } } if(!graph[right].contains(left)){
		 * graph[right].add(left); if(ifWeighted){ weight=Double.parseDouble(tem[wl]);
		 * w[right].add(weight); } else{ weight=1.0; w[right].add(weight); } }
		 * 
		 * }
		 */
	}

	public void readGraph(String s, String sp, int l, int r, int wl, int startLine, boolean ifWeighted, boolean ifAdd1)
			throws IOException {
		int nodeNum = -1, edgeNum = -1;
		readGraph_root(s, sp, l, r, wl, startLine, edgeNum, nodeNum, ifWeighted, ifAdd1);
	}

	public void readGraph(String s, String sp, int l, int r, int startLine, boolean ifAdd1) throws IOException {
		int nodeNum = -1, edgeNum = -1, wl = -1;
		boolean ifWeighted = false;
		readGraph_root(s, sp, l, r, wl, startLine, edgeNum, nodeNum, ifWeighted, ifAdd1);
	}

	public void readGraph(String s, String sp, boolean ifAdd1) throws IOException {
		int l = 0, r = 1, startLine = 0;
		int nodeNum = -1, edgeNum = -1, wl = -1;
		boolean ifWeighted = false;
		readGraph_root(s, sp, l, r, wl, startLine, edgeNum, nodeNum, ifWeighted, ifAdd1);
	}

	public void readGraph(String s, String sp, int l, int r, int wl, int startLine, int edgeNum, int nodeNum,
			boolean ifWeighted) throws IOException {

		boolean ifAdd1 = false;
		readGraph_root(s, sp, l, r, wl, startLine, edgeNum, nodeNum, ifWeighted, ifAdd1);

		/*
		 * this.edgeNum=edgeNum; graph = new ArrayList[nodeNum+1]; w = new
		 * ArrayList[nodeNum+1];
		 * 
		 * for(int i=1;i<graph.length;i++){ graph[i]=new ArrayList<Integer>(); }
		 * BufferedReader a = new BufferedReader(new FileReader(s)); for(int
		 * i=0;i<startLine;i++){ a.readLine(); } for(int i=0;i<edgeNum;i++){
		 * 
		 * String[]tem = a.readLine().split(sp); int left = Integer.parseInt(tem[l]),
		 * right = Integer.parseInt(tem[r]); double weight=-1;
		 * 
		 * if(Double.parseDouble(tem[wl])==0)continue;
		 * 
		 * if(!graph[left].contains(right)){ graph[left].add(right); if(ifWeighted){
		 * weight=Double.parseDouble(tem[wl]); w[left].add(weight); } else{ weight=1.0;
		 * w[left].add(weight); } } if(!graph[right].contains(left)){
		 * graph[right].add(left); if(ifWeighted){ weight=Double.parseDouble(tem[wl]);
		 * w[right].add(weight); } else{ weight=1.0; w[right].add(weight); } }
		 * 
		 * }
		 */

	}

	public void readGraph(String s, String sp, int l, int r, int wl, int startLine, int edgeNum, int nodeNum,
			boolean ifWeighted, boolean ifAdd1) throws IOException {
		readGraph_root(s, sp, l, r, wl, startLine, edgeNum, nodeNum, ifWeighted, ifAdd1);
	}

	public String[] match = null;
	public HashMap<String, Integer> map = null;

	public void normalize_root(String s, String sp, int nodeLine1, int nodeLine2, int WeightedLine, int startLine,
			int edgeNum, int nodeNum, boolean weighted) throws IOException {
		// to normalize the graph into the one with continuous nodes ID starting from 1
		// unweighted graph will be stored into graph with weight=1

		if (nodeNum != -1 && edgeNum != -1) {
			this.nodeNum = nodeNum;
			this.edgeNum = edgeNum;
		} else {
			this.nodeNum = getNodeNum(s, sp, nodeLine1, nodeLine2, startLine);
			edgeNum = this.edgeNum;
			nodeNum = this.nodeNum;
		}

		graph = new ArrayList[nodeNum + 1];
		w = new ArrayList[nodeNum + 1];
		match = new String[nodeNum + 1];

		for (int i = 1; i < graph.length; i++) {
			graph[i] = new ArrayList<Integer>();
			w[i] = new ArrayList<Double>();
		}

		BufferedReader a = new BufferedReader(new FileReader(s));

		for (int i = 0; i < startLine; i++)
			a.readLine();

		map = new HashMap<String, Integer>();

		int mapCounter = 0;

		if (weighted) {

			for (int i = 0; i < edgeNum; i++) {

				String[] tem = a.readLine().split(sp);
				int left = 0, right = 0;
				// to map the nodes into numbers
				if (!map.containsKey(tem[nodeLine1])) {
					mapCounter++;
					map.put(tem[nodeLine1], mapCounter);
					left = mapCounter;
					match[left] = (tem[nodeLine1]);
				} else {
					left = map.get(tem[nodeLine1]);
				}

				if (!map.containsKey(tem[nodeLine2])) {
					mapCounter++;
					map.put(tem[nodeLine2], mapCounter);
					right = mapCounter;
					match[right] = (tem[nodeLine2]);
				} else {
					right = map.get(tem[nodeLine2]);
				}
				double weight = -1;
				weight = Double.parseDouble(tem[WeightedLine]);

				// graph[left][right]=weight;
				// graph[right][left]=weight;

				if (!graph[left].contains(right)) {
					graph[left].add(right);
					w[left].add(weight);
				}
				if (!graph[right].contains(left)) {
					graph[right].add(left);
					w[right].add(weight);
				}
			}
		} else {
			for (int i = 0; i < edgeNum; i++) {

				String[] tem = a.readLine().split(sp);
				int left = 0, right = 0;
				// to map the nodes into numbers
				if (!map.containsKey(tem[nodeLine1])) {
					mapCounter++;
					map.put(tem[nodeLine1], mapCounter);
					left = mapCounter;
					match[left] = (tem[nodeLine1]);
				} else {
					left = map.get(tem[nodeLine1]);

				}

				if (!map.containsKey(tem[nodeLine2])) {
					mapCounter++;
					map.put(tem[nodeLine2], mapCounter);
					right = mapCounter;
					match[right] = (tem[nodeLine2]);
				} else {
					right = map.get(tem[nodeLine2]);
				}

				if (!graph[left].contains(right)) {
					graph[left].add(right);

				}
				if (!graph[right].contains(left)) {
					graph[right].add(left);

				}

			}
		}

	}

	public void normalize(String s, String sp, int nodeLine1, int nodeLine2, int WeightedLine, int startLine,
			int edgeNum, int nodeNum, boolean weighted) throws IOException {
		normalize_root(s, sp, nodeLine1, nodeLine2, WeightedLine, startLine, edgeNum, nodeNum, weighted);
	}

	public void normalize(String s, String sp, int nodeLine1, int nodeLine2, int WeightedLine, int startLine,
			boolean weighted) throws IOException {
		int nodeNum = -1, edgeNum = -1;
		normalize_root(s, sp, nodeLine1, nodeLine2, WeightedLine, startLine, edgeNum, nodeNum, weighted);
		/*
		 * this.nodeNum=getNodeNum(s,sp,nodeLine1,nodeLine2,startLine); graph = new
		 * ArrayList[nodeNum+1]; w = new ArrayList[nodeNum+1];
		 * 
		 * for(int i=1;i<graph.length;i++){ graph[i]=new ArrayList<Integer>(); w[i]=new
		 * ArrayList<Double>(); } BufferedReader a = new BufferedReader(new
		 * FileReader(s)); for(int i=0;i<startLine;i++){ a.readLine(); }
		 * 
		 * match = new String[nodeNum+1]; map = new HashMap<String, Integer>(); int
		 * mapCounter = 0;
		 * 
		 * for(int i=0;i<edgeNum;i++){ String[]tem=null; try{ tem =
		 * a.readLine().split(sp); }catch(Exception e){ System.out.println(); } int left
		 * = 0, right = 0; // to map the nodes into numbers
		 * if(!map.containsKey(tem[nodeLine1])){ mapCounter++; map.put(tem[nodeLine1],
		 * mapCounter); left = mapCounter; match[left] = (tem[nodeLine1]); }else{ left =
		 * map.get(tem[nodeLine1]); }
		 * 
		 * 
		 * if(!map.containsKey(tem[nodeLine2])){ mapCounter++; map.put(tem[nodeLine2],
		 * mapCounter); right = mapCounter; match[right] = (tem[nodeLine2]); }else{
		 * right = map.get(tem[nodeLine2]); } double weight= -1; if(weighted) weight =
		 * Double.parseDouble(tem[WeightedLine]); else weight=1;
		 * //graph[left][right]=weight; //graph[right][left]=weight;
		 * 
		 * if(!graph[left].contains(right)){ graph[left].add(right);
		 * w[left].add(weight); } if(!graph[right].contains(left)){
		 * graph[right].add(left); w[right].add(weight); }
		 * 
		 * }
		 */

	}

	public int getNodeNum(String s, String sp, int line1, int line2, int uselessLines) throws IOException {
		HashSet<String> h = new HashSet<String>();
		BufferedReader a = new BufferedReader(new FileReader(s));
		for (int i = 0; i < uselessLines; i++)
			a.readLine();
		String[] tem = null;
		String sa = a.readLine();
		edgeNum = 0;
		while (sa != null && !sa.equals("")) {
			tem = sa.split(sp);
			h.add(tem[line1]);
			try {
				h.add(tem[line2]);
			} catch (Exception e) {
				System.out.println(sa);
			}
			sa = a.readLine();
			edgeNum++;
			//if(edgeNum%(1397278/100)==0)
			//	System.out.print(edgeNum/(1397278/100)+"->");
		}
		return h.size();
	}

	// boolean reached[];
	// int reachNum;
	public int[] compSig;// the component-id of each node
	public int components = -1;// the number of components
	public int no = -1;// one member in the main component

	public int diameterAprox() throws IOException {
		boolean[] sig = new boolean[nodeNum + 1];
		for (int i = 1; i <= nodeNum; i++)
			sig[i] = true;
		return diameterAprox(sig);
		/*
		 * reachNum=0; reached = new boolean[nodeNum+1];
		 * 
		 * for(int i=1;i<=nodeNum;i++)reached[i]=false;
		 * 
		 * int dMax = 0; components = 0;
		 * 
		 * no=0;; compSig=new int[nodeNum+1]; for(int i=1;i<=nodeNum;i++){
		 * if(!reached[i]){ components++; int[]r = DFS(i, nodeNum, components); int d =
		 * (DFS(r[1],nodeNum, components)[0]); //System.out.println(d); if(d>dMax){
		 * dMax=d; no=r[1]; }
		 * 
		 * } }
		 * 
		 * System.out.println("Components: "+components);
		 * System.out.println("The biggest connected part ("+dMax+"): "+no);
		 * //writeFunc(nodeNum); return dMax;
		 */
	}

	public int diameterAprox(boolean[] sig) throws IOException {

		int dMax = 0;
		components = 0;
		int compMax = -1;

		no = 0;
		;
		compSig = new int[nodeNum + 1];
		for (int i = 1; i <= nodeNum; i++) {
			if (compSig[i] == 0 && sig[i]) {
				components++;
				int[] r = BFS(i, components, sig);//(diameter, member)
				if (r[0] == 0 || r[1] == 0) {
					continue;
				}
				int d = (BFS(r[1], components, sig)[0]);
				// compSig updated
				if (d > dMax) {
					dMax = d;
					no = r[1];
					compMax = components;
				}

			}
		}

		// System.out.println("Components: "+components);
		// System.out.println("The biggest connected part ("+dMax+"): "+no);
		// writeFunc(nodeNum);
		return dMax;
	}

	public int[] BFS(int s, int currentComp, boolean[] sigSearch) throws IOException {
		// find the node of max distance from s: distance + node
		BFS bfs = new BFS();
		bfs.searchPath(s, Integer.MAX_VALUE, graph, sigSearch);

		compSig[s] = currentComp;
		int maxDist = 0, maxId = 0;
		for (int i = 1; i < graph.length; i++) {
			if (bfs.jump[i] < Integer.MAX_VALUE && bfs.jump[i] > 0) {
				compSig[i] = currentComp;
			}
			if (bfs.jump[i] > maxDist && bfs.jump[i] < Integer.MAX_VALUE) {
				maxDist = bfs.jump[i];
				maxId = i;
			}
		}

		int[] r = new int[2];
		r[0] = maxDist;
		r[1] = maxId;
		return r;

		/*
		 * Queue<Integer>q = new LinkedList<Integer>(); if(!sigSearch[s]) {
		 * System.out.println("Node "+s+" is marked as false!"); return null; }
		 * q.add(s); Boolean sig[] = new Boolean [nodeNum+1]; for(int
		 * i=1;i<=nodeNum;i++){ if(sigSearch[i]){ sig[i]=false; }else{ sig[i]=true; } }
		 * int len [] = new int [nodeNum+1];
		 * 
		 * while(!q.isEmpty()){ int current = q.poll(); sig[current] = true; //scaned
		 * compSig[current]=currentComp; //reached[current] = true;
		 * if(graph[current]==null)continue;
		 * Iterator<Integer>it=graph[current].iterator(); while(it.hasNext()){ int i =
		 * it.next(); if(i==current){ System.out.println("Self-loop find in node "+i);
		 * continue; } if(sig[i])continue;//already searched q.add(i); sig[i] = true;
		 * //if(!reached[i]) // reachNum++; //reached[i] = true; compSig[i]=currentComp;
		 * len[i]=len[current]+1;
		 * 
		 * } }
		 * 
		 * 
		 * int max = 0; int maxId = 0; for (int i=0;i<nodeNum;i++){ if(len[i]>max){
		 * max=len[i]; maxId = i; } } int[]r2 = {max, maxId}; return r2;
		 */
	}

	public int[] BFS(int s, int nodeNum, int currentComp) throws IOException {
		if (nodeNum != graph.length - 1) {
			System.out.println("NodeNum is not comsistent with the graph size!");
		}
		boolean[] sig = new boolean[graph.length];
		for (int i = 1; i < graph.length; i++)
			sig[i] = true;
		return BFS(s, currentComp, sig);

		/*
		 * Queue<Integer>q = new LinkedList<Integer>(); q.add(s); Boolean sig[] = new
		 * Boolean [nodeNum+1]; for(int i=1;i<=nodeNum;i++)sig[i]=false; int len [] =
		 * new int [nodeNum+1];
		 * 
		 * 
		 * 
		 * while(!q.isEmpty()){ int current = q.poll(); sig[current] = true; //scaned
		 * compSig[current]=currentComp; reached[current] = true;
		 * if(graph[current]==null)continue;
		 * Iterator<Integer>it=graph[current].iterator(); while(it.hasNext()){ int i =
		 * it.next(); if(i!=current){ if(!sig[i]){ boolean sigt = false;
		 * if(current>i)if(graph[current].contains(i)){sigt=true;}
		 * if(current<i)if(graph[i].contains(current)){sigt=true;} if(sigt){ q.add(i);
		 * sig[i] = true; if(!reached[i]){ reachNum++;
		 * //if(reachNum%1000==1)System.out.println(reachNum);
		 * //if(reachNum==1000000){System.out.println("start writing");writeFunc(nodeNum
		 * );int[]r = {0, 0};return r;} } reached[i] = true; compSig[i]=currentComp;
		 * len[i]=len[current]+1;
		 * 
		 * } }
		 * 
		 * } } }
		 * 
		 * int max = 0; int maxId = 0; for (int i=0;i<nodeNum;i++){ if(len[i]>max){
		 * max=len[i]; maxId = i; } } int[]r = {max, maxId}; return r;
		 */
	}

	public int[] weightedShortestPath(int so, int ta) {
		double[] m = new double[nodeNum + 1];
		for (int i = 1; i <= nodeNum; i++)
			m[i] = Double.MAX_VALUE;
		m[so] = 0;
		ArrayList<Integer> S = new ArrayList<Integer>();
		ArrayList<Integer> U = new ArrayList<Integer>();
		S.add(so);
		for (int i = 1; i <= nodeNum; i++)
			if (i != so)
				U.add(i);

		while (!U.isEmpty()) {
			boolean ka = true;
			for (int i = 0; i < U.size(); i++) {
				int tem = U.get(i);
				if (!Double.isInfinite(tem))
					ka = false;
			}
			if (ka)
				break;

			// get the smallest m from U
			double nextCurrentValue = Integer.MAX_VALUE;
			int nextCurrent = 0;
			for (int i = 0; i < U.size(); i++) {
				int tem = U.get(i);
				if (m[tem] < nextCurrentValue) {
					nextCurrentValue = m[tem];
					nextCurrent = tem;
				}
			}
			U.remove(U.indexOf(nextCurrent));

			int current = nextCurrent;
			for (int i = 0; i < graph[current].size(); i++) {
				if (U.contains(graph[current].get(i))) {
					double tem = w[current].get(i) + m[current];
					if (m[graph[current].get(i)] > tem) {
						m[graph[current].get(i)] = tem;
					}
				}
			}
			System.out.print(current + ", ");

		}
		return null;

	}

	public void copy(graphReady g) {
		this.edgeNum = g.edgeNum;
		this.nodeNum = g.nodeNum;
		graph = new ArrayList[g.graph.length];
		w = new ArrayList[g.graph.length];
		for (int i = 1; i < graph.length; i++) {
			graph[i] = new ArrayList<Integer>();
			for (int j = 0; j < g.graph[i].size(); j++) {
				graph[i].add(g.graph[i].get(j));
				w[i].add(g.w[i].get(j));
			}
		}
	}

	public double getDegree() {
		double d = 0;
		for (int i = 1; i < graph.length; i++) {
			d += graph[i].size();
		}
		d /= (graph.length - 1);
		return d;
	}

	public void writeGraph(String writedir, String gtdir, String gtsp, boolean weighted) throws IOException {
		// the normalized version only write the biggest components into disk
		// first line: nodes num. + edges num. + ave.degree + diameter + |v int GT|
		// next: m edges

		BufferedWriter b = new BufferedWriter(new FileWriter(writedir));

		int diam = diameterAprox();
		if (components > 1) {
			// change the graph
			System.out.println("Start writing main component of: " + writedir);
			BufferedWriter b2 = new BufferedWriter(new FileWriter(gtdir + "_tem"));
			for (int i = 1; i < graph.length; i++) {
				if (compSig[i] != compSig[no])
					continue;
				for (int j = 0; j < graph[i].size(); j++) {
					int nei = graph[i].get(j);
					if (nei <= i)
						continue;
					if (!weighted)
						b2.write(match[i] + "\t" + match[nei] + "\n");
					else
						b2.write(match[i] + "\t" + match[nei] + "\t" + w[i].get(j) + "\n");
				}

			}
			b2.flush();
			b2.close();
			System.out.println("Start normalizing main component of: " + writedir);
			normalize(gtdir + "_tem", "\t", 0, 1, 2, 0, weighted);
		}

		double aveDegree = 0;
		for (int i = 1; i < graph.length; i++)
			aveDegree += graph[i].size();
		aveDegree /= nodeNum;

		System.out.println("Start writing normalized graph of: " + writedir);

		b.write("nodeNum: " + nodeNum + "\tedgNum: " + edgeNum + "\tave.degree: " + aveDegree + "\tdiameter: " + diam
				+ "\n");
		// b.write(nodeNum+"\t"+edgeNum+"\t"+aveDegree+"\t"+diam+"\n");
		for (int i = 1; i < graph.length; i++) {
			for (int j = 0; j < graph[i].size(); j++) {
				int nei = graph[i].get(j);
				if (nei <= i)
					continue;
				if (weighted)
					b.write(i + "\t" + nei + "\t" + w[i].get(j) + "\n");
				else
					b.write(i + "\t" + nei + "\n");
			}

		}
		b.flush();
		b.close();
		System.out.println("Graph write finished.");

		System.out.println("Start writing ground truth of: " + writedir);

		b = new BufferedWriter(new FileWriter(writedir + "_gt"));
		BufferedReader a = new BufferedReader(new FileReader(gtdir));
		String s = a.readLine();
		while (s != null) {
			String[] tem = s.split(gtsp);
			String t = "";
			boolean touchedSig = false;
			for (int i = 0; i < tem.length; i++) {
				if (map.containsKey(tem[i])) {
					int id = map.get(tem[i]);
					t += (id + "\t");
					touchedSig = true;
				} else {
					t += (-1 + "\t");
				}
			}
			t += "\n";
			if (touchedSig)
				b.write(t);
			s = a.readLine();
		}

		b.flush();
		b.close();
		System.out.println("Ground truth write finished.");

	}

}

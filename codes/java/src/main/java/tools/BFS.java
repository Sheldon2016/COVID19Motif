package tools;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

public class BFS {

	public int search(int s, int t, ArrayList<Integer>[] graph) {
		return searchPath(s,t,graph).size()-1;
		/*
		//ArrayList<Integer>path=new ArrayList<Integer>();
		if(s==t){
			System.out.println("s==t!");
			return 0;
		}
		Queue<Integer>q = new LinkedList<Integer>();
		Boolean sig[] = new Boolean [graph.length];
		for(int i=1;i<sig.length;i++){sig[i]=false;}
		//int last[] = new int[graph.length];
		//for(int i=0;i<nodeNum;i++)sig[i]=false;
		
		q.add(s);
		sig[s]=true;
		int dist = 0;
		while(!q.isEmpty()){
			int current = q.poll();
			dist++;
			//sig[current] = true; //scaned
			for(int i=0;i<graph[current].size();i++){
				int cn = graph[current].get(i);
				if(cn==t){
					return dist;
				}
				if(!sig[cn]){
					q.add(cn);
					sig[cn]=true;
				}
			}
			
		}
		if(!sig[t]){
			return 0;//not connexted
		}
		return dist;
		*/
	}
	
	public int[] searchByHop (int s, int hop, ArrayList<Integer>[] graph) {
		//return the jump list in which the i-th hop nodes from s are marked as i, i<=hop
		
		searchTheHop(s,hop,graph);
		return jump;
		
		/*
		//ArrayList<Integer>path=new ArrayList<Integer>();	
		Queue<Integer>q = new LinkedList<Integer>();
		Boolean sig[] = new Boolean [graph.length];
		jump = new int[graph.length];
		for(int i=1;i<sig.length;i++){sig[i]=false;jump[i]=Integer.MAX_VALUE;}
		int last[] = new int[graph.length];
		//for(int i=0;i<nodeNum;i++)sig[i]=false;
		
		q.add(s);
		sig[s]=true;
		jump[s]=0;
		
		if(hop==0){
			return jump;
		}
		
		while(!q.isEmpty()){
			int current = q.poll();
			//sig[current] = true; //scaned
			for(int i=0;i<graph[current].size();i++){
				int cn = graph[current].get(i);
				if(!sig[cn]){
					q.add(cn);
					sig[cn]=true;
					jump[cn]=jump[current]+1;
					if(jump[cn]>hop) {
						return jump;
					}
					last[cn]=current;
				}
			}
			
		}


		return jump;*/
	}
	public ArrayList<Integer> searchTheHop (int s, int hop, ArrayList<Integer>[] graph) {
		//return the nodes within the hop-th layer from node s
		
		ArrayList<Integer>layer=new ArrayList<Integer>();	
		Queue<Integer>q = new LinkedList<Integer>();
		Boolean sig[] = new Boolean [graph.length];
		jump = new int[graph.length];
		for(int i=1;i<sig.length;i++){sig[i]=false;jump[i]=Integer.MAX_VALUE;}
		int last[] = new int[graph.length];
		//for(int i=0;i<nodeNum;i++)sig[i]=false;
		
		q.add(s);
		sig[s]=true;
		jump[s]=0;
		
		if(hop==0){
			layer.add(s);
			return layer;
		}
		
		while(!q.isEmpty()){
			int current = q.poll();
			//sig[current] = true; //scaned
			for(int i=0;i<graph[current].size();i++){
				int cn = graph[current].get(i);
				if(!sig[cn]){
					q.add(cn);
					sig[cn]=true;
					jump[cn]=jump[current]+1;
					if(jump[cn]>hop) {
						return layer;
					}
					if(jump[cn]==hop) {
						layer.add(cn);
					}
					last[cn]=current;
				}
			}
			
		}


		return layer;
	}
	
	public ArrayList<Integer> searchKNN (int s, int k, ArrayList<Integer>[] graph) {
		//return the nodes within the top-k nearest nodes from node s
		
		Queue<Integer>q = new LinkedList<Integer>();
		Boolean sig[] = new Boolean [graph.length];
		jump = new int[graph.length];
		for(int i=1;i<sig.length;i++){sig[i]=false;jump[i]=Integer.MAX_VALUE;}
		int last[] = new int[graph.length];
		//for(int i=0;i<nodeNum;i++)sig[i]=false;
		
		q.add(s);
		sig[s]=true;
		jump[s]=0;
		
		int counter = 0;
		ArrayList<Integer>touchedNodes = new ArrayList();
		
		while(!q.isEmpty()){
			int current = q.poll();
			//sig[current] = true; //scaned
			for(int i=0;i<graph[current].size();i++){
				int cn = graph[current].get(i);
				if(!sig[cn]){
					q.add(cn);
					touchedNodes.add(cn);
					if(touchedNodes.size()==k)
						return touchedNodes;
					sig[cn]=true;
					jump[cn]=jump[current]+1;
					last[cn]=current;
				}
			}
			
		}


		return touchedNodes;
	}
	
	public int searchWioutST(int s, int t, ArrayList<Integer>[] graph) {
		if(!graph[s].contains(t)||!graph[t].contains(s)) {
			System.out.println("Edge "+s+"-"+t+" does not exist!" );
			return search(s,t,graph);
		}
		int idt = graph[s].indexOf(t);
		int ids = graph[t].indexOf(s);
		int res = -1;
		graph[s].remove(idt);
		graph[t].remove(ids);
		res = search(s,t,graph);
		graph[s].add(idt, t);
		graph[t].add(ids, s);
		return res;
		/*ArrayList<Integer>path=new ArrayList<Integer>();
		if(s==t){
			System.out.println("s==t!");
			return path.size();
		}
		Queue<Integer>q = new LinkedList<Integer>();
		Boolean sig[] = new Boolean [graph.length];
		for(int i=1;i<sig.length;i++){sig[i]=false;}
		int last[] = new int[graph.length];
		//for(int i=0;i<nodeNum;i++)sig[i]=false;
		
		q.add(s);
		sig[s]=true;
		
		while(!q.isEmpty()){
			int current = q.poll();
			//sig[current] = true; //scaned
			for(int i=0;i<graph[current].size();i++){
				int cn = graph[current].get(i);
				if((s==cn&&t==current)||(t==cn&&s==current))
					continue;
				if(cn==t){
					path.add(cn);
					while(last[current]!=0){
						path.add(current);
						current=last[current];
					}
					path.add(s);
					return path.size()-1;
				}
				if(!sig[cn]){
					q.add(cn);
					sig[cn]=true;
					last[cn]=current;
				}
			}
			
		}
		if(!sig[t]){
			return Integer.MAX_VALUE;
		}
		return path.size()-1;*/
	}
	
	public int jump[] = null;
	public ArrayList<Integer> searchPath(int s, int t, ArrayList<Integer>[] graph){
		int currentComp = 1;
		boolean[]sig = new boolean[graph.length];
		for(int i=1;i<graph.length;i++)
			sig[i] = true;
		return searchPath(s,t,graph,sig);
	}
	public ArrayList<Integer> searchPath(int s, int t, ArrayList<Integer>[] graph, boolean[]sigSearch) {
		ArrayList<Integer>path=new ArrayList<Integer>();
		if(s==t){
			System.out.println("s==t!");
			return path;
		}
		if(!sigSearch[s]) {
			System.out.println("Node "+s+" is marked as false!");
			return null;
		}
		int max = Integer.MAX_VALUE;
		if(t!=max&&!sigSearch[t]) {
			System.out.println("Node "+t+" is marked as false!");
			return null;
		}
		
		Queue<Integer>q = new LinkedList<Integer>();
		Boolean sig[] = new Boolean [graph.length];
		jump = new int[graph.length];
		for(int i=1;i<sig.length;i++){
			if(sigSearch[i]){
				sig[i]=false;
			}else{
				sig[i]=true;
			}
			jump[i]=Integer.MAX_VALUE;
			}
		int last[] = new int[graph.length];
		//for(int i=0;i<nodeNum;i++)sig[i]=false;
		
		q.add(s);
		sig[s]=true;
		jump[s]=0;
		
		while(!q.isEmpty()){
			int current = q.poll();
			//sig[current] = true; //scaned
			for(int i=0;i<graph[current].size();i++){
				int cn = graph[current].get(i);
				if(cn==t){
					path.add(cn);
					while(last[current]!=0){
						path.add(current);
						current=last[current];
					}
					path.add(s);
					return path;
				}
				if(!sig[cn]){
					q.add(cn);
					sig[cn]=true;
					jump[cn]=jump[current]+1;
					last[cn]=current;
				}
			}
			
		}

		if(t!=max&&!sig[t]){
			return new ArrayList<Integer>();
		}
		
		return path;
	}

}

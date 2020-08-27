package neo4jdriver;


import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.neo4j.procedure.Name;
import org.neo4j.procedure.UserFunction;

import datapre.covid19kg;
import mmc.Edge;
import mmc.GetMMCGraph;
import mmc.Graph;
import mmc.MainMMC;
import mmc.TestMMC;
import tools.CONF;
import tools.MInsFromGroups;
import tools.Motif;

public class MCLGF {
	
	static covid19kg kg = null;
	
	public MCLGF() throws IOException {
		kg = new covid19kg(CONF.mainDir);
	}
	
	@UserFunction
	public List<List<Integer>> MCLQVis(@Name("value") String degVec, @Name("value") String labels, @Name("value") String snidStr, @Name("value") String slabel) throws IOException, InterruptedException {
		
		List<List<Integer>> mclique = MCLQ(degVec, labels, snidStr, slabel);
		//return a set of motif-instances for visualization
		List<List<Integer>> motifInsForMMC = new MInsFromGroups().getInsFromGroups(mclique);
		return motifInsForMMC;
	}
	
    

    @UserFunction
	public List<List<Integer>> MCLQ(@Name("value") String degVec, @Name("value") String labels, @Name("value") String snidStr, @Name("value") String slabel) throws IOException, InterruptedException {
    	
    	int snid = Integer.parseInt(snidStr);
    	
		String str = CONF.mmcDir;
		GetMMCGraph gmmc = new GetMMCGraph();
		
    	List<Integer> fres = new ArrayList();
    	int slabelid = kg.getLabelID(slabel);
    	int sid = kg.node[slabelid].get(snid);
    	int smmcid = gmmc.eid2id.get(slabelid+","+sid);
    	
    	//generate the file in mmc format

		
		BasicConfigurator.configure();
		Logger logger = Logger.getLogger(MCLGF.class);
		logger.info("start mclique searching thread");
		MainMMC mmc = new MainMMC (str);
		
		//get the motif object
		int[]labelsID = kg.getlabelsfromString(labels);
		ArrayList<Integer>labelsIDArr = new ArrayList();
		for(int i=0;i<labelsID.length;i++) {
			labelsIDArr.add(labelsID[i]);
		}
		Graph g = getGraph(degVec, labelsID);
		List<List<List<Integer>>> cliques = new ArrayList();
		
		mmc.doMCC(g, smmcid);// the source node of the motif-clique
		Vector<HashSet<Integer>> res = mmc.Utilities.cliques;
		int D = -1;
		List<List<Integer>>maxAClique = null;
		for(int i=0;i<res.size();i++) {
			HashSet<Integer>acliq = res.get(i);
			Iterator<Integer>it = acliq.iterator();
			List<List<Integer>> aclique = new ArrayList();
			for(int j=0;j<labelsID.length;j++) {
				List<Integer>agroup = new ArrayList();
				aclique.add(agroup);
			}
			while(it.hasNext()) {
				int cn = it.next();
				String[]tem = gmmc.id2eid.get(cn).split(",");
				int cnlabelID = Integer.parseInt(tem[0]);
				int cnID = Integer.parseInt(tem[1]);
				int cnNID = kg.nodeNID[cnlabelID].get(cnID);
				aclique.get(labelsIDArr.indexOf(cnlabelID)).add(cnNID);
			}
			
			if(acliq.size()>D) {
				D = acliq.size();
				maxAClique = aclique;
			}
			
			cliques.add(aclique);
		}
		
		//return the motif-clique of biggest size 
		
    	
		return maxAClique;
    }

	private Graph getGraph(String degVec, int[]labelsID) {
		
		Graph k = new Graph();
		k.nodeNum=degVec.length();
		for(int i=0; i<k.nodeNum;i++){
			k.adjList.add(new HashSet<Integer>());
			k.labels.add(labelsID[i]);
		}
		
		Motif mf = new Motif(degVec);
		k.edgeNum=mf.edgeNum*2;
		
		for(int i=0;i<mf.motif.length;i++) {
			for(int j=0;j<mf.motif[i].size();j++) {
				k.edges.add(new Edge(i, mf.motif[i].get(j)));
			}
		}

		for(int i=0; i<k.edgeNum; i++){
			int a=k.edges.get(i).from;
			int b=k.edges.get(i).to;
			k.adjList.get(a).add(b);
		}
			
		return k;
	}
    
}

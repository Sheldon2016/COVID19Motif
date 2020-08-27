package neo4jdriver;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.neo4j.procedure.Name;
import org.neo4j.procedure.UserFunction;

import algo.GetMAMGraph;
import datapre.covid19kg;
import mmc.GetMMCGraph;
import tools.BFS;
import tools.CONF;
import tools.FileOps;
import tools.MInsFromGroups;
import tools.graphReady;

public class MCCF {
	
	static covid19kg kg = null;
	static GetMAMGraph gmam = null;
	static graphReady g = null;
	
	public MCCF() throws IOException {
		kg = new covid19kg(CONF.mainDir);
	}
	
	
	@UserFunction
	public List<List<Integer>> MCCVis(@Name("value") String degVec, @Name("value") String labels, @Name("value") String snidStr, @Name("value") String slabel) throws IOException, InterruptedException {
		
		List<List<Integer>> mcc = MCC(degVec, labels, snidStr, slabel);
		//return a set of motif-instances for visualization
		List<List<Integer>> motifInsForMMC = new MInsFromGroups().getInsFromGroups(mcc);
		return motifInsForMMC;
	}
	
	
	@UserFunction
	public List<List<Integer>> MCC(@Name("value") String degVec, @Name("value") String labels, @Name("value") String snidStr, @Name("value") String slabel) throws IOException, InterruptedException {
	    
		List<List<Integer>> res = new ArrayList();
	   
		int snid = Integer.parseInt(snidStr);
	    int slabelid = kg.getLabelID(slabel);
	    int sid = kg.node[slabelid].get(snid);
	    
	    gmam = new GetMAMGraph();
	    graphReady g = gmam.getMAMGraph(degVec, labels, kg);
	    

	    int smamid = gmam.eid2id.get(slabelid+","+sid);
	    //note that the id in graphready is smmcid-1, since graphready starts from 1!
	    
	    int[]labelsID = kg.getlabelsfromString(labels);
		ArrayList<Integer>labelsIDArr = new ArrayList();
		for(int i=0;i<labelsID.length;i++) {
			labelsIDArr.add(labelsID[i]);
			List<Integer>ares = new ArrayList();
			res.add(ares);
		}
		
		
	    int max = Integer.MAX_VALUE;
	    
    	BFS bfs = new BFS();
    	bfs.search(smamid+1, Integer.MAX_VALUE, g.graph);
	    
    	for(int i=1;i<bfs.jump.length;i++) {
    		if(bfs.jump[i]==max)
    			continue;
    		
    		int csmamid = i-1;
    		String[]tem = gmam.id2eid.get(csmamid).split(",");
    		int clabel = Integer.parseInt(tem[0]);
    		int csid = Integer.parseInt(tem[1]);
    		int csnid = kg.nodeNID[clabel].get(csid);
    		//FileOps.jout(labelsIDArr.toString()+": "+clabel);
    		res.get(labelsIDArr.indexOf(clabel)).add(csnid);
    		
    	}
    	
    	
    	return res;
	   }

	

}

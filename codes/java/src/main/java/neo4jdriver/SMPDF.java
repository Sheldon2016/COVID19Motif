package neo4jdriver;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.neo4j.procedure.Name;
import org.neo4j.procedure.UserFunction;

import algo.GetMAMGraph;
import datapre.covid19kg;
import tools.BFS;
import tools.CONF;
import tools.FileOps;
import tools.MInsFromGroups;
import tools.graphReady;

public class SMPDF {

	static covid19kg kg = null;
	static GetMAMGraph gmam = null;
	static graphReady g = null;
	public static List<List<Integer>> path = null;
	
	public SMPDF() throws IOException {
		kg = new covid19kg(CONF.mainDir);
	}
	
	@UserFunction
	public List<List<Integer>> SMPDVis(@Name("value") String degVec, @Name("value") String labels, @Name("value") String snidStr, @Name("value") String slabel, @Name("value") String tnidStr, @Name("value") String tlabel) throws IOException, InterruptedException {
		
		SMPD(degVec, labels, snidStr, slabel, tnidStr, tlabel);
		//return a set of motif-instances for visualization
		List<List<Integer>> motifInsForMMC = new MInsFromGroups().getInsFromGroups(path);
		return motifInsForMMC;
	}
	
	
	@UserFunction
	public String SMPD(@Name("value") String degVec, @Name("value") String labels, @Name("value") String snidStr, @Name("value") String slabel, @Name("value") String tnidStr, @Name("value") String tlabel) throws IOException, InterruptedException {
	    
		path = new ArrayList();
	   
		int snid = Integer.parseInt(snidStr);
		int tnid = Integer.parseInt(tnidStr);
	    int slabelid = kg.getLabelID(slabel);
	    int sid = kg.node[slabelid].get(snid);
	    int tlabelid = kg.getLabelID(tlabel);
	    int tid = kg.node[tlabelid].get(tnid);
	    
	    gmam = new GetMAMGraph();
	    graphReady g = gmam.getMAMGraph(degVec, labels, kg);
	    //graphready starts from 1!

	    int smamid = gmam.eid2id.get(slabelid+","+sid);
	    int tmamid = gmam.eid2id.get(tlabelid+","+tid);
	    
	    int[]labelsID = kg.getlabelsfromString(labels);
		ArrayList<Integer>labelsIDArr = new ArrayList();
		for(int i=0;i<labelsID.length;i++) {
			labelsIDArr.add(labelsID[i]);
			List<Integer>ares = new ArrayList();
			path.add(ares);
		}
		
    	BFS bfs = new BFS();
    	ArrayList<Integer>pathbfs = bfs.searchPath(smamid, tmamid, g.graph);
	    
    	for(int i=0;i<pathbfs.size();i++) {
    		   		
    		int csmamid = pathbfs.get(i);
    		String[]tem = gmam.id2eid.get(csmamid).split(",");
    		int clabel = Integer.parseInt(tem[0]);
    		int csid = Integer.parseInt(tem[1]);
    		int csnid = kg.nodeNID[clabel].get(csid);
    		//FileOps.jout(kg.nodes[clabel]+": "+csnid);
    		path.get(labelsIDArr.indexOf(clabel)).add(csnid);
    		
    	}
    	
    	
    	return pathbfs.size()+"";
	   }
	
}

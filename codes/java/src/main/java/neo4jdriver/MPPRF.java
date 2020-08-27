package neo4jdriver;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.neo4j.procedure.Name;
import org.neo4j.procedure.UserFunction;

import algo.GetMAMGraph;
import datapre.covid19kg;
import tools.CONF;
import tools.graphReady;

public class MPPRF {

	static covid19kg kg = null;
	static GetMAMGraph gmam = null;
	static graphReady g = null;
	
	public MPPRF() throws IOException {
		kg = new covid19kg(CONF.mainDir);
	}
	
	
	@UserFunction
	public List<List<String>> MPPR(@Name("value") String degVec2, @Name("value") String labels2, @Name("value") String snidStr, @Name("value") String slabel, @Name("value") String tlabel, @Name("value") String iterationNumStr, @Name("value") String damplingfactorStr) throws IOException, InterruptedException {
	    //to generate rankings for all tnids with label tlabel from snid

		int snid = Integer.parseInt(snidStr);
	    int slabelid = kg.getLabelID(slabel);
	    int sid = kg.node[slabelid].get(snid);
	    
	    int tlabelid = kg.getLabelID(tlabel);
	    
	    int iterationNum = Integer.parseInt(iterationNumStr);
	    double damplingfactor = Double.parseDouble(damplingfactorStr);
	    
	    gmam = new GetMAMGraph();
	    graphReady g = gmam.getMAMGraph(degVec2, labels2, kg);
	    

	    int smamid = gmam.eid2id.get(slabelid+","+sid);
	    //note that the id in graphready is smmcid-1, since graphready starts from 1!
	    
	    //get tnids with tlabel
	    ArrayList<Integer>tmamids = new ArrayList();
	    for(int i=0;i<kg.nodeNID[tlabelid].size();i++) {
	    	tmamids.add(gmam.eid2id.get(tlabelid+","+i));
	    }
	    
	    
	    double[]mppr = rootedPageRank(smamid, tmamids, g.graph, iterationNum, damplingfactor);
		int min = Integer.MIN_VALUE;
		
	    
		List<List<String>> res = new ArrayList();
	    for(int i=0;i<mppr.length;i++) {
	    	if(mppr[i]<=min)
	    		continue;
	    		
	    	List<String> ares= new ArrayList();
	    	
	    	ares.add(kg.nodeNID[tlabelid].get(i)+"");
	    	ares.add(kg.nodeName[tlabelid].get(i));
	    	ares.add(mppr[i]+"");
	    	
	    	res.add(ares);
	    	//System.out.println(ares);
	    }
    	
    	
    	return res;
	   }
	
	double[] rootedPageRank(int s, ArrayList<Integer>T, ArrayList<Integer>[] graph, int walkNum, double alpha) {
		double res[] = new double[T.size()];
		
		//int hittingNum = 0;
		int cn = s;
		for (int i = 0; i < walkNum; i++) {
			if (T.contains(cn))
				res[T.indexOf(cn)]++;
			double samp = Math.random();
			if (samp < alpha) {
				cn = s;
				continue;
			}
			int cns = graph[cn].size();
			int pickID = (int) (Math.random() * cns);
			cn = graph[cn].get(pickID);
		}
		
		for(int i=0;i<res.length;i++){
			res[i] = (double) 0 - (double) walkNum / res[i];
			}
		return res;
	}
	
	
	
}

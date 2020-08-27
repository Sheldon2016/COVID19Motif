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
import tools.MInsFromGroups;
import tools.graphReady;

public class MGDF {
	
	static covid19kg kg = null;
	static GetMAMGraph gmam = null;
	static graphReady g = null;
	
	public MGDF() throws IOException {
		kg = new covid19kg(CONF.mainDir);
	}
	
	
	
	@UserFunction
	public String MGD(@Name("value") String degVec, @Name("value") String labels) throws IOException, InterruptedException {
	
	    gmam = new GetMAMGraph();
	    graphReady g = gmam.getMAMGraph(degVec, labels, kg);
	       
	    return g.diameterAprox()+"";
	    
	   }
	

}

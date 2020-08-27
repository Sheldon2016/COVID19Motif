package neo4jdriver;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.neo4j.procedure.Name;
import org.neo4j.procedure.UserFunction;

import algo.MotifMatch;
import datapre.covid19kg;
import tools.CONF;
import tools.Motif;

public class MCOUNTF {

	
	@UserFunction
    public List<List<Integer>> MCOUNT(@Name("value") String degVec, @Name("value") String labels) throws IOException {
    	
    	Motif mf = new Motif(degVec);
    	
    	MotifMatch mm = new MotifMatch();
		covid19kg kg = new covid19kg(CONF.mainDir);
		//covid19kg kg = new covid19kg("C:\\Users\\Sheldon\\Documents\\GitHub\\covid19kg\\data\\HPO\\");
		//note that mf.motif is ranked by node degree
		
		int[] labelsInt=kg.getlabelsfromString(labels);
		mf.assignLabels(labelsInt);
		
		ArrayList<ArrayList<Integer>>motifIns = mm.match(kg,mf);// get raw ids rather than NIDs!
		//mm.motifIns2String(motifIns, mf, kg);
        
		List<List<Integer>> res = new ArrayList();
		for(int i=0;i<motifIns.size();i++) {
			List<Integer> ares = new ArrayList();
			for(int j=0;j<motifIns.get(i).size();j++) {
				int id = motifIns.get(i).get(j);
				int label = labelsInt[j];
				
				ares.add(kg.nodeNID[label].get(id));

				//ares.add(id);
				
				//return the nid rather than id!
			}
			//String s = mm.output(motifIns.get(i), mf.motifLabels, kg);
			//System.out.println(s);
			res.add(ares);
		}
		
		return res;
    }
    
    
    
}

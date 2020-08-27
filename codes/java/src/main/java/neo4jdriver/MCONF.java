package neo4jdriver;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.neo4j.procedure.Name;
import org.neo4j.procedure.UserFunction;

import algo.MotifMatch;
import datapre.covid19kg;
import tools.BFS;
import tools.CONF;
import tools.MInsFromGroups;
import tools.Motif;
import tools.graphReady;

public class MCONF {
	
	static covid19kg kg = null;
	
	public MCONF() throws IOException {
		kg = new covid19kg(CONF.mainDir);
	}	
	
	@UserFunction
	public double MCON(@Name("value") String degVec, @Name("value") String labels, @Name("value") String SnidStr, @Name("value") String Slabel) throws IOException, InterruptedException {
	    
		String snidsStr[] = SnidStr.split(",");
		String slabels[] = Slabel.split(",");
		int[] snids = new int[snidsStr.length];
		
		for(int i=0;i<snids.length;i++) {
			snids[i] = Integer.parseInt(snidsStr[i]);
		}
		
		MCOUNTF a = new MCOUNTF();
		List<List<Integer>> motifIns = a.MCOUNT(degVec, labels); //store nids in each ins.
		int cutnum = 0, vols = 0, volt = 0;
		
		for(int i=0;i<motifIns.size();i++) {
			List<Integer> ins = motifIns.get(i);
			
			int snum = insInS(ins, labels, snids, slabels);
			int tnum = ins.size() - snum;
			vols += snum;
			volt += tnum;
			
			if(snum!=0&&tnum!=0)
				cutnum++;
		}
		
		double res = cutnum/Math.min(vols, volt);
	   
    	return res;
	   }
	
	int insInS(List<Integer> ins, String labels, int snids[], String slabels[]) {
		String[]label = labels.split(",");
		//res: number of nodes in S
		int res = 0;
		for(int i=0; i<ins.size(); i++) {
			for(int j=0;j<snids.length;j++) {
				if(label[i].equals(slabels[j])&&ins.get(i)==snids[j]) {
					res++;
				}
			}
		}
		
		return res;
	}

}

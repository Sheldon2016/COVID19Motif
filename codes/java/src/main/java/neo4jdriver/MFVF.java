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

public class MFVF {
	
	@UserFunction
    public List<Integer> MFVN(@Name("value") String degVecStr, @Name("value") String labelsStr, @Name("value") String snidStr, @Name("value") String slabel) throws IOException {
    	covid19kg kg = new covid19kg(CONF.mainDir);
    	String[]degVec = degVecStr.split("[|]");
    	String[]labels = labelsStr.split("[|]");
    	int snid = Integer.parseInt(snidStr);
    	
    	List<Integer> fres = new ArrayList();
    	for(int i=0;i<degVec.length;i++) {
    		fres.add(0);
    	}
    	
    	int slabelid = kg.getLabelID(slabel);
    	//int sid = kg.node[slabelid].get(snid);
    	
    	for(int i=0;i<degVec.length;i++) {
    		MCOUNTF mc = new MCOUNTF();
    		List<List<Integer>> res = mc.MCOUNT(degVec[i], labels[i]);
    		
    		for(int j=0;j<res.size();j++) {
    			List<Integer> ins = res.get(j);
    			if(kg.IfInsContain(ins, snid, labels[i], slabelid))
    				fres.set(i, fres.get(i)+1);
    		}
    		
    	}
    	
		return fres;
    }

    @UserFunction
	public List<Integer> MFV(@Name("value") String degVecStr, @Name("value") String labelsStr, @Name("value") String snidStr,  @Name("value")String slabel, @Name("value") String tnidStr,  @Name("value")String tlabel) throws IOException {
    	covid19kg kg = new covid19kg(CONF.mainDir);
    	List<Integer> fres = new ArrayList();
    	
    	String degVec[] = degVecStr.split("[|]"), labels[] = labelsStr.split("[|]");
    	int snid = Integer.parseInt(snidStr), tnid = Integer.parseInt(tnidStr);
    	
    	for(int i=0;i<degVec.length;i++) {
    		fres.add(0);
    	}
    	
    	int slabelid = kg.getLabelID(slabel), tlabelid = kg.getLabelID(tlabel);
    	//int sid = kg.node[slabelid].get(snid), tid = kg.node[tlabelid].get(tnid);
    	
    	for(int i=0;i<degVec.length;i++) {
    		MCOUNTF mc = new MCOUNTF();
    		List<List<Integer>> res = mc.MCOUNT(degVec[i], labels[i]);
    		// note that mcount returns the list of NIDs
    		
    		//System.out.println(res.toString());
    		
    		for(int j=0;j<res.size();j++) {
    			List<Integer> ins = res.get(j);
    			
    			if(kg.IfInsContain(ins, snid, labels[i], slabelid)&&kg.IfInsContain(ins, tnid, labels[i], tlabelid))
    				fres.set(i, fres.get(i)+1);
    		}
    		
    	}
    	
    	return fres;
    }
}

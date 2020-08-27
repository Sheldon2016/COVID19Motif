package neo4jdriver;


import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.neo4j.procedure.Name;
import org.neo4j.procedure.UserFunction;

import algo.MotifMatch;
import datapre.covid19kg;
import tools.CONF;

public class MAMF {
	
	static covid19kg kg = null;
	public MAMF() throws IOException {
		kg = new covid19kg(CONF.mainDir);
	}
	
	@UserFunction
    public List<List<String>> MAM(@Name("value") String degVec2, @Name("value") String labels2) throws IOException {
    	
    	List<List<String>> res = new ArrayList();
    	String[]degVecs = degVec2.split("[|]");
    	String[]labelss = labels2.split("[|]");
    	
    	HashMap<String, Integer> hp = new HashMap();
    	
    	for(int q=0;q<degVecs.length;q++) {
    		MCOUNTF mc = new MCOUNTF();
        	List<List<Integer>> InsSet = mc.MCOUNT(degVecs[q], labelss[q]);
        	int[]labelsID = kg.getlabelsfromString(labelss[q]);
        		
        	for(int j=0;j<InsSet.size();j++) {
        		List<Integer> ins = InsSet.get(j);
        		for(int i=0;i<ins.size();i++) {
        			int snid = ins.get(i);
        			int slabelID = labelsID[i];
        			for(int k=i+1;k<ins.size();k++) {
        				int tnid = ins.get(k);
        				int tlabelID = labelsID[k];
        				String key = snid+","+slabelID+","+tnid+","+tlabelID;
        				if(tnid<snid)
        					key = tnid+","+tlabelID+","+snid+","+slabelID;
        				if(hp.containsKey(key)) {
        					hp.put(key, hp.get(key)+1);
        				}else {
        					hp.put(key, 1);
        				}
        			}
        		}
        	}
    	}
    	
    	Iterator<Entry<String, Integer>> it = hp.entrySet().iterator();
    	while(it.hasNext()) {
    		Entry e = it.next();
    		String[]tem = (e.getKey()+"").split(","); 
    		// snid+","+slabelID+","+tnid+","+tlabelID+","+count
    		List<String>ares = new ArrayList();
    		ares.add(tem[0]);
    		ares.add(kg.nodes[Integer.parseInt(tem[1])]);
    		ares.add(tem[2]);
    		ares.add(kg.nodes[Integer.parseInt(tem[3])]);
    		ares.add(e.getValue()+"");
    		res.add(ares);
    		
    		//System.out.println(e.getKey() +"\t"+ e.getValue());
    	}
    		
    	
    	
		return res;
    }
}

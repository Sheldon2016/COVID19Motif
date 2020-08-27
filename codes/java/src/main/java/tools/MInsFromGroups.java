package tools;

import java.util.ArrayList;
import java.util.List;

public class MInsFromGroups {
	
	List<List<Integer>> motifInsForMMC = null;
	
	public List<List<Integer>> getInsFromGroups(List<List<Integer>>GroupOfNodes){
		motifInsForMMC = new ArrayList();
		InsFromGroups(GroupOfNodes, 0, new ArrayList());
		return motifInsForMMC;
	}
	
	void InsFromGroups(List<List<Integer>> mclique, int clayer, List<List<Integer>> cres) {
    	if(clayer==mclique.size()-1) {
    		for(int i=0;i<cres.size();i++) {
    			List<Integer>cins = cres.get(i);
    			for(int j=0;j<mclique.get(clayer).size();j++) {
    				List<Integer>ins = new ArrayList();
    				ins.addAll(cins);
    				ins.add(mclique.get(clayer).get(j));
    				motifInsForMMC.add(ins);
    			}
    		}
    	}else {
    		List<List<Integer>>ncres = new ArrayList();
    		if(cres==null||cres.size()==0) {
    			//the first layer: to search the first group in mclique
    			for(int j=0;j<mclique.get(clayer).size();j++) {
    				List<Integer>ins = new ArrayList();
    				ins.add(mclique.get(clayer).get(j));
    				ncres.add(ins);
    			}
    		}else {
    			for(int i=0;i<cres.size();i++) {
        			List<Integer>cins = cres.get(i);
        			for(int j=0;j<mclique.get(clayer).size();j++) {
        				List<Integer>ins = new ArrayList();
        				ins.addAll(cins);
        				ins.add(mclique.get(clayer).get(j));
        				ncres.add(ins);
        			}
        		}
    		}
    		clayer++;
    		InsFromGroups(mclique, clayer, ncres);
    	}
	}
}

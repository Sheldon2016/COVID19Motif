package algo;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import datapre.covid19kg;
import neo4jdriver.MAMF;
import tools.CONF;
import tools.FileOps;
import tools.graphReady;

public class GetMAMGraph {
	
	public HashMap<String,Integer>eid2id = null;
	public ArrayList<String>id2eid = null;
	public int nodeNum = 0, edgeNum = 0;
	static graphReady g = null;
	public GetMAMGraph() {
		eid2id = new HashMap();//labelID+","+eid(id in each file)
		id2eid = new ArrayList();
	}
	
	public graphReady getMAMGraph(String degVec2, String labels2, covid19kg kg) throws IOException {
		MAMF mam = new MAMF();
		BufferedWriter b = FileOps.BWriter(CONF.mamDir);
		List<List<String>>edges = mam.MAM(degVec2, labels2);
		
		nodeNum = 0;
		edgeNum = 0;
		int counter = 1;
		id2eid.add("");
		System.out.println("Start loading the higher-order graph.");
		//int nodeCounter = 0;
		for(int ii=0;ii<edges.size();ii++) {
			// snid+","+slabelID+","+tnid+","+tlabelID+","+count
			List<String>tem = edges.get(ii);
			int snid = Integer.parseInt(tem.get(0));
			int slabelID = kg.getLabelID(tem.get(1));
			int sid = kg.node[slabelID].get(snid);
			String skey = slabelID+","+sid;
			int smamID = -1;
			if(!eid2id.containsKey(skey)) {
				eid2id.put(skey, counter);
				id2eid.add(skey);
				smamID = counter;
				counter++;
			}else {
				smamID = eid2id.get(skey);
			}
			
			int tnid = Integer.parseInt(tem.get(2));
			int tlabelID = kg.getLabelID(tem.get(3));
			int tid = kg.node[tlabelID].get(tnid);
			String tkey = tlabelID+","+tid;
			int tmamID = -1;
			if(!eid2id.containsKey(tkey)) {
				eid2id.put(tkey, counter);
				id2eid.add(tkey);
				tmamID = counter;
				counter++;
			}else {
				tmamID = eid2id.get(tkey);
			}
			
			int count = Integer.parseInt(tem.get(4));
			b.write(smamID+","+tmamID+","+count+"\n");
			edgeNum++;
		}
		
		nodeNum = counter -1;
		System.out.println("NodeNum: "+nodeNum+", EdgeNum: "+edgeNum);
		
		b.flush();
		b.close();
		
		graphReady g = new graphReady();
    	BufferedReader a = FileOps.BRead(CONF.mamDir);
    	g.readGraph(CONF.mamDir, ",", 0, 1, 2, 0, edgeNum, nodeNum, true, false);
		
		return g;
	}
}

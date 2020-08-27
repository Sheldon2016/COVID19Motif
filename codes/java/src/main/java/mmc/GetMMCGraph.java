package mmc;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import datapre.covid19kg;
import tools.CONF;
import tools.FileOps;

public class GetMMCGraph {

	public HashMap<String,Integer>eid2id = null;
	public ArrayList<String>id2eid = null;
	public GetMMCGraph() throws IOException {
		// to check if MMC graph exists; if not, generate it
		covid19kg kg = new covid19kg(CONF.mainDir);
		genMMC(kg);
		/*
		BufferedReader a = null;
		try {
			a = FileOps.BRead(CONF.mmcDir);
			System.out.println("MMC data loaded.");
		}catch(Exception e) {
			System.out.println("MMC data not found, start generating.");
			genMMC(kg);
			a = FileOps.BRead(CONF.mmcDir);
			System.out.println("MMC data loaded.");
		}
		*/
	}

	private void genMMC(covid19kg kg) throws IOException {
		BufferedWriter b = FileOps.BWriter(CONF.mmcDir);
		eid2id = new HashMap();//labelID+","+eid(id in each file)
		id2eid = new ArrayList();
		
		String res = "";
		int nodeNum = 0, edgeNum = 0, labelNum = kg.nodes.length;
		int nodeCounter = 0;
		for(int i=0;i<kg.nodeNID.length;i++) {
			nodeNum += kg.nodeNID[i].size();
			for(int j=0;j<kg.nodeNID[i].size();j++) {
				eid2id.put(i+","+j, nodeCounter);
				id2eid.add(i+","+j);
				res+=(nodeCounter+"\t"+kg.nodeName[i].get(j)+"\n");
				nodeCounter ++;
			}
		}
		
		System.out.println("NodeNum: "+nodeNum+", current node counter: "+nodeCounter);
		
		
		for(int i=0;i<kg.edge.length;i++) {
			for(int j=0;j<kg.edge.length;j++) {
				ArrayList<Integer>[] cgraph = kg.edge[i][j];
				if(cgraph==null)
					continue;
				for(int k=0;k<cgraph.length;k++) {
					int sid = eid2id.get(i+","+k);
					if(cgraph[k]==null)
						continue;
					for(int p=0;p<cgraph[k].size();p++) {
						int tid = eid2id.get(j+","+cgraph[k].get(p));
						res += (sid+"\t"+tid+"\n");
						edgeNum ++;
					}
				}
			}
		}
		
		for(int i=0;i<kg.nodes.length;i++) {
			res+= (i+"\t"+kg.nodes[i]+"\n");
		}
		
		for(int i=0;i<id2eid.size();i++) {
			res+=(id2eid.get(i).split(",")[0]+"\n");
		}
		
		b.write(nodeNum+"\t"+edgeNum+"\t"+labelNum+"\n"+res);
		
		b.flush();
		b.close();
	}

}

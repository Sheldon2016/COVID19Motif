package datapreJSON;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.ParserConfig;

public class geneCovid10 {

	static String mainDir="C:\\Users\\Sheldon\\Documents\\GitHub\\COVID19Motif\\ÐÂ¹ÚÖªÊ¶Í¼Æ×\\Phylogeny\\";
	static int dataid = -1;
	static String[][]parameters = null;
	
	
	public static void main(String[] args) throws IOException {
		
		parameters = new String[4][3];// dir, nodeNum edgeNum nodeLabelNum, nodeLabel
		parameters[0][0] = "0. ²¡¶¾·ÖÀàÍ¼Æ×v2.0"; parameters[0][1] = "205550\t1288731\t13\n"; parameters[0][2] = "";
		parameters[1][0] = "1. ¿¹²¡¶¾Ò©ÎïÍ¼Æ×v1.0"; parameters[1][1] = "7818\t34219\t5\n"; parameters[1][2] = "0\tHost\n1\tVirus\n2\tVirusProtein\n3\tHostProtein\n4\tDrug\n";
		parameters[2][0] = "2. ²¡¶¾Ç×Ôµ¹ØÏµÍ¼Æ×v1.2"; parameters[2][1] = "7316\t19186\t5\n"; parameters[2][2] = "0\tState\n1\tBranch\n2\tHost\n3\tCountry\n4\tStrain\n";
		parameters[3][0] = "3. ÐÂ¹Ú»ù±¾ÐÅÏ¢Í¼Æ×"; parameters[3][1] = "470\t728\t4\n"; parameters[3][2] = "0\tVirusProtein\n1\tHost\n2\tVirus\n3\tGene\n";
		
		dataid = 3;
		generateMCE();
		//findGenes();
		
		
		
		
	}
	public static void generateMCE() throws IOException {
		//get data format for MC-Explorer
		mainDir += parameters[dataid][0]+"\\";
		String nodeDir = mainDir + "phylogeny.lg.nodes", edgeDir = mainDir + "phylogeny.lg.edges";
		ArrayList<Integer>nodeLabelID = new ArrayList();
		BufferedReader a = new BufferedReader(new FileReader(nodeDir));
		BufferedWriter b = new BufferedWriter(new FileWriter(mainDir+"covid19-"+dataid));
		
		b.write(parameters[dataid][1]);
		
		String strs = a.readLine();
		while(strs!=null&&!strs.equals("")) {
			String[]tem=strs.split(",");
			int labelid = Integer.parseInt(tem[1]);
			nodeLabelID.add(labelid);
			b.write(tem[0]+"\t"+tem[2]+"\n");
			strs=a.readLine();
		}
		
		a.close();
		a=new BufferedReader(new FileReader(edgeDir));
		strs = a.readLine();
		while(strs!=null&&!strs.equals("")) {
			String[]tem=strs.split(",");
			b.write(tem[0]+"\t"+tem[1]+"\n");
			strs=a.readLine();
		}
		
		b.write(parameters[dataid][2]);
		
		for(int i=0;i<nodeLabelID.size();i++) {
			b.write(nodeLabelID.get(i)+"\n");
		}
		
		b.flush();
		b.close();
		
		
	}
	
	public static void findGenes() throws IOException {
		String nodeIDDir = mainDir+parameters[1][0]+"\\mappings.lg";
		BufferedReader a = new BufferedReader(new FileReader(nodeIDDir));
		a.readLine();
		ArrayList<String>nodeIDName = new ArrayList();
		
		String strs = a.readLine();
		while(strs!=null&&!strs.equals("")) {
			String[]tem=strs.split(" = ");
			int id = Integer.parseInt(tem[0]);
			nodeIDName.add(tem[1]);
			strs=a.readLine();
		}
		a.close();
		
		nodeIDDir = mainDir+parameters[3][0]+"\\mappings.lg";
		a = new BufferedReader(new FileReader(nodeIDDir));
		a.readLine();
		
		strs = a.readLine();
		while(strs!=null&&!strs.equals("")) {
			String[]tem=strs.split(" = ");
			int id = Integer.parseInt(tem[0]);
			String name = tem[1];
			if(nodeIDName.contains(name)) {
				System.out.println(strs);
			}
			//nodeIDName.add(tem[1]);
			strs=a.readLine();
		}
		a.close();
	}
	
	public static void findGenesStream() throws IOException {
		String nodeDir = mainDir + "¿¹²¡¶¾Ò©ÎïÍ¼Æ×v1.0/mappings.lg";
		
		ArrayList<String>nodeIDName = new ArrayList();
		BufferedReader a = new BufferedReader(new FileReader(nodeDir));
		a.readLine();		
		String strs = a.readLine();
		while(strs!=null&&!strs.equals("")) {
			String[]tem=strs.split(" = ");
			int id = Integer.parseInt(tem[0]);
			nodeIDName.add(tem[1]);
			strs=a.readLine();
		}
		
		a.close();
		String[]nodeIDLabel=new String[nodeIDName.size()];
		ParserConfig.getGlobalInstance().setAutoTypeSupport(true); 
		
		String path = mainDir+"virusnetwork.sars-cov-22.27.json";
		String s = new Phylogeny().readJsonFile(path);
		JSONObject jobj = JSON.parseObject(s);
		JSONArray graph = jobj.getJSONArray("@graph");
		HashMap<String, Integer> nodeNameID = new HashMap<String,Integer>();
		for(int i = 0 ; i < graph.size();i++){
			JSONObject obj = (JSONObject)graph.get(i);
			String nodeIdxA = (String)obj.get("@id");
			if(!nodeIDName.contains(nodeIdxA)) 
				continue;
			if(nodeNameID.containsKey(nodeIdxA)) 
				continue;
			int id = nodeIDName.indexOf(nodeIdxA);
			
			String nodeLabelText = "";
			try {
				JSONArray subObj = obj.getJSONArray("label");
				for(int j = 0;j<subObj.size();j++){
					JSONObject nodeLabelObj = (JSONObject)subObj.get(j);
					String lan = (String) nodeLabelObj.get("@language");
					if(lan.equals("en")) {// English
						String cLable = (String) nodeLabelObj.get("@value");
						if(cLable!=null&&!cLable.equals("null")) {
							nodeLabelText += cLable;
							//System.out.println(nodeLabel);
							}
					}
				}

				}catch(Exception e) {
					
					JSONObject nodeLabelObj = obj.getJSONObject("label");
					String lan = (String) nodeLabelObj.get("@language");
					if(lan.equals("en")) {// English
						String cLable = (String) nodeLabelObj.get("@value");
						if(cLable!=null&&!cLable.equals("null")) {
							nodeLabelText += cLable;
							//System.out.println(nodeLabel);
							}
					}
				}

			nodeIDLabel[id] = (String)obj.get("type")+","+nodeLabelText;//("@type");
			System.out.println(nodeIDLabel[id]);
		}
	
		
	}

}

package datapreJSON;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
//import net.sf.json.JSON;
//import net.sf.json.JSONArray;
//import net.sf.json.JSONObject;
import com.alibaba.fastjson.parser.ParserConfig;

public class Phylogeny {
	
	static HashSet<String> nodeLabelSet;
	static HashSet<String> edgeLabelSet;
	static HashSet<String> allNodesSet;
	static HashMap<String, String> nodeLabelmap = new HashMap<String, String>();
	static HashMap<String,String> edgeStringLabelMap;
	static String mainDir="C:\\Users\\Sheldon\\Documents\\GitHub\\COVID19Motif\\ÐÂ¹ÚÖªÊ¶Í¼Æ×\\Phylogeny\\", jsonDir = null;
	static int dataid = -1;
	static String[][]parameters = null;
	
	public static void main(String[] args) throws  Exception{
		
		parameters = new String[10][2];
		parameters[0][0] = "0. ²¡¶¾·ÖÀàÍ¼Æ×v2.0\\taxonomy.json"; parameters[0][1] = "@type";
		parameters[1][0] = "1. ¿¹²¡¶¾Ò©ÎïÍ¼Æ×v1.0\\virusnetwokr.drug2.27.json"; parameters[1][1] = "type";
		parameters[2][0] = "2. ²¡¶¾Ç×Ôµ¹ØÏµÍ¼Æ×v1.2\\json"; parameters[2][1] = "@type";
		parameters[3][0] = "3. ÐÂ¹Ú»ù±¾ÐÅÏ¢Í¼Æ×v1.0\\virusnetwork.sars-cov-22.27.json"; parameters[3][1] = "type";
		parameters[4][0] = "4. ÐÂ¹ÚÎÄÏ×³éÈ¡Í¼Æ×v1.0\\virusnetwork.extract-paper.json"; parameters[4][1] = "type";
		
		dataid = 2;
		jsonDir = parameters[dataid][0];
		
		nodeLabelSet = new HashSet<String>();
		edgeLabelSet = new HashSet<String>();
		allNodesSet = new HashSet<String>();//store all the nodes
		edgeStringLabelMap = new HashMap<String,String>();//(edgeString, edgeLabel) ("100_200",edgeLabel)
		ParserConfig.getGlobalInstance().setAutoTypeSupport(true); 
		//HashMap<String, String> LabelMap= getNodeLabels();//C1 - Virus
		
		//scan for label of all nodes
		HashMap<String, String> allNodesWithLabel = allNodes(dataid);
		//scan all edges
		getRelationships(allNodesWithLabel);
        
		HashMap<String,Integer> nodeLabelMap = new HashMap<>();
		HashMap<String,Integer> edgeLabelMap = new HashMap<>();
		HashMap<String,Integer> allNodesMap = new HashMap<>();
		
		Iterator nodeLabelIt = nodeLabelSet.iterator();
		int nodeLabelCount = 0;
		while (nodeLabelIt.hasNext()) {
			nodeLabelMap.put((String) nodeLabelIt.next(), nodeLabelCount);
			nodeLabelCount ++;
		}
		
		Iterator edgeLabelIt = edgeLabelSet.iterator();
		int edgeLabelCount = 0;
		while (edgeLabelIt.hasNext()) {
			edgeLabelMap.put((String) edgeLabelIt.next(), edgeLabelCount);
			edgeLabelCount ++;
		}
		
		Iterator allNodesIt = allNodesSet.iterator();
		int nodeCount = 0;
		while (allNodesIt.hasNext()) {
			allNodesMap.put((String) allNodesIt.next(), nodeCount);
			nodeCount ++;
		}
		allNodesMap = (HashMap<String, Integer>) sortByValueAscending(allNodesMap);
//		System.out.println("nodeLabelMap:"+nodeLabelMap);
//		System.out.println("edgeLabelMap:"+edgeLabelMap);
//		System.out.println("allNodesMap:"+allNodesMap);
		
		System.out.println("Start write graph files!");
		writeGraphFiles(allNodesMap, edgeStringLabelMap, nodeLabelMap, edgeLabelMap, allNodesWithLabel);
		writeLabelFiles(nodeLabelMap, edgeLabelMap);
		
		//write mapping relationships
		writeMappingRelation(allNodesMap);
        
    }
	
	public static void writeMappingRelation(HashMap<String,Integer> allNodesMap){
		System.out.println(allNodesMap.size());
		HashMap<Integer,String> reMap = new HashMap<Integer,String>();
		
		FileWriter fw;
		try {
			String fName = "mappings.lg";
			fw = new FileWriter(mainDir+fName);
			fw.write("t # " + ":\n");
			System.out.println("-------------write mappings-------------");
			for(Entry<String,Integer> entry:allNodesMap.entrySet()){
				String nodeString = entry.getKey();
				int nodeId = entry.getValue();
				String out = nodeId + " = " + nodeString + "\n";
				fw.write(out);
				reMap.put(nodeId, nodeString);
			}
			fw.flush();
			fw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println(reMap.size());
	}
	
	public static HashMap<String, String> allNodes(int datasetID){
		String path = mainDir+jsonDir;
		String s = readJsonFile(path);
		JSONObject jobj = JSON.parseObject(s);
		JSONArray graph = jobj.getJSONArray("@graph");
		HashMap<String, String> allNodesWithLabel = new HashMap<String,String>();
		for(int i = 0 ; i < graph.size();i++){
			JSONObject obj = (JSONObject)graph.get(i);
			String nodeIdxA = (String)obj.get("@id");
			if(allNodesWithLabel.containsKey(nodeIdxA)) 
				continue;
			
			String nodeLabelFinal = "";
			switch(datasetID){
			case 1: nodeLabelFinal = nodeLabelDeal1(obj); break;
			case 2: nodeLabelFinal = nodeLabelDeal2(obj); break;
			case 3: nodeLabelFinal = nodeLabelDeal3(obj); break;
			case 4: nodeLabelFinal = nodeLabelDeal4(obj); break;
			}
					
					
			
			//System.out.println(nodeLabelText);
			nodeLabelmap.put(nodeIdxA, nodeLabelFinal);
			
			String nodeLabel = (String)obj.get(parameters[dataid][1]);//("@type"); // use this one to tune two kinds of json files
			allNodesWithLabel.put(nodeIdxA, nodeLabel);
		}
		return allNodesWithLabel;
	}
	
	private static String nodeLabelDeal4 (JSONObject obj) {
		String nodeLabelText = getNodeLabelText(obj);
		
		String[]labelsOI = {"property_is"};
		
		for(int i=0;i<labelsOI.length;i++) {
			String label = labelsOI[i];
			if(obj.containsKey(label)) {
				nodeLabelText += ","+textDeal(obj.get(label)+"");
			}else {
				nodeLabelText += ",null";
			}
		}
		
		return (nodeLabelText);
	}
	
	
	private static String getNodeLabelText(JSONObject obj, String label, String splitter) {
		String nodeLabelText = "";
		if(!obj.containsKey(label))
			return "";
		try {
			JSONArray subObj = obj.getJSONArray(label);
			for(int j = 0;j<subObj.size();j++){
				JSONObject nodeLabelObj = (JSONObject)subObj.get(j);
				nodeLabelText += languageDeal(nodeLabelObj, splitter);				
				}
			}catch(Exception e) {
				JSONObject nodeLabelObj = obj.getJSONObject(label);
				nodeLabelText += languageDeal(nodeLabelObj, splitter);
			}
		return textDeal(nodeLabelText);
	}
	private static String languageDeal(JSONObject nodeLabelObj, String splitter) {		
		if(nodeLabelObj.containsKey("@language")) {
			String lan = (String) nodeLabelObj.get("@language");
			if(lan.equals("en")) {// English
				String cLable = (String) nodeLabelObj.get("@value");
				if(cLable!=null&&!cLable.equals("null")) {
					return (splitter+cLable);
					//System.out.println(nodeLabel);
					}
			}
		}else {
			String cLable = (String) nodeLabelObj.get("@value");
			if(cLable!=null&&!cLable.equals("null")) {
				return (splitter+cLable);
				//System.out.println(nodeLabel);
				}
		}
			
		return "";
	}

	private static String getNodeLabelText(JSONObject obj) {
		return getNodeLabelText(obj, "label", "");
	}

	private static String nodeLabelDeal3 (JSONObject obj) {
		String nodeLabelText = getNodeLabelText(obj);
		
		String[]labelsOI = {"identifier","url"};
		
		for(int i=0;i<labelsOI.length;i++) {
			String label = labelsOI[i];
			if(obj.containsKey(label)) {
				nodeLabelText += ","+textDeal((String)obj.get(label));
			}else {
				nodeLabelText += ",null";
			}
		}
		
		return (nodeLabelText);
	}
	
	private static String nodeLabelDeal2 (JSONObject obj) {
		String nodeLabelText = getNodeLabelText(obj);
		
		String[]labelsOI = {"aa_mutation","nucleotide_mutation","divergence","virus_type"};
		
		for(int i=0;i<labelsOI.length;i++) {
			String label = labelsOI[i];
			if(obj.containsKey(label)) {
				nodeLabelText += ","+textDeal(obj.get(label)+"");
			}else {
				nodeLabelText += ",null";
			}
		}
		
		return (nodeLabelText);
	}
	
	private static String nodeLabelDeal1(JSONObject obj) {
		String nodeLabelText = getNodeLabelText(obj);
		String alias = "", drugbankid = "";
		
		if(obj.containsKey("drugbank_drug_id")) {
			JSONObject nodeLabelObj = obj.getJSONObject("drugbank_drug_id");
			String lan = (String) nodeLabelObj.get("@language");
			if(lan.equals("en")) {// English
				drugbankid = (String) nodeLabelObj.get("@value");
				}
		}else if(obj.containsKey("drugbank_virus_id")) {
			drugbankid = (String)obj.get("drugbank_virus_id");
		}else {
			drugbankid = -1+"";
		}
		
		if(obj.containsKey("uniprotkb_entry_name")) {
			alias+="|"+(String)obj.get("uniprotkb_entry_name");
		}		
		if(obj.containsKey("alias")) {
			alias+=getNodeLabelText(obj, "alias", "|");
		}
		
		return (nodeLabelText+textDeal(alias)+","+textDeal(drugbankid));
	}

	private static String textDeal(String s) {
		//to process in neo4j, each tuple cannot include , or "
		s = s.replaceAll(",", "-");
		s = s.replaceAll("\"","");
		//s = s.replaceAll("[","");
		//s = s.replaceAll("]","");
		return s;
	}

	public static void getRelationships(HashMap<String, String> allNodesWithLabel) {
		String path = mainDir+jsonDir;
		String s = readJsonFile(path);
		JSONObject jobj = JSON.parseObject(s);
		JSONArray graph = jobj.getJSONArray("@graph");
		for (int i = 0; i < graph.size(); i++) {
			JSONObject obj = (JSONObject) graph.get(i);
			String nodeIdxA = (String) obj.get("@id");
//			System.out.println(obj.toString());
			Iterator iter = obj.entrySet().iterator();
			while (iter.hasNext()) {
				Map.Entry entry = (Map.Entry) iter.next();
				String key = entry.getKey().toString();
				
//				JSONArray value = obj.getJSONArray(key);
				if(entry.getValue() == null) continue;
				String value = entry.getValue().toString();
				if(value.length() < 48) continue;
				//System.out.println(key+"\t"+value);
				String tmp = value.substring(0, 47);
				if(key.equals("@id")) continue;
				if (tmp.equals("http://www.openkg.cn/COVID-19/research/resource")) {
					handleAnEdge(allNodesWithLabel, key, value, nodeIdxA);
				}else {
					if(tmp.equals("[\"http://www.openkg.cn/COVID-19/research/resour")) {
						String tem = value.substring(1, value.length()-1);
						String[]temarr = tem.split(",");
						for(int j=0;j<temarr.length;j++) {
							String atem = temarr[j].substring(1, temarr[j].length()-1);
							//System.out.println(nodeIdxA+"\t"+atem);
							handleAnEdge(allNodesWithLabel, key, atem, nodeIdxA);
						}
					}
				}
				//end edge scan
			}
		}

	}
	
	private static void handleAnEdge(HashMap<String, String> allNodesWithLabel, String key, String value, String nodeIdxA) {
		// TODO Auto-generated method stub
		//System.out.println(key+"\t"+value);
		String nodeIdxB = value;
		String edgeLabel = key;
		String labelA = allNodesWithLabel.get(nodeIdxA);
		String labelB = allNodesWithLabel.get(nodeIdxB);
		if (labelA == null || labelB == null)
			return;
		if (!allNodesSet.contains(nodeIdxA))
			allNodesSet.add(nodeIdxA);
		if (!allNodesSet.contains(nodeIdxB))
			allNodesSet.add(nodeIdxB);
		if (!nodeLabelSet.contains(labelA))
			nodeLabelSet.add(labelA);
		if (!nodeLabelSet.contains(labelB))
			nodeLabelSet.add(labelB);
		if (!edgeLabelSet.contains(edgeLabel))
			edgeLabelSet.add(edgeLabel);
		String AB = nodeIdxA + "#" + nodeIdxB;
		edgeStringLabelMap.put(AB, edgeLabel);
	}

	public static HashMap<String, String> getNodeLabels(){
		String path = mainDir+jsonDir;
		String s = readJsonFile(path);
		JSONObject jobj = JSON.parseObject(s);
		JSONArray graph = jobj.getJSONArray("@graph");
		//get node labels
		HashMap<String, String> nodeLabelmap = new HashMap<String, String>();
		for (int i = 0 ; i < graph.size();i++){
			JSONObject obj = (JSONObject)graph.get(i);
			String nodeLabelId = (String)obj.get("@id");
			String nodetType = (String)obj.get("type");
			String nodeLabel = "";
			try {
			JSONArray subObj = obj.getJSONArray("label");
			for(int j = 0;j<subObj.size();j++){
				JSONObject nodeLabelObj = (JSONObject)subObj.get(j);
				String lan = (String) nodeLabelObj.get("@language");
				if(lan.equals("en")) {// English
					nodeLabel = (String) nodeLabelObj.get("@value");
					if(!nodeLabel.equals("null"))
						System.out.println(nodeLabelId+":\t"+nodeLabel);
				}
			}
			nodeLabelmap.put(nodeLabelId, nodeLabel);
			}catch(Exception e) {
				
			}
		}
		return nodeLabelmap;
	}
	
	/**
     * read json file
     * @param fileName
     * @return
     */
    public static String readJsonFile(String fileName) {
        String jsonStr = "";
        try {
            File jsonFile = new File(fileName);
            FileReader fileReader = new FileReader(jsonFile);

            Reader reader = new InputStreamReader(new FileInputStream(jsonFile),"utf-8");
            int ch = 0;
            StringBuffer sb = new StringBuffer();
            while ((ch = reader.read()) != -1) {
                sb.append((char) ch);
            }
            fileReader.close();
            reader.close();
            jsonStr = sb.toString();
            return jsonStr;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValueAscending(Map<K, V> map) {
		List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>(map.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
			@Override
			public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
				int compare = (o1.getValue()).compareTo(o2.getValue());
				return compare;
			}
		});

		Map<K, V> result = new LinkedHashMap<K, V>();
		for (Map.Entry<K, V> entry : list) {
			result.put(entry.getKey(), entry.getValue());
		}
		return result;
	}
    
    
    public static void writeGraphFiles(Map<String, Integer> allNodesMap, HashMap<String, String> edgeStringLabelMap,
			HashMap<String,Integer> nodeLabelMap, HashMap<String,Integer> edgeLabelMap, HashMap<String,String> typeMap) {

		FileWriter fw;
		try {
			String fName = "phylogeny.lg";
			fw = new FileWriter(mainDir+fName+".nodes");
			//fw.write("t # " + ":\n");
			System.out.println("-------------write nodes-------------");
			for (Entry<String, Integer> entry : allNodesMap.entrySet()) {
				String nodeStringIdx = entry.getKey();
				int nodeIdx = entry.getValue();
				String nodeID = typeMap.get(nodeStringIdx);
				Integer nodeLabelIdx = nodeLabelMap.get(nodeID);
				String nodeLabel = nodeLabelmap.get(nodeStringIdx);
				//String out = "v " + nodeIdx + " " + nodeLabelIdx + "\n";
				String out = nodeIdx +","+nodeLabelIdx+","+nodeLabel+"\n";
				fw.write(out);
			}
			fw.flush();
			fw.close();
			
			System.out.println("-------------write edges-------------");
			fw = new FileWriter(mainDir+fName+".edges");
			for (Entry<String, String> entry : edgeStringLabelMap.entrySet()) {
				String AB = entry.getKey();
		        String[] strArray = AB.split("#");
		        String nodeA = strArray[0];
		        String nodeB = strArray[1];
				Integer edgeLabelIdx = edgeLabelMap.get(entry.getValue());
				//String out = "e " + allNodesMap.get(nodeA) + " " + allNodesMap.get(nodeB) + " " + edgeLabelIdx + "\n";
				String out = allNodesMap.get(nodeA) + "," + allNodesMap.get(nodeB) + "," + edgeLabelIdx + "\n";
				fw.write(out);
			}
			fw.flush();
			fw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	public static void writeLabelFiles(HashMap<String,Integer> nodeLabelMap, HashMap<String,Integer> edgeLabelMap) {

		// nodes
		FileWriter fw;
		try {
			String fName = "Phy_Label.txt";
			fw = new FileWriter(mainDir+fName);
			fw.write("Labels correspondence " + ":\n");
			fw.write("-------------------nodeLabels-------------------" + "\n");
			
			
			for(Entry<String,Integer> entry:nodeLabelMap.entrySet()) {
				fw.write(entry.getValue()+ ":");
				fw.write(entry.getKey()+ "\n");
			}
			
			fw.write("-------------------edgeLabels-------------------" + "\n");
			for(Entry<String,Integer> entry:edgeLabelMap.entrySet()) {
				fw.write(entry.getValue()+ ":");
				fw.write(entry.getKey()+ "\n");
			}
			fw.flush();
			fw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}

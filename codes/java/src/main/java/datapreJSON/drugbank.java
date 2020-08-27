package datapreJSON;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
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

public class drugbank {
	
	static HashSet<String> nodeLabelSet;
	static HashSet<String> edgeLabelSet;
	static HashSet<String> allNodesSet;
	static HashMap<String, String> nodeLabelmap = new HashMap<String, String>();
	static HashMap<String,String> edgeStringLabelMap;
	
	static BufferedWriter b;
	
	public static void main(String[] args) throws  Exception{
		
		
		String jsonDir = "C:\\Users\\Sheldon\\dataset\\database.json";
		
		nodeLabelSet = new HashSet<String>();
		edgeLabelSet = new HashSet<String>();
		allNodesSet = new HashSet<String>();//store all the nodes
		edgeStringLabelMap = new HashMap<String,String>();//(edgeString, edgeLabel) ("100_200",edgeLabel)
		ParserConfig.getGlobalInstance().setAutoTypeSupport(true); 
		//HashMap<String, String> LabelMap= getNodeLabels();//C1 - Virus
		
		b = new BufferedWriter(new FileWriter("C:\\Users\\Sheldon\\Documents\\GitHub\\covid19kg\\data\\HPO\\nodes\\drug2.txt"));
		
		//scan for label of all nodes
		allNodes(jsonDir);

		
        
    }
	
	public static void allNodes(String jsonDir) throws IOException{
		String path = jsonDir;
		String s = readJsonFile(path);
		JSONObject jobj = JSON.parseObject(s);
		JSONArray graph = jobj.getJSONArray("drug");
		ArrayList<Integer>ids = new ArrayList();
		ArrayList<String>names = new ArrayList();
		for(int i = 0 ; i < graph.size();i++){
			JSONObject obj = (JSONObject)graph.get(i);
			
			String dbid = getNodeLabelText(obj, "drugbank-id");
			String dbname = textDeal(obj.get("name")+"");	
			String synonyms = getNodeLabelText2(obj.getJSONObject("synonyms"), "synonym");
			
			String[]tem = dbid.split("[|]");
			if(tem.length==1) {
				arraydeal(tem[0], ids, names, dbname+synonyms);	
			}else {
				for(int k=0;k<tem.length;k++) {
					//duplicate the drug name for each copy of drug id
					if(tem[k].length()==0)
						continue;
					arraydeal(tem[k], ids, names, dbname+synonyms);	
				}
			}
			
		}
		
		for(int i=0;i<ids.size();i++) {
			b.write(ids.get(i)+","+names.get(i)+"\n");
		}
		
		b.flush();
		b.close();
		
	}
	
	private static void arraydeal(String idStr, ArrayList<Integer> ids, ArrayList<String> names, String name) {
		int id2 = Integer.parseInt(idStr);
		if(ids.size()==0) {
			ids.add(id2);
			names.add(name);
			return;
		}
		for(int j=0;j<ids.size();j++) {
			if(ids.get(j)==id2) {
				System.out.println("Duplicates found: "+id2+","+name);
			}
			if(ids.get(j)>id2) {
				ids.add(j, id2);
				names.add(j, name);
				break;
			}
			if(j==ids.size()-1 && ids.get(j)<id2) {
				ids.add(id2);
				names.add(name);
				break;
			}
		}
	}

	private static void jout(String s) throws IOException {
		b.write(s+"\n");
	}

	private static String getNodeLabelText2 (JSONObject obj, String label) {
		String nodeLabelText = "";
		if(obj==null||!obj.containsKey(label))
			return "";
		try {
			JSONArray subObj = obj.getJSONArray(label);
			for(int j = 0;j<subObj.size();j++){
				nodeLabelText += "|"+subObj.get(j);	
				}
			}catch(Exception e) {
				nodeLabelText += "|"+obj.get(label);
			}
		return textDeal(nodeLabelText);
	}
	
	
	private static String getNodeLabelText(JSONObject obj, String label) throws IOException {
		String nodeLabelText = "";
		if(obj==null||!obj.containsKey(label))
			return "";
		try {
			JSONArray subObj = obj.getJSONArray(label);
			for(int j = 0;j<subObj.size();j++){
				nodeLabelText += languageDeal(subObj.get(j)+"")+"|";			
				}
			while(nodeLabelText.charAt(nodeLabelText.length()-1)=='|')
				nodeLabelText = nodeLabelText.substring(0, nodeLabelText.length()-1);
			}catch(Exception e) {
				nodeLabelText += languageDeal(obj.get(label)+"");
			}
		return textDeal(nodeLabelText);
	}
	private static String languageDeal(String nodeLabelObj) throws IOException {
		//System.out.println(nodeLabelObj+"");
		String s = nodeLabelObj+"";
		int id = -1;
		if(s.contains("DB")) {
			s = s.substring(2);
			try {
				id= Integer.parseInt(s);
				return id+"";
			}catch(Exception e){System.out.println("error:\t"+nodeLabelObj+"");}
		}
		return "";
	}

	private static String getNodeLabelText(JSONObject obj) throws IOException {
		return getNodeLabelText(obj, "label");
	}

	private static String nodeLabelDeal3 (JSONObject obj) throws IOException {
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
	
	private static String nodeLabelDeal2 (JSONObject obj) throws IOException {
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
	
	private static String nodeLabelDeal1(JSONObject obj) throws IOException {
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
			alias+=getNodeLabelText(obj, "alias");
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
	

}

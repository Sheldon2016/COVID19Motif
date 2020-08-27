package datapre;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

public class covid19dataAlign {

	public static void main(String[] args) throws IOException {
		
		String mainDir = "C:\\Users\\Sheldon\\Documents\\GitHub\\covid19kg\\data\\HPO\\";
		
		BufferedReader a = new BufferedReader (new FileReader(mainDir+"nodes//Drug2.txt"));
		ArrayList<Integer>gtIDs = new ArrayList();
		ArrayList<String>gtNames = new ArrayList();
		String s = a.readLine();
		while(s.charAt(0)=='#') {
				s=a.readLine();
			}
		while(s!=null&&!s.equals("")) {
			//System.out.println(s);
			String[]tem=s.split(",");
			int nid = Integer.parseInt(tem[0]);
			gtIDs.add(nid);
			gtNames.add(tem[1]);
			s=a.readLine();
		}
		
		a = new BufferedReader (new FileReader(mainDir+"nodes/Drug.txt"));
		s = a.readLine();
		while(s.charAt(0)=='#') {
			s=a.readLine();
		}
		while(s!=null&&!s.equals("")) {
			//System.out.println(s);
			String[]tem=s.split(",");
			int nid1 = getInt(tem[0]);
			if(gtIDs.contains(nid1)) {
				int id = gtIDs.indexOf(nid1);
				String oldName = gtNames.get(id);
				if(tem.length>1) {
					String newName = combine(oldName,tem[1]);
					gtNames.set(id, newName);
				}
			}else {
				for(int i=0;i<gtIDs.size();i++) {
					if(i==gtIDs.size()-1&&gtIDs.get(i)<nid1) {
						gtIDs.add(nid1);
						gtNames.add(tem[1]);
						break;
					}
					if(gtIDs.get(i)>nid1) {
						gtIDs.add(i,nid1);
						gtNames.add(i,tem[1]);
						break;
					}
				}
			}
			s=a.readLine();
		}
		
		BufferedWriter b = new BufferedWriter(new FileWriter(mainDir+"nodes/drug22.txt"));
		for(int i=0;i<gtIDs.size();i++) {
			b.write(gtIDs.get(i)+","+gtNames.get(i)+"\n");
		}
		b.flush();
		b.close();
	}
	
	static void getNodeList() throws IOException {
		
		//getVirusProteinIDfromRaw();
		
		String mainDir = "C:\\Users\\Sheldon\\Documents\\GitHub\\covid19kg\\data\\HPO\\";
		
		String nodeFile = mainDir + "nodes/HostProtein.txt";
		String[]files= 		{"virusprotein_hostprotein.txt", "virus_hostprotein.txt", "drug_gene.txt", "HPOID_geneID.txt", "disease_gene.txt"};
		int[]columID = 		{4, 3, 2, 2, 1};
		int[]columName = 	{5, 4, 3, -1, -1};
		
		//String nodeFile = mainDir + "nodes/VirusProtein.txt";
		//String[]files= 		{"virusprotein_hostprotein.txt", "drug_virusprotein.txt", "virusgene_virusprotein.txt", "virus_virusprotein.txt"};
		//int[]columID = 		{1, 2, 0, 2};
		//int[]columName = 	{2, 3, 1, 3};
		
		//String nodeFile = mainDir + "nodes/Virus.txt";
		//String[]files= 		{"virus_hostprotein.txt", "virus_virusprotein.txt", "virus_virusgene.txt", "virus_disease.txt","drug_virus.txt"};
		//int[]columID = 		{0, 0, 0, 0, 2};
		//int[]columName = 	{1, 1, 1, 1, 3};
		
		//String nodeFile = mainDir + "nodes/HPO.txt";
		//String[]files= 		{"HPOID_diseaseID_disease.txt", "HPOID_geneID.txt"};
		//int[]columID = 		{0, 0};
		//int[]columName = 	{-1, -1};
		
		//String nodeFile = mainDir + "nodes/Disease.txt";
		//String[]files= 		{"HPOID_diseaseID_disease.txt", "virus_disease.txt","disease_gene.txt"};
		//int[]columID = 		{1, 3, 3};
		//int[]columName = 	{2, 2, 4};
		
		//String nodeFile = mainDir + "nodes/Drug.txt";
		//String[]files= 		{"drug_virus.txt", "drug_virusprotein.txt","drug_gene.txt"};
		//int[]columID = 		{0, 0, 0};
		//int[]columName = 	{1, 1, -1};
		
		getNodeFromLinks(mainDir, nodeFile, files, columID, columName);
		
	}
	
	private static void getNodeFromLinksHostProtein() throws IOException {
		
		String mainDir = "C:\\Users\\Sheldon\\Documents\\GitHub\\covid19kg\\data\\HPO\\";
		BufferedWriter b = new BufferedWriter (new FileWriter(mainDir+"\\nodes\\HostProtein2.txt"));
		ArrayList<Integer>IDs = new ArrayList();
		ArrayList<String>Names = new ArrayList();
			
		BufferedReader a = new BufferedReader (new FileReader(mainDir+"geneID_hostprotein.txt"));
		ArrayList<Integer>gtIDs = new ArrayList();
		ArrayList<String>gtNames = new ArrayList();
		String s = a.readLine();
		while(s.charAt(0)=='#') {
				s=a.readLine();
			}
		while(s!=null&&!s.equals("")) {
			//System.out.println(s);
			String[]tem=s.split(",");
			int nid = Integer.parseInt(tem[1]);
			String name = tem[2];
			if(!gtIDs.contains(nid)) {
				gtIDs.add(nid);
				gtNames.add(name);
			}else {
				int nameID = gtIDs.indexOf(nid);
				String name2 = combine(gtNames.get(nameID), name);
				gtNames.set(nameID, name2);
			}
			s=a.readLine();
		}
		
		a = new BufferedReader (new FileReader(mainDir+"nodes\\HostProtein.txt"));
		s = a.readLine();
		while(s.charAt(0)=='#') {
				s=a.readLine();
			}
		while(s!=null&&!s.equals("")) {
			//System.out.println(s);
			String[]tem=s.split(",");
			int nid = Integer.parseInt(tem[0]);
			String name = "";
			if(tem.length>1)
				name = tem[1];
			int nameID = gtIDs.indexOf(nid);
			String name2 = name;
			if(nameID!=-1)
				name2 = combine(gtNames.get(nameID), name);
			b.write(nid+","+name2+"\n");
			
			s=a.readLine();
		}
		
		
		b.flush();
		b.close();
		
		
	}
	
	private static void getNodeFromLinks(String mainDir, String nodeFile, String[] files, int[] columID, int[] columName) throws IOException {
		BufferedWriter b = new BufferedWriter (new FileWriter(nodeFile));
		ArrayList<Integer>IDs = new ArrayList();
		ArrayList<String>Names = new ArrayList();
		for(int i=0;i<files.length;i++) {
			BufferedReader a = new BufferedReader (new FileReader(mainDir+"edges\\"+files[i]));
			String s = a.readLine();
			while(s.charAt(0)=='#') {
				s=a.readLine();
			}
			while(s!=null&&!s.equals("")) {
				//System.out.println(s);
				String[]tem=s.split(",");
				int id = -1;
				if(tem.length>columID[i] && !tem[columID[i]].equals(""))
					id = Integer.parseInt(tem[columID[i]]);
				String name = "";
				if(columName[i]!=-1)
					name = tem[columName[i]];
				
				if(!IDs.contains(id)) {
					IDs.add(id);
					Names.add(name);
				}else {
					int nameID = IDs.indexOf(id);

					String name2 = combine(Names.get(nameID), name);
					Names.set(nameID, name2);
					
				}
				
				s=a.readLine();
			}
		}
		
		for(int j=0;j<IDs.size();j++) {
			for(int k=j+1;k<IDs.size();k++) {
				if(IDs.get(j)>IDs.get(k)) {
					int temi = IDs.get(j);
					IDs.set(j, IDs.get(k));
					IDs.set(k, temi);
					String tems = Names.get(j);
					Names.set(j, Names.get(k));
					Names.set(k, tems);
				}
			}
			b.write(IDs.get(j)+","+Names.get(j)+"\n");
		}
		
		b.flush();
		b.close();
		
		
	}

	static void getVirusProteinIDfromRaw() throws IOException {
		String mainDir = "C:\\Users\\Sheldon\\Documents\\GitHub\\covid19kg\\data\\HPO\\";
		BufferedReader a = new BufferedReader (new FileReader("C:\\Users\\Sheldon\\Downloads\\gene_info"));
		BufferedWriter b = null;//new BufferedWriter (new FileWriter(mainDir+"virusprotein_hostprotein2.txt"));
		
		Hashtable<String,Integer>genes = new Hashtable();
		String s = a.readLine();
		s=a.readLine();
		while(s!=null&&!s.equals("")) {
			String[]tem = s.split("	");
			int id = Integer.parseInt(tem[1]);
			String symbol = tem[2];
			if(!tem[4].equals("-"))
				symbol+="|"+tem[4];
			String[]symbols = symbol.split("[|]");
			for(int i=0;i<symbols.length;i++) {
				String str = symbols[i].toLowerCase();
				if(!genes.containsKey(str)) {
					genes.put(str,id);
				}
			}
			s=a.readLine();
		}
		System.out.println("Genes read finished");
		
		//virusGeneIDdeal(mainDir,"virusprotein_hostprotein_full2",0,genes);
		//virusGeneIDdeal(mainDir,"virus_virusgene",2,genes);
		//virusGeneIDdeal(mainDir,"virus_virusprotein",2,genes);
		//virusGeneIDdeal(mainDir,"virusgene_virusprotein",0,genes);
		virusGeneIDdeal(mainDir,"drug_virusprotein",2,genes);

	}
	private static void virusGeneIDdeal(String mainDir, String dir, int virusColumnID,Hashtable<String,Integer>virus) throws IOException {
		BufferedReader a = new BufferedReader (new FileReader(mainDir+dir+".txt"));
		BufferedWriter b = new BufferedWriter (new FileWriter(mainDir+dir+"2.txt"));
		String s = a.readLine();
		while(s.charAt(0)=='#') {
			s=a.readLine();
		}
		ArrayList<Integer>genes = new ArrayList();
		while(s!=null&&!s.equals("")) {
			String[]tem= s.split(",");
			int id = -1;
			
			String[]viruses = tem[virusColumnID].split("[|]");
			for(int i=0;i<viruses.length;i++) {
				String str = viruses[i].toLowerCase();
				if(virus.containsKey(str)) {
					id=virus.get(str);
					break;
				}
			}
			
			if(id!=-1) {
				for(int i=0;i<tem.length;i++) {
					if(i==virusColumnID) {
						b.write(id+","+tem[i]);
						if(i==tem.length-1)
							b.write("\n");
						else
							b.write(",");
						}else {
							if(i==tem.length-1)
								b.write(tem[i]+"\n");
							else
								b.write(tem[i]+",");
						}
				}
				/*
				if(genes.contains(id)) {
					b.write(genes.indexOf(id)+","+id+","+s+"\n");
				}else {
					genes.add(id);
					b.write((genes.size()-1)+","+id+","+s+"\n");
					
					}
					*/
			}
			s=a.readLine();
		}		
		b.flush();
		b.close();
		
	}
	private static void getVirusID() throws IOException {
		String mainDir = "C:\\Users\\Sheldon\\Documents\\GitHub\\covid19kg\\data\\HPO\\";
		BufferedReader a = new BufferedReader (new FileReader("C:\\Users\\Sheldon\\Downloads\\names.dmp"));
		BufferedWriter b = null;//new BufferedWriter (new FileWriter(mainDir+"virusprotein_hostprotein2.txt"));
		
		String s = a.readLine();
		while(s.charAt(0)=='#') {
			s=a.readLine();
		}
		
		Hashtable<String,Integer>virus = new Hashtable();
		while(s!=null&&!s.equals("")) {
			String[]tem= s.split("	[|]	");
			int id = Integer.parseInt(tem[0]);
			virus.put(tem[1].toLowerCase(), id);
			s=a.readLine();
		}
		
		System.out.println("Virus read finished: "+virus.containsKey("HIV".toLowerCase())+" "+virus.containsKey("SARS-CoV".toLowerCase()));
		
		//virusIDdeal(mainDir,"virus_virusprotein",0,virus);
		//virusIDdeal(mainDir,"virus_hostprotein",0,virus);
		//virusIDdeal(mainDir,"virus_disease",0,virus);
		virusIDdeal(mainDir,"drug_virus",2,virus);
		
	}
	private static void virusIDdeal(String mainDir, String dir, int virusColumnID,Hashtable<String,Integer>virus) throws IOException {
		BufferedReader a = new BufferedReader (new FileReader(mainDir+dir+".txt"));
		BufferedWriter b = new BufferedWriter (new FileWriter(mainDir+dir+"2.txt"));
		String s = a.readLine();
		while(s.charAt(0)=='#') {
			s=a.readLine();
		}
		while(s!=null&&!s.equals("")) {
			String[]tem= s.split(",");
			int id = -1;
			
			String[]viruses = tem[virusColumnID].split("[|]");
			for(int i=0;i<viruses.length;i++) {
				String str = viruses[i].toLowerCase();
				if(virus.containsKey(str)) {
					id=virus.get(str);
					break;
				}
			}
			for(int i=0;i<tem.length;i++) {
				if(i==virusColumnID) {
					b.write(id+","+tem[i]);
					if(i==tem.length-1)
						b.write("\n");
					else
						b.write(",");
					}else {
						if(i==tem.length-1)
							b.write(tem[i]+"\n");
						else
							b.write(tem[i]+",");
					}
			}
			
			s=a.readLine();
		}		
		b.flush();
		b.close();
		
	}
	static void getvirusproteinID() throws IOException {
		String mainDir = "C:\\Users\\Sheldon\\Documents\\GitHub\\covid19kg\\data\\HPO\\";
		BufferedReader a = new BufferedReader (new FileReader(mainDir+"virusprotein_hostprotein.txt"));
		BufferedWriter b = new BufferedWriter (new FileWriter(mainDir+"virusprotein_hostprotein2.txt"));
		
		String s = a.readLine();
		while(s.charAt(0)=='#') {
			s=a.readLine();
		}
		
		ArrayList<String>virusprotein = new ArrayList();
		
		while(s!=null&&!s.equals("")) {
			String[]tem= s.split(",");
			int id = -1;
			
			for(int i=0;i<virusprotein.size();i++) {
				if(ifmatch(virusprotein.get(i),tem[0])) {
					String str = combine(virusprotein.get(i),tem[0]);
					if(str!=null) {
						virusprotein.set(i, str);
					}
					id = i;
					break;
				}
			}
			
			if(id==-1) {
				virusprotein.add(tem[0]);
				id = virusprotein.size()-1;
			}
			
			b.write(id+","+s+"\n");
			
			s=a.readLine();
		}
		
		b.flush();
		b.close();
		
		b = new BufferedWriter (new FileWriter(mainDir+"virusprotein_id.txt"));
		for(int i=0;i<virusprotein.size();i++) {
			b.write(i+","+virusprotein.get(i)+"\n");
		}
		b.flush();
		b.close();
		
	}
	
	private static String combine(String s1, String s2) {
		if(s1==null)
			return s2;
		if(s2==null)
			return s1;
		
		if(s1.replaceAll(" ","").length()==0)
			return s2;
		if(s2.replaceAll(" ","").length()==0)
			return s1;
		
		if(s1.replaceAll("\t","").length()==0)
			return s2;
		if(s2.replaceAll("\t","").length()==0)
			return s1;
		
		
		ArrayList<String>names = new ArrayList();
		String[]p = s1.split("[|]");
		String[]q = s2.split("[|]");
		for(int j=0;j<p.length;j++){
			if(!names.contains(p[j].toLowerCase()))
				names.add(p[j].toLowerCase());
		}
		for(int j=0;j<q.length;j++) {
			if(!names.contains(q[j].toLowerCase()))
				names.add(q[j].toLowerCase());
		}
		
		String s = "";
		
		for(int i=0;i<names.size();i++) {
			if(i==names.size()-1)
				s+=names.get(i);
			else
				s+=names.get(i)+"|";
		}
		
		return s;
	}

	static boolean ifmatch(String sp, String sq) {
		String[]p = sp.split("[|]");
		String[]q = sq.split("[|]");
		for(int j=0;j<p.length;j++){
			for(int k=0;k<q.length;k++) {
				if(q[k].toLowerCase().equals(p[j].toLowerCase())) {
					return true;
				}
			}
		}
		return false;
	}
	public static void diseaseID() throws IOException{
		String mainDir = "C:\\Users\\Sheldon\\Documents\\GitHub\\covid19kg\\data\\HPO\\";
		BufferedReader a = new BufferedReader (new FileReader(mainDir+"hpoID_diseaseID_disease.txt"));
		BufferedWriter b = null; //new BufferedWriter (new FileWriter(mainDir+"gene_disease.txt"));
		
		String s = a.readLine();
		while(s.charAt(0)=='#') {
			s=a.readLine();
		}
		
		Hashtable<String,Integer>diseaseHPOOriginal = new Hashtable();
		Hashtable<String,Integer>diseaseHPO = new Hashtable();
		ArrayList<Integer>diseases = new ArrayList();

		while(s!=null&&!s.equals("")) {
			String[]tem= s.split(",");
			int diseaseID = Integer.parseInt(tem[1]);
			if(!diseases.contains(diseaseID)) {
				diseases.add(diseaseID);
				diseaseHPO.put(tem[2].toLowerCase(), diseaseID);
				diseaseHPOOriginal.put(tem[2], diseaseID);
			}
			s=a.readLine();
		}
		
		//mergeHostProtein(a,b,genes,hostproteins,s, mainDir);
		a = new BufferedReader (new FileReader(mainDir+"geneID_hostprotein.txt"));
		s = a.readLine();
		while(s.charAt(0)=='#') {
			s=a.readLine();
		}
		ArrayList<Integer>genes = new ArrayList();
		ArrayList<String>hostproteins = new ArrayList();
		while(s!=null&&!s.equals("")) {
			String[]tem= s.split(",");
			int geneID = Integer.parseInt(tem[1]);
			genes.add(geneID);
			hostproteins.add(tem[2]);
			s=a.readLine();
		}
		
		a = new BufferedReader (new FileReader("C:\\Users\\Sheldon\\Google 云端硬盘（xdli@connect.hku.hk）\\DESK\\documents\\researchWorks\\motif-path\\motif-data\\boxuan dataset\\all_gene_disease_associations.tsv"));
		b = new BufferedWriter (new FileWriter(mainDir+"disease_gene2.txt"));
		s = a.readLine();
		while(s.charAt(0)=='#') {
			s=a.readLine();
		}
		while(s!=null&&!s.equals("")) {
			String[]tem= s.split("	");
			int gene = Integer.parseInt(tem[0]);
			if(!genes.contains(gene)) {
				s=a.readLine();
				continue;
			}
			int geneID = genes.indexOf(gene);
			
			String diseaseIDConcept = tem[2];
			String diseaseName = tem[3].toLowerCase(), diseaseNameOrigin = tem[3];
			
			int diseaseID = -1;
			if(diseaseHPO.containsKey(diseaseName)) {
				diseaseID = diseaseHPO.get(diseaseName);
			}

			if(diseaseID!=-1) {
				b.write(geneID+","+gene+","+diseaseID+","+diseaseIDConcept+","+diseaseNameOrigin+"\n");
			}else {
				//System.out.println("not found: "+s);
			}
			
			s=a.readLine();
		}
		
		b.flush();
		b.close();
		
	
	}
	public static void hostProteinID() throws IOException{
		String mainDir = "C:\\Users\\Sheldon\\Documents\\GitHub\\covid19kg\\data\\HPO\\";
		BufferedReader a = new BufferedReader (new FileReader(mainDir+"geneID_hostprotein.txt"));
		BufferedWriter b = null;//new BufferedWriter (new FileWriter(mainDir+"geneID_hostprotein2.txt"));
		
		String s = a.readLine();
		while(s.charAt(0)=='#') {
			s=a.readLine();
		}
		
		ArrayList<Integer>genes = new ArrayList();
		ArrayList<String>hostproteins = new ArrayList();

		while(s!=null&&!s.equals("")) {
			String[]tem= s.split(",");
			int geneID = Integer.parseInt(tem[1]);
			genes.add(geneID);
			hostproteins.add(tem[2]);
			s=a.readLine();
		}
		
		System.out.println("Data read finished.");
		
		mergeHostProtein(a,b,genes,hostproteins,s, mainDir);
		
		//mergeDrugGene(a,b,genes,hostproteins,s,mainDir);
		
		
	}
	
	private static void mergeDrugGene(BufferedReader a, BufferedWriter b, ArrayList<Integer> genes,ArrayList<String> hostproteins, String s, String mainDir) throws IOException {
		a = new BufferedReader (new FileReader(mainDir+"drug_gene.txt"));
		b = new BufferedWriter (new FileWriter(mainDir+"drug_gene2.txt"));
		s = a.readLine();
		while(s.charAt(0)=='#') {
			s=a.readLine();
		}
		while(s!=null&&!s.equals("")) {
			String[]tem= s.split(",");
			String drugname = tem[0].split("B")[1];
			int drugID = Integer.parseInt(drugname);
			int id = -1;
			for(int i=0;i<hostproteins.size();i++) {
				String[]p = hostproteins.get(i).split("[|]");
				for(int j=0;j<p.length;j++){
					if(tem[1].equals(p[j])) {
						id = i;
						break;
					}
				}
				if(id!=-1)
					break;
			}
			
			if(id!=-1) {
				b.write(drugID+","+id+","+tem[1]+"\n");
			}else {
				//System.out.println("not found: "+s);
			}
			
			s=a.readLine();
		}
		
		b.flush();
		b.close();
		
	}
	private static void mergeHostProtein(BufferedReader a, BufferedWriter b, ArrayList<Integer> genes, ArrayList<String> hostproteins, String s, String mainDir) throws IOException {
		a = new BufferedReader (new FileReader(mainDir+"virusprotein_hostprotein_full.txt"));
		b = new BufferedWriter (new FileWriter(mainDir+"virusprotein_hostprotein_full2.txt"));
		s = a.readLine();
		while(s.charAt(0)=='#') {
			s=a.readLine();
		}
		int count = 0;
		while(s!=null&&!s.equals("")) {
			String[]tem= s.split(",");
			String hostprotein = tem[1];
			int id = -1;
			for(int i=0;i<hostproteins.size();i++) {
				String[]p = hostproteins.get(i).split("[|]");
				String[]q = hostprotein.split("[|]");
				for(int j=0;j<p.length;j++){
					for(int k=0;k<q.length;k++) {
						if(q[k].toLowerCase().equals(p[j].toLowerCase())) {
							id = i;
							break;
						}
					}
					if(id!=-1)
						break;
				}
				if(id!=-1)
					break;
			}
			
			if(id!=-1) {
				b.write(tem[0]+","+id+","+tem[1]+"\n");
			}else {
				//System.out.println("not found: "+s);
			}
			
			s=a.readLine();
			count++;
			if(count%266==0)
				System.out.print((count/266)+"->");
		}
		
		b.flush();
		b.close();
		
	}

	public static void run() throws IOException {
		String mainDir = "C:\\Users\\Sheldon\\Documents\\GitHub\\COVID19Motif\\新冠知识图谱\\Phylogeny\\6. nature20\\proteinGene.txt";
		//BufferedWriter b = new BufferedWriter (new FileWriter(mainDir+"2"));
		BufferedReader a = new BufferedReader (new FileReader(mainDir));
		//a.readLine();
		
		ArrayList<Integer>geneIDSet = getGeneNode();
	
		String s = a.readLine();
		int counter = 0; 
		while(s!=null&&!s.equals("")) {
			String[]tem= s.split(",");
			int geneID = Integer.parseInt(tem[2]);
			String protein = tem[1];
			if(geneIDSet.contains(geneID)) {
				System.out.println(s);
			}else {
				counter++;
				//System.out.println(geneID);
			}
			//geneIDSet.add(geneID);
			s=a.readLine();
		}
		System.out.println(counter);
		//b.flush();
		//b.close();
	}
	
	public static void getEdges() throws IOException {
		ArrayList<String>hs = getNode();
		String mainDir = "C:\\Users\\Sheldon\\Documents\\GitHub\\COVID19Motif\\新冠知识图谱\\Phylogeny\\6. nature20\\Gene_Drug.csv";
		BufferedWriter b = new BufferedWriter (new FileWriter(mainDir+"2"));
		BufferedReader a = new BufferedReader (new FileReader(mainDir));
		//a.readLine();
		String s = a.readLine();
		while(s!=null&&!s.equals("")) {
			String[]tem= s.split(",");
			int gid = getInt(tem[0]);
			int drugid = Integer.parseInt(tem[1]);
			String drug = hs.get(drugid-1);
			b.write(gid+","+drug+",1,"+tem[2]+"\n");
			s=a.readLine();
		}
		b.flush();
		b.close();
	}
	public static void getPPIEdges() throws IOException {
		String mainDir = "C:\\Users\\Sheldon\\Documents\\GitHub\\COVID19Motif\\新冠知识图谱\\Phylogeny\\6. nature20\\Net_PPI.txt";
		BufferedReader a = new BufferedReader (new FileReader(mainDir));
		a.readLine();
		String s = a.readLine();
		while(s!=null&&!s.equals("")) {
			String[]tem= s.split(",");
			int gid1 = getInt(tem[0]);
			int gid2 = getInt(tem[1]);
			System.out.println(gid1+","+gid2+",0,"+tem[2]+","+tem[3]);
			s=a.readLine();
		}
	}
	static int getInt(String s) {
		int gid = 0;
		if(s.contains("e+")) {
			String[]tem = s.split("e+");
			double gidd = Double.parseDouble(tem[0])*Math.pow(10,Integer.parseInt(tem[1]));
			gid = (int)gidd;
		}else {
			gid = Integer.parseInt(s);
		}
		return gid;
	}
	public static ArrayList getNode() throws IOException {
		// TODO Auto-generated method stub
		//getGeneNode();
		ArrayList<String>hs = new ArrayList();
		String mainDir = "C:\\Users\\Sheldon\\Documents\\GitHub\\COVID19Motif\\新冠知识图谱\\Phylogeny\\6. nature20\\Drug_List.txt";
		BufferedReader a = new BufferedReader (new FileReader(mainDir));
		//a.readLine();
		String s = a.readLine();
		while(s!=null&&!s.equals("")) {
			hs.add(s);
			//System.out.println("1,"+s);
			s=a.readLine();
		}
		return hs;
	}
	public static ArrayList getGeneNode() throws IOException {
		String mainDir = "C:\\Users\\Sheldon\\Documents\\GitHub\\COVID19Motif\\新冠知识图谱\\Phylogeny\\6. nature20\\Gene_Drug.csv";
		BufferedReader a = new BufferedReader (new FileReader(mainDir));
		//a.readLine();
		ArrayList<Integer>hs = new ArrayList();
		String s = a.readLine();
		while(s!=null&&!s.equals("")) {
			String num = s.split(",")[0];
			try {
				int gid = Integer.parseInt(num);
				if(!hs.contains(gid))
					hs.add(gid);
			}catch(Exception e) {
				String[]tem = num.split("e+");
				double gidd = Double.parseDouble(tem[0])*Math.pow(10,Integer.parseInt(tem[1]));
				int gid = (int)gidd;
				if(!hs.contains(gid))
					hs.add(gid);
				//System.out.println(num);
			}
			s=a.readLine();
		}
		
		for(int i=0;i<hs.size();i++){
			//System.out.println("0,"+hs.get(i));
		}
		
		return hs;

	}

}

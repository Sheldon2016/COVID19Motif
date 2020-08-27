package datapreJSON;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class test {
	
	public static void main(String[]args) throws IOException {
		String mainDir = "C:\\Users\\Sheldon\\Documents\\GitHub\\COVID19Motif\\ÐÂ¹ÚÖªÊ¶Í¼Æ×\\Phylogeny\\2. ²¡¶¾Ç×Ôµ¹ØÏµÍ¼Æ×v1.2\\";
		BufferedReader a = new BufferedReader(new FileReader(mainDir+"phylogeny.lg.nodes"));
		BufferedWriter b = null;
		
		ArrayList<String>city = new ArrayList(), state = new ArrayList(), country = new ArrayList(), strain = new ArrayList(), branch = new ArrayList();
		ArrayList<String>strainall = new ArrayList(), branchall = new ArrayList();
		ArrayList<Integer>locationTypes = new ArrayList();
		HashMap<Integer,Integer> hcity = new HashMap(), hstate = new HashMap(), hcountry = new HashMap(), hstrain = new HashMap(), hbranch = new HashMap();
		String s = a.readLine();
		while(s!=null&&!s.equals("")) {
			String[]tem = s.split(",");
			int locationType = Integer.parseInt(tem[1]);
			locationTypes.add(locationType);
			
			if(locationType==1) {
				entryDeal(branch, hbranch, tem, branchall);
			}
			if(locationType==4) {
				entryDeal(strain, hstrain, tem, strainall);
				}
			if(locationType==2){
				System.out.println(s);
				}		
			if(locationType==0) {
				entryDeal(state, hstate, tem);
			}
			if(locationType==3) {
				entryDeal(country, hcountry, tem);
			}
			s = a.readLine();
		}
		
		b = new BufferedWriter(new FileWriter(mainDir+"Location.txt"));
		for(int i=0;i<country.size();i++) {
			b.write(i+",0,"+country.get(i)+"\n");
		}
		for(int i=0;i<state.size();i++) {
			b.write((i+country.size())+",1,"+state.get(i)+"\n");
		}
		b.flush();
		b.close();
		
		b = new BufferedWriter(new FileWriter(mainDir+"Strain.txt"));
		for(int i=0;i<strain.size();i++) {
			b.write(i+",0,"+strainall.get(i)+"\n");
		}
		for(int i=0;i<branch.size();i++) {
			b.write((i+strain.size())+",1,"+branchall.get(i)+"\n");
		}
		b.flush();
		b.close();
		
		BufferedWriter bmutation = new BufferedWriter(new FileWriter(mainDir+"strain_strain.txt"));
		BufferedWriter bblong = new BufferedWriter(new FileWriter(mainDir+"location_location.txt"));
		BufferedWriter bcome = new BufferedWriter(new FileWriter(mainDir+"strain_location.txt"));
		
		ArrayList<Integer>locations=new ArrayList(), starins=new ArrayList();
		locations.add(0);locations.add(3);starins.add(1);starins.add(4);
		a = new BufferedReader(new FileReader(mainDir+"2edges"));
		s = a.readLine();
		while(s!=null&&!s.equals("")) {
			String[]tem = s.split(",");
			int so = Integer.parseInt(tem[0]), ta = Integer.parseInt(tem[1]);
			int sol = locationTypes.get(so), tal = locationTypes.get(ta);
			if(starins.contains(sol)&&starins.contains(tal)) {
				//a mutation link
				int sid = -1, tid = -1;
				if(sol==4) {
					sid = hstrain.get(so);
				}
				if(sol==1) {
					sid = strain.size()+hbranch.get(so);
				}
				if(tal==4) {
					tid = hstrain.get(ta);
				}
				if(tal==1) {
					tid = strain.size()+hbranch.get(ta);
				}
				bmutation.write(sid+","+tid+"\n");
			}
			if(locations.contains(sol)&&locations.contains(tal)) {
				//a location_belong link
				int sid = -1, tid = -1;
				if(sol==3) {
					sid = hcountry.get(so);
				}
				if(sol==0) {
					sid = country.size()+hstate.get(so);
				}
				if(tal==3) {
					tid = hcountry.get(ta);
				}
				if(tal==0) {
					tid = country.size()+hstate.get(ta);
				}
				bcome.write(sid+","+tid+"\n");
			}
			if(starins.contains(sol)&&locations.contains(tal)) {
				//a location_belong link
				int sid = -1, tid = -1;
				if(sol==4) {
					sid = hstrain.get(so);
				}
				if(sol==1) {
					sid = strain.size()+hbranch.get(so);
				}
				if(tal==3) {
					tid = hcountry.get(ta);
				}
				if(tal==0) {
					tid = country.size()+hstate.get(ta);
				}
				bblong.write(sid+","+tid+"\n");
			}
			if(starins.contains(tal)&&locations.contains(sol)) {
				System.out.println("from location to strain: "+s);
			}
			s = a.readLine();
		}
		
		bmutation.flush();
		bmutation.close();
		bblong.flush();
		bblong.close();
		bcome.flush();
		bcome.close();
		
		//System.out.println(state.toString());
		//System.out.println(country.toString());
		
	}

	private static void entryDeal(ArrayList<String> state, HashMap<Integer, Integer> h, String[] tem) {
		if(state.contains(tem[2])) {
			h.put(Integer.parseInt(tem[0]), state.indexOf(tem[2]));
			//System.out.println("State contains "+tem[2]);
			}
		else {
			h.put(Integer.parseInt(tem[0]), state.size());
			state.add(tem[2]);
			}
	}
	
	private static void entryDeal(ArrayList<String> state, HashMap<Integer, Integer> h, String[] tem, ArrayList<String> stateall) {
		if(state.contains(tem[2])) {
			h.put(Integer.parseInt(tem[0]), state.indexOf(tem[2]));
			//System.out.println("State contains "+tem[2]);
			}
		else {
			h.put(Integer.parseInt(tem[0]), state.size());
			state.add(tem[2]);
			String s = tem[2]+","+tem[3]+","+tem[4]+","+tem[5]+","+tem[6];
			stateall.add(s);
			}
	}

}

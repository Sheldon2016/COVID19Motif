package datapre;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import tools.FileOps;

public class getNodeFromEdges {

	public static void main(String[] args) throws IOException {
		FileOps fo = new FileOps ();
		String mainDir = "C:\\Users\\Sheldon\\Documents\\GitHub\\covid19kg\\data\\HPO\\";
		BufferedReader a = fo.BRead(mainDir+"edges/drug_symptom.txt");
		int columID = 2, columAttachID = 3;
		ArrayList<Integer>ids = new ArrayList();
		ArrayList<String>names = new ArrayList();
		
		BufferedWriter b = fo.BWriter(mainDir+"Disease");
		
		String s = a.readLine();
		while(s!=null&&!s.equals("")) {
			String[]tem = s.split(",");
			int id = Integer.parseInt(tem[columID]);
			String name = tem[columAttachID];
			
			if(!ids.contains(id)) {
				ids.add(id);
				names.add(name);
				b.write(id+","+name+"\n");
			}
			
			
			s = a.readLine();
		}
		
		a = fo.BRead(mainDir+"nodes/HPO.txt");
		s = a.readLine();
		ArrayList<Integer>ids2 = new ArrayList();
		while(s!=null&&!s.equals("")) {
			String[]tem = s.split(",");
			int id = Integer.parseInt(tem[0]);
			
			if(!ids.contains(id)) {
				b.write(id+","+"\n");
				//System.out.println(id);
			}
			ids2.add(id);
			s = a.readLine();
		}
		
		ArrayList<Integer>catched = new ArrayList();
		for(int i=0;i<ids.size();i++) {
			if(!ids2.contains(ids.get(i)))
				System.out.println(ids.get(i)+","+names.get(i));
			else
				catched.add(i);
		}
		
		System.out.println();
		for(int i=0;i<catched.size();i++) {
			System.out.println(ids.get(catched.get(i))+","+names.get(catched.get(i)));
		}
		
		System.out.println(ids.size()-catched.size());
		
		b.flush();
		b.close();
	}

}

package datapre;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import tools.FileOps;

public class Merge {
	
	public static void main(String[]args) throws IOException {
		FileOps fo = new FileOps();
		String mainDirNew = "C:\\Users\\Sheldon\\Downloads\\", mainDirOld="C:\\Users\\Sheldon\\Documents\\GitHub\\covid19kg\\data\\HPO\\";
		String[]filesNew = {"virusprotein-hostprotein.txt"};
		String[]filesOld = {"edges/virusprotein_hostprotein.txt"};
		String[]matchColumsNew = {"1,0"}, attachColumsNew= {"3,2"};
		String[]matchColumsOld = {"1,4"}, attachColumsOld= {"2,5"};
		
		for(int i=0;i<filesNew.length;i++) {
			String strNew = mainDirNew+filesNew[i], strOld = mainDirOld+filesOld[i];
			BufferedReader anew = fo.BRead(strNew);
			BufferedWriter b = fo.BWriter(strNew+"2");
			HashMap<String,String>h = new HashMap();
			HashMap<String,Boolean>h2 = new HashMap();
			
			String snew = anew.readLine();
			while(snew!=null&&!snew.equals("")) {
				while(snew.charAt(0)=='#') {
					snew = anew.readLine();
				}
				String key = getKey(snew, matchColumsNew[i]);
				if(!h.containsKey(key)) {
					h.put(key, snew);
					h2.put(key, false);
				}else {
					fo.jout("Repeated record: "+snew);
				}
				snew = anew.readLine();
			}
			
			//if(!filesOld.equals("")) 
				//only use new files
			//	continue;
			
			BufferedReader aold = fo.BRead(strOld);
			String sold = aold.readLine();
			while(sold!=null&&!sold.equals("")) {
				while(sold.charAt(0)=='#') {
					sold = aold.readLine();
				}
				String key = getKey(sold, matchColumsOld[i]);
				if(!h.containsKey(key)) {
					//use the old record
					//String attach = getKey(sold,attachColumsOld[i]);
					//b.write(key+attach.substring(0,attach.length()-1)+"\n");
					b.write(sold+"\n");
				}else {
					//merge attach with attach old
					String attachOld = getKey(sold,attachColumsOld[i]);
					String attachNew = h.get(key);
					h2.put(key, true);
					System.out.println(attachOld+"\n"+attachNew+"\n");
					b.write(sold+"\n");
				}
				sold = aold.readLine();
			}
			
			Set keys = h2.keySet();
			Iterator iterator=keys.iterator();
			while(iterator.hasNext()) {
				String str = iterator.next()+"";
				if(!h2.get(str)) {
					String attachNew = getKey(h.get(str), attachColumsNew[i]);
					//name1,name2,taxonomy1,taxonomy2
					String[]names = attachNew.split(",");
					String[]IDs   = str.split(",");
					b.write("-1,"+IDs[0]+","+names[0]+",-1,"+IDs[1]+","+names[1]+"\n");
				}
			}
			
			b.flush();
			b.close();
		}
		
		
		
		
	}

	private static String getKey(String snew, String matchColumsNewi) {
		String[]tem = snew.split(",");
		String[]colums = matchColumsNewi.split(",");
		String key = "";
		for(int j=0;j<colums.length;j++) {
			key += tem[Integer.parseInt(colums[j])]+",";
		}
		return key;
	}

}

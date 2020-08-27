package datapre;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class ChrisDeal {

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		String mainDir = "C:\\Users\\Sheldon\\Documents\\GitHub\\covid19kg\\data\\HPO\\edges\\";
		String file = "drug_symptom";
		BufferedReader a = new BufferedReader(new FileReader(mainDir+file+".txt"));
		BufferedWriter b = new BufferedWriter(new FileWriter(mainDir+file+"2.txt"));
		
		String s = a.readLine();
		while(s!=null&&!s.equals("")) {
			String[]tem = s.split(",");
			if(tem.length==4) {
				b.write(s+"\n");
			}else {
				if(s.contains("\"")) {
					String[]tem2 = s.split("\"");
					String s2 = "";
					if(tem2.length<4) {
						s2 = tem2[0]+tem2[1].replaceAll(",", "-")+tem2[2];
					}else if(tem2.length<6) {
						s2 = tem2[0]+tem2[1].replaceAll(",", "-")+tem2[2]+tem2[3].replaceAll(",", "-");
						if(tem2.length==5)
							s2+=tem2[4];
					}else {
						System.out.println(s);
						return;
					}
					b.write(s2+"\n");
				}else {
					System.out.println(s);
					return;
				}
			}
			s=a.readLine();
		}
		
		b.flush();
		b.close();
		System.out.println("Finish.");
	}

}

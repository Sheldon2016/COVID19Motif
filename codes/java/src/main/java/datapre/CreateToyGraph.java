package datapre;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import tool.Datasets;
import tool.FileOps;

public class CreateToyGraph {

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		Datasets dts = new Datasets("C:\\Users\\Sheldon\\Google ‘∆∂À”≤≈Ã£®xdli@connect.hku.hk£©\\DESK\\documents\\researchWorks\\motif-path\\motif-data\\", true);
		FileOps fo = new FileOps();
		dts.Initialize(81);
		int[]label = new int[dts.g.graph.length];
		String[]name = {"A", "B", "C", "D"};
		
		
		for(int i=1;i<label.length;i++) {
			label[i] = (int)(Math.random()*4);
			BufferedWriter a = new BufferedWriter(new FileWriter("C:\\Users\\Sheldon\\Downloads\\"+name[label[i]]+".txt", true));
			a.write(i+"\n");
			a.flush();
			a.close();
			}
		

		
		for(int i=1;i<dts.g.graph.length;i++) {
			for(int j=0;j<dts.g.graph[i].size();j++) {
				int nei = dts.g.graph[i].get(j);
				if(nei<i)
					continue;
				int labelI = label[i], labelNei = label[nei];
				//write i-nei in file "labelI_labelNei" if labelI<=labelNei
				//write nei-i in file "labelNei_labelI" otherwise
				
				if(labelI <= labelNei) {
					BufferedWriter b = new BufferedWriter(new FileWriter("C:\\Users\\Sheldon\\Downloads\\"+name[labelI]+"_"+name[labelNei]+".txt", true));
					b.write(i+","+nei+"\n");
					b.flush();
					b.close();
				}else {
					BufferedWriter b = new BufferedWriter(new FileWriter("C:\\Users\\Sheldon\\Downloads\\"+name[labelNei]+"_"+name[labelI]+".txt", true));
					b.write(nei+","+i+"\n");
					b.flush();
					b.close();
				}
				
			}
		}
		
	}

}

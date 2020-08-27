package algo;

import java.io.IOException;
import java.util.ArrayList;

import datapre.covid19kg;
import tools.CONF;
import tools.Motif;

public class RunMotifMatch {
	public static void main(String[] args) throws IOException {

		MotifMatch mm = new MotifMatch();
		//covid19kg kg = new covid19kg("C:\\Users\\Sheldon\\Documents\\GitHub\\covid19kg\\data\\toyKG\\");
		covid19kg kg = new covid19kg(CONF.mainDir);
		Motif mf = new Motif();
		//note that mf.motif is ranked by node degree
		
		//mf.getDiamond();
		//mf.getTriangle(); int[] labels= {2, 0, 3}; mf.assignLabels(labels);
		//mf.get4clique();
		mf.getTailedTriangle(); int[] labels= {0, 3, 2, 1}; mf.assignLabels(labels);
		
		ArrayList<ArrayList<Integer>>motifIns = mm.match(kg,mf);
		mm.motifIns2String(motifIns, mf, kg);
		
	}
	
	//0:"Disease"
	//1:"Drug"
	//2:"HostProtein"
	//3:"HPO"
	//4:"Virus"
	//5:"VirusProtein"
	//6:"Strain"
	//7:"Location"
	
}

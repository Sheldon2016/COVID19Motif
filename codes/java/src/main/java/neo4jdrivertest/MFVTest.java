package neo4jdrivertest;


import java.io.IOException;
import java.util.List;

import neo4jdriver.MFVF;

public class MFVTest {

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		MFVF a = new MFVF();
		String degVecs= "112|112|112";
		String labels= "B,C,A|A,C,B|A,B,C";
		int snid = 5, tnid = 111;
		String slabel = "A", tlabel = "C";
		
		List<Integer>freVec = a.MFV(degVecs, labels, snid+"", slabel, tnid+"", tlabel);
		System.out.println(freVec.toString());
		
	}

}

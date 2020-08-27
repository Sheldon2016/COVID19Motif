package neo4jdrivertest;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import algo.MotifMatch;
import neo4jdriver.MCOUNTF;

public class MCountTest {
	public static void main(String[]args) throws IOException {
		MCOUNTF mc = new MCOUNTF();
		
		List<List<Integer>>motifIns = mc.MCOUNT("1122", "A,B,B,A");
		
		System.out.println(motifIns.size());
	}
}

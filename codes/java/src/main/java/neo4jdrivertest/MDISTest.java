package neo4jdrivertest;


import java.io.IOException;
import java.util.List;

import neo4jdriver.MDISF;


public class MDISTest {

	public static void main(String[] args) throws IOException, InterruptedException {
		// TODO Auto-generated method stub
		MDISF a = new MDISF();
		String labels = "A,B,C,D", kStr = 4 +"";
		List<List<String>> res = a.MDIS(labels, kStr); // , 5+"", "A"
		//System.out.println(res);
	}

}

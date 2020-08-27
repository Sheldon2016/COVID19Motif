package neo4jdrivertest;


import java.io.IOException;
import java.util.List;

import neo4jdriver.MCLGF;


public class MCLQTest {

	public static void main(String[] args) throws IOException, InterruptedException {
		
		MCLGF a = new MCLGF();
		List<List<Integer>> res = a.MCLQ("112", "A,B,C", 5+"", "A");
		System.out.println(res.toString());
	}

}

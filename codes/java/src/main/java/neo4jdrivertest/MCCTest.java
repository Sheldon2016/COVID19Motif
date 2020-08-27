package neo4jdrivertest;


import java.io.IOException;
import java.util.List;

import neo4jdriver.MCCF;


public class MCCTest {

	public static void main(String[] args) throws IOException, InterruptedException {
		MCCF a = new MCCF();
		List<List<Integer>> res = a.MCC("112", "A,B,C", 5+"", "A");
		System.out.println(res.toString());
	}

}

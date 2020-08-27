package neo4jdrivertest;


import java.io.IOException;

import neo4jdriver.MPPRF;

public class MPPRTest {

	public static void main(String[] args) throws IOException, InterruptedException {
		// TODO Auto-generated method stub
		
		MPPRF m = new MPPRF();
		System.out.println(m.MPPR("1122", "A,B,C,D", "5", "A", "D", 10000+"", 0.85+""));
	}

}

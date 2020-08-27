package neo4jdrivertest;


import java.io.IOException;

import neo4jdriver.MAMF;

public class MAMTest {

	public static void main(String[] args) throws IOException {
		
		MAMF a = new MAMF();
		System.out.println(a.MAM("112", "A,B,C"));
	}

}

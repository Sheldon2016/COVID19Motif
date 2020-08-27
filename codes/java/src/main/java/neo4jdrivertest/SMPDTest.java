package neo4jdrivertest;


import java.io.IOException;
import java.util.List;

import neo4jdriver.SMPDF;


public class SMPDTest {

	public static void main(String[] args) throws IOException, InterruptedException {
		SMPDF a = new SMPDF();
		String pathlength = a.SMPD("112", "A,B,C", "5", "A", "17", "A");
		System.out.println(pathlength+": "+a.path.toString());
	}

}

package mmc;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

public class TestMMC {

	private static Logger logger = Logger.getLogger(TestMMC.class);
	
	public static void main(String[] args) throws InterruptedException {

		String s = "C:\\Users\\Sheldon\\Documents\\GitHub\\COVID19Motif\\codes\\mc-explorer\\platform\\WebContent\\file\\input1-DBLP.txt";
		
		BasicConfigurator.configure();
		logger.info("start mclique searching thread");

		MainMMC mmc = new MainMMC (s);
		
		Graph g = mmc.setDummyMotif2();
		
		mmc.doMCC(g, 4);
		
	}

}

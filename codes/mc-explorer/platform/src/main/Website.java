package main;


import org.apache.log4j.Logger;

import java.util.HashMap;



import java.util.HashSet;
import java.util.Vector;

import mmc.Graph;
import mmc.MainMMC;


public class Website {
	private static Logger logger = Logger.getLogger(Website.class);

	public static HashMap<Integer,String> main= new HashMap<>();

	public static final String MAIN_MMC = "mainMMC";

	static{
		String s1, s2, s3, s4, s5;

		String basePath = Website.class.getClassLoader().getResource("/").getPath();
		s1 = basePath + "file/input1-DBLP.txt";
		s2 = basePath + "file/input2-amazon.txt";
        s3 = basePath + "file/input3-movielens.txt";
		s4 = basePath + "file/input4-DrugBank(1).txt";
		s5 = basePath + "file/input5-DrugBank(2).txt";

		main.put(1, s1);
		main.put(2, s2);
		main.put(3, s3);
        main.put(4, s4);
		main.put(5, s5);
		logger.info("Done Initializing website");
	}

	public static void getMMCModeDefault(int which, Graph motif, int mustContain) throws InterruptedException {
		MainMMC temp = new MainMMC(main.get(which));
		temp.doMCC(motif, mustContain);
	}

	public static Vector<HashSet<Integer>> getTraditionalCliquesModeDefault(int which, int mustContain, int limit) {
		MainMMC temp = new MainMMC(main.get(which));
		temp.MMCLimit = limit;
		return temp.doBK(mustContain);
	}
	
	public static void getMMCModeUpload(MainMMC mainMMC, Graph motif, int mustContain) throws InterruptedException {
		mainMMC.doMCC(motif, mustContain);
	}

	public static Vector<HashSet<Integer>> getTraditionalCliquesModeUpload(MainMMC mainMMC, int mustContain, int limit) {
		mainMMC.MMCLimit = limit;
		return mainMMC.doBK(mustContain);
	}

	/*
	public static void main(String[] args) throws InterruptedException{
		long startTime = System.currentTimeMillis();
		//w.main.get(1).graph.print();
		Graph m = main.get(1).setDummyMotif3();
		m.print();
		main.get(1).doMCC(m, -1);
		// ... do something ...
		long estimatedTime = System.currentTimeMillis() - startTime;
		logger.info("Elapsed time is: " + estimatedTime / 1000.0);
	}

	public static Vector<HashSet<Integer>> test1() throws InterruptedException {
		MainMMC temp = main.get(1);
		Graph m = main.get(1).setDummyMotif2();
		main.get(1).doMCC(m, -1);
		Vector<HashSet<Integer>> result = new Vector<HashSet<Integer>>();
		for(int i = 0, size = temp.maxMotifCliques.size(); i < size; i++) {
			result.add( new HashSet<Integer>(temp.maxMotifCliques.get(i)));
		}
		return result;
	}
	*/
}

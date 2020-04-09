package mmc;

import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Vector;


public class Utilities {
	private static Logger logger = Logger.getLogger(Utilities.class);
	public static<T extends Utilities>  void Print( Iterable<T> A) {
		for (Object a : A) {
			System.out.print(" " + a);
		}
		logger.info("");
	}

	public static void Print( Vector<Vector<Integer> > subgraphs) {
		for (int i = 0, size = subgraphs.size(); i < size; i++) {
			logger.info( "Solution " + i+1 + ":");
			for (int j = 0, size2=subgraphs.get(i).size(); j < size2; j++) {
				logger.info(j + ". " + subgraphs.get(i).get(j));
			}
		}
	}

	public static void PrintMap( HashMap<Integer, HashSet<Integer> > mp) {
		for (Map.Entry<Integer, HashSet<Integer> > e : mp.entrySet()) {
			System.out.print("label " + e.getKey() + ':');
			for (Integer u : e.getValue()) {
				System.out.print(" " + u);
			}
			logger.info("");
		}
	}
}

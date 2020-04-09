package main;

import java.util.*;

import mmc.Graph;
import org.apache.log4j.Logger;

public class Utilities {
	private static Logger logger = Logger.getLogger(Utilities.class);
	// raw cliques found by mclique algorithm
	public static Vector<HashSet<Integer>> cliques;
	// combine cliques together based on a same node of a certain type
	// note that a combinedClique is a combination of one or more
	// m-cliques. We always respond to client with combinedCliques,
	// even if we don't really combine mcliques.
	public static Vector<HashSet<Integer>> combinedCliques;
	// the type of node based on which we want to combine
	// use type == -1 to represent the situation where we don't want to combine mcliques
	// based on a same node
	public static Integer type;
	public static boolean hasNewData;
	public static boolean searchFinished;
	
	public static HashMap<Integer, Integer> upperBound = new HashMap<Integer, Integer>();
	public static HashMap<Integer, Integer> lowerBound = new HashMap<Integer, Integer>();

	public static double weightLowerBound = 0;

	public static ArrayList<Integer> mustNotContain = new ArrayList<>();
	
	// sortingCriteria:
	// 0: First Found (Default)
	// 1: Sort by number of nodes in result (descending)
	// 2: Sort by percentage of nodes with same subtype (descending)
	public static int sortingCriteria = 0;

	public static int motifCount = 0;
	
	/**
	 * Combine cliques with a same node of a certain type
	 * @param type
	 * @return 
	 */
	public static void combine(Graph g, HashSet<Integer> mclique) {
		HashSet<Integer> s1 = getNodeFromType(g, mclique, type);
		boolean shouldAdd = true;
		if (combinedCliques == null) {
			combinedCliques = new Vector<HashSet<Integer>>();
		} else if (type != -1) {
			// if type == -1, we don't combine mcliques
			for (HashSet<Integer> combinedClique: combinedCliques) {
				HashSet<Integer> s2 = getNodeFromType(g, combinedClique, type);
				s2.retainAll(s1);
				Optional<Integer> intersection = s2.stream().findFirst();
				if (intersection.isPresent()) {
					Integer value = intersection.get();
					logger.info("intersection node found, number=" + value);
					// no need to add the mclique to list again
					shouldAdd = false;
					// combine mclique and combinedClique together
					combinedClique.addAll(mclique);
				}
			}
		}
		if (shouldAdd) {
			combinedCliques.add(mclique);
		}
		if (sortingCriteria == 1) {
			// sort by number of nodes in result (descending order)
			// TODO: use insertion sort
			combinedCliques.sort((x, y) -> y.size() - x.size());
		}
		if (sortingCriteria == 2) {
			// sort by percentage of nodes with same subtype (descending)
			logger.info("Sorting according to subtype will be done in the frontend (info related to category is not stored in backend");
		}
	}
	
	/**
	 * helper function to get number of nodes with a certain type
	 * @param g
	 * @param mclique
	 * @param type
	 * @return
	 */
	public static HashSet<Integer> getNodeFromType(Graph g, HashSet<Integer> mclique, Integer type) {
		HashSet<Integer> nodes = new HashSet<Integer>();
		mclique.forEach((node) -> {
			if (g.labels.get(node) == type) {
				nodes.add(node);
			}
		});
		return nodes;
	}
	
	/**
	 * check if current mclique has exceeded upper bound limit
	 * @param g
	 * @return
	 */
	public static boolean checkIfUpperBound(Graph g, HashSet<Integer> mclique) {
		for (Integer key: upperBound.keySet()) {
			int type = key;
			int threshold = upperBound.get(key);
			if (getNodeFromType(g, mclique, type).size() > threshold) {
				return true;
			}
		}
		return false;
	}

	/**
	 * check if current mclique has not reached lower bound limit
	 * @param g
	 * @return true if lower bound limit is not reached yet
	 */
	public static boolean checkIfLowerBound(Graph g, HashSet<Integer> mclique) {
		for (Integer key: lowerBound.keySet()) {
			int type = key;
			int threshold = lowerBound.get(key);
			if (getNodeFromType(g, mclique, type).size() < threshold) {
				return true;
			}
		}
		return false;
	}
}

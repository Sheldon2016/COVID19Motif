package mmc;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

//import main.Utilities;
import org.apache.log4j.Logger;

import javax.rmi.CORBA.Util;

public class MaximalMotifClique {
    
	MainUtilities Utilities = new MainUtilities();
	
	private static Logger logger = Logger.getLogger(MaximalMotifClique.class);

    public MainMMC main = null;

    MaximalMotifClique() {
    }

    MaximalMotifClique(MainMMC main) {
        this.main = main;
    }

    public void GetLabel2NodesMap(Graph g, HashMap<Integer, HashSet<Integer>> label2nodes) {
        label2nodes.clear();
        for (int i = 0; i < g.nodeNum; i++) {
            if (label2nodes.containsKey(g.labels.get(i))) { //Jiafeng
                label2nodes.get(g.labels.get(i)).add(i);
            } else {
                HashSet<Integer> temp = new HashSet<Integer>();
                temp.add(i);
                label2nodes.put(g.labels.get(i), temp);
            }
        }
    }

    public void GetLabelAdj(Graph g, HashMap<Integer, HashSet<Integer>> labelAdj) {
        labelAdj.clear();
        int i, j;
        for (Edge e : g.edges) {
            i = e.from;
            j = e.to;
            if (labelAdj.containsKey(g.labels.get(i))) {
                labelAdj.get(g.labels.get(i)).add(g.labels.get(j));
            } else {
                HashSet<Integer> temp = new HashSet<Integer>();
                temp.add(g.labels.get(j));
                labelAdj.put(g.labels.get(i), temp);
            }
        }
    }

    public void GetLabel2NodesMap(int current_node, HashSet<Integer> U, Vector<Integer> labels,
            HashMap<Integer, HashSet<Integer>> label2nodes, HashSet<Integer> label_set) {
        for (Integer u : U) {
            if (u == current_node) {
                continue;
            }
            if (label2nodes.containsKey(labels.get(u))) {
                label2nodes.get(labels.get(u)).add(u);
            } else {
                HashSet<Integer> temp = new HashSet<Integer>();
                temp.add(u);
                label2nodes.put(labels.get(u), temp);
                label_set.add(labels.get(u));
            }
        }
    }

    public void copyGraph(Graph target, Graph origin, HashSet<Integer> nodes) {
        HashMap<Integer, Integer> nodeMap = new HashMap<Integer, Integer>();
        int count = 0;
        target.labels.setSize(nodes.size());
        for (Integer e : nodes) {
            nodeMap.put(e, count);
            target.labels.set(count, origin.labels.get(e));
            count++;
        }

        target.nodeNum = count;
        for (int i = 0; i < count; i++) {
            target.adjList.add(new HashSet<Integer>());
        }
        for (Integer e : nodes) {
            for (Integer v : nodes) {
                if (origin.adjList.get(e).contains(v)) {
                    target.adjList.get(nodeMap.get(e)).add(nodeMap.get(v));
                    target.edges.add(new Edge(nodeMap.get(e), nodeMap.get(v)));
                }
            }
        }
        target.edgeNum = target.edges.size();
    }

    public void IsoPreCheckNodeSet(HashSet<Integer> nodes, Graph graph, HashSet<Integer> coverset) {
        boolean flag;
        for (Integer e : main.pre_isoCheckSet) {
            if (e == main.currentU) {
                continue;
            }
            flag = true;
            for (Integer w : nodes) {
                if (w == main.currentU || w == e) {
                    continue;
                }

                if (graph.adjList.get(e).contains(w)
                        && main.motif_labelAdj.get(graph.labels.get(e)).contains(graph.labels.get(w)) // only for undirected graph
                        && !graph.adjList.get(main.currentU).contains(w)) {
                    flag = false;
                    break;
                }
            }
            if (flag) {
                coverset.add(e);
            }
        }
    }

    public boolean DFSForIsomorphismCheck(HashMap<Integer, HashSet<Integer>> cands,
            HashMap<Integer, HashSet<Integer>> cand_label2nodes, Graph motif, Graph graph,
            HashSet<Integer> label_set, boolean Flag) throws InterruptedException {

        if (Flag == false) {
            return Flag;
        }
        if (label_set.isEmpty()) {
            if (main.tempGraph == null) {
                main.tempGraph = new Graph();
            } else {
                main.tempGraph.clear();
            }
            HashSet<Integer> nodes = new HashSet<Integer>();
            for (HashSet<Integer> e : cands.values()) {
                for (Integer u : e) {
                    nodes.add(u);
                }
            }

            main.tempMatchedSubgraphs.clear();
            copyGraph(main.tempGraph, graph, nodes);

            // Here we still use the naive subgraph matching method to do checking.
            main.sgm.GetMatchedSubgraph(motif, main.tempGraph, main.tempMatchedSubgraphs, true);

            if (main.tempMatchedSubgraphs.isEmpty()) {
                Flag = false;
            }
            return Flag;
        }
        //TODO: check the iterator
        Iterator<Integer> label = label_set.iterator();
        Integer label_value = label.next();
        Iterator<Integer> temp_node = cand_label2nodes.get(label_value).iterator();
        Integer temp_node_value = temp_node.next();
        cand_label2nodes.get(label_value).remove(temp_node_value);
        InsertNode(cands, temp_node_value, label_value);
        boolean remove_label = false;

        if (cands.get(label_value).size() == main.motif_label2Nodes.get(label_value).size()) {
            label_set.remove(label_value);
            remove_label = true;
        }

        Flag = DFSForIsomorphismCheck(cands, cand_label2nodes, motif, graph, label_set, Flag);
        if (remove_label == true) {
            label_set.add(label_value);
        }
        cands.get(label_value).remove(temp_node_value);
        if (cand_label2nodes.get(label_value).size() >= (main.motif_label2Nodes.get(label_value).size() - cands.get(label_value).size())) {
            Flag = DFSForIsomorphismCheck(cands, cand_label2nodes, motif, graph, label_set, Flag);
        }
        InsertNode(cand_label2nodes, temp_node_value, label_value);
        return Flag;
    }

    public void InsertNode(HashMap<Integer, HashSet<Integer>> cands, int v, int label) {
        if (cands.containsKey(label)) {
            cands.get(label).add(v);
        } else {
            HashSet<Integer> temp = new HashSet<Integer>();
            temp.add(v);
            cands.put(label, temp);
        }
    }

    public boolean IsomorphismCheck(HashSet<Integer> U, int u, Graph motif, Graph graph) throws InterruptedException {

        main.candLabel2Nodes.clear();
        main.labelSet.clear();

        GetLabel2NodesMap(u, U, graph.labels, main.candLabel2Nodes, main.labelSet);
        main.pre_isoCheckSet.clear();
        for (Integer e : main.candLabel2Nodes.get(graph.labels.get(u))) {
            main.pre_isoCheckSet.add(e);
        }
        main.currentU = u;

        boolean Flag = true;

        for (HashSet<Integer> e : main.cands.values()) {
            e.clear();
        }
        main.cands.clear();

        HashSet<Integer> coverset = new HashSet<Integer>();
        IsoPreCheckNodeSet(U, graph, coverset);

        if (coverset.size() + 1 > main.motif_label2Nodes.get(graph.labels.get(u)).size()) {
            return true;
        }

        for (Integer e : coverset) {
            InsertNode(main.cands, e, graph.labels.get(e));
            main.candLabel2Nodes.get(graph.labels.get(e)).remove(e);
        }

        InsertNode(main.cands, u, graph.labels.get(u));
        if (main.motif_label2Nodes.get(graph.labels.get(u)).size() == main.cands.get(graph.labels.get(u)).size()) {
            main.labelSet.remove(graph.labels.get(u));
        }

        Flag = DFSForIsomorphismCheck(main.cands, main.candLabel2Nodes, motif, graph, main.labelSet, Flag);

        return Flag;
    }

    public boolean IsomorphismCheck(HashSet<Integer> U, int u, int v, Graph motif, Graph graph) throws InterruptedException {

        for (HashSet<Integer> e : main.candLabel2Nodes.values()) {
            e.clear();
        }
        main.candLabel2Nodes.clear();
        main.labelSet.clear();

        GetLabel2NodesMap(u, U, graph.labels, main.candLabel2Nodes, main.labelSet);

        boolean Flag = true;
        main.cands.clear();

        main.pre_isoCheckSet.clear();
        for (Integer e : main.candLabel2Nodes.get(graph.labels.get(v))) {
            main.pre_isoCheckSet.add(e);
        }
        main.currentU = v;

        HashSet<Integer> coverset = new HashSet<Integer>();

        //It is intended for deep copy or shallow copy
        HashSet<Integer> Y = new HashSet<Integer>();
        for (Integer e : U) {
            Y.add(e);
        }
        Y.add(u);
        IsoPreCheckNodeSet(Y, graph, coverset);
        coverset.remove(u);
        int x = 1;
        if (graph.labels.get(u) == graph.labels.get(v)) {
            x++;
        }
        if (coverset.size() + x > main.motif_label2Nodes.get(graph.labels.get(v)).size()) {
            return true;
        }
        for (Integer e : coverset) {
            InsertNode(main.cands, e, graph.labels.get(e));
            main.candLabel2Nodes.get(graph.labels.get(e)).remove(e);
        }

        InsertNode(main.cands, u, graph.labels.get(u));
        InsertNode(main.cands, v, graph.labels.get(v));

        if (main.motif_label2Nodes.get(graph.labels.get(u)).size() < 
                main.cands.get(graph.labels.get(u)).size()) {
            return true;
        } else if (main.motif_label2Nodes.get(graph.labels.get(u)).size() == 
                main.cands.get(graph.labels.get(u)).size()) {
            main.labelSet.remove(graph.labels.get(u));
        }

        if (graph.labels.get(v) != graph.labels.get(u)
                && main.motif_label2Nodes.get(graph.labels.get(v)).size() == 
                main.cands.get(graph.labels.get(v)).size()) {
            main.labelSet.remove(graph.labels.get(v));
        }

        Flag = DFSForIsomorphismCheck(main.cands, main.candLabel2Nodes, motif, 
                graph, main.labelSet, Flag);

        return Flag;
    }

    public void GetCandidates_Special(HashMap<Integer, HashSet<Integer>> label2adj, 
            HashSet<Integer> current_node_set, Graph motif, Graph graph, HashSet<Integer> candidates) {

        if (main.isVisited == null) {
            main.isVisited = new Boolean[graph.nodeNum];
            for (int i = 0; i < graph.nodeNum; i++) {
                main.isVisited[i] = false;
            }
        }

        HashSet<Integer> temp_visited = new HashSet<Integer>();

        for (Integer e : current_node_set) {
            main.isVisited[e] = true;
            temp_visited.add(e);
        }
        boolean flag;

        for (Integer id : current_node_set) {
            for (Integer neighbor : graph.adjList.get(id)) {
                int label = graph.labels.get(neighbor);
                if (!main.isVisited[neighbor]) {
                    main.isVisited[neighbor] = true;
                    temp_visited.add(neighbor);

                    // check label is in label2adj
                    if (label2adj.containsKey(label)) {
                        // check nid is connected to all nodes in label2adj[label]
                        flag = true;
                        for (Integer element : label2adj.get(label)) {
                            if (!graph.adjList.get(neighbor).contains(element)) {
                                flag = false;
                                break;
                            }
                        }
                        if (flag) {
                            candidates.add(neighbor);
                        }
                    }
                }
            }
        }

        for (Integer e : temp_visited) {
            main.isVisited[e] = false;
        }
    }

    public void GetCandidates(HashMap<Integer, HashSet<Integer>> label2adj, HashSet<Integer> current_node_set,
            Graph motif, Graph graph, HashSet<Integer> candidates) throws InterruptedException {

        if (main.isVisited == null) {
            main.isVisited = new Boolean[graph.nodeNum];

            for (int i = 0; i < graph.nodeNum; i++) {
                main.isVisited[i] = false;
            }
            //Arrays.fill(main.isVisited, false);
        }

        HashSet<Integer> temp_visited = new HashSet<Integer>();

        for (Integer e : current_node_set) {
            main.isVisited[e] = true;
            temp_visited.add(e);
        }
        boolean flag;
        for (Integer id : current_node_set) {
            for (Integer neighbor : graph.adjList.get(id)) {

                int label = graph.labels.get(neighbor);
                if (!main.isVisited[neighbor]) {
                    main.isVisited[neighbor] = true;
                    temp_visited.add(neighbor);

                    // check label is in label2adj
                    if (label2adj.containsKey(label)) {
                        // check nid is connected to all nodes in label2adj[label]
                        flag = true;
                        for (Integer element : label2adj.get(label)) {
                            if (!graph.adjList.get(neighbor).contains(element)) {
                                flag = false;
                                break;
                            }
                        }

                        if (flag || IsomorphismCheck(current_node_set, neighbor, motif, graph)) {
                            candidates.add(neighbor);
                        }
                    }
                }
            }
        }
        for (Integer e : temp_visited) {
            main.isVisited[e] = false;
        }
    }

    public void GetCandidates(HashSet<Integer> current_node_set,
            int new_node, HashMap<Integer, HashSet<Integer>> label2adj, HashSet<Integer> candidates,
            Graph motif, Graph graph, HashSet<Integer> NOT, HashSet<Integer> NOT_new, HashSet<Integer> new_candidate_set) throws InterruptedException {
        Boolean flag;
        for (Integer nid : candidates) {

            int label = graph.labels.get(nid);
            // if the node nid can connect to all nodes in label2adj[label], then add it into new_candidate_set
            flag = true;

            for (Integer element : label2adj.get(label)) {
                if (!graph.adjList.get(nid).contains(element)) {
                    flag = false;
                    break;
                }
            }
            if (flag || IsomorphismCheck(current_node_set, new_node, nid, motif, graph)) {
                new_candidate_set.add(nid);
            }
        }

        // generate NOT_new
        for (Integer nid : NOT) {
            int label = graph.labels.get(nid);
            // if the node nid can connect to all nodes in label2adj[label], then add it into NOT_new
            flag = true;

            for (Integer element : label2adj.get(label)) {
                if (!graph.adjList.get(nid).contains(element)) {
                    flag = false;
                    break;
                }
            }

            if (flag || IsomorphismCheck(current_node_set, new_node, nid, motif, graph)) {
                NOT_new.add(nid);
                if (new_candidate_set.isEmpty()) {
                    return;
                }
            }
        }
    }

    public void GetCandidates_Special(HashSet<Integer> current_node_set,
            int new_node, HashMap<Integer, HashSet<Integer>> label2adj, 
            HashSet<Integer> candidates, Graph motif, Graph graph, 
            HashSet<Integer> NOT, HashSet<Integer> NOT_new, HashSet<Integer> candidate_new) {
        boolean flag;

        for (Integer nid : candidates) {

            int label = graph.labels.get(nid);
            // if the node nid can connect to all nodes in label2adj[label], then insert it into new_candidate_set
            flag = true;

            for (Integer element : label2adj.get(label)) {
                if (!graph.adjList.get(nid).contains(element)) {
                    flag = false;
                    break;
                }
            }

            if (flag) {
                candidate_new.add(nid);
            }
        }

        // generate NOT_new
        for (Integer nid : NOT) {
            int label = graph.labels.get(nid);
            // if the node nid can connect to all nodes in label2adj[label], then insert it into NOT_new
            flag = true;

            for (Integer element : label2adj.get(label)) {
                if (!graph.adjList.get(nid).contains(element)) {
                    flag = false;
                    break;
                }
            }

            if (flag) {
                NOT_new.add(nid);
                if (candidate_new.isEmpty()) {
                    return;
                }
            }
        }
    }

    public void FindMaximalMotifClique_Special(HashMap<Integer, HashSet<Integer>> label2adj,
            Vector<Integer> matched_subgraph, Graph motif, Graph graph,
            HashSet<Integer> current_node_set, HashSet<Integer> candidates, Vector<HashSet<Integer>> maximal_motif_cliques) throws InterruptedException {

        // Create Label2Adj structure based on motif
        for (Integer from : matched_subgraph) {
            for (Integer to : matched_subgraph) {
                if (from < to && graph.adjList.get(from).contains(to)
                        && main.motif_labelAdj.get(graph.labels.get(from)).contains(graph.labels.get(to))) {

                    if (!label2adj.containsKey(graph.labels.get(from))) { // label.get(from) does not exist in label2adj
                        label2adj.put(graph.labels.get(from), new HashSet<Integer>());
                    }
                    label2adj.get(graph.labels.get(from)).add(to);

                    if (!label2adj.containsKey(graph.labels.get(to))) { // label.get(to) does not exist in label2adj
                        label2adj.put(graph.labels.get(to), new HashSet<Integer>());
                    }
                    label2adj.get(graph.labels.get(to)).add(from);
                }
            }
        }
        current_node_set.clear();
        for (Integer e : matched_subgraph) {
            current_node_set.add(e);
        }

        GetCandidates_Special(label2adj, current_node_set, motif, graph, candidates);

        HashSet<Integer> NOT = new HashSet<Integer>();

        DFSMaximal_Special(label2adj, current_node_set, motif, graph, candidates,
                NOT, maximal_motif_cliques);

    }

    public void FindMaximalMotifClique(HashMap<Integer, HashSet<Integer>> label2adj,
            Vector<Integer> matched_subgraph, Graph motif, Graph graph,
            HashSet<Integer> current_node_set, HashSet<Integer> candidates, Vector<HashSet<Integer>> maximal_motif_cliques) throws InterruptedException {

        HashSet<Integer> labels = new HashSet<Integer>();
        for (Integer i : motif.labels) {
            labels.add(i);
        }
        if (labels.size() == motif.nodeNum || motif.edgeNum == motif.nodeNum * (motif.nodeNum - 1)) {
            FindMaximalMotifClique_Special(label2adj, matched_subgraph, motif, graph,
                    current_node_set, candidates, maximal_motif_cliques);
            return;
        }

        // Create Label2Adj structure
        for (Integer from : matched_subgraph) {
            for (Integer to : matched_subgraph) {
                if (from < to && graph.adjList.get(from).contains(to)
                        && main.motif_labelAdj.get(graph.labels.get(from)).contains(graph.labels.get(to))) {
                    if (!label2adj.containsKey(graph.labels.get(from))) { // label.get(from) does not exist in label2adj
                        label2adj.put(graph.labels.get(from), new HashSet<Integer>());
                    }
                    label2adj.get(graph.labels.get(from)).add(to);

                    if (!label2adj.containsKey(graph.labels.get(to))) { // label.get(to) does not exist in label2adj
                        label2adj.put(graph.labels.get(to), new HashSet<Integer>());
                    }
                    label2adj.get(graph.labels.get(to)).add(from);
                }
            }
        }
        current_node_set.clear();
        for (Integer e : matched_subgraph) {
            current_node_set.add(e);

        }

        GetCandidates(label2adj, current_node_set, motif, graph, candidates);

        HashSet<Integer> NOT = new HashSet<Integer>();

        DFSMaximal(label2adj, current_node_set, motif, graph, candidates,
                NOT, maximal_motif_cliques);

    }

    public int GetRandomElement(HashSet<Integer> elements) {

        if (elements.isEmpty()) {
            return -1;
        }
        //	    int id = rand() % elements.size(); c++ version
        //	    below is java version
        int id = 0;
        try {
            id = Random.class.newInstance().nextInt(elements.size());
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        //TODO: check the iterator
        Iterator<Integer> iter = elements.iterator();

        Integer result = new Integer(0);
        while (id != -1) {
            result = iter.next();
            id--;
        }
        return result;
    }

    public int Pick(HashSet<Integer> current_node_set, Graph motif,
            Graph graph, HashSet<Integer> candidates, HashSet<Integer> NOT) {
	// try to find a node x which can not be included into the result HashSet with element_in_not simultaneously.

        //extern HashMap<Integer, HashSet<Integer> > motif_label2nodes;
        if (NOT.isEmpty()) {
            return GetRandomElement(candidates);
        }

        if (EarlyStopCheck(current_node_set, motif, graph, candidates, NOT)) {
            return -1;
        } else {
            return GetRandomElement(candidates);
        }
    }

    public int Pick_Special(HashSet<Integer> current_node_set, Graph motif,
            Graph graph, HashSet<Integer> candidates, HashSet<Integer> NOT) {
        // try to find a node x which can not be included into the result HashSet with element_in_not simultaneously.

        if (NOT.isEmpty()) {
            return GetRandomElement(candidates);
        }

        for (Integer x : NOT) {
            boolean flag = true;
            for (Integer y : candidates) {
                if (main.motif_labelAdj.get(graph.labels.get(y)).contains(graph.labels.get(x))
                        && !graph.adjList.get(x).contains(y)) {
                    flag = false;
                    break;
                }
            }
            if (flag) {
                return -1;
            }
        }

        return GetRandomElement(candidates);
    }

    public boolean SubgraphInSetTrieNode(Vector<Integer> subgraph) {
        //extern SetTrieNode *root;
        Vector<Integer> subgraph_temp = new Vector<Integer>(subgraph);
        Collections.sort(subgraph_temp);
        int i = 0;
        SetTrieNode temp = main.root;
        while (!temp.isEndPoint()) {
            temp = temp.getChild(subgraph_temp.get(i++));
            if (temp == null) {
                return false;
            }
        }
        return true;
    }

    public boolean FindEndPointInSetTrieNode(HashSet<Integer> current_node_set) {

        // Jiafeng: Elements in current_node_set should be visited in ascending order;
        Set<Integer> set = new TreeSet<Integer>(current_node_set);

        HashSet<SetTrieNode> temp_set = new HashSet<SetTrieNode>();
        HashSet<SetTrieNode> stn_cands = new HashSet<SetTrieNode>();
        SetTrieNode temp = main.root;
        stn_cands.add(main.root);
        int n = current_node_set.size();
        for (Integer e : set) {
            n--;
            temp_set.clear();
            for (SetTrieNode stn : stn_cands) {
                temp = stn.getChild(e);
                if (temp != null) {
                    if (temp.isEndPoint()) {
                        return true;
                    }
                    temp_set.add(temp);
                }
                if (stn.getHeight() + n >= main.motifNodeSize) {
                    temp_set.add(stn);
                }
            }
            stn_cands.clear();
            for (SetTrieNode x : temp_set) {
                stn_cands.add(x);
            }
            //stn_cands = temp_set;
        }
        return false;
    }

    public boolean EarlyStopCheck(HashSet<Integer> current_node_set, Graph motif, Graph graph, HashSet<Integer> candidates, HashSet<Integer> NOT) {

        //extern HashMap<Integer, HashSet<Integer> > motif_label2nodes;
        int num = 0;
        boolean flag;
        int label;

        for (Integer element_in_not : NOT) {
            num = 0;
            label = graph.labels.get(element_in_not);
            for (Integer e : current_node_set) {
                if (graph.labels.get(e) == label) {
                    flag = true;
                    for (Integer w : current_node_set) {
                        if (e != w && graph.adjList.get(e).contains(w)
                                && main.motif_labelAdj.get(graph.labels.get(e)).contains(graph.labels.get(w)) // only for undirected graph
                                && !graph.adjList.get(element_in_not).contains(w)) {
                            flag = false;
                            break;
                        }
                    }
                    if (!flag) {
                        continue;
                    }
                    for (Integer w : candidates) {
                        if (e != w && graph.adjList.get(e).contains(w)
                                && main.motif_labelAdj.get(graph.labels.get(e)).contains(graph.labels.get(w))
                                // only for undirected graph
                                && !graph.adjList.get(element_in_not).contains(w)) {
                            flag = false;
                            break;
                        }
                    }
                    if (flag) {
                        num++;
                    }
                }
            }

            if (1 + num > main.motif_label2Nodes.get(label).size()) {
                return true;
            }
        }
        return false;
    }

    public void DFSMaximal(HashMap<Integer, HashSet<Integer>> label2adj, HashSet<Integer> current_node_set,
            Graph motif, Graph graph, HashSet<Integer> candidates, HashSet<Integer> NOT,
            Vector<HashSet<Integer>> maximal_motif_cliques) throws InterruptedException {

        for (int mustNotContainNode: Utilities.mustNotContain) {
            if (current_node_set.contains(mustNotContainNode)) {
                return;
            }
        }

    	if (Utilities.checkIfUpperBound(graph, current_node_set) == true) {
    		return;
    	}
    	
    	
        if (main.MMCLimit != -1 && maximal_motif_cliques.size() > main.MMCLimit) {
            return;
        }

        if (candidates.isEmpty() && NOT.isEmpty()) {

    	    if (Utilities.checkIfLowerBound(graph, current_node_set) == true) {
    	        return;
            }

            main.maxCliqueSize = Math.max(main.maxCliqueSize, (int) current_node_set.size());

            HashSet<Integer> temp = new HashSet<Integer>();
            for (Integer e : current_node_set) {
                temp.add(e);
            }

            maximal_motif_cliques.add(temp);
            Utilities.combine(graph, temp);
            Utilities.hasNewData = true;
            if (Thread.interrupted()) {
            	logger.info("worker thread receives stop signal");
            	throw new InterruptedException();
            }
			Thread.yield();
            logger.info("A new mmc found, " + maximal_motif_cliques.size() + " found in total");
            return;
        }
        if (candidates.isEmpty()) {
            return;
        }

        HashSet<Integer> NOT_new = new HashSet<Integer>();
        HashSet<Integer> candidates_new = new HashSet<Integer>();

        while (!candidates.isEmpty()) {
            if (main.MMCLimit != -1 && maximal_motif_cliques.size() >= main.MMCLimit) {
                return;
            }
            int new_node = Pick(current_node_set, motif, graph, candidates, NOT);
            // new_node equals -1 means all nodes are "containable" with at least one node in NOT (i.e., the first element of NOT)
            if (new_node == -1) {
                return;
            }
            candidates.remove(new_node);
            current_node_set.add(new_node);

            if (!FindEndPointInSetTrieNode(current_node_set)) {

                // Update label2adj.
                HashSet<Integer> new_set = new HashSet<Integer>();
                int label = graph.labels.get(new_node);
                for (Integer e : current_node_set) {
                    if (graph.adjList.get(new_node).contains(e)
                            && main.motif_labelAdj.get(label).contains(graph.labels.get(e))) {

                        label2adj.get(graph.labels.get(e)).add(new_node);
                        if (!label2adj.get(label).contains(e)) {
                            new_set.add(e);
                            label2adj.get(label).add(e);
                        }
                    }
                }

                candidates_new.clear();
                NOT_new.clear();

                // Compute the new candidates HashSet and the new NOT HashSet after adding the current node.
                GetCandidates(current_node_set, new_node, label2adj, candidates, motif,
                        graph, NOT, NOT_new, candidates_new);

                if (!candidates_new.isEmpty() || NOT_new.isEmpty()) {
                    DFSMaximal(label2adj, current_node_set, motif, graph, candidates_new, NOT_new,
                            maximal_motif_cliques);
                }

                // Recover label2adj.
                if (label2adj.get(label).contains(new_node)) {
                    label2adj.get(label).remove(new_node);
                }
                for (Integer e : label2adj.get(label)) {
                    label2adj.get(graph.labels.get(e)).remove(new_node);
                }
                for (Integer e : new_set) {
                    label2adj.get(label).remove(e);
                }
            }
            current_node_set.remove(new_node);

            NOT.add(new_node);
        }
    }

    public void DFSMaximal_Special(HashMap<Integer, HashSet<Integer>> label2adj,
            HashSet<Integer> current_node_set, Graph motif, Graph graph, HashSet<Integer> candidates,
            HashSet<Integer> NOT, Vector<HashSet<Integer>> maximal_motif_cliques) throws InterruptedException {
        if (main.MMCLimit != -1 && maximal_motif_cliques.size() > main.MMCLimit) {
            return;
        }

        // TODO: check it when adding a node to current_node_set can save time

        for (int mustNotContainNode: Utilities.mustNotContain) {
            if (current_node_set.contains(mustNotContainNode)) {
                return;
            }
        }

    	if (Utilities.checkIfUpperBound(graph, current_node_set) == true) {
    		return;
    	}

        if (candidates.isEmpty() && NOT.isEmpty()) {
            if (Utilities.checkIfLowerBound(graph, current_node_set) == true) {
                return;
            }

            main.maxCliqueSize = Math.max(main.maxCliqueSize, (int) current_node_set.size());
            HashSet<Integer> temp = new HashSet<Integer>();
            for (Integer e : current_node_set) {
                temp.add(e);
            }
            maximal_motif_cliques.add(temp);
            Utilities.combine(graph, temp);
            Utilities.hasNewData = true;
            if (Thread.interrupted()) {
            	logger.info("worker thread receives stop signal");
            	throw new InterruptedException();
            }
			Thread.yield();
            logger.info("A new mmc found, " + maximal_motif_cliques.size() + " found in total");

            return;
        }
        if (candidates.isEmpty()) {
            return;
        }

        HashSet<Integer> NOT_new = new HashSet<Integer>();
        HashSet<Integer> candidates_new = new HashSet<Integer>();

        while (!candidates.isEmpty()) {
            if (main.MMCLimit != -1 && maximal_motif_cliques.size() >= main.MMCLimit) {
                return;
            }
            int new_node = Pick_Special(current_node_set, motif, graph, candidates, NOT); // Pick_Special

            // new_node equals -1 means all nodes are "containable" with at least one node in NOT (i.e., the first element of NOT)
            if (new_node == -1) {
                return;
            }

            candidates.remove(new_node);
            current_node_set.add(new_node);

            if (!FindEndPointInSetTrieNode(current_node_set)) {
                Utilities.motifCount++;

                // Update label2adj.
                HashSet<Integer> new_set = new HashSet<Integer>();
                int label = graph.labels.get(new_node);
                for (Integer e : current_node_set) {
                    if (graph.adjList.get(new_node).contains(e)
                            && main.motif_labelAdj.get(label).contains(graph.labels.get(e))) {

                        label2adj.get(graph.labels.get(e)).add(new_node);
                        if (!label2adj.get(label).contains(e)) {
                            new_set.add(e);
                            label2adj.get(label).add(e);
                        }
                    }
                }

                candidates_new.clear();
                NOT_new.clear();

                // Compute the new candidates HashSet and the new NOT HashSet after adding the current node.
                GetCandidates_Special(current_node_set, new_node, label2adj, candidates, motif,
                        graph, NOT, NOT_new, candidates_new);

                if (!candidates_new.isEmpty() || NOT_new.isEmpty()) {
                    DFSMaximal_Special(label2adj, current_node_set, motif, graph, candidates_new, NOT_new,
                            maximal_motif_cliques);
                }

                // Recover label2adj.
                if (label2adj.get(label).contains(new_node)) {
                    label2adj.get(label).remove(new_node);
                }
                for (Integer e : label2adj.get(label)) {
                    label2adj.get(graph.labels.get(e)).remove(new_node);
                }
                for (Integer e : new_set) {
                    label2adj.get(label).remove(e);
                }
            }
            current_node_set.remove(new_node);

            NOT.add(new_node);
        }
    }

    //	public void WriteSubgraph(Vector<Vector<Integer> > subgraphs, String output_path) {
    //	    ofstream fout(output_path);
    //	    fout << subgraphs.size() << endl;
    //	    for (int i = 0; i < subgraphs.size(); ++i) {
    //	        fout << "Mapping " << i + 1 << " =>";
    //	        for (int j = 0; j < subgraphs[i].size(); j++) {
    //	            fout << " " << j << ":" << subgraphs[i][j];
    //	        }
    //	        fout << endl;
    //	    }
    //	    fout.close();
    //	}
//    public void CheckSubgraphMatchingAlgorithm(Graph graph, Graph motif, Vector<Vector<Integer>> subgraphs) {
//        int count = 0;
//        for (Vector<Integer> e : subgraphs) {
//            if (main.tempGraph == null) {
//                main.tempGraph = new Graph();
//            } else {
//                main.tempGraph.clear();
//            }
//            HashSet<Integer> nodes = new HashSet<Integer>();
//            for (int i = 0, size = e.size(); i < size; i++) {
//                nodes.add(e.get(i));
//            }
//            copyGraph(main.tempGraph, graph, nodes);
//            main.tempMatchedSubgraphs.clear();
//            main.sgm.GetMatchedSubgraph(motif, main.tempGraph, main.tempMatchedSubgraphs, true);
//            if (!main.tempMatchedSubgraphs.isEmpty()) {
//                count++;
//            }
//        }
//        logger.info("Exact subgraph match: " + count);
//    }
    // Checks whether two sets A and B are the same.
    public boolean Check(HashSet<Integer> A, HashSet<Integer> B) {
        if (A.size() != B.size()) {
            return false;
        }
        for (Integer e : A) {
            if (!B.contains(e)) // cannot find *it in B
            {
                return false;
            }
        }
        return true;
    }

    // Check whether A is a subset of B
    public boolean CheckSubset(Vector<Integer> A, HashSet<Integer> B) {
        if (A.size() > B.size()) {
            return false;
        }
        for (Integer e : A) {
            if (!B.contains(e)) // cannot find e in B
            {
                return false;
            }
        }
        return true;
    }

    // TODO: maximal_motif_clique is a wrong variable name! It is traditional clique, not motif clique!
    public void BK(Graph graph, HashSet<Integer> current_node_set, HashSet<Integer> candidates, HashSet<Integer> NOT, Vector<HashSet<Integer>> maximal_motif_cliques) {
        // logger.debug("current node set size = " + current_node_set.size() + " candidates size = " + candidates.size()
        //       + " NOT set size = " + NOT.size() + " # of maximal cliques found = " + maximal_motif_cliques.size());
        if (main.MMCLimit != -1 && maximal_motif_cliques.size() >= main.MMCLimit) {
                return;
            }

        if (candidates.isEmpty() && NOT.isEmpty()) {
            if (current_node_set.size() > 1) {
                HashSet<Integer> temp = new HashSet<Integer>();
                for (Integer e : current_node_set) {
                    temp.add(e);
                }
                logger.info("One traditional clique found! cliques size = " + maximal_motif_cliques.size());
                maximal_motif_cliques.add(temp);
            }
            return;
        }

        HashSet<Integer> NOT_new = new HashSet<Integer>();
        HashSet<Integer> candidates_new = new HashSet<Integer>();
        boolean flag;
        while (!candidates.isEmpty()) {
            if (main.MMCLimit != -1 && maximal_motif_cliques.size() >= main.MMCLimit) {
                return;
            }
            
            flag = false;
            for (Integer e : NOT) {
                flag = true;
                for (Integer w : candidates) {
                    if (!graph.adjList.get(e).contains(w)) {
                        flag = false;
                        break;
                    }
                }
                if (flag) {
                    break;
                }
            }
            if (flag) {
                return;
            }
            int new_node = GetRandomElement(candidates);
            candidates.remove(new_node);
            current_node_set.add(new_node);

            candidates_new.clear();
            NOT_new.clear();

            // logger.debug("adjacent list for new node " + new_node + " size = " + graph.adjList.get(new_node).size());
            for (Integer e : graph.adjList.get(new_node)) {
                if (candidates.contains(e)) {
                    candidates_new.add(e);
                }
            }

            for (Integer e : graph.adjList.get(new_node)) {
                if (NOT.contains(e)) {
                    NOT_new.add(e);
                }
            }

            if (!candidates_new.isEmpty() || NOT_new.isEmpty()) {
                BK(graph, current_node_set, candidates_new, NOT_new, maximal_motif_cliques);
            }
            current_node_set.remove(new_node);
            NOT.add(new_node);
        }
    }
}

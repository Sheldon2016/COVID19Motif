package mmc;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;

//import main.Utilities;

/**
 * Determines initial candidates of query vertices: for each node u \in Motif,
 * find its mapping HashSet {u'} \in G criteria: label(u) = label(u'), degree(u)
 * <= degree(u')
 */
public class SubGraphMatch {

	MainUtilities Utilities = new MainUtilities();
	
    private MainMMC main = null;

    public int invalid = 0;

    SubGraphMatch() {
    }

    SubGraphMatch(MainMMC main) {
        this.main = main;
    }

    /**
     * determines initial candidates of query vertices: criteria --> label(u) =
     * label(u'), degree(u) <= degree(u')
     */
    public void initialize(Vector<HashSet<Integer>> candidates, Graph motif, Graph graph) {

        // store out_degree of query node and data node
        int deg_u, deg_v;

        // go through each motif node
        for (int u = 0; u < motif.nodeNum; u++) {
            deg_u = motif.adjList.get(u).size();

            // go through each graph node
            for (int v = 0; v < graph.nodeNum; v++) {
                // get out_degree of graph node
                deg_v = graph.adjList.get(v).size();

	            // determine if the current graph node (v) is a candidate 
                // of the current motif node (u)
                if (motif.labels.get(u) == graph.labels.get(v) && deg_u <= deg_v) {
                    candidates.get(u).add(v);
                }
            }
        }

    }

    /**
     * finds all candidate edges for given motif edge (u->v) returns a candidate
     * HashSet of edges.
     */
    public void FindMatchedEdges(int u, int v, Vector<HashSet<Integer>> c_set, Graph graph, HashSet<Edge> ret) {

        // get candidate HashSet of given lead vertex (u)
        HashSet<Integer> leadCand = c_set.get(u);

        // get candidate HashSet of follow vertex (v)
        HashSet<Integer> followCand = c_set.get(v);

        // go through candidate HashSet of lead vertex
        for (Integer lead_nid : leadCand) {
	        // go through candidate HashSet of follow vertex. If the number of neighbors
            // of the node (*leadIter) is larger than the size of followCand, we 
            // check the elements in followCand. Otherwise, we check its neighbors.
            if (graph.adjList.get(lead_nid).size() > followCand.size()) {
                for (Integer follow_nid : followCand) {
                    //double check that vertices are next to one another
                    if (graph.adjList.get(lead_nid).contains(follow_nid)) {
                        ret.add(new Edge(lead_nid, follow_nid));
                    }
                }
            } else {
                for (Integer e : graph.adjList.get(lead_nid)) {
                    //double check that vertices are next to one another
                    if (followCand.contains(e)) {
                        ret.add(new Edge(lead_nid, e));
                    }
                }
            }
        }
    }

    /**
     * determines the visit order for finding partial solutions returns the
     * order as a integer vector with each value = index that should be visited,
     * i.e. at index 0 the value is 15 so the first index visited is 15
     */
    public void Setup(Vector<HashSet<Edge>> c_edges, Graph motif, Vector<Integer> ret) {
        //edges visited
        boolean[] edgeVisit = new boolean[motif.edgeNum];
        Arrays.fill(edgeVisit, false);

        // query nodes visited
        boolean[] nodeVisit = new boolean[motif.nodeNum];
        Arrays.fill(nodeVisit, false);

        // stores minimum index
        int _min = 0, index = 0;

        // first find query edge with smallest number of candidates
        for (index = 1; index < motif.edgeNum; ++index) {
            if (c_edges.get(index).size() <= c_edges.get(_min).size()) {
                _min = index;
            }
        }

        // mark edge as visited
        edgeVisit[_min] = true;

        // mark lead vertex node as visited
        nodeVisit[motif.edges.get(_min).from] = true;

        // mark follow vertex node as visited
        nodeVisit[motif.edges.get(_min).to] = true;

        // add first index to return Vector
        ret.add(_min);

        // continue selecting vertices as long as all edges have not been visited
        while (ret.size() != motif.edgeNum) {
	        //node A tracks edge with 2 visited nodes with smallest number of candidates
            //node B tracks edge with 1 visited node with smallest number of candidates
            //node C tracks edge with 0 visited nodes with smallest number of candidates --> used for multiple (disconnected) solutions
            int nodeA = -1, nodeB = -1, nodeC = -1;
            for (index = 0; index < motif.edgeNum; ++index) {
                if (!edgeVisit[index]) {
                    //vertices of current edge
                    int lead = motif.edges.get(index).from;
                    int follow = motif.edges.get(index).to;

                    if (nodeVisit[lead] && nodeVisit[follow]) {
                        if (nodeA == -1 || c_edges.get(index).size() <= c_edges.get(nodeA).size()) {
                            nodeA = index;
                        }
                    } else if (nodeVisit[lead] || nodeVisit[follow]) {
                        if (nodeB == -1 || c_edges.get(index).size() <= c_edges.get(nodeB).size()) {
                            nodeB = index;
                        }
                    } else {
                        if (nodeC == -1 || c_edges.get(index).size() <= c_edges.get(nodeC).size()) {
                            nodeC = index;
                        }
                    }
                }
            }

            int current = -1;
            if (nodeA != -1) {
                current = nodeA;
            } else if (nodeB != -1) {
                current = nodeB;
            } else if (nodeC != -1) {
                current = nodeC;
            }

            //mark edge as visited
            edgeVisit[current] = true;

            //mark lead vertex as visited
            nodeVisit[motif.edges.get(current).from] = true;

            //mark follow vertex node as visited
            nodeVisit[motif.edges.get(current).to] = true;

            //add index to return Vector
            ret.add(current);
        }
    }

	// goes through all possible solutions and returns the matches
    public void Join(Vector<Integer> order, Graph motif, Graph graph,
            Vector<HashSet<Edge>> c_edges, int[] motif_assign, int[] graph_assign,
            int current, int[] levelSet, Vector<Vector<Integer>> matched_subgraphs,
            boolean Isomorphism_Check) throws InterruptedException {
        //if dfs has reached last level
        if (current == motif.edgeNum) {
            return;
        }
        if (Isomorphism_Check && !matched_subgraphs.isEmpty()) {
            return;
        }
        if (main.subgraphLimit != -1 && matched_subgraphs.size() >= main.subgraphLimit) {
            return;
        }

        int lead, follow;
        int leadCand, followCand;
        //gets the lead node of edge order[current]
        lead = motif.edges.get(order.get(current)).from;

        //gets the follow node of edge order[current]
        follow = motif.edges.get(order.get(current)).to;

        //for the current level (query node), given by order[current], go through all candidate edges 
        if (motif_assign[lead] != -1 && motif_assign[follow] != -1) {
            Edge temp = new Edge(motif_assign[lead], motif_assign[follow]);
            if (c_edges.get(order.get(current)).contains(temp)) {
                if (current + 1 >= motif.edgeNum) {
                    Vector<Integer> toBeAdded = new Vector<Integer>();
                    for (int i = 0; i < motif.nodeNum; i++) {
                        toBeAdded.add(motif_assign[i]);
                    }
                    // from C++ matched_subgraphs.push_back(Vector<Integer>(motif_assign, motif_assign + motif.nodeNum));
                    matched_subgraphs.add(toBeAdded);
                    if (Isomorphism_Check) {
                        return;
                    }
                    GetMMC(motif, graph, matched_subgraphs.get(matched_subgraphs.size() - 1));
                    if (main.subgraphLimit != -1 && matched_subgraphs.size() >= main.subgraphLimit) {
                        return;
                    }
                } else { // call join function on next level
                    Join(order, motif, graph, c_edges, motif_assign, graph_assign, current + 1, levelSet, matched_subgraphs,
                            Isomorphism_Check);
                }

                if (levelSet[lead] == current) {
                    graph_assign[motif_assign[lead]] = -1;
                    motif_assign[lead] = -1;
                    levelSet[lead] = -1;
                }

                if (levelSet[follow] == current) {
                    graph_assign[motif_assign[follow]] = -1;
                    motif_assign[follow] = -1;
                    levelSet[follow] = -1;
                }
            }
        } else {
            for (Edge e : c_edges.get(order.get(current))) {
                if (Isomorphism_Check && !matched_subgraphs.isEmpty()) {
                    return;
                }
                invalid = 0;
                leadCand = e.from;
                followCand = e.to;

                // first check data node lead assignment
                if (graph_assign[leadCand] != -1 && graph_assign[leadCand] != lead) {
                    invalid = 1;
                } else if (graph_assign[followCand] != -1
                        && graph_assign[followCand] != follow) { //data node follow assignment
                    invalid = 1;
                } else if (motif_assign[lead] != -1 && motif_assign[lead] != leadCand) { //query node lead
                    invalid = 1;
                } else if (motif_assign[follow] != -1 && motif_assign[follow] != followCand) { //query node follow
                    invalid = 1;
                }

                //if still a valid solution path
                if (invalid == 0) {
	                //if query nodes are unassigned
                    //HashSet level where assigned
                    if (motif_assign[lead] == -1) {
                        graph_assign[leadCand] = lead; //HashSet new data assignments
                        motif_assign[lead] = leadCand; //HashSet new query assignments
                        levelSet[lead] = current;
                    }
                    if (motif_assign[follow] == -1) {
                        graph_assign[followCand] = follow;
                        motif_assign[follow] = followCand;
                        levelSet[follow] = current;
                    }
                    //if the current level is the last level then the assignments represent a solution
                    if (current + 1 >= motif.edgeNum) {
                        Vector<Integer> toBeAdded = new Vector<Integer>();
                        for (int i = 0; i < motif.nodeNum; i++) {
                            toBeAdded.add(motif_assign[i]);
                        }
                        // from C++ matched_subgraphs.push_back(Vector<Integer>(motif_assign, motif_assign + motif.nodeNum));
                        matched_subgraphs.add(toBeAdded);
                        if (Isomorphism_Check) {
                            return;
                        }
                        GetMMC(motif, graph, matched_subgraphs.get(matched_subgraphs.size() - 1));
                        if (matched_subgraphs.size() >= main.subgraphLimit) {
                            return;
                        }
                    } else {//call join function on next level
                        Join(order, motif, graph, c_edges, motif_assign, graph_assign,
                                current + 1, levelSet, matched_subgraphs, Isomorphism_Check);
                    }
                    if (levelSet[lead] == current) {
                        graph_assign[motif_assign[lead]] = -1;
                        motif_assign[lead] = -1;
                        levelSet[lead] = -1;
                    }
                    if (levelSet[follow] == current) {
                        graph_assign[motif_assign[follow]] = -1;
                        motif_assign[follow] = -1;
                        levelSet[follow] = -1;
                    }
                }//if match was invalid then do nothing
            }
        }
    }

    /* 
     * black box for finding all matched subgraphs
     */
    public void GetMatchedSubgraph(Graph motif, Graph graph,
            Vector<Vector<Integer>> matched_subgraphs, boolean Isomorphism_Check) throws InterruptedException {

        //stores candidate information for each query node
        Vector<HashSet<Integer>> c_set = new Vector<HashSet<Integer>>();

        for (int i = 0; i < motif.nodeNum; i++) {
            c_set.add(new HashSet<Integer>());
        }
        //sets original candidates of each query node based on: label(u) = label(u') && deg(u) <= deg(u')
        initialize(c_set, motif, graph);

        //create array to store candidate edges
        Vector<HashSet<Edge>> c_edges = new Vector<HashSet<Edge>>();
        for (int i = 0; i < motif.edgeNum; i++) {
            c_edges.add(new HashSet<Edge>());
        }
        //go through all query edges
        for (int i = 0; i < motif.edgeNum; ++i) {
            //get candidate edges
            FindMatchedEdges(motif.edges.get(i).from, motif.edges.get(i).to, c_set, graph, c_edges.get(i));
        }

        //Determine the order in which edges should be visited
        Vector<Integer> order = new Vector<Integer>();

        Setup(c_edges, motif, order);

        int[] motif_values = new int[motif.nodeNum];
        int[] l_set = new int[motif.nodeNum];
        int[] graph_values = new int[graph.nodeNum];
        Arrays.fill(motif_values, -1);
        Arrays.fill(l_set, -1);
        Arrays.fill(graph_values, -1);

        Join(order, motif, graph, c_edges, motif_values, graph_values, 0, l_set,
                matched_subgraphs, Isomorphism_Check);

    }

    /*
     * black box for finding all matched subgraphs with "must contain nodes"
     */
    public void GetMatchedSubgraph(Graph motif, Graph graph, Vector<Vector<Integer>> matched_subgraphs, int must_contain_node) throws InstantiationException, IllegalAccessException, InterruptedException {
//	    long ticks = 0;
//	    ticks = Date.class.newInstance().getTime();

        //stores candidate information for each query node
        Vector<HashSet<Integer>> c_set = new Vector<HashSet<Integer>>();
        for (int i = 0; i < motif.nodeNum; i++) {
            c_set.add(new HashSet<Integer>());
        }
        //sets original candidates of each query node based on: label(u) = label(u') && deg(u) <= deg(u')
        initialize(c_set, motif, graph);

        //create array to store candidate edges
        Vector<HashSet<Edge>> c_edges = new Vector<HashSet<Edge>>(motif.edgeNum);
        for (int i = 0; i < motif.edgeNum; i++) {
            c_edges.add(new HashSet<Edge>());
        }
        //go through all query edges
        for (int i = 0; i < motif.edgeNum; ++i) {
            //get candidate edges
            FindMatchedEdges(motif.edges.get(i).from, motif.edges.get(i).to, c_set, graph, c_edges.get(i));
        }

        //Determine the order in which edges should be visited
        Vector<Integer> order = new Vector<Integer>();

        Setup(c_edges, motif, order);

        int[] motif_values = new int[motif.nodeNum];
        int[] l_set = new int[motif.nodeNum];
        int[] graph_values = new int[graph.nodeNum];
        Arrays.fill(motif_values, -1);
        Arrays.fill(l_set, -1);
        Arrays.fill(graph_values, -1);

        for (Integer i : graph.adjList.get(must_contain_node)) {
            Edge edge = new Edge(must_contain_node, i);
            //edge is directed.
            for (int j = 0; j < motif.edges.size(); j++) {
                if (motif.labels.get(motif.edges.get(j).from) == graph.labels.get(edge.from)
                        && motif.labels.get(motif.edges.get(j).to) == graph.labels.get(edge.to)) {

                    motif_values[motif.edges.get(j).from] = must_contain_node;
                    graph_values[must_contain_node] = motif.edges.get(j).from;
                    motif_values[motif.edges.get(j).to] = i;
                    graph_values[i] = motif.edges.get(j).to;

                    Vector<Integer> new_order = new Vector<Integer>();
                    for (int k = 0, size = order.size(); k < size; k++) {
                        new_order.add(k);
                    }
                    int index = new_order.indexOf(j);
                    if (index != -1) {
                        int tp = new_order.get(0);
                        new_order.setElementAt(0, j);
                        new_order.setElementAt(index, tp);
                    }

                    l_set[motif.edges.get(j).to] = 0;
                    l_set[motif.edges.get(j).from] = 0;
                    Join(new_order, motif, graph, c_edges, motif_values, graph_values, 1, l_set, matched_subgraphs, false);

                    motif_values[motif.edges.get(j).from] = -1;
                    graph_values[must_contain_node] = -1;
                    motif_values[motif.edges.get(j).to] = -1;
                    graph_values[i] = -1;
                    l_set[motif.edges.get(j).to] = -1;
                    l_set[motif.edges.get(j).from] = -1;
                }
            }
        }

	   // double timeAll = ((double) (clock() - ticks) / CLOCKS_PER_SEC);
//	    cout << matched_subgraphs.size() << " subgraphs have been found using the basic method in " << timeAll << "seconds"
//	         << endl;
    }

    public void GetMMC(Graph motif, Graph graph, Vector<Integer> subgraph) throws InterruptedException {

        HashSet<Integer> current_node_set = new HashSet<Integer>();
        HashSet<Integer> candidates = new HashSet<Integer>();
        HashMap<Integer, HashSet<Integer>> label2adj = new HashMap<Integer, HashSet<Integer>>();
        if (main.mmc.SubgraphInSetTrieNode(subgraph)) {
            return;
        }
        main.mmc.FindMaximalMotifClique(label2adj, subgraph, motif, graph, current_node_set, candidates,
                Utilities.cliques);
        HashSet<Integer> clone = new HashSet<Integer>();
        for (int i = 0, size = subgraph.size(); i < size; i++) {
            clone.add(subgraph.get(i));
        }
        main.root.addSet(clone, main.globalHashId);
    }
}

//
// Created by bxli on 2019-09-21.
//
#include <sstream>
#include <unordered_set>
#include <algorithm>
#include <sys/time.h>
#include "dynamic-algo.h"

class MyHash {
public:
	std::size_t operator()(const std::unordered_set<int> &s) const {
		vector<int> v;
		v.insert(v.end(), s.begin(), s.end());
		// sort and convert to string
		sort(v.begin(), v.end());
		hash<int> hasher;
		size_t result = 0;
		for (size_t i = 0; i < v.size(); ++i) {
			result = result * 31 + hasher(v[i]);
		}
		return result;
	}
};


unordered_set<unordered_set<int>, MyHash> cartesian_product(vector<vector<int> > cands);

template<typename S>
int select(const S &s, size_t n) {
	auto it = std::begin(s);
	// 'advance' the iterator n times
	std::advance(it, n);
	return *it;
}

template<typename T>
bool is_subset_of(const std::unordered_set<T> &a, const std::unordered_set<T> &b) {
	// return true if all members of a are also in b
	auto const is_in_b = [&b](T const &x) { return b.find(x) != b.end(); };

	return a.size() <= b.size() && std::all_of(a.begin(), a.end(), is_in_b);
}

/**
 * Generate a temporary graph and an embedding, with all node ids transformed
 * @param input: graph
 * @param input: original_mclique
 * @param output: temporary_graph
 * @param output: subgraph (embedding)
 */
void generate_temporary_graph(const Graph &graph, const vector<int> &original_mclique,
							  Graph &temporary_graph, vector<int> &subgraph) {
	temporary_graph.nodeNum = 0;
	temporary_graph.edgeNum = 0;
	for (int i = 0; i < original_mclique.size(); i++) {
		int node_i = original_mclique[i];
		// check if this node is in initial embedding
		if (initial_embedding.find(node_i) != initial_embedding.end()) {
			subgraph.push_back(i);
		}
		temporary_graph.labels.push_back(graph.labels[node_i]);
		set<int> adjList;
		for (int j = 0; j < original_mclique.size(); j++) {
			int node_j = original_mclique[j];
			if (graph.adjList[node_i].find(node_j) != graph.adjList[node_i].end()) {
				adjList.insert(j);
				temporary_graph.edgeNum++;
			}
		}
		temporary_graph.adjList.push_back(adjList);
		temporary_graph.nodeNum++;
	}
	temporary_graph.edgeNum /= 2;
}


void removeNodeFromMClique(Graph &graph, const Graph &motif,
						   set<int> &mclique) {
	assert(initial_embedding.size() != 0);
	// generate a random node to be removed
	// for the purpose of comparison between our algorithm and naive approach,
	// pick up a node that is not in the initial embedding
	// because the naive approach has to rely on a starting point
	if (mclique.size() == motif.nodeNum) {
		cout << "mclique size = motif node num = " << motif.nodeNum << " , skip" << endl;
		return;
	}
	set<int> tmp;
	for (auto i: mclique) {
		if (initial_embedding.find(i) == initial_embedding.end()) {
			tmp.insert(i);
		}
	}
	int r = rand() % tmp.size();
	int n = select(tmp, r);
	vector<int> original_mclique;
	for (auto i: mclique) original_mclique.push_back(i);

	dynamic_graph_mode = true;
	// remove node n from graph
	set<int> adjList = graph.adjList[n];
	for (auto &l: graph.adjList) {
		l.erase(n);
		graph.edgeNum--;
	}
	graph.adjList[n].clear();

	/************ Our Algorithm **************************/
	struct timeval start, end;
	long long mtime, seconds, useconds;
	gettimeofday(&start, NULL);

	// get label l of node n
	const int l = graph.labels[n];

	int count_g = 0, count_m = 0;

	// get number of nodes with label l in mclique
	// TODO: maybe we can make mclique a structure that also keeps their labels
	for (auto i: mclique) {
		if (graph.labels[i] == l) {
			count_g++;
		}
	}

	// get number of nodes with label l in motif
	for (auto i: motif.labels) {
		if (i == l) {
			count_m++;
		}
	}

#ifdef DEBUG
	cout << "## Remove node from mclique: ##" << endl;
	cout << "label of node " << n << " is: " << l;
	cout << " count_g = " << count_g << " count_m = " << count_m << endl;
	cout << "before removal, mclique size = " << mclique.size() << endl;
#endif

	if (count_g > count_m) {
		mclique.erase(n);
	} else {
		mclique.clear();
	}


	vector<set<int>> mcliques;
	mcliques.push_back(mclique);

	gettimeofday(&end, NULL);
	seconds = end.tv_sec - start.tv_sec;
	useconds = end.tv_usec - start.tv_usec;
	mtime = seconds * 1000000 + useconds;
	dynamic_graph_algo_time_rn += mtime / 1000.0;

	cout << "after removal, mclique size = " << mclique.size() << endl;
	ostringstream stringStream;
	stringStream << "PRINT remove node from mclique result with removed node ";
	stringStream << n;
	stringStream << endl;

	PrintResults(mcliques, OUTPUT_PATH + "-node-removal.txt", graph, NAME2ID_PATH, stringStream.str(), true);
	/************ Our Algorithm **************************/


	/********* NAIVE APPROACH **************/

	vector<set<int>> maximal_motif_cliques;
	vector<int> subgraph;
	Graph temporary_graph;
	generate_temporary_graph(graph, original_mclique, temporary_graph, subgraph);

	gettimeofday(&start, NULL);
	GetMMC(motif, temporary_graph, subgraph, maximal_motif_cliques);

	gettimeofday(&end, NULL);
	seconds = end.tv_sec - start.tv_sec;
	useconds = end.tv_usec - start.tv_usec;
	mtime = seconds * 1000000 + useconds;
	dynamic_graph_naive_time_rn += mtime / 1000.0;

	assert(maximal_motif_cliques.size() == 1);
	// note that the ids are already changed, so we cannot compare them directly
	assert(maximal_motif_cliques[0].size() == mclique.size());
	/********* NAIVE APPROACH **************/

	// recover graph
	for (auto i: adjList) {
		graph.adjList[i].insert(n);
		graph.edgeNum++;
	}
	graph.adjList[n] = adjList;
	mclique.clear();
	for (auto i: original_mclique) mclique.insert(i);
	dynamic_graph_mode = false;
	dynamic_graph_counter_rn++;
	cout << "RemoveNode [total time] our algo time: " << dynamic_graph_algo_time_rn << ", naive approach time: "
		 << dynamic_graph_naive_time_rn << endl;
	cout << "RemoveNode [avg time] our algo time: " << dynamic_graph_algo_time_rn / dynamic_graph_counter_rn
		 << ", naive approach time: " << dynamic_graph_naive_time_rn / dynamic_graph_counter_rn << endl << endl;
}


// to test if the edge n1-n2 is necessary for the given maximal mclique
bool isEffective(Graph &graph, const Graph &motif, set<int> &mclique,
				 const int n1, const int n2) {
	bool effective = true;
	int l1 = graph.labels[n1];
	int l2 = graph.labels[n2];

	// this might help us speed up the checking process
	for (int i = 0; i < motif.labels.size(); i++) {
		for (int j = 0; j < motif.labels.size(); j++) {
			if (motif.labels[i] == l1 && motif.labels[j] == l2) {
				if (motif.adjList[i].find(j) == motif.adjList[i].end()) {
					effective = false;
					break;
				}
			}
		}
	}

	// if effective is True, we are done. Otherwise we need to do label-matched set checking
	if (!effective) {
		// remove the edge n1-n2 from global graph
		graph.adjList[n1].erase(n2);
		graph.adjList[n2].erase(n1);
		// remove nodes n1, n2 from mclique
		mclique.erase(n1);
		mclique.erase(n2);
		// check if the mclique - edge (n1-n2) is still mclique
		// if it is still mclique, then the edge is not effective
		effective = !IsomorphismCheck(mclique, n1, n2, motif, graph);
		// add n1, n2 back to mclique
		mclique.insert(n1);
		mclique.insert(n2);
		// add edge n1-n2 back to global graph
		graph.adjList[n1].insert(n2);
		graph.adjList[n2].insert(n1);
	}

	return effective;

}

void removeEdgeFromMClique(Graph &graph, const Graph &motif, set<int> &mclique) {

	vector<int> original_mclique;
	for (auto i: mclique) original_mclique.push_back(i);
	/*****************************************************
	 * For testing purpose, pick up the first edge from mclique that is not in the original embedding
	 */

	int n1 = -1, n2 = -1;
	for (auto i: mclique) {
		for (auto j: mclique) {
			if (initial_embedding.find(i) != initial_embedding.end() &&
				initial_embedding.find(j) != initial_embedding.end()) {
				// skip the edge if it's in the initial embedding
				// otherwise it would be difficult for our naive approach to handle
				continue;
			}
			if (graph.adjList[i].find(j) != graph.adjList[i].end()) {
				n1 = i;
				n2 = j;
				break;
			}
		}
	}
	if (n1 == -1 || n2 == -1) {
		cout << "no applicable edge to remove! skip" << endl;
		return;
	}

#ifdef DEBUG
	cout << "#############################################" << endl
		 << "picking up random edge " << n1 << "--" << n2
		 << " from mclique with size = " << mclique.size()
		 << " with motif size = " << motif.nodeNum << endl;
	/*****************************************************/
#endif


	ostringstream stringStream;
	stringStream << "PRINT remove node from mclique result with removed edge ";
	stringStream << n1 << "-" << n2 << endl;

	dynamic_graph_mode = true;

	/************ Our Algorithm **************************/
	struct timeval start, end;
	long long mtime, seconds, useconds;
	gettimeofday(&start, NULL);

	bool effective = isEffective(graph, motif, mclique, n1, n2);

	// remove edge n1-n2 from global graph
	graph.adjList[n1].erase(n2);
	graph.adjList[n2].erase(n1);
	graph.edgeNum--;

	cout << "edge " << n1 << "-" << n2 << " is " << (effective ? "effective" : "not effective") << endl;

	vector<set<int>> mcliques;

	// if the edge is an ineffective (useless) edge, we can simply remove it
	if (!effective) {
		mcliques.push_back(mclique);

	} else {
		// otherwise we need to remove one or more edges to retain the maximal motif-clique property
		// of the residual graph
		vector<vector<int>> cands; // a vector which records which nodes can be removed for each label-matched set

		/**
		 * make use of IsomorphismCheck to get label-matched sets that are not embeddings
		 * we remove n1, n2 from mclique first, and also remove the edge n1-n2 from the global graph
		 * then we do IsomorphismCheck: mclique + n1 + n2, and retrieve all label-matched sets that
		 * are not embeddings
		 */
		vector<set<int>> label_match_not_embedding_sets;

		// remove nodes n1, n2 from mclique
		mclique.erase(n1);
		mclique.erase(n2);

		bool checked = IsomorphismCheck(mclique, n1, n2, motif, graph, label_match_not_embedding_sets);
		assert(checked == false); // The check shall not pass as the edge n1-n2 is effective

#ifdef DEBUG
		cout << "@@@ label-matched, but not embedding sets size = "
			 << label_match_not_embedding_sets.size() << " @@@" << endl;
		for (int k = 0; k < label_match_not_embedding_sets.size(); k++) {
			cout << "The " << k << "th group:" << endl;
			set<int> &s = label_match_not_embedding_sets[k];
			for (auto iter = s.begin(); iter != s.end(); ++iter) {
				cout << "	" << *iter;
			}
			cout << endl;
		}
#endif

		// add n1, n2 back to mclique
		mclique.insert(n1);
		mclique.insert(n2);

		bool isValid = true;
		for (auto H: label_match_not_embedding_sets) {
			vector<int> cand;
			for (auto h: H) {
				int count_g = 0, count_m = 0;
				for (auto k: mclique) {
					if (graph.labels[k] == graph.labels[h]) {
						count_g++;
					}
				}
				for (auto k: motif.labels) {
					if (k == graph.labels[h]) {
						count_m++;
					}
				}
				if (count_g > count_m) {
					cand.push_back(h);
				}
			}
			if (cand.empty()) {
				cout << "The residual graph cannot be a mclique!" << endl;
				stringStream << "The residual graph cannot be a mclique!" << endl;
				isValid = false;
				break;
			}
			cands.push_back(cand);
		}
		if (isValid && cands.size() > 1000) {
			/**
		     * TODO: if label-matched sets size is very big,
		     * cartesian product would be almost impossible (theoretically, it is possible, it's just
		     * we don't implement an efficient version - by using recursion atm, stack can blow up
		     * in that case, maybe we only need a subset of results
		     * for convenience, we simply skip large motif cliques
		     */
			cout << "cartesian product cands size = " << cands.size() << ", too large, skip" << endl;
			// finished, do recovery, add deleted edge back
			graph.adjList[n1].insert(n2);
			graph.adjList[n2].insert(n1);
			graph.edgeNum++;
			dynamic_graph_mode = false;
			return;
		} else if (isValid) {
			// iterate through each unique set C of Cartesian product of cands
			unordered_set<unordered_set<int>, MyHash> cands_cartesian_product = cartesian_product(cands);
#ifdef DEBUG
			cout << "nodes to be removed: " << endl;
			for (auto s: cands_cartesian_product) {
				cout << endl;
				for (auto k: s) {
					cout << k << " ";
				}
			}
			cout << endl;
#endif

			for (auto s: cands_cartesian_product) {
				set<int> tmp(mclique);
				// remove cartesian_product result from mclique
				for (auto ele: s) tmp.erase(ele);
				bool isValid = true;
				// check whether the new mclique candidate is valid
				for (auto label: motif.labels) {
					int count_g = 0, count_m = 0;
					for (auto k: tmp) {
						if (graph.labels[k] == label) {
							count_g++;
						}
					}
					for (auto k: motif.labels) {
						if (k == label) {
							count_m++;
						}
					}
					if (count_g < count_m) {
#ifdef DEBUG
						cout << "count_g = " << count_g << " count_m = " << count_m << endl;
#endif
						isValid = false;
						break;
					}
				}
				if (isValid) {
					mcliques.push_back(tmp);
				} else {
#ifdef DEBUG
					cout << "residual mclique is invalid, dropped" << endl;
#endif
				}
			}
		}

	}
	gettimeofday(&end, NULL);
	seconds = end.tv_sec - start.tv_sec;
	useconds = end.tv_usec - start.tv_usec;
	mtime = seconds * 1000000 + useconds;
	dynamic_graph_algo_time_re += mtime / 1000.0;
	PrintResults(mcliques, OUTPUT_PATH + "-edge-removal.txt", graph, NAME2ID_PATH, stringStream.str(), true);

	/************ Our Algorithm **************************/



	/************ Naive Approach **************************/
	vector<set<int>> maximal_motif_cliques;
	vector<int> subgraph;
	Graph temporary_graph;
	generate_temporary_graph(graph, original_mclique, temporary_graph, subgraph);

	gettimeofday(&start, NULL);

	GetMMC(motif, temporary_graph, subgraph, maximal_motif_cliques);
	// TODO: this cannot be '==' because for naive approach, we start from a particular embedding
	// in our approach, the result might not necessarily contain the initial embedding - it can be some other
	// we can use the GetMatchedSubgraph method instead, which does not require an embedding as input
	assert(mcliques.size() >= maximal_motif_cliques.size());

	gettimeofday(&end, NULL);
	seconds = end.tv_sec - start.tv_sec;
	useconds = end.tv_usec - start.tv_usec;
	mtime = seconds * 1000000 + useconds;
	dynamic_graph_naive_time_re += mtime / 1000.0;
	/************ Naive Approach **************************/

	// finished, do recovery, add deleted edge back
	graph.adjList[n1].insert(n2);
	graph.adjList[n2].insert(n1);
	graph.edgeNum++;

	dynamic_graph_mode = false;
	dynamic_graph_counter_re++;
	cout << "Result: our algo finds " << mcliques.size() << ", naive algo finds " << maximal_motif_cliques.size() << endl;
	cout << "RemoveEdge [total time] our algo time: " << dynamic_graph_algo_time_re << ", naive approach time: "
		 << dynamic_graph_naive_time_re << endl;
	cout << "RemoveEdge [avg time] our algo time: " << dynamic_graph_algo_time_re / dynamic_graph_counter_re
		 << ", naive approach time: " << dynamic_graph_naive_time_re / dynamic_graph_counter_re << endl << endl;
}

/**
 * Generate cartesian products
 * e.g.
 * cands: [[1, 2], [1, 3], [4, 5, 6]]
 * cartesian_product result: [[1, 4], [1, 5], ... [1, 3, 6], ... [2, 3, 6]]
 * Note that in our case, each result MUST not be a superset of another result
 * @param cands
 * @return
 */
unordered_set<unordered_set<int>, MyHash> cartesian_product(vector<vector<int> > cands) {
	unordered_set<unordered_set<int>, MyHash> result;
	unordered_set<unordered_set<int>, MyHash> result_unfiltered;
	result.clear();
	if (cands.size() == 0) {
		unordered_set<int> tmp;
		result.insert(tmp);
		return result;
	}
	vector<int> last_cand = cands[cands.size() - 1];
	cands.pop_back();
	unordered_set<unordered_set<int>, MyHash> partial_product = cartesian_product(cands);
	for (auto p: partial_product) {
		bool skip = false;
#ifdef DEBUG
		cout << "partial product result: ";
		for (auto c: p) {
			cout << c << " ";
		}
		cout << endl;
		// for example, if last_cand = [1, 2], p = [1, 3, 4, 5]
		// since p already contains '1', we don't want to add 1 or 2 to it
		// because [1, 3, 4, 5] is a subset of [1, 2, 3, 4, 5]
		cout << "last cand = ";
		for (auto l: last_cand) {
			cout << l << " ";
		}
		cout << endl;
#endif
		for (auto c: last_cand) {
			if (p.find(c) != p.end()) {
				skip = true;
				break;
			}
		}
		if (skip) {
			result.insert(p);
			continue;
		}
		for (auto c: last_cand) {
			unordered_set<int> tmp(p);
			tmp.insert(c);
			result.insert(tmp);
		}
	}
	for (auto it = result.begin(); it != result.end();) {
		bool erased = false;
		for (auto it2 = result.begin(); it2 != result.end(); it2++) {
			if (it != it2 && is_subset_of(*it2, *it)) {
				it = result.erase(it);
				erased = true;
				break;
			}
		}
		if (!erased) it++;
	}
	return result;
}

void addNodeToMClique(Graph &graph, const Graph &motif, set<int> &mclique) {
	/**
	 * Randomly add a new node as well as its connected edges
	 * For testing purpose, let's add a new node that is connected to the current mclique
	 */
	vector<int> original_mclique;
	for (auto i: mclique) original_mclique.push_back(i);
	int new_node = graph.nodeNum;
	graph.nodeNum += 1;
	graph.labels.push_back(motif.labels[0]); // assign the new node a label in motif
	set<int> tmp;
	graph.adjList.push_back(tmp);
	for (int n: mclique) {
		// 50% percentage
		if (rand() % 1 == 0) {
			// no need to add InAdjList & OutAdjList
			graph.adjList[n].insert(new_node);
			graph.adjList[new_node].insert(n);
			graph.edgeNum++;
		}
	}

	dynamic_graph_mode = true;
	/************* Finish Adding a new node and its edges *******************/

	/************ Our Algorithm **************************/
	struct timeval start, end;
	long long mtime, seconds, useconds;
	gettimeofday(&start, NULL);

	bool canAdd = IsomorphismCheck(mclique, new_node, motif, graph);

	ostringstream stringStream;
	stringStream << endl << endl << endl << "==================================================" << endl;

	if (canAdd) {
		cout << "new node " << new_node << " CAN be added to current mclique!" << endl;
		stringStream << "new node " << new_node << " CAN be added to current mclique!" << endl;
		mclique.insert(new_node);
	} else {
		cout << "new node " << new_node << " CANNOT be added to currenct mclique!" << endl;
		stringStream << "new node " << new_node << " CANNOT be added to currenct mclique!" << endl;
	}

	vector<set<int>> mcliques;
	mcliques.push_back(mclique);

	gettimeofday(&end, NULL);
	seconds = end.tv_sec - start.tv_sec;
	useconds = end.tv_usec - start.tv_usec;
	mtime = seconds * 1000000 + useconds;
	dynamic_graph_algo_time_an += mtime / 1000.0;

	/************ Our Algorithm **************************/

	/************ Naive Approach **************************/
	assert(graph.nodeNum == graph.labels.size());
	assert(new_node == graph.adjList.size() - 1);

	gettimeofday(&start, NULL);

	vector<set<int>> maximal_motif_cliques;

	// the main difference between our algorithm and naive algorithm is that
	// our algorithm knows, if a node can be added, then no more nodes would be
	// added as a consequence. The naive approach would continue to try other nodes.
	GetMMC(motif, graph, original_mclique, maximal_motif_cliques);

	gettimeofday(&end, NULL);
	seconds = end.tv_sec - start.tv_sec;
	useconds = end.tv_usec - start.tv_usec;
	mtime = seconds * 1000000 + useconds;
	dynamic_graph_naive_time_an += mtime / 1000.0;


	assert(maximal_motif_cliques.size() == 1);
	assert(maximal_motif_cliques[0] == mclique);
	/************ Naive Approach **************************/

	// print results
	PrintResults(mcliques, OUTPUT_PATH + "-node-add.txt", graph, NAME2ID_PATH, stringStream.str(), true);

	// start recovery
	graph.nodeNum--;
	graph.labels.pop_back();
	for (auto n: graph.adjList[graph.nodeNum]) {
		graph.adjList[n].erase(graph.nodeNum);
		graph.edgeNum--;
	}
	graph.adjList.pop_back();
	mclique.erase(new_node);

	dynamic_graph_mode = false;
	dynamic_graph_counter_an++;
	cout << "AddNode [total time] our algo time: " << dynamic_graph_algo_time_an << ", naive approach time: "
		 << dynamic_graph_naive_time_an << endl;
	cout << "AddNode [avg time] our algo time: " << dynamic_graph_algo_time_an / dynamic_graph_counter_an
		 << ", naive approach time: " << dynamic_graph_naive_time_an / dynamic_graph_counter_an << endl << endl;
}


void addEdgeToMClique(Graph &graph, const Graph &motif, set<int> &mclique) {
	/**
	 * Randomly add a new edge to the mclique
	 * For simplicity, we just add an edge to the first found pair
	 * We also don't consider the case that one endpoint is in mclique, while the other
	 * one is not, because that case is essentially a simplified case of addNodeToMclique
	 */
	int n1 = -1, n2 = -1;
	bool found = false;
	for (auto i: mclique) {
		if (found) break;
		for (auto j: mclique) {
			if (i != j && graph.adjList[i].find(j) == graph.adjList[i].end()) {
				// 25% chance
				if (rand() % 4 == 0) {
					n1 = i;
					n2 = j;
					found = true;
					break;
				}
			}
		}
	}
	if (!found) return;

	ostringstream stringStream;
	stringStream << endl << endl << endl << "==================================================" << endl;

	if (n1 == -1 || n2 == -1) {
		cout << "mclique is complete graph! skip" << endl;
		return;
	}

	dynamic_graph_mode = true;
	// add new edge to graph
	graph.adjList[n1].insert(n2);
	graph.adjList[n2].insert(n1);
	graph.edgeNum++;

	cout << "add new edge " << n1 << " - " << n2 << endl;
	stringStream << "add new edge " << n1 << " - " << n2 << endl;

	/************ Our Algorithm **************************/

	struct timeval start, end;
	long long mtime, seconds, useconds;
	gettimeofday(&start, NULL);

	// after adding the new edge, some new nodes might be eligible to be added to the mclique now
	// get candidates: all nodes that are connected to the mclique
	set<int> candidates;
	set<int> motif_labels;
	for (auto n: initial_embedding) motif_labels.insert(graph.labels[n]);
	for (auto n: initial_embedding) {
		for (auto c: graph.adjList[n]) {
			if (mclique.find(c) == mclique.end()) {
				// IsomorphismCheck will return true if the candidate node is of a label that not in motif!
				// TODO: figure out how normal flow finds candidate nodes
				if (motif_labels.find(graph.labels[c]) != motif_labels.end()
					&& IsomorphismCheck(mclique, c, motif, graph)) {
					candidates.insert(c);
				}
			}
		}
	}

	cout << "number of candidate nodes: " << candidates.size() << endl;
	vector<set<int>> mcliques;

	if (candidates.size() == 0) {
		graph.adjList[n1].erase(n2);
		graph.adjList[n2].erase(n1);
		graph.edgeNum--;
		cout << "no candidate node" << endl;
	} else {
		map<int, set<int>> label2adj;
		// Create Label2Adj structure
		for (auto from: mclique) {
			for (auto to: mclique) {
				if (from < to && graph.adjList[from].find(to) != graph.adjList[from].end() &&
					motif_labelAdj[graph.labels[from]].find(graph.labels[to]) !=
					motif_labelAdj[graph.labels[from]].end()) {
					if (label2adj.find(graph.labels[from]) ==
						label2adj.end()) { // label[from] does not exist in label2adj
						label2adj.insert(make_pair(graph.labels[from], set<int>()));
					}
					label2adj[graph.labels[from]].insert(to);

					if (label2adj.find(graph.labels[to]) == label2adj.end()) { // label[to] does not exist in label2adj
						label2adj.insert(make_pair(graph.labels[to], set<int>()));
					}
					label2adj[graph.labels[to]].insert(from);
				}
			}
		}

		set<int> NOT;
		DFSMaximal(label2adj, mclique, motif, graph, candidates, NOT, mcliques);
	}

	gettimeofday(&end, NULL);
	seconds = end.tv_sec - start.tv_sec;
	useconds = end.tv_usec - start.tv_usec;
	mtime = seconds * 1000000 + useconds;
	dynamic_graph_algo_time_ae += mtime / 1000.0;


	cout << "number of new maximal mcliques: " << mcliques.size() << endl;
	stringStream << "number of new maximal mcliques: " << mcliques.size() << endl;
	PrintResults(mcliques, OUTPUT_PATH + "-edge-add.txt", graph, motif, NAME2ID_PATH, stringStream.str(), true);
	/************ Our Algorithm **************************/


	/************ Naive Approach **************************/

//	gettimeofday(&start, NULL);
//
//
//
//	gettimeofday(&end, NULL);
//	seconds = end.tv_sec - start.tv_sec;
//	useconds = end.tv_usec - start.tv_usec;
//	mtime = seconds * 1000000 + useconds;
//	dynamic_graph_naive_time += mtime / 1000.0;

	/************ Naive Approach **************************/

	// remove new edge from graph
	graph.adjList[n1].erase(n2);
	graph.adjList[n2].erase(n1);
	graph.edgeNum--;
	dynamic_graph_mode = false;
	dynamic_graph_counter_ae++;
	cout << "AddEdge [total time] our algo time: " << dynamic_graph_algo_time_ae << endl;
	cout << "AddEdge [avg time] our algo time: " << dynamic_graph_algo_time_ae / dynamic_graph_counter_ae << endl << endl;
}

void reportDynamicGraphExperiments() {
	cout << "RemoveNode, counter = " << dynamic_graph_counter_rn << endl;
	cout << "new:\t" << dynamic_graph_algo_time_rn / dynamic_graph_counter_rn << endl;
	cout << "naive:\t" << dynamic_graph_naive_time_rn / dynamic_graph_counter_rn << endl;


	cout << "RemoveEdge, counter = " << dynamic_graph_counter_re << endl;
	cout << "new:\t" << dynamic_graph_algo_time_re / dynamic_graph_counter_re << endl;
	cout << "naive:\t" << dynamic_graph_naive_time_re / dynamic_graph_counter_re << endl;


	cout << "AddNode, counter = " << dynamic_graph_counter_an << endl;
	cout << "new:\t" << dynamic_graph_algo_time_an / dynamic_graph_counter_an << endl;
	cout << "naive:\t" << dynamic_graph_naive_time_an / dynamic_graph_counter_an << endl;


	cout << "AddEdge, counter = " << dynamic_graph_counter_ae << endl;
	cout << "new:\t" << dynamic_graph_algo_time_ae / dynamic_graph_counter_ae << endl;
	cout << "naive:\t" << dynamic_graph_naive_time_ae / dynamic_graph_counter_ae << endl;
}

//
// Created by Hujiafeng on 15/10/2017.
//

#ifndef MOTIF_SRC_MAXIMAL_MOTIF_CLIQUE_H
#define MOTIF_SRC_MAXIMAL_MOTIF_CLIQUE_H

#include<iostream>
#include<vector>
#include<set>
#include<map>
#include"Graph.h"
#include"SetTrieNode.h"
#include"global-variable.h"
#include"subgraph_match.h"
#include"dynamic-algo.h"
#include<fstream>

using namespace std;

bool IsomorphismCheck(const set<int> &U, const int u, const Graph &motif, const Graph &graph);

bool IsomorphismCheck(const set<int> &U, const int u, const int v, const Graph &motif, const Graph &graph);

bool IsomorphismCheck(const set<int> &U, const int u, const int v, const Graph &motif, const Graph &graph,
					  vector<set<int>> &label_match_not_embedding_sets);

void GetLabel2NodesMap(const Graph &graph, map<int, set<int> > &label2nodes);

void GetLabelAdj(const Graph &g, map<int, set<int> > &labelAdj);

void GetLabel2NodesMap(const int current_node, const set<int> &U, const vector<int> &labels,
					   map<int, set<int> > &label2nodes, set<int> &label_set);

bool
EarlyStopCheck(const set<int> &current_node_set, const Graph &motif, const Graph &graph, const set<int> &candidates,
			   const set<int> &NOT);

void FindMaximalMotifClique(map<int, set<int> > &label2adj,
							const vector<int> &subgraph, const Graph &motif, Graph &graph,
							set<int> &current_node_set,
							set<int> &candidates, vector<set<int>> &maximal_motif_cliques);

void GetCandidates(const map<int, set<int> > &label2adj, const set<int> &current_node_set, const Graph &motif,
				   const Graph &graph, set<int> &candidates);

void GetCandidates(const set<int> &current_node_set, const int new_node, const map<int, set<int> > &label2adj,
				   const set<int> &candidates, const Graph &mitif, const Graph &graph, const set<int> &NOT,
				   set<int> &NOT_new,
				   set<int> &candidates_new);

void DFSMaximal(map<int, set<int> > &label2adj, set<int> &current_node_set, const Graph &motif,
				Graph &graph, set<int> &candidates, set<int> &NOT,
				vector<set<int> > &maximal_motif_cliques);

void DFSMaximal_Approx(map<int, set<int> > &label2adj, set<int> &current_node_set, const Graph &motif,
					   const Graph &graph, set<int> &candidates, set<int> &NOT, int step,
					   vector<set<int> > &maximal_motif_cliques);


bool SubgraphInSetTrieNode(vector<int> subgraph);

void InsertNode(map<int, set<int> > &cands, const int v, const int label);

template<typename T>
void Print(const T &A);

void Print(const vector<vector<int> > &subgraphs);

void PrintMap(const map<int, set<int> > &mp);

bool Check(set<int> &A, set<int> &B);

bool CheckSubset(const vector<int> &A, const set<int> &B);

#endif //MOTIF_SRC_MAXIMAL_MOTIF_CLIQUE_H

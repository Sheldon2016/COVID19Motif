//
// Created by bxli on 2019-09-21.
//

#ifndef MOTIF_SRC_DYNAMIC_ALGO_H
#define MOTIF_SRC_DYNAMIC_ALGO_H

#include<iostream>
#include<vector>
#include<set>
#include<map>
#include"Graph.h"
#include"SetTrieNode.h"
#include"global-variable.h"
#include"subgraph_match.h"
#include"maximal-motif-clique.h"
#include"utils.h"
#include<fstream>

using namespace std;

void removeNodeFromMClique(Graph &graph, const Graph &motif,
						   set<int> &mclique);

void removeEdgeFromMClique(Graph &graph, const Graph &motif, set<int> &mclique);

void addNodeToMClique(Graph &graph, const Graph &motif, set<int>&mclique);

void addEdgeToMClique(Graph &graph, const Graph &motif, set<int>&mclique);

void reportDynamicGraphExperiments();

#endif

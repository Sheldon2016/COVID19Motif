//
// Created by Hujiafeng on 15/10/2017.
//

#ifndef MOTIF_SRC_GLOBAL_VARIABLE_H
#define MOTIF_SRC_GLOBAL_VARIABLE_H

#include"SetTrieNode.h"
#include<vector>
#include<set>
#include<sys/time.h>
#include<map>
#include<string>
#include"Graph.h"

//#define DEBUG 1
extern int MMC_LIMIT;
extern int SUBGRAPH_LIMIT;
//extern int CandidateLimit;

/**
 * boolean variables to control whether we want to test dynamic graph features
 */
extern bool dynamic_graph_enabled;
extern bool dynamic_graph_mode;
extern int dynamic_graph_threshold;

extern bool removeNode;
extern int dynamic_graph_counter_rn;
extern double dynamic_graph_algo_time_rn;
extern double dynamic_graph_naive_time_rn;

extern bool removeEdge;
extern int dynamic_graph_counter_re;
extern double dynamic_graph_algo_time_re;
extern double dynamic_graph_naive_time_re;

extern bool addNode;
extern int dynamic_graph_counter_an;
extern double dynamic_graph_algo_time_an;
extern double dynamic_graph_naive_time_an;

extern bool addEdge;
extern int dynamic_graph_counter_ae;
extern double dynamic_graph_algo_time_ae;
extern double dynamic_graph_naive_time_ae;


extern set<int> initial_embedding;

extern string OUTPUT_PATH;
extern string NAME2ID_PATH;

extern bool RandomSelection;
extern bool DUPLICATION;
extern bool WithoutIsoCheckPruning;

extern bool NOEARLYSTOPCHECK;

extern bool directed;
extern bool APPROXIMATION;
extern bool GETEXACTMMC;

extern double R;
extern double DELTA;
extern int OMEGA;


extern int average_number_mmc;
extern int average_size_mmc;
extern double average_density_mmc;

extern int duplicated_answer_num;
extern int iso_check_num;

//for debug
extern int global_hash_id;
extern int motif_node_size;
extern int solNum;
extern int maximum_clique_size;

extern SetTrieNode *root;
extern Graph *temp_graph;

extern int test_num;
extern int max_test_num;

// Global structures used in IsoCheck
extern vector<vector<int> > temp_matched_subgraphs;
extern map<int, set<int>> cand_label2nodes;
extern set<int> label_set;
extern map<int, set<int> > cands;
extern set<int> pre_IsoCheck_set;
extern int current_u;

extern bool *IsVisited;
extern map<int, string> id2name; // map node id to real info.
extern vector<vector<int> > subgraphs; // matched subgraphs
extern map<int, set<int> > motif_label2nodes;

extern map<int, set<int> > motif_labelAdj;


extern vector<set<int> > maximal_motif_cliques; // the result set
extern set<int> current_node_set, candidates;
extern map<int, set<int> > label2adj;

extern Graph motif;
extern Graph graph;

extern vector<double> approx_improvement;

extern vector<double> real_approx_ratio;

extern vector<double> real_failure_prob;


extern bool CHECKSPECIES;
extern set<int> validNodes;


#endif //MOTIF_SRC_GLOBAL_VARIABLE_H

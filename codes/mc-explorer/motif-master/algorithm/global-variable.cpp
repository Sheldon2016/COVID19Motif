//
// Created by Hujiafeng on 15/10/2017.
//
#include "global-variable.h"

using namespace std;

int SUBGRAPH_LIMIT = -1;  // default value -1
int MMC_LIMIT = -1; // default value -1
//int CandidateLimit=50;

bool dynamic_graph_enabled = true;
bool dynamic_graph_mode = false;
int dynamic_graph_threshold = 1000;

bool removeNode = true;
int dynamic_graph_counter_rn = 0;
double dynamic_graph_algo_time_rn = 0;
double dynamic_graph_naive_time_rn = 0;

bool removeEdge = true;
int dynamic_graph_counter_re = 0;
double dynamic_graph_algo_time_re = 0;
double dynamic_graph_naive_time_re = 0;

bool addNode = true;
int dynamic_graph_counter_an = 0;
double dynamic_graph_algo_time_an = 0;
double dynamic_graph_naive_time_an = 0;

bool addEdge = true;
int dynamic_graph_counter_ae = 0;
double dynamic_graph_algo_time_ae = 0;
double dynamic_graph_naive_time_ae = 0;

set<int> initial_embedding;


string OUTPUT_PATH = "";
string NAME2ID_PATH = "";

bool RandomSelection = false;
bool DUPLICATION = false;
bool WithoutIsoCheckPruning = false;
bool directed = false;
bool APPROXIMATION = false;
bool NOEARLYSTOPCHECK = false;
bool GETEXACTMMC = false;

double R = 5;
double DELTA = 0.01;
int OMEGA;

int average_number_mmc = 0;
int average_size_mmc = 0;
double average_density_mmc = 0;
int duplicated_answer_num = 0;
int iso_check_num = 0;

int global_hash_id = 0;
int motif_node_size = 0;
int solNum = 0;
int maximum_clique_size = 0;

SetTrieNode *root = NULL;
Graph *temp_graph = NULL;

int test_num = 0;
int max_test_num = 0;

// Global structures used in IsoCheck
vector<vector<int> > temp_matched_subgraphs;
map<int, set<int>> cand_label2nodes;
set<int> label_set;
map<int, set<int> > cands;
set<int> pre_IsoCheck_set;
int current_u;

bool *IsVisited;
map<int, string> id2name; // map node id to real info.
vector<vector<int> > subgraphs; // matched subgraphs
map<int, set<int> > motif_label2nodes;
map<int, set<int> > motif_labelAdj;

vector<set<int> > maximal_motif_cliques; // the result set
//set<int> maximum_motif_clique;
set<int> current_node_set, candidates;
map<int, set<int> > label2adj;

Graph motif;
Graph graph;

vector<double> approx_improvement;

vector<double> real_approx_ratio;

vector<double> real_failure_prob;

bool CHECKSPECIES = false;
set<int> validNodes;
//
// Created by bxli on 2019-09-22.
//

#ifndef MOTIF_SRC_UTILS_H
#define MOTIF_SRC_UTILS_H

#include "global-variable.h"
#include "Graph.h"

// print results with motif
void PrintResults(const vector<set<int> > &result, const string &output_path, const Graph &graph, const Graph &motif,
				  const string &name2id_path, string custom_message, const bool append);

void PrintResults(const vector<set<int> > &result, const string &output_path, const Graph &graph,
				  const string &name2id_path, string custom_message, const bool append);

void ReadId2Name(const string &name2id_path);

#endif //MOTIF_SRC_UTILS_H

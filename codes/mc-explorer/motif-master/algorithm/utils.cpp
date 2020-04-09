//
// Created by bxli on 2019-09-22.
//
#include <set>
#include <iostream>
#include <sstream>
#include "utils.h"

unsigned int split(const string &txt, vector<string> &strs, char ch) {
	//this is the general case
	size_t pos = txt.find(ch);
	size_t initialPos = 0;
	strs.clear();
	// Decompose statement
	while (pos != string::npos) {
		strs.push_back(txt.substr(initialPos, pos - initialPos));
		initialPos = pos + 1;
		pos = txt.find(ch, initialPos);
	}
	// Add the last one
	strs.push_back(txt.substr(initialPos, min(pos, txt.size()) - initialPos));
	//return the size of the vector
	return int(strs.size());
}

void ReadId2Name(const string &name2id_path) {
	ifstream ifile;
	ifile.open(name2id_path);
	string str, name;
	int id;

	while (getline(ifile, str)) {
		vector<string> v;
		split(str, v, '\t');
		name = v[0];
		char *pEnd;
		id = strtol(v[1].c_str(), &pEnd, 10);
		id2name[id] = name;
	}
	ifile.close();
	ifile.clear();
}


void PrintResults(const vector<set<int> > &result, const string &output_path, const Graph &graph, const Graph &motif,
				  const string &name2id_path, const string custom_msg = "", const bool append=false) {
	ostringstream stringStream;
	stringStream << custom_msg;
	stringStream << "@@ motif nodeNum = " << motif.nodeNum << " edgeNum = " << motif.edgeNum << endl;
	for (int i = 0; i < motif.labels.size(); i++) {
		stringStream << "node " << i << "'s label = " << motif.labels[i] << endl;
	}
	for (int i = 0; i < motif.adjList.size(); i++) {
		for (auto n: motif.adjList[i]) {
			stringStream << "edge " << i << " - " << n << endl;
		}
	}
	stringStream << endl;
	PrintResults(result, output_path, graph, name2id_path, stringStream.str(), append);
}

void PrintResults(const vector<set<int> > &result, const string &output_path, const Graph &graph,
				  const string &name2id_path, const string custom_msg = "", const bool append=false) {

	if (name2id_path.empty()) {
		cout << "name2id_path is empty!" << endl;
		return;
	}
	FILE *fout;

	string open_way = append ? "a" : "w";
	fout = fopen(output_path.c_str(), open_way.c_str());
	int index = 0;

	fprintf(fout, custom_msg.c_str());

	for (const auto &e : result) {

		//if(e.size() < 6 ) continue;
		index++;

		map<int, int> label_count;
		for (const auto u : e) {
			if (label_count.find(graph.labels[u]) == label_count.end()) {
				label_count[graph.labels[u]] = 1;
			} else label_count[graph.labels[u]]++;
		}

//        bool flag = true;
//        for (auto x : label_count) {
//            if (x.second < 3) {
//                flag = false;
//                break;
//            }
//        }
//        if (!flag) continue;

		fprintf(fout, "The %d th MMC: ", (index));
		fprintf(fout, "num_nodes: %d", int(e.size()));
		for (const auto u : e) {
			fprintf(fout, "\t%d", u);
		}
		fprintf(fout, "\n");
		if (id2name.empty()) {
			ReadId2Name(name2id_path);
		}
		fprintf(fout, "id\tproduct-name\ttype\n");
		for (const auto u : e) {
			fprintf(fout, "%d\t%s\t%d\n", u, id2name[u].c_str(), graph.labels[u]);
		}
		fprintf(fout, "Print the graph:\n");
		for (const auto u: e) {
			for (const auto w:e) {
				if (u != w && u < w) {
					if (!directed) {
						if (graph.adjList[u].find(w) != graph.adjList[u].end()) {
							fprintf(fout, "%d\t%d\n", u, w);
						}
					} else {
						if (graph.InAdjList[u].find(w) != graph.InAdjList[u].end()) {
							fprintf(fout, "%d\t%d\n", w, u);
						}

						if (graph.OutAdjList[u].find(w) != graph.OutAdjList[u].end()) {
							fprintf(fout, "%d\t%d\n", u, w);
						}
					}
				}
			}
		}
	}
	fclose(fout);
}



On Maximal Motif Clique Enumeration

Options:
Parameters:
	-genMotif
		Generate motifs from the inputted graph
	-mmc
		Enumerate MMCs.
	-graph <file>
		the target graph
	-motif <int>
		the file of inputted motifs
	-output <file>
		Save the MMCs for each motif in <file>_motifId.txt
	-name2idpath <file>
		The file contains all mappings from node names to ids.
	-vf3
		Use VF3 in subgraph matching.
	-motif_size <int>
		the size of each motif to be generated.
	-motif_num <int>
		number of motifs to be generated.
	-subgraphlimit <int>
		The maximum number of subgraphs to be explored.
	-mmclimit <int>
		The maximum number of mmcs to be explored.
	-containnode <int>
		The assigned node to be contained in detected MMCs.
	-directed <int>
		Consider directed graphs.
	-RandomSelection
		Randomly return a node in the Pick function.
	-DUPLICATION
		Don't set-trie to avoid duplication
	-WithoutIsoCheckPruning
		Don't use IsoCheck pruning.

Example (Generate motifs):
./motif_src -genMotif -graph network.txt -output motif.txt -motif_size 3 -motif_num 100


Example (Enumerate MMCs):
./motif_src -mmc -vf3 -graph net.txt -motif motif.txt -name2idpath name2idpath.txt -output mmcs.txt -subgraphlimit 100 -mmclimit 100 


Example (Enumerate MMCs with an assigned node):
./motif_src -mmc -vf3 -graph net.txt -containnode 1 -motif motif.txt -name2idpath name2idpath.txt -output mmcs.txt -subgraphlimit 100 -mmclimit 100 

e.g. 
./motif-demo-src -mmc -vf3
    -graph ~/Dropbox/mclique/dataset/dblp4area-new/graph.txt
    -name2idpath ~/Dropbox/mclique/dataset/dblp4area-new/allN.txt
    -motif ~/Dropbox/mclique/dataset/dblp4area-new/motif_4_300.txt
    -output ./output/dblp4area-new/mmce_4
    -subgraphlimit 3000 -mmclimit 1000
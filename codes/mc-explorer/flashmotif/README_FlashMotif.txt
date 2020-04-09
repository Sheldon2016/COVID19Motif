Readme file for FlashMotif.jar
Author: Giovanni Micale

---1. Description---

FlashMotif is a JAR program for finding statistically significant colored motifs with analytically derived p-values.
FlashMotif works on both directed and undirected networks and can handle induced and non-induced injective or multiset topological colored motifs 
with color-topology dependency or independency. The user can also define constraints on both colors and topologies of motifs to restrict the search space.
FlashMotif uses GLabTrie algorithm to count occurrences of colored motifs in a graph and computes motif p-values
using the analytical model described in the following paper:

"Analytical Methods for Finding Significant Colored Graph Motifs"
G. Micale, R. Giugno, A. Ferro, M. Mongiovi, D. Shasha, A. Pulvirenti.

The results of FlashMotif are written in a text file containing a list of the motifs found (indexed by topology and set of node colors) and related statistics.


---2. Input parameters---

Usage: java -jar FlashMotif.jar -n <networkFile> [-m <motifSize> -dep -multi -nonind -cc <colorConstraints> 
-tc <topoConstraints>  -pa <pvalAnalThresh> -f <minFrequency> -o <resultsFile>]

REQUIRED PARAMETERS:
-n	Input network file

OPTIONAL PARAMETERS:
-m	Motif size (default=3)
-indep	Use EDD with color-degree independency as random model (default=color-topology dependent EDD model)
-multi	Search for multiset topological colored motifs (default=injective topological colored motifs)
-nonind	Search for non-induced motifs (default=induced motifs)
-cc	String of color constraints (e.g. 1,1,2 means that motif must contain at least 2 nodes of color '1' and 1 node of color '2') (default=no constraints)
-tc	Text file with adjacency strings of topologies that motifs must have (default=no topology constraints)
-pa	Return only motifs with analytical p-value below this threshold (default=1.0)
-f	Return only motifs whose frequency in the input network >= this value (default=1)
-o	Output file where results will be saved (default=results.txt)

Note that the list of color constraints is not ordered.  
So, for instance, "1,1,2" does not mean that the first node of the motif must have color 1, second node must have color 1 and third node must have color 2.
It means that the motif must contains two nodes of color 1 and one node of color 2, independently from the order of colors in the motif.


---3. Input network file format---

Colored networks are provided as text files. Here are two examples of input network text file:

Example A:

undirected
5
1
2
1
1
2
0 1
0 3
0 4
1 2
1 3
2 4

Example B:

directed
6
1
3
2
1
3
3
0 1
0 4
1 0
1 3
2 4
3 0
3 1
5 3

The text files must follow this format:
- The first line defines network orientation ("directed" for directed networks, "undirected" for undirected networks);
- The second line contains the number N of network nodes. Nodes are indexed from 0 to N;
- In the following N lines, the corresponding node colors are reported (so the third line contains the color of the node with index 1, 
the fourth line contains the color of node with index 2, and so on). Color types must be indicated with integer numbers.
- In the following lines, the list of network edges is reported. The first number is the index of source node, 
the second number is the index of destination node. The two numbers are separated by a single space character.
Note that in the undirected network edges are reported only once (this means that if edge (a,b) is present, edge (b,a) is not reported).

In the example A, we define an undirected network with 5 nodes, two color types (1 and 2) and 6 edges. 
The color of first node is 1, the color of the second node is 2, and so on. Node 1 is connected with nodes 2, 4 and 5.

In the example B, we define a directed network with 6 nodes, three color types (1, 2 and 3) and 8 edges. 
The color of first node is 1, the color of the second node is 3, the color of the third node is 2, and so on. 
Arcs between node 1 and node 2 and between node 1 and node 5 exist. 
There is also an arc between node 2 and node 1, meaning that there is a reciprocal edge between nodes 1 and 2.

Some examples of colored networks generated according to this format can be found here: alpha.dmi.unict.it/flashMotif/files/Networks.zip.


---4. Motif topology file---

Motif topology file contains a list of adjacency strings, representing the topology of motifs to search in the input network.

An adjacency string is a string of '0' and '1' characters, given by the concatenation of the row of the motif's adjacency matrix.

For example, let's consider the adjacency matrix of a clique with 3 nodes:
011
101
110

The corresponding adjacency string is:
011101110

The motif topology file is a text file, where each line contains the adjacency string of a motif topology.

Here is an example of motif topology file:
0001000100011110
0011001111001100
0111101111011110

In this example, motif topology file contains a path with 4 nodes, a square and a 4-clique.

Some examples of motif topology files generated according to this format can be found here: alpha.dmi.unict.it/flashMotif/files/Topologies.zip.


---5. File format of output results---

Results are saved in a text file (default name=results.txt) in a tabular format, with different fields separated by 'tab' (\t) characters.

The header (first row of the file) defines the list of fields: 
a) "Motif (Topology:nodeColors)": the motif, represented by the adjacency string of its topology (see also section 4) followed by a list of node colors.
The list of node colors is ordered, meaning that the first element of the list is the color of the first node, and so on;
b) "Num_occ_input_graph": number of occurrences of the motif in the input network;
c) "Mean_analytical": average number of occurrences of the motif in the ensemble of EDD random graphs, according to the analitical model;
d) "Variance_analytical": variance of the number of occurrences of the motif in the ensemble of EDD random graphs, according to the analitical model;
e) "Pval_analytical": analytical p-value of the motif.


---6. Usage examples---

Example 1) 
Find induced injective motifs of size 3 with color-topology dependency in ROGET network
	java -jar FlashMotif.jar -n Networks/roget.txt -m 3
We can also type:
	java -jar FlashMotif.jar -n Networks/roget.txt

Example 2)
Find non-induced multiset motifs of size 3 with color-topology independency in DBLP network
	java -jar FlashMotif.jar -n Networks/dblp.txt -m 3 -nonind -multi -indep
We can also type:
	java -jar FlashMotif.jar -n Networks/dblp.txt -nonind -multi -indep
	
Example 3)
Find non-induced injective motifs of size 4 with color-topology dependency in NEURALWORM network
	java -jar FlashMotif.jar -n Networks/neuralWorm.txt -m 4 -nonind

Example 5)
Find non-induced multiset motifs of size 5 with color-topology independency in ROGET network with 2 nodes of color 2 and 1 node of color 5
	java -jar FlashMotif.jar -n Networks/roget.txt -m 5 -nonind -multi -indep -cc 2,2,5
	
Example 6)
Find non-induced multiset motifs of size 4 with color-topology dependency in PPIHUMAN network, satisfying a set of topology constraints (see also section 4)
	java -jar FlashMotif.jar -n Networks/ppiHuman.txt -m 4 -nonind -multi -tc Topologies/topoConstr.txt

Example 7)
Find induced injective motifs of size 3 with color-topology independency that occur at least 10 times in DBLP network, with analytical p-value at most 0.02
	java -jar FlashMotif.jar -n Networks/dblp.txt -m 3 -f 10 -ps 0.02 -indep
	
Example 8)
Find non-induced multiset motifs of size 4 in HAMSTER network with color-topology dependency and set different file name for result file
	java -jar FlashMotif.jar -n Networks/hamster.txt -m 4 -nonind -multi -o hamsterResults.txt

Example 9) 
Find non-induced injective motifs of size 6 with color-topology dependency in "myNet" network with 1 nodes of color 2, 2 nodes of color 3 and 2 nodes of color 5,
with analytical p-value at most 0.01 
	java -jar FlashMotif.jar -n myNet.txt -m 6 -nonind -cc 2,3,3,5,5 -pa 0.01

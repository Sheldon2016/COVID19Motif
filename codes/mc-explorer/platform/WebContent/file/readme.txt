Put your dataset into a single txt file.
The required format is as follows:

1. The first line contains 3 numbers, representing number of nodes (n), number of edges (m) and number of labels (k)
2. Next n lines contain node information, following the format: node_id	node_name	node_property(optional)
   e.g.
   0	Bob	23|Asian|Male
   1	Amy	21||Female
   2	Charlie	||Male
   Note that `property` info is optional. Property info will be displayed on the side panel. Multiple property
   info are seperated by |. In the above example, there are 3 properties: Age|Race|Sex. It is allowed that some nodes have
   all property info while others have less or even none.

   Of course, you can make `property` info as nodes. For the above example, you can have a node named 23 with label age,
   and connected to Bob. It is your choice. The rule of thumb is whether you need that info in the motif. If you want to
   include age in your motif, definitely make it a node instead of a property.
3. Next m lines contain edge information, following the format: edge_id	edge_id	weight(optional)
   e.g.
   0	1
   1	2	0.3
   Note that weight should range from 0 to 1.
4. Next k lines contain label information, following the format: label_id	label_name
   e.g.
   0	drug
   1	disease
   2	gene
5. Next n lines contain label for each node, following the format: label_id
   e.g.
   0
   0
   1
   1
   0
   2

The following is optional, i.e. your input file is still valid if you don't have the following data.

6. Next 1 line contains property info, following the format: property1_name|property2_name|...
   e.g.
   age|race|sex
   Note that in step 2, we only included values of properties, but not property names.
   If you don't include any property info in step 2, put a blank line here for consistency.
7. Next 1 line contains a number s
8. Next s lines contain description info, following the format: node_id	description
   e.g.
   0	Exenatide is a functional analog of Glucagon-Like Peptide-1 (GLP-1), a naturally occuring peptide.
   Description info will be shown in the main panel (hided). Left click on a node to see its description.

---------------------------------------- NOTE --------------------------------------------

-Ids must start from zero
-Unless mentioned, delimiter is always '\t'
-Edges are undirected

---------------------------------------- Example ----------------------------------------

n	m	k
id_0	name_0
id_1	name_1
.....
id_n-1	name_n-1
edge_0
edge_1
...
edge_m-1
0	label_0
1	label_1
2	label_2
...
k-1	label_k-1
node_0_label
node_1_label 
... 
node_n-1_label


Example:
10	20	3
0	3 kingdom
1	History of China
...
9	The Mathematics
0	1
0	3
0	7
...
8	9
0	book
1	author
2	publisher
0
0
1
...
0

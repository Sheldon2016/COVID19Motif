# Datasets for Covid-19 Knowledge Graph
This repository contains codes and datasets for Covid-19 Knowledge Graph, which can be integrated from different data sources and domains.
## Neo4j Datasets
The datasets are maintaiend by groups like neo4j. The datasets are mainly from publications and case statistics. 
* [url](http://www.odbms.org/2020/03/we-build-a-knowledge-graph-on-covid-19/)
* [detailed describtion](https://covidgraph.org)
* pros: easy to access.
* cons: currently only gene and publication related information are available.

#### scheme
<p align="center">
  <img width="800" src="neo4j-scheme.png">
</p>
The publication dataset and the gene dataset (marked in green) are linked by a "MENTIONS" link (marked in pink), which means the corresponding paper abstract mentions the gene symbol.

#### examples
<p align="center">
  <img width="800" src="neo4j-gene.svg">
</p>
<p align="center">
  <img width="400" src="neo4j-gene-label.png">
</p>
The subgraphs around the gene "".
<p align="center">
  <img width="800" src="neo4j-author.svg">
</p>
<p align="center">
  <img width="400" src="neo4j-autohr-label.png">
</p>
The subgraphs around the authors in Hong Kong.
<p align="center">
  <img width="800" src="neo4j-location.svg">
</p>
<p align="center">
  <img width="400" src="neo4j-location-label.png">
</p>
The subgraphs of the case statistics around Hong Kong.

## OpenKG Datasets
The datasets are maintained by several universities and companies from mainland, e.g., Tsinghua University and Huawei. The datsets are from different areas, and the datasets for research purposes include information about host, virus, drugs, gene and protein.
#### [url](http://www.openkg.cn/dataset/covid-19-research)
#### [detailed describtion](https://mp.weixin.qq.com/s/eHbkrMtYpg-oEmWS92970w)
#### pros: wide coverage with rich knowledge
#### cons: need coding effords from original data sources in JSON.
#### scheme
<p align="center">
  <img width="500" src="graph.svg">
</p>
#### nodes
`nodeID,nodeLabelID,nodeName`
#### edges
`nodeID1,nodeID2,edgeLabelID`
#### labels
Labels for nodes `nodeLabelID:nodeLabel` and edges `edgeLabelID:edgeLabel`
<p align="center">
  <img width="600" src="covid19.png">
</p>

## Usage 

### Open `codes/webpage/mcypher.html` for local view.

### Components to be added:
* Motif input [Matin];
* Output visulization [Matin];
* M-Cypher parser [Xiaodong];
* 4-page paper [Xiaodong];
* Declarative functionalities [Xiaodong]:
	* Motif counting (see Q1);
	* Motif instance enumerating (a.k.a isomorphic subgraph detection, see Q2);
	* Motif-paths finding (e.g., nodes reachable by triangle connectivities, see Q3);
	* Motif-components finding (e.g., k-cliques);
* Embedded API functionalities [Matin and Xiaodong]:
	* Motif Page Rank for better node ranking;
	* Motif conductance for better graph clustering;	
	* Motif Discovery;
	* Motif adjacency matrix calculation;
	* Motif feature vectors for better link prediction.

### Use cases
In the following query examples, we demonstrate three use cases (Q1, Q2 and Q3) by motif M, which is predefined by the user in the GUI.
#### Q1: What is the significance of motif pattern M?
* cypher:	`MATCH p=(:Country)<-[:from_country]-(:Strain)-[:mutate_from_branch]->(:Branch) RETURN COUNT (p)`
* m-cypher:	`MATCH (m:M) RETURN COUNT (m)`
* What if M is a large motif? 
	* Almost impossible to describe M by path pattern queries in cypher! 
	* Even so, there will be many duplicates!
#### Q2: What are the instances of motif pattern M?
* cypher:	`MATCH (a:Location)<-[:from_location]-(b:Strain)-[:mutate_from_branch]->(c:Branch) RETURN a,b,c`
* m-cypher:	`MATCH (m:M) RETURN m`
* Same problems exists as Q1!
#### Q3: How virus mutates when spreading from Location a to Location b?
* cypher:	NA
* m-cypher:	`MATCH (a:Location{name:a})-[m:M*]->(b:Location{name:b})`

<p align="center">
  <img width="600" src="motifM.PNG">
</p>

## Dependencies
### Neo4j 
* Install [neo4j-desktop](https://neo4j.com/developer/neo4j-desktop/).
* Put data into `<neo4j-home>/import`. 
For example, `C:/Users/<user>/.Neo4jDesktop/neo4jDatabases/<database>/installation-<version>/import` in windows.
* Import COVID19 data into neo4j.
```
#put 'nodes' and 'edges' in '<neo4j-home>/import' beforehand
LOAD CSV FROM 'file:///nodes' AS line
FOREACH ( ignoreMe in CASE WHEN line[1]='0' THEN [1] ELSE [] END | CREATE (:Host {id:toInteger(line[0]),label:line[2]}))

LOAD CSV FROM 'file:///nodes' AS line
FOREACH ( ignoreMe in CASE WHEN line[1]='1' THEN [1] ELSE [] END | CREATE (:Virus {id:toInteger(line[0]),label:line[2]}))

LOAD CSV FROM 'file:///nodes' AS line
FOREACH ( ignoreMe in CASE WHEN line[1]='2' THEN [1] ELSE [] END | CREATE (:VirusProtein {id:toInteger(line[0]),label:line[2]}))

LOAD CSV FROM 'file:///nodes' AS line
FOREACH ( ignoreMe in CASE WHEN line[1]='3' THEN [1] ELSE [] END | CREATE (:HostProtein {id:toInteger(line[0]),label:line[2]}))

LOAD CSV FROM 'file:///nodes' AS line
FOREACH ( ignoreMe in CASE WHEN line[1]='4' THEN [1] ELSE [] END | CREATE (:Drug {id:toInteger(line[0]),label:line[2]}))

LOAD CSV FROM 'file:///edges' AS line
MATCH (n:Drug {id:toInteger(line[0])}), (m:Virus{id:toInteger(line[1])})
FOREACH ( ignoreMe in CASE WHEN line[2]='0' THEN [1] ELSE [] END | MERGE (n)-[:Effect]->(m))

LOAD CSV FROM 'file:///edges' AS line
MATCH (n:HostProtein {id:toInteger(line[0])}), (m:VirusProtein{id:toInteger(line[1])})
FOREACH ( ignoreMe in CASE WHEN line[2]='1' THEN [1] ELSE [] END | MERGE (n)-[:Interact]->(m))

LOAD CSV FROM 'file:///edges' AS line
MATCH (n:VirusProtein {id:toInteger(line[0])}), (m:HostProtein{id:toInteger(line[1])})
FOREACH ( ignoreMe in CASE WHEN line[2]='2' THEN [1] ELSE [] END | MERGE (n)-[:Bind]->(m))

LOAD CSV FROM 'file:///edges' AS line
MATCH (n:HostProtein {id:toInteger(line[0])}), (m:Host{id:toInteger(line[1])})
FOREACH ( ignoreMe in CASE WHEN line[2]='3' THEN [1] ELSE [] END | MERGE (n)-[:Belong_to]->(m))

LOAD CSV FROM 'file:///edges' AS line
MATCH (n:Virus {id:toInteger(line[0])}), (m:VirusProtein{id:toInteger(line[1])})
FOREACH ( ignoreMe in CASE WHEN line[2]='4' THEN [1] ELSE [] END | MERGE (n)-[:Produce]->(m))
```
### MC-Explorer 
#### [Motif-clique explorer system](http://motif.cs.hku.hk/)
#### [Format of graph for MC-explorer](http://motif.cs.hku.hk/file/readme.txt)
#### Codes 
* See `codes/mc-explorer`.
* About motif input:
see`codes/mc-explorer/platform/WebContent/js/graphM.js`, `codes/mc-explorer/platform/WebContent/js/utilities.js`
and `codes/mc-explorer/platform/WebContent/js/graphResult.js`.
* About graph visualization: [cytoscape](https://cytoscape.org/).

#### [COVID19 VLDB demo video](https://www.dropbox.com/s/xhpczwsv7m4cut1/covid_19_vldb_demo_%20compression.mp4?dl=0). 
#### [COVID19 datasource](https://mp.weixin.qq.com/s/eHbkrMtYpg-oEmWS92970w). Chinese version only.



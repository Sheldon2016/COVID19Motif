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
  <img width="900" src="neo4j-scheme.png">
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
  <img width="400" src="neo4j-author-label.png">
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
* [url](http://www.openkg.cn/dataset/covid-19-research)
* [detailed describtion](https://mp.weixin.qq.com/s/eHbkrMtYpg-oEmWS92970w)
* pros: wide coverage with rich knowledge
* cons: need coding effords from original data sources in JSON.

#### schemes
<p align="center">
  <img width="800" src="openkg-scheme.png">
</p>

#### examples
<p align="center">
  <img width="500" src="openkg-city-strain-branch.png">
</p>
yellow: city, red: strain, pink: strain branch
<p align="center">
  <img width="800" src="openkg-host-virus-protein-1.svg">
</p>
yellow: virus (covid-19), red: protein, blue: host
<p align="center">
  <img width="300" src="openkg-host-virus-protein-2.svg">
</p>
yellow: virus (sars), red: protein, blue: host
<p align="center">
  <img width="800" src="openkg-virus-protein-gene.svg">
</p>
yellow: virus (covid-19), red: protein, grey: gene

## Virus-Protein-Drug network
The datasets are released in the science paper published in Cell Discovery 2020. In the human PPI network, they analyzed human proteins that functionally associate with Covid-19 viral infection and proteins that serve as drug targets. 
* [url](https://github.com/ChengF-Lab/GPSnet)
* [describtion](https://www.nature.com/articles/s41421-020-0153-3.pdf)
* pros: proved data for potential drug discovery.
* cons: domain knowledge needed to integrate the data.

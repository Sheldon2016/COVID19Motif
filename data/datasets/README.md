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

* The subgraphs around the gene "CD191|CKR-1|CKR1|CMKBR1|HM145|MIP1aR|SCYAR1".
<p align="center">
  <img width="800" src="neo4j-gene.svg">
</p>
<p align="center">
  <img width="400" src="neo4j-gene-label.png">
</p>

* The subgraphs around the authors in Hong Kong.
<p align="center">
  <img width="800" src="neo4j-author.svg">
</p>
<p align="center">
  <img width="500" src="neo4j-author-label.png">
</p>

* The subgraphs of the case statistics around Hong Kong.
<p align="center">
  <img width="800" src="neo4j-location.svg">
</p>
<p align="center">
  <img width="400" src="neo4j-location-label.png">
</p>


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

* yellow: city, red: strain, pink: strain branch
<p align="center">
  <img width="800" src="openkg-city-strain-branch.svg">
</p>

* yellow: virus (covid-19), red: protein, blue: host
<p align="center">
  <img width="800" src="openkg-host-virus-protein-1.svg">
</p>

* yellow: virus (sars), red: protein, blue: host
<p align="center">
  <img width="300" src="openkg-host-virus-protein-2.svg">
</p>

* yellow: virus (covid-19), red: protein, grey: gene
<p align="center">
  <img width="800" src="openkg-virus-protein-gene.svg">
</p>

## Virus-Protein-Drug network
The datasets are released in the science paper published in Cell Discovery 2020. In the human PPI network, they analyzed human proteins that functionally associate with Covid-19 viral infection and proteins that serve as drug targets. 
* [url](https://github.com/ChengF-Lab/2019-nCoV)
* [describtion](https://www.nature.com/articles/s41421-020-0153-3.pdf)
* pros: proved data for potential drug discovery.
* cons: result onbly; domain knowledge needed to integrate the data.

#### problems
* how to evaluate the drugs discovered:
  * single drug: numeric indicators (z-score, p value),  bioinformatics validation (drug-induced gene signatures and HCoV induced transcriptomics), literature-derived antiviral evidence
  * drug combinations: numeric indicators (z-score, p value)
* data to be released:
  * HCoV–host protein interactions
  * drug–target network
  * human protein–protein interactome
  
#### examples

* The subgraphs around the HostProtein "ACE2" which is functionally associate with Covid-19 viral infection.
<p align="center">
  <img width="800" src="all-ace2.svg">
</p>
<p align="center">
  <img width="800" src="all-ace2-label.png">
</p>

|Coronavirus | Host-Protein | Host-Gene-ID | PubMed-ID|
| --- | --- | --- | --- |
|IBV | NONO | 4841 | 23637410|
|IBV | GSK3A | 2931 | 23637410|
|IBV | GSK3B | 2932 | 23637410|
|IBV | PABPC1 | 26986 | 23637410|
|IBV | PABPC4 | 8761 | 23637410|
|IBV | HNRNPA1 | 3178 | 23637410|
|IBV | HNRNPA2B1 | 3181 | 23637410|
|IBV | NPM1 | 4869 | 23637410|
|IBV | G3BP1 | 10146 | 23637410|
|IBV | G3BP2 | 9908 | 23637410|
|IBV | RPL19 | 6143 | 23637410|
|IBV | PARP1 | 142 | 23637410|
|IBV | NCL | 4691 | 23637410|
|IBV | DDX1 | 1653 | 23637410|
|SARS | RYBP | 23429 | 22046132|
|SARS | PPIA | 5478 | 22046132|
|SARS | NOMO3 | 408050 | 22046132|
|SARS | FKBP1A | 2280 | 22046132|
|SARS | PPIG | 9360 | 22046132|
|SARS | MARK3 | 4140 | 22046132|
|SARS | PPIH | 10465 | 22046132|
|SARS | RCAN3 | 11123 | 22046132|
|SARS | HGS | 9146 | 22046132|
|SARS | BAG6 | 7917 | 22046132|
|SARS | DDAH2 | 23564 | 22046132|
|SARS | CAMLG | 819 | 22046132|
|SARS | CHMP2B | 25978 | 22046132|
|SARS | SNAP47 | 116841 | 22046132|
|SARS | MKRN2 | 23609 | 22046132|
|SARS | TPSAB1 | 7177 | 22046132|
|SARS | SERPING1 | 710 | 22046132|
|SARS | MKRN3 | 7681 | 22046132|
|SARS | PSMA2 | 5683 | 22046132|
|SARS | ABHD17A | 81926 | 22046132|
|SARS | PFDN5 | 5204 | 22046132|
|SARS | MIF4GD | 57409 | 22046132|
|SARS | NDUFA10 | 4705 | 22046132|
|SARS | VKORC1 | 79001 | 22046132|
|SARS | LAS1L | 81887 | 22046132|
|SARS | H2AFY2 | 55506 | 22046132|
|SARS | RPS20 | 6224 | 22046132|
|SARS | CHEK2 | 11200 | 22046132|
|SARS | TERF1 | 7013 | 22046132|
|SARS | DCTN2 | 10540 | 22046132|
|SARS | DDX5 | 1655 | 22046132|
|SARS | C11orf74 | 119710 | 22046132|
|SARS | EIF3F | 8665 | 22046132|
|SARS | EEF1A1 | 1915 | 22046132|
|SARS | CAV1 | 857 | 22046132|
|SARS | IKBKB | 3551 | 22046132|
|SARS | UBE2I | 7329 | 22046132|
|SARS | SGTA | 6449 | 22046132|
|SARS | ATP6V1G1 | 9550 | 22046132|
|SARS | BTF3 | 689 | 22046132|
|SARS | ATF5 | 22809 | 22046132|
|SARS | ND4L | 4539 | 22046132|
|SARS | COX2 | 4513 | 22046132|
|SARS | HNRNPA1 | 3178 | 22046132|
|SARS | **ACE2** | 59272 | 22046132|
|SARS | CLEC4G | 339390 | 22046132|
|SARS | CD209 | 30835 | 22046132|
|SARS | CLEC4M | 10332 | 22046132|
|SARS | IRF3 | 3661 | 22046132|
|SARS | KPNA2 | 3838 | 22046132|
|SARS | SFTPD | 6441 | 22046132|
|SARS | PPIA | 5478 | 22046132|
|SARS | BCL2L1 | 598 | 22046132|
|SARS | BCL2L2 | 599 | 22046132|
|SARS | MCL1 | 4170 | 22046132|
|SARS | BCL2A1 | 597 | 22046132|
|SARS | BCL2 | 596 | 22046132|
|MERS | SKP2 | 6502 | 31852899|
|MERS | KPNA4 | 3840 | 29370303|
|MERS | PRKRA | 8575 | 24522921|
|MERS | CD9 | 928 | 28759649|
|MERS | TMPRSS2 | 7113 | 28759649|
|SARS | IKBKB | 3551 | 17705188|
|HCoV-229E | ANPEP | 290 | 28643204|
|HCoV-NL63 | ACE2 | 59272 | 28643204|
|IBV | ZCRB1 | 85437 | 28643204|
|MERS | DPP4 | 1803 | 28643204|
|MHV | HNRNPA1 | 3178 | 28643204|
|MHV | SYNCRIP | 10492 | 28643204|
|MHV | PTBP1 | 5725 | 28643204|
|MHV | CEACAM1 | 634 | 28643204|
|SARS | ZCRB1 | 85437 | 28643204|
|SARS | ACE2 | 59272 | 28643204|
|IBV | ANXA2 | 302 | 28643204|
|MHV | HNRNPA2B1 | 3181 | 28643204|
|MHV | HNRNPA3 | 220988 | 28643204|
|MHV | ACO2 | 50 | 28643204|
|MHV | DNAJB1 | 3337 | 28643204|
|MHV | HSPD1 | 3329 | 28643204|
|MHV | HSPA9 | 3313 | 28643204|
|MHV | COPB2 | 9276 | 30632963|
|MHV | RPL13A | 23521 | 30632963|
|MHV | EIF3E | 3646 | 30632963|
|MHV | EIF3I | 8668 | 30632963|
|MHV | NMT1 | 4836 | 30632963|
|MHV | CHMP4B | 128866 | 30632963|
|MHV | EIF3F | 8665 | 30632963|
|MHV | GBF1 | 8729 | 30632963|
|MHV | RRM2 | 6241 | 30632963|
|MHV | KIF11 | 3832 | 30632963|
|MHV | PSMD1 | 5707 | 30632963|
|MHV | SRP54 | 6729 | 30632963|
|MHV | NUDCD1 | 84955 | 30632963|
|MHV | NACA | 4666 | 30632963|
|MHV | SNX9 | 51429 | 30632963|
|MHV | BTF3 | 689 | 30632963|
|MHV | SCFD1 | 23256 | 30632963|
|MHV | PSMC2 | 5701 | 30632963|
|MHV | TFEB | 7942 | 30632963|
|MHV | TWF2 | 11344 | 30632963|
|MHV | YKT6 | 10652 | 30632963|
|MHV | KPNB1 | 3837 | 30632963|
|MHV | STX5 | 6811 | 30632963|
|MHV | STAT5A | 6776 | 30632963|
|MHV | RSL24D1 | 51187 | 30632963|
|MHV | ACBD5 | 91452 | 30632963|
|SARS | PHB | 5245 | STRING | Viruses|
|SARS | JUN | 3725 | STRING | Viruses|
|SARS | STAT3 | 6774 | STRING | Viruses|
|SARS | PPP1CA | 5499 | STRING | Viruses|
|SARS | SPECC1 | 92521 | STRING | Viruses|
|SARS | FGL2 | 10875 | STRING | Viruses|
|SARS | XPO1 | 7514 | STRING | Viruses|
|HCoV-229E | TGFB1 | 7040 | STRING | Viruses|
|HCoV-229E | FGL2 | 10875 | STRING | Viruses|
|MHV | SMAD3 | 4088 | STRING | Viruses|

| Coronavirus | GenBank ID | Identity% | Protein |
| --- | --- | --- | --- |
| 2019-nCoV[Wuhan-Hu-1] | QHD43415.1 | 100 | ORF1ab |
| 2019-nCoV[HKU-SZ-002a] | QHN73794.1 | 100 | ORF1ab |
| 2019-nCoV[HKU-SZ-005b] | QHN73809.1 | 99.99 | ORF1ab |
| SARS-CoV[GZ02] | AAS00002.1 | 86.51 | ORF1ab |
| SARS-CoV[Sino1-11] | AAR23243.1 | 86.44 | ORF1ab |
| SARS-CoV[GD01] | AAP51225.1 | 86.35 | ORF1ab |
| SARS-CoV[NS-1] | AAR91584.1 | 86.35 | ORF1ab |
| MERS-CoV[ChinaGD01] | AKJ80135.2 | 48 | ORF1ab |
| MERS-CoV[2014KSA_683] | AIL23987.1 | 47.97 | ORF1ab |
| HCoV-OC43 | AIX09804.1 | 44.93 | ORF1ab |
| HCoV-HKU1 | ABD75567.1 | 44.69 | ORF1ab |
| MHV | YP_209229.2 | 44.64 | ORF1ab |
| HCoV-NL63 | AVA26872.1 | 40.05 | ORF1ab |
| HCoV-229E | QEG03783.1 | 39.95 | ORF1ab |
| IBV | ATJ01091.1 | 27.86 | ORF1ab |
| 2019-nCoV[Wuhan-Hu-1] | QHD43416.1 | 100 | Spike |
| 2019-nCoV[HKU-SZ-005b] | QHN73810.1 | 100 | Spike |
| 2019-nCoV[HKU-SZ-002a] | QHN73795.1 | 100 | Spike |
| SARS-CoV[GZ02] | AAS00003.1 | 77.62 | Spike |
| SARS-CoV[GD01] | AAP51227.1 | 77.54 | Spike |
| SARS-CoV[Sino1-11] | AAR23250.1 | 77.46 | Spike |
| SARS-CoV[NS-1] | AAR91586.1 | 77.38 | Spike |
| MHV | YP_209233.1 | 32.12 | Spike |
| MERS-CoV[ChinaGD01] | AKJ80137.2 | 31.93 | Spike |
| MERS-CoV[2014KSA_683] | AID55073.1 | 31.93 | Spike |
| HCoV-OC43 | AIX09807.1 | 30.84 | Spike |
| HCoV-HKU1 | ABD75561.1 | 30.17 | Spike |
| HCoV-229E | QEG03785.1 | 27.37 | Spike |
| IBV | ATJ01093.1 | 27.14 | Spike |
| HCoV-NL63 | AVA26873.1 | 26.89 | Spike |
| 2019-nCoV[Wuhan-Hu-1] | QHD43418.1 | 100 | Envelope Protein |
| 2019-nCoV[HKU-SZ-002a] | QHN73797.1 | 100 | Envelope Protein |
| 2019-nCoV[HKU-SZ-005b] | QHN73812.1 | 100 | Envelope Protein |
| SARS-CoV[NS-1] | AAR91589.1 | 96 | Envelope Protein |
| SARS-CoV[GZ02] | AAS00006.1 | 96 | Envelope Protein |
| SARS-CoV[GD01] | AAP51230.1 | 94.67 | Envelope Protein |
| SARS-CoV[Sino1-11] | AAR23247.1 | 94.59 | Envelope Protein |
| MERS-CoV[2014KSA_683] | AIL23994.1 | 36 | Envelope Protein |
| MERS-CoV[ChinaGD01] | AKJ80142.1 | 36 | Envelope Protein |
| HCoV-HKU1 | ABD75563.1 | 29.33 | Envelope Protein |
| HCoV-229E | QEG03787.1 | 27.14 | Envelope Protein |
| MHV | YP_209236.1 | 21.33 | Envelope Protein |
| IBV | ATJ01096.1 | 20.27 | Envelope Protein |
| HCoV-OC43 | AIX09809.1 | 18.67 | Envelope Protein |
| HCoV-NL63 | AVA26875.1 | 17.14 | Envelope Protein |
| 2019-nCoV[Wuhan-Hu-1] | QHD43419.1 | 100 | Membrane |
| 2019-nCoV[HKU-SZ-002a] | QHN73798.1 | 100 | Membrane |
| 2019-nCoV[HKU-SZ-005b] | QHN73813.1 | 100 | Membrane |
| SARS-CoV[GD01] | AAP51231.1 | 89.59 | Membrane |
| SARS-CoV[GZ02] | AAS00007.1 | 89.59 | Membrane |
| SARS-CoV[NS-1] | AAR91590.1 | 89.59 | Membrane |
| SARS-CoV[Sino1-11] | AAR23248.1 | 89.14 | Membrane |
| MERS-CoV[2014KSA_683] | AIL23995.1 | 39.27 | Membrane |
| MERS-CoV[ChinaGD01] | AKJ80143.1 | 39.27 | Membrane |
| HCoV-OC43 | AIX09810.1 | 38.29 | Membrane |
| MHV | YP_209237.1 | 37.1 | Membrane |
| HCoV-HKU1 | ABD75564.1 | 35.29 | Membrane |
| IBV | ATJ01097.1 | 30.56 | Membrane |
| HCoV-229E | QEG03788.1 | 28.77 | Membrane |
| HCoV-NL63 | AVA26876.1 | 27.73 | Membrane |
| 2019-nCoV[Wuhan-Hu-1] | QHD43423.2 | 100 | Nucleocapsid |
| 2019-nCoV[HKU-SZ-002a] | QHN73802.1 | 100 | Nucleocapsid |
| 2019-nCoV[HKU-SZ-005b] | QHN73817.1 | 100 | Nucleocapsid |
| SARS-CoV[GD01] | AAP51234.1 | 89.74 | Nucleocapsid |
| SARS-CoV[GZ02] | AAS00011.1 | 89.74 | Nucleocapsid |
| SARS-CoV[Sino1-11] | AAR23249.1 | 89.74 | Nucleocapsid |
| SARS-CoV[NS-1] | AAR91593.1 | 89.74 | Nucleocapsid |
| MERS-CoV[2014KSA_683] | AIL23996.1 | 48.85 | Nucleocapsid |
| MERS-CoV[ChinaGD01] | AKJ80144.1 | 48.85 | Nucleocapsid |
| MHV | YP_209238.1 | 35.31 | Nucleocapsid |
| HCoV-HKU1 | ABD75565.1 | 35.22 | Nucleocapsid |
| HCoV-OC43 | AIX09811.1 | 34.79 | Nucleocapsid |
| IBV | ATJ01100.1 | 29.46 | Nucleocapsid |
| HCoV-NL63 | AVA26877.1 | 28.03 | Nucleocapsid |
| HCoV-229E | QEG03789.1 | 27.35 | Nucleocapsid |

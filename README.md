# M-Cypher: A GQL Supporting Motifs
This repository contains codes and datasets used in [M-Cypher: A GQL Supporting Motifs].

## Authors 

- Anonymous during paper review

## Dependencies
### Install [neo4j-desktop](https://neo4j.com/developer/neo4j-desktop/)
### Put data into `<neo4j-home>/import`
### Import COVID19 data into neo4j
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

## Datasets
The code takes the **edge list** of the graph. Every row indicates an edge between two nodes separated by a comma. The datasets used in the paper are included in the  `data/` directory.



# Sparql Query Dynamics

A framework for predicting SPARQL Query Dynamics.



# Table of Contents
1. [Installation](#install)
2. [Data](#data)
3. [Queries](#query)
4. [Indexes](#index)
5. [Results](#result)
6. [Predictions](#predict)

<a id='install'></a>
## Requirements
### Gz-sort
Gz-sort sorts gzipped data files. http://kmkeen.com/gz-sort/
```
sudo apt-get install libz-dev make
git clone https://github.com/keenerd/gz-sort; cd gz-sort; make; ./gz-sort -h
```
### virtuoso-opensource-7.2.5

Virtuoso Engine and its SPARQL endpoint http://vos.openlinksw.com/owiki/wiki/VOS

```
sparql-query-dynamics\virtuoso\setup\setup.sh
sparql-query-dynamics\virtuoso\setup\configure.sh
```
---

<a id='data'></a>
## Data

### Source
https://www.wikidata.org/wiki/Wikidata:Database_download#RDF_dumps
https://dumps.wikimedia.org/wikidatawiki/entities/
https://www.mediawiki.org/wiki/Wikibase/Indexing/RDF_Dump_Format#Truthy_statements

### pre-processing
flip (spo2pso, clean)
sort (duplicate remove)
flip (pso2spo)

### processing
cardinality stats
predicate dynamics
predicate multiplicity

---

<a id='query'></a>
## Queries

### Source
### pre-processing
truthy
lang
bgp

### processing
card
stats

---

<a id='index'></a>
## Indexes
### load
### query
---

<a id='result'></a>
## Results
cmp
diff
rdyn

---

<a id='predict'></a>
## Predictions
### Training
### Evaluation
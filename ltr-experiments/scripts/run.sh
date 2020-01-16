#!/bin/bash

#Rscript ./createFolds.R $PWD
Rscript ./train.R compare $PWD 3 NDCG@10 no-normalization 4 NDCG@10 34 "-tolerance 0.1 -round 50" LOVSimple AKTiveRank DWRank CBRBench LOVBenchFull LOVBenchLight
Rscript ./train.R compare $PWD 1 NDCG@10 zscore 4 NDCG@10 34 "-epoch 14 -layer 2 -lr 0.0001 -node 10" LOVSimple AKTiveRank DWRank CBRBench LOVBenchFull LOVBenchLight

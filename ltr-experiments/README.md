# LOVBench Module: LTR Experiments

This module contains R scripts to learn and evaluate ranking models.

## What is inside

- resources: contains a overview of features and configurations (i.e., list of feature ids from LOVBench dataset that should be used) for different models.
- scripts: contains scripts that generates LTR datasets in RankLib/LibSVM format and scripts that issue calls to the RankLib library to train and test models.

## How to use

### Requirements

This module requires [RankLib](https://sourceforge.net/p/lemur/wiki/RankLib%20How%20to%20use/). If the RankLib jar library does not exist in the resource/bin folder, it will automatically be downloaded.

### Execution


To replicate the experiments from the paper, simply run from this directory:

```
./scripts/run.sh
```

If you would like to change the arguments to RankLib (such as LTR algorithm, parameters, feature configurations), simply run similar commands as in the run.sh file with the adjusted arguments. New feature configurations should be added to resources/configurations folder.
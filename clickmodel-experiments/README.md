# LOVBench Module: Click model experiments

This LOVBench module contains the Python and R scripts that are used to learn and evaluate user click models based on the clean LOV search logs.

## Requirements

It is necessary to install the [PyClick](https://github.com/markovi/PyClick) library.

## How to use

The experiments can then be reproduced by running:

```
./scripts/run.sh
```

## What's inside

- scripts/model: the python scripts to learn and evaluate click models based on PyClick
- scripts/relevance: R helper functions to compute evaluation metrics and handle LOV term URIs
- scripts/run_clickmodel_experiments.R: R script that runs the experiments and generates the ground truth output file,
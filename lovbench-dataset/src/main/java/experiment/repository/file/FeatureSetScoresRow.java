package experiment.repository.file;

import experiment.feature.extraction.AbstractFeature;
import experiment.model.Relevance;

import java.util.HashMap;
import java.util.Map;

public class FeatureSetScoresRow {

    Map<AbstractFeature,Double> featureScores = new HashMap<>();

    Relevance relevance;
}

package experiment.repository.file;

import experiment.model.RankingElement;
import experiment.model.query.AbstractQuery;

import java.util.Map;

public class LTRFileRow {

    AbstractQuery query;
    RankingElement element;
    Map<String,Double> featureScores;

}

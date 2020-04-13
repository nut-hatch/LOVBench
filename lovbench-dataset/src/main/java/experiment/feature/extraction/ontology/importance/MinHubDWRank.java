package experiment.feature.extraction.ontology.importance;

import experiment.feature.scoring.graph.HubDWRankScorer;
import experiment.model.Ontology;
import experiment.repository.triplestore.AbstractOntologyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Returns the minimum hub score for terms in an ontology as specified by DWRank.
 */
public class MinHubDWRank extends AbstractOntologyImportanceFeature {

    HubDWRankScorer hubDWRankScorer;

    public static final String FEATURE_NAME = "Min_Hub_O";

    private static final Logger log = LoggerFactory.getLogger( MinHubDWRank.class );

    public MinHubDWRank(AbstractOntologyRepository repository, HubDWRankScorer hubDWRankScorer) {
        super(repository);
        this.hubDWRankScorer = hubDWRankScorer;
    }

    @Override
    public Map<Ontology, Double> computeScores(Set<Ontology> ontologySet) {
        Map<Ontology, Double> scores = new HashMap<>();
        for (Ontology ontology : ontologySet) {
            scores.put(ontology, this.hubDWRankScorer.getMinHubScore(ontology));
        }
        this.scores.putAll(scores);
        return scores;
    }

    @Override
    public String getFeatureName() {
        return MinHubDWRank.FEATURE_NAME;
    }
}

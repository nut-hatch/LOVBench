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
 * Returns the maximum hub score for terms in an ontology as specified by DWRank.
 */
public class MaxHubDWRank extends AbstractOntologyImportanceFeature {

    HubDWRankScorer hubDWRankScorer;

    private static final Logger log = LoggerFactory.getLogger( MaxHubDWRank.class );

    @Override
    public Map<Ontology, Double> computeScores(Set<Ontology> ontologySet) {
        Map<Ontology, Double> scores = new HashMap<>();
        for (Ontology ontology : this.repository.getAllOntologies()) {
            scores.put(ontology, this.hubDWRankScorer.getMaxHubScore(ontology));
        }
        this.setScores(scores);
        return scores;
    }

    public MaxHubDWRank(AbstractOntologyRepository repository, HubDWRankScorer hubDWRankScorer) {
        super(repository);
        this.hubDWRankScorer = hubDWRankScorer;
    }

    @Override
    protected void computeAllScores() {
        for (Ontology ontology : this.repository.getAllOntologies()) {
            this.scores.put(ontology, this.hubDWRankScorer.getMaxHubScore(ontology));
        }
    }

    @Override
    public String getFeatureName() {
        return "Max_Hub_O";
    }
}

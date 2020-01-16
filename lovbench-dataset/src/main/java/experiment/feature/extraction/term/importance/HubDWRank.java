package experiment.feature.extraction.term.importance;

import experiment.feature.scoring.graph.HubDWRankScorer;
import experiment.model.Ontology;
import experiment.model.Term;
import experiment.repository.triplestore.AbstractOntologyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Computes the Hub score of DWRank.
 * It is based on the PageRank score of a reversed ontology graph and zscore normalization.
 *
 */
public class HubDWRank extends AbstractTermImportanceFeature {

    HubDWRankScorer hubDWRankScorer;

    private static final Logger log = LoggerFactory.getLogger( HubDWRank.class );

    public HubDWRank(AbstractOntologyRepository repository, HubDWRankScorer hubDWRankScorer) {
        super(repository);
        this.hubDWRankScorer = hubDWRankScorer;
    }

    @Override
    public Map<Term, Double> computeScores(Set<Term> termSet) {
        Map<Term, Double> scores = new HashMap<>();

        // Since the scores are anyways computed for the complete graph, also add scores for all graph here..
        for (Ontology ontology : this.repository.getAllOntologies()) {
            Map<Term,Double> allOntologyTermScores = this.hubDWRankScorer.getHubScores(ontology);
            scores.putAll(allOntologyTermScores);
        }
        this.setScores(scores);
        return scores;
    }

    @Override
    protected void computeAllScores() {
        // Reversed PageRank and normalization
        for (Ontology ontology : this.repository.getAllOntologies()) {
            Map<Term,Double> allOntologyTermScores = this.hubDWRankScorer.getHubScores(ontology);
            this.scores.putAll(allOntologyTermScores);
        }
    }

    @Override
    public String getFeatureName() {
        return "Hub_T";
    }
}

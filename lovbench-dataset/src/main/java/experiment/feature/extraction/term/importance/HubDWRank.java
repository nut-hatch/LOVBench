package experiment.feature.extraction.term.importance;

import experiment.feature.scoring.graph.HubDWRankScorer;
import experiment.model.Ontology;
import experiment.model.Term;
import experiment.repository.triplestore.AbstractOntologyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Computes the Hub score of DWRank.
 * It is based on the PageRank score of a reversed ontology graph and zscore normalization.
 *
 */
public class HubDWRank extends AbstractTermImportanceFeature {

    HubDWRankScorer hubDWRankScorer;

    public static final String FEATURE_NAME = "Hub_T";

    private static final Logger log = LoggerFactory.getLogger( HubDWRank.class );

    public HubDWRank(AbstractOntologyRepository repository, HubDWRankScorer hubDWRankScorer) {
        super(repository);
        this.hubDWRankScorer = hubDWRankScorer;
    }

    @Override
    public Map<Term, Double> computeScores(Set<Term> termSet, Ontology ontology) {
        Map<Term, Double> scores = new HashMap<>();
        if (ontology == null && !termSet.isEmpty()) {
            ontology = new Ontology(termSet.iterator().next().getOntologyUriOfTerm());
        }
        Map<Term,Double> allOntologyTermScores = this.hubDWRankScorer.getHubScores(ontology);
        for (Term term : termSet) {
            if (allOntologyTermScores.containsKey(term)) {
                scores.put(term, allOntologyTermScores.get(term));
            } else {
                scores.put(term, 0.0);
            }
        }
        this.scores.putAll(scores);
        return scores;
    }

    @Override
    public String getFeatureName() {
        return HubDWRank.FEATURE_NAME;
    }
}

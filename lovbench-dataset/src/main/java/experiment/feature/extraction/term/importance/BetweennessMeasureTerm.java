package experiment.feature.extraction.term.importance;

import experiment.feature.scoring.graph.BetweennessScorer;
import experiment.model.Ontology;
import experiment.model.Term;
import experiment.repository.triplestore.AbstractOntologyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Adopts the betweenness measure for terms.
 */
public class BetweennessMeasureTerm extends AbstractTermImportanceFeature {

    BetweennessScorer betweennessScorer;

    public static final String FEATURE_NAME = "Betweenness_T";

    private static final Logger log = LoggerFactory.getLogger( BetweennessMeasureTerm.class );

    public BetweennessMeasureTerm(AbstractOntologyRepository repository, BetweennessScorer betweennessScorer) {
        super(repository);
        this.betweennessScorer = betweennessScorer;
        if (this.scores == null) {
            this.scores = new HashMap<>();
        }
    }

    @Override
    public Map<Term, Double> computeScores(Set<Term> termSet, Ontology ontology) {
        Map<Term, Double> scores = new HashMap<>();
        for (Term term : termSet) {
            if (ontology == null) {
                ontology = new Ontology(term.getOntologyUriOfTerm());
            }
            double score = this.betweennessScorer.betweenness(term,ontology);
            scores.put(term, score);
        }
        this.scores.putAll(scores);
        return scores;
    }

    @Override
    public String getFeatureName() {
        return BetweennessMeasureTerm.FEATURE_NAME;
    }
}

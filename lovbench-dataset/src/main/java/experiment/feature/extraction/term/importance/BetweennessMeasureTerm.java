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

    private static final Logger log = LoggerFactory.getLogger( BetweennessMeasureTerm.class );

    public BetweennessMeasureTerm(AbstractOntologyRepository repository, BetweennessScorer betweennessScorer) {
        super(repository);
        this.betweennessScorer = betweennessScorer;
    }

    @Override
    public Map<Term, Double> computeScores(Set<Term> termSet) {
        Map<Term, Double> scores = new HashMap<>();
        for (Term term : termSet) {
            log.info(term.getTermUri());
            log.info(term.getOntologyUriOfTerm());
            double score = this.betweennessScorer.betweenness(term,new Ontology(term.getOntologyUriOfTerm()));
            scores.put(term, score);
        }
        this.setScores(scores);
        return scores;
    }

    @Override
    protected void computeAllScores() {
        for (Ontology ontology : this.repository.getAllOntologies()) {
            this.scores.putAll(this.betweennessScorer.allBetweennessScores(ontology));
        }
    }

    @Override
    public String getFeatureName() {
        return "Betweenness_T";
    }
}

package experiment.feature.extraction.term.importance;

import experiment.feature.extraction.term.AbstractTermFeature;
import experiment.model.Term;
import experiment.repository.triplestore.AbstractOntologyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Abstract class for all term importance features (those independent from the query).
 *
 */
public abstract class AbstractTermImportanceFeature extends AbstractTermFeature {

    /**
     * Cache of scores for all ontologies.
     */
    protected Map<Term, Double> scores = null;

    private static final Logger log = LoggerFactory.getLogger( AbstractTermImportanceFeature.class );

    public AbstractTermImportanceFeature(AbstractOntologyRepository repository) {
        super(repository);
    }

    /**
     * Function that computes all scores for a feature in a batch-manner.
     * @deprecated
     */
    protected abstract void computeAllScores();

    public abstract Map<Term, Double> computeScores(Set<Term> termSet);

    /**
     * Gets the score of the feature from cache, or computes all scores if cache is empty in batch.
     *
     * @param term
     * @return double
     */
    public double getScore(Term term) {
        double score = 0.0;

        if (this.scores == null) {
            this.scores = new HashMap<>();
            this.computeAllScores();
        }

        for (Map.Entry<Term, Double> entry : scores.entrySet()) {
            log.debug(entry.getKey().toString() + ": " + entry.getValue());
        }

        if (!(this.scores.isEmpty()) && (this.scores.containsKey(term))) {
            score = this.scores.get(term);
        }
        return score;
    }

    public Map<Term, Double> getScores() {
        return scores;
    }

    public void setScores(Map<Term, Double> scores) {
        this.scores = scores;
    }

}

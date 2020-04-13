package experiment.feature.extraction.term.importance;

import experiment.feature.extraction.term.AbstractTermFeature;
import experiment.model.Ontology;
import experiment.model.Term;
import experiment.repository.triplestore.AbstractOntologyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Abstract class for all term importance features (those independent from the query).
 */
public abstract class AbstractTermImportanceFeature extends AbstractTermFeature {

    /**
     * Cache of scores for all ontologies.
     */
    protected Map<Term, Double> scores = new HashMap<>();

    private static final Logger log = LoggerFactory.getLogger(AbstractTermImportanceFeature.class);

    public AbstractTermImportanceFeature(AbstractOntologyRepository repository) {
        super(repository);
    }

    /**
     * Function that computes all scores for a feature in a batch-manner.
     */
    public void computeAllScores() {
        for (Ontology ontology : this.repository.getAllOntologies()) {
            this.computeScores(this.repository.getAllTerms(ontology), ontology);
        }
    }

    /**
     * Function that computes the scores for a given set of terms.
     *
     * @param termSet
     * @return
     */
    public abstract Map<Term, Double> computeScores(Set<Term> termSet, Ontology ontology);

    public Map<Term, Double> computeScores(Set<Term> termSet) {
        return this.computeScores(termSet, null);
    }

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

package experiment.feature.extraction.ontology.importance;

import experiment.feature.extraction.ontology.AbstractOntologyFeature;
import experiment.model.Ontology;
import experiment.repository.triplestore.AbstractOntologyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * The abstract class for all ontology importance features (those that are independent of the query).
 *
 */
public abstract class AbstractOntologyImportanceFeature extends AbstractOntologyFeature {

    /**
     * Cache of scores for all ontologies.
     */
    protected Map<Ontology, Double> scores = new HashMap<>();

    private static final Logger log = LoggerFactory.getLogger( AbstractOntologyImportanceFeature.class );

    public AbstractOntologyImportanceFeature(AbstractOntologyRepository repository) {
        super(repository);
    }


    public abstract Map<Ontology, Double> computeScores(Set<Ontology> ontologySet);

    /**
     * Function that computes all scores for a feature in a batch-manner.
     */
    public void computeAllScores() {
        this.computeScores(this.repository.getAllOntologies());
    }

    /**
     * Gets the score of the feature from cache, or computes all scores if cache is empty in batch.
     *
     * @param ontology
     * @return double
     */
    public double getScore(Ontology ontology) {
        double score = 0.0;

        if (!(this.scores.isEmpty()) && (this.scores.containsKey(ontology))) {
            score = this.scores.get(ontology);
        }

        return score;
    }

    public Map<Ontology, Double> getScores() {
        return scores;
    }

    public void setScores(Map<Ontology, Double> scores) {
        this.scores = scores;
    }

}

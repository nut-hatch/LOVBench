package experiment.feature.extraction.ontology.relevance;

import experiment.feature.extraction.ontology.AbstractOntologyFeature;
import experiment.model.Ontology;
import experiment.model.query.AbstractQuery;
import experiment.repository.triplestore.AbstractOntologyRepository;

/**
 * The abstract class for all ontology relevance features (those that depend on the query).
 *
 * Can be computed for ontology and for term queries!
 *
 */
public abstract class AbstractOntologyRelevanceFeature extends AbstractOntologyFeature {

    public AbstractOntologyRelevanceFeature(AbstractOntologyRepository repository) {
        super(repository);
    }

    /**
     * Computes the score of an ontology for a query.
     *
     * @param query
     * @param ontology
     * @return double
     */
    abstract public double getScore(AbstractQuery query, Ontology ontology);

}

package experiment.feature.extraction.term.relevance;

import experiment.feature.extraction.term.AbstractTermFeature;
import experiment.model.query.TermQuery;
import experiment.model.Term;
import experiment.repository.triplestore.AbstractOntologyRepository;

/**
 * Abstract class for all term relevance scores (those that depend on the query).
 *
 */
public abstract class AbstractTermRelevanceFeature extends AbstractTermFeature {

    public AbstractTermRelevanceFeature(AbstractOntologyRepository repository) {
        super(repository);
    }

    abstract public double getScore(TermQuery query, Term term);

}

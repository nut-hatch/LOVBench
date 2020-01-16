package experiment.feature.extraction.term;

import experiment.feature.extraction.AbstractFeature;
import experiment.repository.triplestore.AbstractOntologyRepository;

/**
 * Abstract class for all term features.
 *
 */
public abstract class AbstractTermFeature extends AbstractFeature {

    public AbstractTermFeature(AbstractOntologyRepository repository) {
        super(repository);
    }

}

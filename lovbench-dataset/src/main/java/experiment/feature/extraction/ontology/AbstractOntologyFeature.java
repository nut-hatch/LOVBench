package experiment.feature.extraction.ontology;

import experiment.feature.extraction.AbstractFeature;
import experiment.repository.triplestore.AbstractOntologyRepository;

/**
 * The abstract class for all ontology features.
 *
 */
public abstract class AbstractOntologyFeature extends AbstractFeature {

    public AbstractOntologyFeature(AbstractOntologyRepository repository) {
        super(repository);
    }

}

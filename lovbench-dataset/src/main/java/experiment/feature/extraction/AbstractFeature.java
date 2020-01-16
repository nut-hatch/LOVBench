package experiment.feature.extraction;

import experiment.repository.triplestore.AbstractOntologyRepository;

import java.util.Objects;

/**
 * Abstract class for all features.
 *
 */
public abstract class AbstractFeature {

    /**
     * Repository for access to ontology collection.
     */
    protected AbstractOntologyRepository repository;

    /**
     * Function that returns the name of the feature.
     *
     * @return String
     */
    abstract public String getFeatureName();

    public AbstractFeature(AbstractOntologyRepository repository) {
        this.repository = repository;
    }

    public AbstractOntologyRepository getRepository() {
        return repository;
    }

    public void setRepository(AbstractOntologyRepository repository) {
        this.repository = repository;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractFeature that = (AbstractFeature) o;
        return Objects.equals(getFeatureName(), that.getFeatureName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getFeatureName());
    }
}

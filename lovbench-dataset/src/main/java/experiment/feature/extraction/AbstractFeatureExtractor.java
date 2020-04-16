package experiment.feature.extraction;

import experiment.feature.extraction.ontology.AbstractOntologyFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract class for feature extractors.
 *
 */
public abstract class AbstractFeatureExtractor {

    /**
     * The ontology features that shall be extracted.
     */
    List<AbstractOntologyFeature> ontologyFeatures = new ArrayList<>();

    private static final Logger log = LoggerFactory.getLogger( AbstractFeatureExtractor.class );

    public List<AbstractOntologyFeature> getOntologyFeatures() {
        return ontologyFeatures;
    }

    public void setOntologyFeatures(List<AbstractOntologyFeature> ontologyFeatures) {
        this.ontologyFeatures = ontologyFeatures;
    }

    public void addFeature(AbstractFeature feature) {
        if (feature instanceof AbstractOntologyFeature) {
            this.addOntologyFeature((AbstractOntologyFeature)feature);
        } else {
            log.error("Feature type not supported for this extractor");
        }
    }

    public void addOntologyFeature(AbstractOntologyFeature f) {
        this.getOntologyFeatures().add(f);
    }

    public void addOntologyFeatures(AbstractOntologyFeature... features) {
        for (AbstractOntologyFeature feature : features) {
            this.getOntologyFeatures().add(feature);
        }
    }
}

package experiment.feature.extraction;

import com.opencsv.CSVWriter;
import experiment.feature.extraction.ontology.AbstractOntologyFeature;
import experiment.model.FeatureScore;
import experiment.model.Term;
import experiment.model.query.TermQuery;
import experiment.repository.file.ExperimentConfiguration;
import experiment.repository.file.FeatureScores;
import experiment.repository.file.FileUtil;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Abstract class for feature extractors.
 *
 */
public abstract class AbstractFeatureExtractor {

    /**
     * The ontology features that shall be extracted.
     */
    List<AbstractOntologyFeature> ontologyFeatures = new ArrayList<>();

    public List<AbstractOntologyFeature> getOntologyFeatures() {
        return ontologyFeatures;
    }

    public void setOntologyFeatures(List<AbstractOntologyFeature> ontologyFeatures) {
        this.ontologyFeatures = ontologyFeatures;
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

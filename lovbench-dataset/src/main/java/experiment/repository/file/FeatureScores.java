package experiment.repository.file;

import com.opencsv.CSVWriter;
import experiment.feature.extraction.AbstractFeature;
import experiment.feature.extraction.ontology.AbstractOntologyFeature;
import experiment.feature.extraction.term.AbstractTermFeature;
import experiment.model.FeatureScore;
import experiment.model.query.enums.ExtractionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

/**
 * Collects the scores for a term feature.
 *
 * @deprecated
 */
public class FeatureScores<Q,R> {

    ExtractionType extractionType;

    /**
     * Name of the feature to collect the score for.
     */
    AbstractFeature feature;

    /**
     * The scores.
     */
    List<FeatureScore<Q,R>> featureScores = new ArrayList<>();

    private static final Logger log = LoggerFactory.getLogger( FeatureScores.class );

    public FeatureScores(AbstractFeature feature, ExtractionType extractionType) {
        this.feature = feature;
        this.extractionType = extractionType;
    }

    /**
     * Writes the feature scores to file.
     */
    public void writeCsv() {
        String filename = this.getFeatureFileName();
        File file = new File(filename);
        FileUtil.createFolderIfNotExists(file);
        try (
                Writer writer = Files.newBufferedWriter(Paths.get(filename), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                CSVWriter csvWriter = FileUtil.getCSVWriter(writer);
        ) {
            for (FeatureScore score : this.featureScores) {
                csvWriter.writeNext(new String[]{score.getQuery().toString(),score.getRankingelement().toString(), String.valueOf(score.getScore())});
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates the filename for the feature scores.
     *
     * @return String
     */
    private String getFeatureFileName() {
        String type = "";
        if (this.extractionType.equals(ExtractionType.TERM)) {
            type = "term";
        } else if (this.extractionType.equals(ExtractionType.ONTOLOGY)) {
            type = "ontology";
        }
        return ExperimentConfiguration.getInstance().getResultDir() + type + "/" + this.feature.getFeatureName() + ".csv";
    }

    public List<FeatureScore<Q, R>> getFeatureScores() {
        return featureScores;
    }

    public void setFeatureScores(List<FeatureScore<Q, R>> featureScores) {
        this.featureScores = featureScores;
    }
}

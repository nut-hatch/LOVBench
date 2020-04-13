package experiment.repository.file;

import com.opencsv.CSVWriter;
import experiment.configuration.ExperimentConfiguration;
import experiment.feature.extraction.AbstractFeature;
import experiment.model.query.enums.ExtractionType;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;

/**
 * Collects the computed feature scores of query-rankingelement pairs during feature extraction and writes scores to file.
 *
 * @param <Q> Query type
 * @param <R> Ranking element type
 */
public class FeatureSetScores<Q,R> {

    /**
     * The extraction type of the feature extraction run.
     */
    ExtractionType extractionType;

    /**
     * The list of features that are collected. For ordered output.
     */
    List<AbstractFeature> featureList = new ArrayList<>();

    /**
     * The scores.
     */
    Map<Pair<Q,R>, Map<AbstractFeature,Double>> featureScores = new HashMap<>();

    public FeatureSetScores(ExtractionType extractionType) {
        this.extractionType = extractionType;
    }

    /**
     * Adds a score for a query element pair and a feature.
     *
     * @param queryElementPair
     * @param feature
     * @param score
     */
    public void addScore(Pair<Q,R> queryElementPair, AbstractFeature feature, double score) {
        if (!this.featureScores.containsKey(queryElementPair)) {
            this.featureScores.put(queryElementPair, new HashMap<>());
        }
        if (!this.featureList.contains(feature)) {
            this.featureList.add(feature);
        }
        this.featureScores.get(queryElementPair).put(feature,score);
    }

    /**
     * Writes the feature scores to file.
     */
    public void writeCsv(AbstractFeature feature) {
        String filename = this.getFeatureFileName(feature);
        File file = new File(filename);
        FileUtil.createFolderIfNotExists(file);
        try (
                Writer writer = Files.newBufferedWriter(Paths.get(filename), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                CSVWriter csvWriter = new CSVWriter(writer,
                        CSVWriter.DEFAULT_SEPARATOR,
                        CSVWriter.DEFAULT_QUOTE_CHARACTER,
                        CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                        CSVWriter.DEFAULT_LINE_END)
        ) {
            csvWriter.writeNext(new String[]{"Query", "RankingElement", "Score"});
            ArrayList<List<String>> rows = new ArrayList<>();
            for (Map.Entry<Pair<Q,R>, Map<AbstractFeature,Double>> entry : this.featureScores.entrySet()) {
                List<String> row = new ArrayList<>();
                Q query = entry.getKey().getLeft();
                R rankingElement = entry.getKey().getRight();
                double score = entry.getValue().get(feature);
                row.add(query.toString());
                row.add(rankingElement.toString());
                row.add(String.valueOf(score));
                rows.add(row);
            }
            rows.sort(Comparator.comparing(l -> l.get(0)));
            for (List<String> row : rows) {
                csvWriter.writeNext(row.toArray(new String[0]));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Write all scores to file.
     */
    public void writeCsv() {
        String filename = "";
        if (this.extractionType.equals(ExtractionType.TERM)) {
            filename = ExperimentConfiguration.getInstance().getAllScoresTermFile();
        } else if (this.extractionType.equals(ExtractionType.ONTOLOGY)) {
            filename = ExperimentConfiguration.getInstance().getAllScoresOntologyFile();
        }
        File file = new File(filename);
        FileUtil.createFolderIfNotExists(file);
        try (
                Writer writer = Files.newBufferedWriter(Paths.get(filename), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                CSVWriter csvWriter = new CSVWriter(writer,
                        CSVWriter.DEFAULT_SEPARATOR,
                        CSVWriter.DEFAULT_QUOTE_CHARACTER,
                        CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                        CSVWriter.DEFAULT_LINE_END)
        ) {
            // Write title
            ArrayList<String> title = new ArrayList<>();
            title.add("Query");
            title.add("RankingElement");
            for (AbstractFeature feature : this.featureList) {
                title.add(feature.getFeatureName());
            }
            csvWriter.writeNext(title.toArray(new String[0]));

            // Write rows
            ArrayList<List<String>> rows = new ArrayList<>();

            for (Map.Entry<Pair<Q,R>, Map<AbstractFeature,Double>> entry : this.featureScores.entrySet()) {
                ArrayList<String> row = new ArrayList<>();
                Q query = entry.getKey().getLeft();
                R rankingElement = entry.getKey().getRight();
                row.add(query.toString());
                row.add(rankingElement.toString());
                for (AbstractFeature feature : this.featureList) {
                    double score = entry.getValue().get(feature);
                    row.add(String.valueOf(score));
                }
                rows.add(row);
            }
            rows.sort(Comparator.comparing(l -> l.get(0)));
            for (List<String> row : rows) {
                csvWriter.writeNext(row.toArray(new String[0]));
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
    private String getFeatureFileName(AbstractFeature feature) {
        String type = "";
        if (this.extractionType.equals(ExtractionType.TERM)) {
            type = "term";
        } else if (this.extractionType.equals(ExtractionType.ONTOLOGY)) {
            type = "ontology";
        }
        return ExperimentConfiguration.getInstance().getResultDir() + type + "/" + feature.getFeatureName() + ".csv";
    }

    public Map<Pair<Q, R>, Map<AbstractFeature, Double>> getFeatureScores() {
        return featureScores;
    }

    public void setFeatureScores(Map<Pair<Q, R>, Map<AbstractFeature, Double>> featureScores) {
        this.featureScores = featureScores;
    }
}

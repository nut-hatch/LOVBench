package experiment.feature.extraction;

import com.google.common.collect.Table;
import experiment.feature.extraction.ontology.AbstractOntologyFeature;
import experiment.feature.extraction.ontology.importance.AbstractOntologyImportanceFeature;
import experiment.feature.extraction.ontology.relevance.AbstractOntologyRelevanceFeature;
import experiment.model.FeatureScore;
import experiment.model.Ontology;
import experiment.model.Relevance;
import experiment.model.Term;
import experiment.model.query.OntologyQuery;
import experiment.model.query.TermQuery;
import experiment.model.query.enums.ExtractionType;
import experiment.repository.file.FeatureScores;
import experiment.repository.file.FeatureSetScores;
import experiment.repository.file.GroundTruthOntologyRanking;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.Set;

/**
 * Class that extracts features of ontologies.
 *
 */
public class FeatureExtractorOntologies extends AbstractFeatureExtractor {

    private static final Logger log = LoggerFactory.getLogger( FeatureExtractorOntologies.class );

    /**
     * Extracts all features currently contained in the features lists for a given ontology ground truth.
     *
     * @param groundTruthOntologyRanking
     */
    public void extract(GroundTruthOntologyRanking groundTruthOntologyRanking) {

        if (this.ontologyFeatures.size() > 0) {
            FeatureSetScores featureSetScores = new FeatureSetScores(ExtractionType.ONTOLOGY);

            for (AbstractOntologyFeature ontologyFeature : this.ontologyFeatures) {
                log.info(String.format("############ Extracting Scores For %s ############", ontologyFeature.getFeatureName()));
                Iterator tableIterator = groundTruthOntologyRanking.getGroundTruthTable().cellSet().iterator();

                if (ontologyFeature instanceof AbstractOntologyImportanceFeature) {
                    Set<Ontology> ontologySet = groundTruthOntologyRanking.getGroundTruthTable().columnKeySet();
                    ((AbstractOntologyImportanceFeature) ontologyFeature).computeScores(ontologySet);

                    while (tableIterator.hasNext()) {
                        Table.Cell<OntologyQuery, Ontology, Relevance> groundTruthRow = (Table.Cell) tableIterator.next();
                        OntologyQuery query = groundTruthRow.getRowKey();
                        Ontology ontology = groundTruthRow.getColumnKey();
                        double score = ((AbstractOntologyImportanceFeature) ontologyFeature).getScore(ontology);
                        featureSetScores.addScore(Pair.of(query,ontology),ontologyFeature,score);
                    }

                } else if (ontologyFeature instanceof AbstractOntologyRelevanceFeature) {

                    while (tableIterator.hasNext()) {
                        Table.Cell<OntologyQuery, Ontology, Relevance> groundTruthRow = (Table.Cell) tableIterator.next();
                        OntologyQuery query = groundTruthRow.getRowKey();
                        Ontology ontology = groundTruthRow.getColumnKey();
                        log.info(String.format("Computing score for feature %s and query %s and ontology %s", ontologyFeature.getFeatureName(), query, ontology));
                        double score = ((AbstractOntologyRelevanceFeature) ontologyFeature).getScore(query, ontology);
                        log.info(String.format("Computing score for feature %s and query %s and ontology %s, resulted in %s", ontologyFeature.getFeatureName(), query, ontology, score));
                        featureSetScores.addScore(Pair.of(query, ontology), ontologyFeature, score);
                    }
                }
                featureSetScores.writeCsv(ontologyFeature);
            }
            featureSetScores.writeCsv();
        }
    }
}

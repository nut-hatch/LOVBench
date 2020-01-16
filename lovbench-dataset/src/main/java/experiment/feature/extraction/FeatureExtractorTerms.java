package experiment.feature.extraction;

import com.google.common.collect.Table;
import experiment.feature.extraction.ontology.AbstractOntologyFeature;
import experiment.feature.extraction.ontology.importance.AbstractOntologyImportanceFeature;
import experiment.feature.extraction.ontology.relevance.AbstractOntologyRelevanceFeature;
import experiment.feature.extraction.term.AbstractTermFeature;
import experiment.feature.extraction.term.importance.AbstractTermImportanceFeature;
import experiment.feature.extraction.term.relevance.AbstractTermRelevanceFeature;
import experiment.model.*;
import experiment.model.query.TermQuery;
import experiment.model.query.enums.ExtractionType;
import experiment.repository.file.FeatureScores;
import experiment.repository.file.FeatureSetScores;
import experiment.repository.file.GroundTruthTermRanking;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Class to extract features for terms.
 * Ontology features for terms can also be extracted (i.e. the term receives the score for the ontology in which it is defined).
 *
 */
public class FeatureExtractorTerms extends AbstractFeatureExtractor {

    /**
     * The term features that shall be extracted.
     */
    List<AbstractTermFeature> termFeatures = new ArrayList<>();

    private static final Logger log = LoggerFactory.getLogger( FeatureExtractorTerms.class );

    /**
     * Extracts all features currently contained in the features lists for a given term ground truth.
     *
     * @param groundTruthTermRanking
     */
    public void extract(GroundTruthTermRanking groundTruthTermRanking) {

        Map<AbstractFeature, FeatureScores<TermQuery,Term>> allScores = new HashMap<>();
        FeatureSetScores<TermQuery,Term> featureSetScores = new FeatureSetScores<>(ExtractionType.TERM);

        if (this.termFeatures.size() > 0) {
            for (AbstractTermFeature termFeature : this.termFeatures) {
                log.info(String.format("############ Extracting Scores For %s ############", termFeature.getFeatureName()));
                Iterator tableIterator = groundTruthTermRanking.getGroundTruthTable().cellSet().iterator();

                if (termFeature instanceof AbstractTermImportanceFeature) {
                    Set<Term> termSet = groundTruthTermRanking.getGroundTruthTable().columnKeySet();
                    ((AbstractTermImportanceFeature) termFeature).computeScores(termSet);
                    int i = 1;
                    while (tableIterator.hasNext()) {
                        Table.Cell<TermQuery, Term, Relevance> groundTruthRow = (Table.Cell) tableIterator.next();
                        TermQuery query = groundTruthRow.getRowKey();
                        Term term = groundTruthRow.getColumnKey();
                        log.info("Query: " + i);
                        i++;
                        double score = ((AbstractTermImportanceFeature) termFeature).getScore(term);
                        featureSetScores.addScore(Pair.of(query,term),termFeature,score);
                    }
                } else if (termFeature instanceof AbstractTermRelevanceFeature) {
                    int i = 1;
                    while (tableIterator.hasNext()) {
                        Table.Cell<TermQuery, Term, Relevance> groundTruthRow = (Table.Cell) tableIterator.next();
                        TermQuery query = groundTruthRow.getRowKey();
                        Term term = groundTruthRow.getColumnKey();

                        log.info("Query: " + i);
                        i++;
                        double score = ((AbstractTermRelevanceFeature) termFeature).getScore(query, term);
                        featureSetScores.addScore(Pair.of(query,term),termFeature,score);
                    }
                }
                featureSetScores.writeCsv(termFeature);
            }
        }

        if (this.ontologyFeatures.size() > 0) {

            for (AbstractOntologyFeature ontologyFeature : this.ontologyFeatures) {
                log.info(String.format("############ Extracting Scores For %s ############", ontologyFeature.getFeatureName()));

                Iterator tableIterator = groundTruthTermRanking.getGroundTruthTable().cellSet().iterator();

                if (ontologyFeature instanceof AbstractOntologyImportanceFeature) {
                    Set<Term> termSet = groundTruthTermRanking.getGroundTruthTable().columnKeySet();
                    Set<Ontology> ontologySet = new HashSet<>();
                    for (Term term : termSet) {
                        Ontology ontology = new Ontology(term.getOntologyUriOfTerm());
                        if (!ontologySet.contains(ontology)) {
                            ontologySet.add(ontology);
                        }
                    }
                    ((AbstractOntologyImportanceFeature) ontologyFeature).computeScores(ontologySet);

                    int i = 1;
                    while (tableIterator.hasNext()) {
                        Table.Cell<TermQuery, Term, Relevance> groundTruthRow = (Table.Cell) tableIterator.next();
                        TermQuery query = groundTruthRow.getRowKey();
                        Term term = groundTruthRow.getColumnKey();
                        Ontology ontology = new Ontology(term.getOntologyUriOfTerm());

                        log.info("Query: " + i);
                        i++;
                        double score = ((AbstractOntologyImportanceFeature) ontologyFeature).getScore(ontology);
                        featureSetScores.addScore(Pair.of(query,term),ontologyFeature,score);
                    }

                } else if (ontologyFeature instanceof AbstractOntologyRelevanceFeature) {
                    int i = 1;
                    while (tableIterator.hasNext()) {
                        Table.Cell<TermQuery, Term, Relevance> groundTruthRow = (Table.Cell) tableIterator.next();
                        TermQuery query = groundTruthRow.getRowKey();
                        Term term = groundTruthRow.getColumnKey();
                        Ontology ontology = new Ontology(term.getOntologyUriOfTerm());

                        log.info("Query: " + i);
                        i++;
                        double score = ((AbstractOntologyRelevanceFeature) ontologyFeature).getScore(query, ontology);
                        featureSetScores.addScore(Pair.of(query, term), ontologyFeature, score);
                    }
                }
                featureSetScores.writeCsv(ontologyFeature);
            }
        }
        featureSetScores.writeCsv();
    }

    public void addTermFeature(AbstractTermFeature f) {
        this.getTermFeatures().add(f);
    }

    public List<AbstractTermFeature> getTermFeatures() {
        return termFeatures;
    }

    public void setTermFeatures(List<AbstractTermFeature> termFeatures) {
        this.termFeatures = termFeatures;
    }


    public void addTermFeatures(AbstractTermFeature... features) {
        for (AbstractTermFeature feature : features) {
            this.termFeatures.add(feature);
        }
    }
}

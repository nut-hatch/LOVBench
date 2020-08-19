package export.elasticsearch.feature;

import experiment.TestUtil;
import experiment.configuration.ExperimentConfiguration;
import experiment.feature.FeatureFactory;
import experiment.feature.extraction.AbstractFeature;
import experiment.feature.extraction.FeatureExtractorTerms;
import experiment.feature.extraction.ontology.importance.PageRankVoaf;
import experiment.feature.extraction.ontology.relevance.BetweennessMeasure;
import experiment.feature.extraction.term.importance.BetweennessMeasureTerm;
import experiment.model.Ontology;
import experiment.model.Relevance;
import experiment.model.Term;
import experiment.model.query.TermQuery;
import experiment.repository.file.FeatureSetScores;
import experiment.repository.file.GroundTruthTermRanking;
import export.elasticsearch.index.ElasticSearchIndex;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

public class FeatureRequestHelperTest {

    String featureSetFile = getClass().getClassLoader().getResource("LOVBenchLight.txt").getFile().toString();
    String groundTruthFile = getClass().getClassLoader().getResource("LOVBench_GroundTruth.csv").getFile().toString();

    private static final Logger log = LoggerFactory.getLogger( FeatureRequestHelperTest.class );


    @Before
    public void setUp() throws Exception {
        new TestUtil().setNqFileConfiguration();
    }


    @Test
    public void readFeatureSetFile() throws FileNotFoundException {
        List<AbstractFeature> features = FeatureRequestHelper.readFeatureSetFile(featureSetFile);
        for (AbstractFeature feature : features) {
            log.debug(feature.getFeatureName());
        }
        assertEquals(12, features.size());
    }

    @Test
    public void getFeatureMapping() throws FileNotFoundException {
        JSONObject mapping = FeatureRequestHelper.getFeatureMapping(FeatureRequestHelper.readFeatureSetFile(featureSetFile));
        log.debug(mapping.toString());
        assertNotNull(mapping);
    }


    @Test
    public void getFeatureSetSpecification() throws FileNotFoundException {
        JSONObject featureSetSpecification = FeatureRequestHelper.getFeatureSetSpecification(FeatureRequestHelper.readFeatureSetFile(featureSetFile));
        log.debug(featureSetSpecification.toString());
        assertNotNull(featureSetSpecification);
    }

    @Test
    public void createDocUpdatesFromImportanceScores() {
        FeatureExtractorTerms fe = new FeatureExtractorTerms();
        FeatureFactory factory = new FeatureFactory();
        fe.addFeature(factory.getFeature(PageRankVoaf.FEATURE_NAME));
//        fe.addFeature(factory.getFeature(BetweennessMeasureTerm.FEATURE_NAME));
        FeatureSetScores<TermQuery, Term> featureSetScores = fe.extractImportance(ExperimentConfiguration.getInstance().getRepository().getAllTerms());
        Map<String, JSONObject> docUpdates = FeatureRequestHelper.createDocUpdatesFromImportanceScores(featureSetScores);
        log.debug(featureSetScores.getFeatureScores().size()+"");
        log.debug(docUpdates.size()+"");
        assertEquals(featureSetScores.getFeatureScores().size(), docUpdates.size());
        for (Map.Entry<String, JSONObject> docUpdate : docUpdates.entrySet()) {
            log.debug("id: " + docUpdate.getKey());
            log.debug("update: " + docUpdate.getValue());
            break;
        }
    }

    @Test
    public void createFeatureLoggingSLTRQuery() {
        GroundTruthTermRanking groundTruthTermRanking = GroundTruthTermRanking.parse(this.groundTruthFile);
        for(Map.Entry<TermQuery, Map<Term, Relevance>> groundTruthRowMapEntry : groundTruthTermRanking.getGroundTruthTable().rowMap().entrySet()) {
            TermQuery query = groundTruthRowMapEntry.getKey();
            Set<Term> terms = groundTruthRowMapEntry.getValue().keySet();
            JSONObject sltrQuery = FeatureRequestHelper.createFeatureLoggingSLTRQuery(query, terms, FilenameUtils.getBaseName(this.featureSetFile));
            System.out.println(sltrQuery.toString());
            break;
        }
    }
}
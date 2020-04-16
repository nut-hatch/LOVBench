package export.elasticsearch.index;

import experiment.TestUtil;
import experiment.configuration.ExperimentConfiguration;
import experiment.feature.FeatureFactory;
import experiment.feature.extraction.AbstractFeature;
import experiment.feature.extraction.FeatureExtractorTerms;
import experiment.feature.extraction.ontology.importance.PageRankVoaf;
import experiment.feature.extraction.term.importance.BetweennessMeasureTerm;
import experiment.model.Term;
import experiment.model.query.TermQuery;
import experiment.repository.file.FeatureSetScores;
import export.elasticsearch.feature.FeatureRequestHelper;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class ElasticSearchIndexTest {

    String featureSetName = "LOVBenchLight";
    String featureSetFile = getClass().getClassLoader().getResource("LOVBenchLight.txt").getFile().toString();
    ElasticSearchIndex termIndex = new ElasticSearchIndex("elasticsearch", "localhost", "9300", "terms", "term");

    private static final Logger log = LoggerFactory.getLogger( ElasticSearchIndexTest.class );


    @Before
    public void setUp() throws Exception {
        new TestUtil().setNqFileConfiguration();
    }

    @Test
    public void exists() {
        if (ExperimentConfiguration.getInstance().isMakeElasticSearchTests()) {
            assertTrue(termIndex.exists());
        }
    }

    @Test
    public void get() {
        if (ExperimentConfiguration.getInstance().isMakeElasticSearchTests()) {
            JSONObject response = termIndex.get("http://www.w3.org/2006/03/test-description#TestCase");
            log.debug(response.toString());
            assertEquals(response.getString("localName"), "TestCase");
            assertEquals(response.getString("termType"), "class");
            log.debug(termIndex.get("http://www.semanticdesktop.org/ontologies/2007/08/15/nao#hasTag").get(BetweennessMeasureTerm.FEATURE_NAME) + "");
        }
    }

    @Test
    public void bulkUpdate() {
        if (ExperimentConfiguration.getInstance().isMakeElasticSearchTests()) {
            FeatureExtractorTerms fe = new FeatureExtractorTerms();
            FeatureFactory factory = new FeatureFactory();
            fe.addFeature(factory.getFeature(PageRankVoaf.FEATURE_NAME));
            fe.addFeature(factory.getFeature(BetweennessMeasureTerm.FEATURE_NAME));
            FeatureSetScores<TermQuery, Term> featureSetScores = fe.extractImportance(ExperimentConfiguration.getInstance().getRepository().getAllTerms());
            Map<String, JSONObject> docUpdates = FeatureRequestHelper.createDocUpdatesFromImportanceScores(featureSetScores);
            log.debug(featureSetScores.getFeatureScores().size() + "");
            log.debug(docUpdates.size() + "");
            assertEquals(featureSetScores.getFeatureScores().size(), docUpdates.size());
            for (Map.Entry<String, JSONObject> docUpdate : docUpdates.entrySet()) {
                log.debug("id: " + docUpdate.getKey());
                log.debug("update: " + docUpdate.getValue());
                break;
            }
            boolean allSuccessful = termIndex.bulkUpdate(docUpdates);
//        assertTrue(allSuccessful);
        }
    }

    @Test
    public void put() throws FileNotFoundException {
        if (ExperimentConfiguration.getInstance().isMakeElasticSearchTests()) {
            List<AbstractFeature> featureList = FeatureRequestHelper.readFeatureSetFile(this.featureSetFile);
            JSONObject featureConfigurationMapping = FeatureRequestHelper.getFeatureMapping(featureList);
            termIndex.put(featureConfigurationMapping.toString());
        }
    }
}
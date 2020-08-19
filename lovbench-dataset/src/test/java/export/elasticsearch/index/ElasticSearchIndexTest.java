package export.elasticsearch.index;

import experiment.TestUtil;
import experiment.configuration.ExperimentConfiguration;
import experiment.feature.FeatureFactory;
import experiment.feature.extraction.AbstractFeature;
import experiment.feature.extraction.FeatureExtractorTerms;
import experiment.feature.extraction.ontology.importance.PageRankVoaf;
import experiment.feature.extraction.term.importance.BetweennessMeasureTerm;
import experiment.model.Relevance;
import experiment.model.Term;
import experiment.model.query.TermQuery;
import experiment.model.query.enums.ExtractionType;
import experiment.repository.file.FeatureSetScores;
import experiment.repository.file.GroundTruthTermRanking;
import export.elasticsearch.cli.config.ElasticsearchConfiguration;
import export.elasticsearch.feature.FeatureRequestHelper;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONArray;
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

public class ElasticSearchIndexTest {

//    String featureSetName = "LOVBenchLight";
//    String featureSetFile = getClass().getClassLoader().getResource("LOVBenchLight.txt").getFile().toString();
//    ElasticSearchIndex termIndex = new ElasticSearchIndex("elasticsearch", "localhost", "9300", "9200", "terms", "term");
//    String groundTruthFile = getClass().getClassLoader().getResource("LOVBench_GroundTruth.csv").getFile().toString();

    String configFilePath = getClass().getClassLoader().getResource("testconfig.properties").getFile().toString();
    ElasticsearchConfiguration configuration;
    ElasticSearchIndex termIndex;
    String groundTruthFile;
    String featureSetFile;

    private static final Logger log = LoggerFactory.getLogger( ElasticSearchIndexTest.class );

    @Before
    public void setUp() throws Exception {
        new TestUtil().setNqFileConfiguration();
        this.configuration = new ElasticsearchConfiguration(this.configFilePath);
        this.termIndex = new ElasticSearchIndex(this.configuration);
        this.groundTruthFile = getClass().getClassLoader().getResource(this.configuration.getGroundTruthFilePath()).getFile().toString();
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

    @Test
    public void search() {
        GroundTruthTermRanking groundTruthTermRanking = GroundTruthTermRanking.parse(this.groundTruthFile);

        FeatureFactory featureFactory = new FeatureFactory();

//        Map<String, AbstractFeature> featureCache = new HashMap<>();

        FeatureSetScores<TermQuery,Term> scores = new FeatureSetScores<>(ExtractionType.TERM);

        for(Map.Entry<TermQuery, Map<Term, Relevance>> groundTruthRowMapEntry : groundTruthTermRanking.getGroundTruthTable().rowMap().entrySet()) {
            // build query
            TermQuery query = groundTruthRowMapEntry.getKey();
            Set<Term> terms = groundTruthRowMapEntry.getValue().keySet();
            JSONObject sltrQuery = FeatureRequestHelper.createFeatureLoggingSLTRQuery(query, terms, FilenameUtils.getBaseName(this.featureSetFile));

            // make query
            System.out.println(sltrQuery.toString());
            JSONArray hits = termIndex.search(sltrQuery);
            System.out.println(hits.toString());

            // parse response
            for (int i = 0; i < hits.length(); i++) {
                JSONObject termHit = hits.getJSONObject(i);
                Term term = new Term(termHit.getString("_id"));

                JSONArray termScores = termHit.getJSONObject("fields").getJSONArray("_ltrlog").getJSONObject(0).getJSONArray("LogEntry_"+FilenameUtils.getBaseName(this.featureSetFile));
                for (int j = 0; j < termScores.length(); j++) {
                    JSONObject termHitScore = termScores.getJSONObject(j);
                    scores.addScore(Pair.of(query,term), featureFactory.getFeature(termHitScore.getString("name")), termHitScore.getDouble("value"));
                }
            }
            break;
        }
        scores.writeCsv();
    }
}
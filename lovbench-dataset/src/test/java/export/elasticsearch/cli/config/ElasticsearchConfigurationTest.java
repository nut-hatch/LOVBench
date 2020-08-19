package export.elasticsearch.cli.config;

import org.junit.Test;

import static org.junit.Assert.*;

public class ElasticsearchConfigurationTest {

    String configFilePath = getClass().getClassLoader().getResource("testconfig.properties").getFile().toString();

    @Test
    public void readConfigFileTest() {
        ElasticsearchConfiguration configuration = new ElasticsearchConfiguration(this.configFilePath);
        assertEquals("elasticsearch", configuration.getClusterName());
        assertEquals("localhost", configuration.getHostName());
        assertEquals("9300", configuration.getTransportPort());
        assertEquals("9200", configuration.getRestPort());
        assertEquals("terms", configuration.getTermIndexName());
        assertEquals("term", configuration.getTermIndexMappingType());
        assertEquals("_ltr", configuration.getLtrIndexName());
        assertEquals("_featureset", configuration.getLtrIndexMappingType());
        assertEquals("./resources/dumps/2020-03-22_lov.nq", configuration.getLovNqFilePath());
        assertEquals("./resources/ltr/LOVBench_GroundTruth.csv", configuration.getGroundTruthFilePath());
        assertEquals("./resources/ltr/feature-sets/LOVBenchLight.txt", configuration.getFeatureSetDefinitionFilePath());
        assertEquals("./resources/ltr/models/LOVBenchLight_LambdaMart.txt", configuration.getModelFilePath());

    }
}
package export.elasticsearch.cli;

import experiment.feature.extraction.AbstractFeature;
import export.elasticsearch.cli.config.ElasticsearchConfiguration;
import export.elasticsearch.feature.FeatureRequestHelper;
import org.apache.commons.io.FilenameUtils;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

import static org.junit.Assert.*;

public class SetupLTRElasticSearchTest {

    String featureSetFilePath = getClass().getClassLoader().getResource("LOVBenchLight.txt").getFile().toString();
    SetupLTRElasticSearch setupScript = new SetupLTRElasticSearch(new String[]{});
    String configFilePath = getClass().getClassLoader().getResource("testconfig.properties").getFile().toString();

    @Before
    public void setUp() throws Exception {
        this.setupScript.setConfiguration(new ElasticsearchConfiguration(this.configFilePath));
    }

    @Test
    public void setupDocumentIndex() throws FileNotFoundException {
        List<AbstractFeature> featureList = FeatureRequestHelper.readFeatureSetFile(this.featureSetFilePath);
        assertTrue(this.setupScript.setupDocumentIndex(featureList));
    }

    @Test
    public void setupLTRIndex() throws FileNotFoundException {
        List<AbstractFeature> featureList = FeatureRequestHelper.readFeatureSetFile(this.featureSetFilePath);
        assertTrue(this.setupScript.setupDocumentIndex(featureList));
    }
}
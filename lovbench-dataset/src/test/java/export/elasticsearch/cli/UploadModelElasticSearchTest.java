package export.elasticsearch.cli;

import export.elasticsearch.cli.config.ElasticsearchConfiguration;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class UploadModelElasticSearchTest {

    UploadModelElasticSearch uploadModelScript = new UploadModelElasticSearch(new String[]{});
    String configFilePath = getClass().getClassLoader().getResource("testconfig.properties").getFile().toString();

    @Before
    public void setUp() throws Exception {
        this.uploadModelScript.setConfiguration(new ElasticsearchConfiguration(configFilePath));
    }

    @Test
    public void testUpload() {
        assertTrue(this.uploadModelScript.uploadModel());
    }
}
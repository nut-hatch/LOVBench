package export.elasticsearch.index;

import experiment.TestUtil;
import experiment.configuration.ExperimentConfiguration;
import experiment.feature.extraction.AbstractFeature;
import export.elasticsearch.feature.FeatureRequestHelper;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.util.List;

import static org.junit.Assert.*;

public class LTRIndexTest {

    String featureSetName = "LOVBenchLight";
    String featureSetFile = getClass().getClassLoader().getResource("LOVBenchLight.txt").getFile().toString();
    LTRIndex ltrIndex = new LTRIndex("elasticsearch", "localhost", "9300","9200", "_ltr", "_featureset");

    private static final Logger log = LoggerFactory.getLogger( LTRIndexTest.class );


    @Before
    public void setUp() throws Exception {
        ExperimentConfiguration.getInstance().setMakeElasticSearchTests(false);
    }

    @Test
    public void exists() {
        if (ExperimentConfiguration.getInstance().isMakeElasticSearchTests()) {
            log.debug("LTR has been initialized? " + ltrIndex.exists());
        }
    }

    @Test
    public void initialize() {
        if (ExperimentConfiguration.getInstance().isMakeElasticSearchTests()) {
            if (ltrIndex.initialize()) {
                log.debug("LTR plugin initialized!");
            } else {
                log.debug("Error: Index creation not acknowledged!");
            }
            log.debug("LTR has been initialized? " + ltrIndex.exists());
            assertTrue(ltrIndex.exists());
        }
    }

    @Test
    public void featureSetExists() {
        if (ExperimentConfiguration.getInstance().isMakeElasticSearchTests()) {
            log.debug("Feature set exists? " + ltrIndex.featureSetExists(featureSetName));
        }
    }


    @Test
    public void addFeatureSet() throws FileNotFoundException {
        if (ExperimentConfiguration.getInstance().isMakeElasticSearchTests()) {
            List<AbstractFeature> featureList = FeatureRequestHelper.readFeatureSetFile(this.featureSetFile);
            JSONObject featureSetSpecification = FeatureRequestHelper.getFeatureSetSpecification(featureList);
            log.info(featureSetSpecification.toString());
            if (ltrIndex.addFeatureSet(featureSetName, featureSetSpecification.toString()).equals("created")) {
                log.debug("Feature set created!");
            } else {
                log.debug("Error: creation not acknowledged!");
            }
            log.debug("Feature set exists? " + ltrIndex.featureSetExists(featureSetName));
            assertTrue(ltrIndex.featureSetExists(featureSetName));
        }
    }

    @Test
    public void featureSetDelete() {
        if (ExperimentConfiguration.getInstance().isMakeElasticSearchTests()) {
            log.debug("Feature set exists? " + ltrIndex.featureSetExists(featureSetName));
            log.debug("Delete: ");
            log.debug(ltrIndex.featureSetDelete(featureSetName));
            log.debug("Feature set exists? " + ltrIndex.featureSetExists(featureSetName));
            assertFalse(ltrIndex.featureSetExists(featureSetName));
        }
    }

}
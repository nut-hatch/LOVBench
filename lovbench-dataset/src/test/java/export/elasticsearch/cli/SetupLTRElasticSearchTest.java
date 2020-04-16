package export.elasticsearch.cli;

import org.apache.commons.io.FilenameUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

import static org.junit.Assert.*;

public class SetupLTRElasticSearchTest {

    String featureSetFile = getClass().getClassLoader().getResource("LOVBenchLight.txt").getFile().toString();

    private static final Logger log = LoggerFactory.getLogger( SetupLTRElasticSearchTest.class );

    @Test
    public void setupDocumentIndex() {
    }

    @Test
    public void setupLTRIndex() {
        log.info(new File(featureSetFile).getName());
        log.info(FilenameUtils.getExtension(featureSetFile));
        log.info(FilenameUtils.getBaseName(featureSetFile));

    }
}
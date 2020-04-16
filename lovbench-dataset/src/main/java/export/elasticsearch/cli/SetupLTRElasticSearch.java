package export.elasticsearch.cli;

import arq.cmdline.CmdGeneral;
import experiment.feature.extraction.AbstractFeature;
import export.elasticsearch.feature.FeatureRequestHelper;
import export.elasticsearch.index.ElasticSearchIndex;
import export.elasticsearch.index.ElasticSearchIndexException;
import export.elasticsearch.index.LTRIndex;
import org.apache.commons.io.FilenameUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.util.List;


/**
 * This script updates an existing index of LOV classes and properties with numeric fields that hold
 * query-independent scores used for ranking.
 */
public class SetupLTRElasticSearch extends CmdGeneral {

    private String clusterName;
    private String hostName;
    private String transportPort;
    private String httpPort;
    private String termIndexName;
    private String termIndexMappingType = "term";
    private String featureSetFile;
    private String featureStore = "_ltr"; // ES LTR default index
    private String featureMapping = "_featureset"; // ES LTR default mapping

    private static final Logger log = LoggerFactory.getLogger(SetupLTRElasticSearch.class);

    public static void main(String... args) {
        new SetupLTRElasticSearch(args).mainRun();
    }

    public SetupLTRElasticSearch(String[] argv) {
        super(argv);
        getUsage().startCategory("Arguments");
        getUsage().addUsage("clusterName", "ElasticSearch cluster name (e.g., elasticsearch)");
        getUsage().addUsage("hostname", "ElasticSearch hostname (e.g., localhost)");
        getUsage().addUsage("transportPort", "ElasticSearch port (e.g., 9300)");
        getUsage().addUsage("httpPort", "ElasticSearch port (e.g., 9200)");
        getUsage().addUsage("termIndexName", "Target ElasticSearch term index (e.g., terms)");
        getUsage().addUsage("featureSetFile", "Name of the file that contains the feature set to be set up (e.g., ./resources/feature-sets/LOVBenchLight.txt).");

    }

    @Override
    protected String getSummary() {
        return getCommandName() + " clusterName hostname transportPort httpPort termIndexName featureSetFile";
    }

    @Override
    protected void processModulesAndArgs() {
        if (getPositional().size() < 6) {
            doHelp();
        }
        this.clusterName = getPositionalArg(0);
        this.hostName = getPositionalArg(1);
        this.transportPort = getPositionalArg(2);
        this.httpPort = getPositionalArg(3);
        this.termIndexName = getPositionalArg(4);
        this.featureSetFile = getPositionalArg(5);
    }

    @Override
    protected void exec() {
        try {
            List<AbstractFeature> featureList = FeatureRequestHelper.readFeatureSetFile(this.featureSetFile);
            this.setupDocumentIndex(featureList);
            this.setupLTRIndex(featureList);
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
            System.exit(1);
        }

    }

    public void setupDocumentIndex(List<AbstractFeature> featureList) {
        JSONObject featureConfigurationMapping = FeatureRequestHelper.getFeatureMapping(featureList);
        ElasticSearchIndex termIndex = new ElasticSearchIndex(this.clusterName, this.hostName, this.transportPort, this.termIndexName, this.termIndexMappingType);
        this.updateDocumentMapping(termIndex, featureConfigurationMapping);
    }

    private void updateDocumentMapping(ElasticSearchIndex index, JSONObject mapping) {
        try {
            if (!index.exists()) {
                throw new ElasticSearchIndexException("Index '" + index.getIndexName() + "' does not exist on the cluster. Create the index first!");
            }
            index.put(mapping.toString());
        } finally {
            index.close();
        }
    }

    public void setupLTRIndex(List<AbstractFeature> featureList) {
        JSONObject featureSetSpecification = FeatureRequestHelper.getFeatureSetSpecification(featureList, "test", this.termIndexName);
        LTRIndex index = new LTRIndex(this.clusterName, this.hostName, this.httpPort, this.featureStore, this.featureMapping);
        String featureSetName = FilenameUtils.getBaseName(featureSetFile);
        this.createFeatureSet(index, featureSetName, featureSetSpecification);

    }

    private void createFeatureSet(LTRIndex index, String featureSetName, JSONObject featureSetSpecification) {

        try {
            if (!index.exists()) {
                log.info("LTR plugin requires an initial setup (PUT _ltr)");
                if (index.initialize()) {
                    log.info("LTR plugin initialized!");
                } else {
                    log.error("Error: Index creation not acknowledged!");
                }
            } else {
                log.info("LTR already initialized, good.");
            }

            if (index.featureSetExists(featureSetName)) {
                log.info("Deleting existing feature set.");
                index.featureSetDelete(featureSetName);
            }
            log.info("Adding feature set.");
            log.info(featureSetSpecification.toString());
            index.addFeatureSet(featureSetName, featureSetSpecification.toString());
        } finally {
            index.close();
        }
    }

    @Override
    protected String getCommandName() {
        return "setup-ltr-lov";
    }
}

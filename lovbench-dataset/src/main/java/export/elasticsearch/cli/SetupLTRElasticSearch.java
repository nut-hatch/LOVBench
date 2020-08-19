package export.elasticsearch.cli;

import arq.cmdline.CmdGeneral;
import experiment.feature.extraction.AbstractFeature;
import export.elasticsearch.cli.config.ElasticsearchConfiguration;
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
 * <p>
 * It further initialises the ElasticSearch LTR plugin and creates adds an elasticsearch feature set.
 * The feature set is described in a file, and the name of the feature set is derived from the filename.
 */
public class SetupLTRElasticSearch extends CmdGeneral {

    //    private String clusterName;
//    private String hostName;
//    private String transportPort;
//    private String restPort;
//    private String termIndexName;
//    private String termIndexMappingType = "term";
//    private String featureSetFile;
//    private String featureStore = "_ltr"; // ES LTR default index
//    private String featureMapping = "_featureset"; // ES LTR default mapping
    private ElasticsearchConfiguration configuration;

    private static final Logger log = LoggerFactory.getLogger(SetupLTRElasticSearch.class);

    public static void main(String... args) {
        new SetupLTRElasticSearch(args).mainRun();
    }

    public SetupLTRElasticSearch(String[] argv) {
        super(argv);
        getUsage().startCategory("Arguments");
//        getUsage().addUsage("clusterName", "ElasticSearch cluster name (e.g., elasticsearch)");
//        getUsage().addUsage("hostname", "ElasticSearch hostname (e.g., localhost)");
//        getUsage().addUsage("transportPort", "ElasticSearch port (e.g., 9300)");
//        getUsage().addUsage("restPort", "ElasticSearch port (e.g., 9200)");
//        getUsage().addUsage("termIndexName", "Target ElasticSearch term index (e.g., terms)");
//        getUsage().addUsage("featureSetFile", "Name of the file that contains the feature set to be set up (e.g., ./resources/feature-sets/LOVBenchLight.txt).");
        getUsage().addUsage("configFilePath", "Path to the config.properties file");
    }

    @Override
    protected String getSummary() {
        return getCommandName() + " configFilePath";
    }

    @Override
    protected void processModulesAndArgs() {
        if (getPositional().size() < 1) {
            doHelp();
        }
        this.configuration = new ElasticsearchConfiguration(getPositionalArg(0));
    }

    @Override
    protected void exec() {
        try {
            List<AbstractFeature> featureList = FeatureRequestHelper.readFeatureSetFile(this.configuration.getFeatureSetDefinitionFilePath());
            this.setupDocumentIndex(featureList);
            this.setupLTRIndex(featureList);
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
            System.exit(1);
        }
    }

    @Override
    protected String getCommandName() {
        return "setup-ltr";
    }

    /**
     * Adds fields to the mapping of the targeted document index that holds importance scores for the document
     *
     * @param featureList
     */
    public boolean setupDocumentIndex(List<AbstractFeature> featureList) {
        JSONObject featureConfigurationMapping = FeatureRequestHelper.getFeatureMapping(featureList);
        ElasticSearchIndex termIndex = new ElasticSearchIndex(this.configuration);
        boolean success = false;

        if (!termIndex.exists()) {
            log.error("Index '" + termIndex.getIndexName() + "' does not exist on the cluster. Create the index first!");
        } else {
            success = termIndex.put(featureConfigurationMapping.toString());
        }

        termIndex.close();

        return success;
    }

    /**
     * Given a list of feature specifications, creates the corresponding JSON specification for elastic search
     * and adds the feature set specification to the LTR index (_ltr). If the feature set already exists, its
     * specification will be overriden.
     *
     * @param featureList
     */
    public boolean setupLTRIndex(List<AbstractFeature> featureList) {

        LTRIndex index = new LTRIndex(this.configuration);
        JSONObject featureSetSpecification = FeatureRequestHelper.getFeatureSetSpecification(featureList, "test", this.configuration.getTermIndexMappingType());
        String featureSetName = FilenameUtils.getBaseName(this.configuration.getFeatureSetDefinitionFilePath());
        boolean success = false;

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
        log.info("Adding feature set: " + featureSetSpecification.toString());

        success = index.addFeatureSet(featureSetName, featureSetSpecification.toString()) != null;

        index.close();

        return success;
    }

    public void setConfiguration(ElasticsearchConfiguration configuration) {
        this.configuration = configuration;
    }
}

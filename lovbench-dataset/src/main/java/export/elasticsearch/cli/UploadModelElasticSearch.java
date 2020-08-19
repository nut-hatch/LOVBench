package export.elasticsearch.cli;

import arq.cmdline.CmdGeneral;
import export.elasticsearch.cli.config.ElasticsearchConfiguration;
import export.elasticsearch.feature.FeatureRequestHelper;
import export.elasticsearch.index.LTRIndex;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Generates a script that allows to upload a trained model to elasticsearch.
 *
 * The model is associated to a feature set that should have been previous initialized (see {@link SetupLTRElasticSearch}),
 * its scores for the current collection should have been computed and written to the document index (see {@link ExtractFeaturesElasticsearch}),
 * and the training dataset to learn the model that is being upload should have been created through the logging extension (see {@link LogFeaturesElasticSearch}).
 */
public class UploadModelElasticSearch extends CmdGeneral {

//    private String clusterName;
//    private String hostName;
//    private String transportPort;
//    private String restPort;
//    private String termIndexName;
//    private String termIndexMappingType = "term";
//    private String featureSetFile;
//    private String featureStore = "_ltr"; // ES LTR default index
//    private String featureMapping = "_featureset"; // ES LTR default mapping
//    private String modelFile;
    private ElasticsearchConfiguration configuration;

    private static final Logger log = LoggerFactory.getLogger( UploadModelElasticSearch.class );

    public static void main(String... args) {
        new UploadModelElasticSearch(args).mainRun();
    }

    public UploadModelElasticSearch(String[] argv) {
        super(argv);
        getUsage().startCategory("Arguments");
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
//        this.clusterName = getPositionalArg(0);
//        this.hostName = getPositionalArg(1);
//        this.transportPort = getPositionalArg(2);
//        this.restPort = getPositionalArg(3);
//        this.termIndexName = getPositionalArg(4);
//        this.featureSetFile = getPositionalArg(5);
//        this.modelFile = getPositionalArg(6);
    }

    @Override
    protected String getCommandName() {
        return "upload-model";
    }

    @Override
    protected void exec() {
        this.uploadModel();
    }

    /**
     *
     */
    public boolean uploadModel() {
        LTRIndex index = new LTRIndex(this.configuration);

        try {
            String modelDefinition = new String(Files.readAllBytes(Paths.get(this.configuration.getModelFilePath())), Charset.defaultCharset());
            String featureSetName = FilenameUtils.getBaseName(this.configuration.getFeatureSetDefinitionFilePath());
            String modelName = FilenameUtils.getBaseName(this.configuration.getModelFilePath());
            if (index.addModel(featureSetName, FeatureRequestHelper.createAddModelRequest(modelName, "ranklib", modelDefinition))) {
                log.info("Model upload successful, model named: " + modelName);
                return true;
            } else {
                log.error("Model could not be uploaded to elasticsearch");
                return false;
            }
        } catch (IOException e) {
            log.error("Could not read model file from " + this.configuration.getModelFilePath());
            e.printStackTrace();
        }
        return false;
    }

    public void setConfiguration(ElasticsearchConfiguration configuration) {
        this.configuration = configuration;
    }
}

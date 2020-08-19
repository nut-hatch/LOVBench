package export.elasticsearch.cli;

import arq.cmdline.CmdGeneral;
import experiment.feature.FeatureFactory;
import experiment.feature.extraction.AbstractFeature;
import experiment.feature.extraction.FeatureExtractorTerms;
import experiment.feature.extraction.ontology.AbstractOntologyFeature;
import experiment.feature.extraction.ontology.importance.AbstractOntologyImportanceFeature;
import experiment.feature.extraction.term.AbstractTermFeature;
import experiment.feature.extraction.term.importance.AbstractTermImportanceFeature;
import experiment.model.Term;
import experiment.model.query.TermQuery;
import experiment.configuration.ExperimentConfiguration;
import experiment.repository.file.FeatureSetScores;
import experiment.repository.triplestore.AbstractOntologyMetadataRepository;
import experiment.repository.triplestore.AbstractOntologyRepository;
import experiment.repository.triplestore.connector.JenaConnector;
import export.elasticsearch.cli.config.ElasticsearchConfiguration;
import export.elasticsearch.feature.FeatureRequestHelper;
import export.elasticsearch.index.ElasticSearchIndex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.util.List;

/**
 * This script computes LOV importance ranking scores and writes these values to the respective
 * document field in the ElasticSearch index.
 *
 * This script should be executed after the LTR index and featureset have been setup. See {@link SetupLTRElasticSearch}
 *
 * Furthermore, this script needs to be executed whenever the document corpus changes (updated, deleted or added vocabularies).
 */
public class ExtractFeaturesElasticsearch extends CmdGeneral {

//    private String clusterName;
//    private String hostName;
//    private String transportPort;
//    private String restPort;
//    private String termIndexName;
//    private String termIndexMappingType = "term";
//    private String lovNqFile;
//    private String featureSetFile;
//    private List<String> features;
    private ElasticsearchConfiguration configuration;

    private static final Logger log = LoggerFactory.getLogger( ExtractFeaturesElasticsearch.class );

    public static void main(String... args) {
        new ExtractFeaturesElasticsearch(args).mainRun();
    }

    public ExtractFeaturesElasticsearch(String[] argv) {
        super(argv);
        getUsage().startCategory("Arguments");
//        getUsage().addUsage("clusterName", "ElasticSearch cluster name (e.g., elasticsearch)");
//        getUsage().addUsage("hostname", "ElasticSearch hostname (e.g., localhost)");
//        getUsage().addUsage("transportPort", "ElasticSearch transport port (e.g., 9300)");
//        getUsage().addUsage("restPort", "ElasticSearch rest port (e.g., 9200)");
//        getUsage().addUsage("termIndexName", "Target ElasticSearch term index (e.g., terms)");
//        getUsage().addUsage("lov.nq", "Filename or URL of the LOV N-Quads dump");
////        getUsage().addUsage("features", "List of features to extract, only accepts importance features, no relevance features");
//        getUsage().addUsage("featureSetFile", "Name of the file that contains the feature set to be extracted (e.g., ./resources/feature-sets/LOVBenchLight.txt).");
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
    protected String getCommandName() {
        return "extract-features";
    }

    @Override
    protected void exec() {
        this.updateScoresInESIndex();
    }

    /**
     * Computes the importance scores for the given feature set definition, writes them to file and writes the values
     * to elasticsearch. Relevance features in the feature set are ignored, as these are dependent on the query.
     */
    public void updateScoresInESIndex() {

        AbstractOntologyRepository repository = ExperimentConfiguration.getInstance().getRepository();
        repository.setConnector(new JenaConnector(this.configuration.getLovNqFilePath()));

        AbstractOntologyMetadataRepository metadataRepository = ExperimentConfiguration.getInstance().getRepositoryMetadata();
        metadataRepository.setConnector(new JenaConnector(this.configuration.getLovNqFilePath()));

        try {
            // Initialize feature extractor
            List<AbstractFeature> featureList = FeatureRequestHelper.readFeatureSetFile(this.configuration.getFeatureSetDefinitionFilePath());
            FeatureExtractorTerms featureExtractorTerms = this.prepareFeatureExtractor(featureList);

            // Extract
            FeatureSetScores<TermQuery,Term> featureSetScores = featureExtractorTerms.extractImportance(repository.getAllTerms());
            featureSetScores.writeCsv();

            // Elasticsearch index
            ElasticSearchIndex termIndex = new ElasticSearchIndex(this.configuration);

            // Update values
            termIndex.bulkUpdate(FeatureRequestHelper.createDocUpdatesFromImportanceScores(featureSetScores));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    /**
     * Prepares a feature extractor based on the feature set definition, only keeps importance features.
     *
     * @param featureList
     * @return
     */
    private FeatureExtractorTerms prepareFeatureExtractor(List<AbstractFeature> featureList) {
        FeatureExtractorTerms featureExtractorTerms = new FeatureExtractorTerms();

        for (AbstractFeature feature : featureList) {
            if (feature instanceof AbstractTermImportanceFeature) {
                featureExtractorTerms.addTermFeature((AbstractTermFeature)feature);
            } else if (feature instanceof AbstractOntologyImportanceFeature) {
                featureExtractorTerms.addOntologyFeature((AbstractOntologyFeature)feature);
            } else {
                log.info("Feature " + feature.getFeatureName() + " is not an importance feature and is thus ignored. (Relevance features are directly computed in elasticsearch)");
            }
        }

        return featureExtractorTerms;
    }

//    private FeatureExtractorTerms prepareFeatureExtractor(List<String> features) {
//        FeatureExtractorTerms featureExtractorTerms = new FeatureExtractorTerms();
//
//        FeatureFactory featureFactory = new FeatureFactory();
//        for (String featureName : features) {
//            AbstractFeature feature = featureFactory.getFeature(featureName);
//            if (feature == null) {
//                log.error(featureName + " invalid - no corresponding feature class with this name exists.");
//            } else if (feature instanceof AbstractTermFeature) {
//                featureExtractorTerms.addTermFeature((AbstractTermFeature)feature);
//            } else if (feature instanceof AbstractOntologyFeature) {
//                featureExtractorTerms.addOntologyFeature((AbstractOntologyFeature)feature);
//            }
//        }
//
//        return featureExtractorTerms;
//
//    }
}

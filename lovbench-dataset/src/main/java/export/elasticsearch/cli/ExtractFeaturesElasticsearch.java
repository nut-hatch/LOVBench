package export.elasticsearch.cli;

import arq.cmdline.CmdGeneral;
import experiment.feature.FeatureFactory;
import experiment.feature.extraction.AbstractFeature;
import experiment.feature.extraction.FeatureExtractorTerms;
import experiment.feature.extraction.ontology.AbstractOntologyFeature;
import experiment.feature.extraction.term.AbstractTermFeature;
import experiment.model.Term;
import experiment.model.query.TermQuery;
import experiment.configuration.ExperimentConfiguration;
import experiment.repository.file.FeatureSetScores;
import experiment.repository.triplestore.AbstractOntologyMetadataRepository;
import experiment.repository.triplestore.AbstractOntologyRepository;
import experiment.repository.triplestore.connector.JenaConnector;
import export.elasticsearch.feature.FeatureRequestHelper;
import export.elasticsearch.index.ElasticSearchIndex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ExtractFeaturesElasticsearch extends CmdGeneral {

    private String clusterName;
    private String hostName;
    private String transportPort;
    private String termIndexName;
    private String termIndexMappingType = "term";
    private String lovNqFile;
    private List<String> features;

    private static final Logger log = LoggerFactory.getLogger( ExtractFeaturesElasticsearch.class );

    public static void main(String... args) {
        new ExtractFeaturesElasticsearch(args).mainRun();
    }

    public ExtractFeaturesElasticsearch(String[] argv) {
        super(argv);
        getUsage().startCategory("Arguments");
        getUsage().addUsage("clusterName", "ElasticSearch cluster name (e.g., elasticsearch)");
        getUsage().addUsage("hostname", "ElasticSearch hostname (e.g., localhost)");
        getUsage().addUsage("transportPort", "ElasticSearch transport port (e.g., 9300)");
        getUsage().addUsage("termIndexName", "Target ElasticSearch term index (e.g., terms)");
        getUsage().addUsage("lov.nq", "Filename or URL of the LOV N-Quads dump");
        getUsage().addUsage("features", "List of features to extract, only excepts importance and no relevance features");

    }

    @Override
    protected String getSummary() {
        return getCommandName() + " clusterName hostname transportPort termIndexName lov.nq features...";
    }

    @Override
    protected void processModulesAndArgs() {
        if (getPositional().size() < 5) {
            doHelp();
        }
        this.clusterName = getPositionalArg(0);
        this.hostName = getPositionalArg(1);
        this.transportPort = getPositionalArg(2);
        this.termIndexName = getPositionalArg(3);
        this.lovNqFile = getPositionalArg(4);
        this.features = getArgList().subList(5, getArgList().size());
    }

    @Override
    protected void exec() {
        this.updateScoresInESIndex();
    }

    public void updateScoresInESIndex() {

        AbstractOntologyRepository repository = ExperimentConfiguration.getInstance().getRepository();
        repository.setConnector(new JenaConnector(this.lovNqFile));

        AbstractOntologyMetadataRepository metadataRepository = ExperimentConfiguration.getInstance().getRepositoryMetadata();
        metadataRepository.setConnector(new JenaConnector(this.lovNqFile));

        // Initialize feature extractor
        FeatureExtractorTerms featureExtractorTerms = this.prepareFeatureExtractor(this.features);

        // Extract
        FeatureSetScores<TermQuery,Term> featureSetScores = featureExtractorTerms.extractImportance(repository.getAllTerms());
//        featureSetScores.writeCsv();

        // Elasticsearch index
        ElasticSearchIndex termIndex = new ElasticSearchIndex(this.clusterName, this.hostName, this.transportPort, this.termIndexName, this.termIndexMappingType);

        // Update values
        termIndex.bulkUpdate(FeatureRequestHelper.createDocUpdatesFromImportanceScores(featureSetScores));

    }

    @Override
    protected String getCommandName() {
        return "extract-features-lov";
    }

    private FeatureExtractorTerms prepareFeatureExtractor(List<String> features) {
        FeatureExtractorTerms featureExtractorTerms = new FeatureExtractorTerms();

        FeatureFactory featureFactory = new FeatureFactory();
        for (String featureName : features) {
            AbstractFeature feature = featureFactory.getFeature(featureName);
            if (feature == null) {
                log.error(featureName + " invalid - no corresponding feature class with this name exists.");
            } else if (feature instanceof AbstractTermFeature) {
                featureExtractorTerms.addTermFeature((AbstractTermFeature)feature);
            } else if (feature instanceof AbstractOntologyFeature) {
                featureExtractorTerms.addOntologyFeature((AbstractOntologyFeature)feature);
            }
        }

        return featureExtractorTerms;

    }
}

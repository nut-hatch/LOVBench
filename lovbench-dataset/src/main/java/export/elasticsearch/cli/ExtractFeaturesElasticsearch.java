package export.elasticsearch.cli;

import arq.cmdline.CmdGeneral;
import experiment.feature.extraction.FeatureExtractorTerms;
import experiment.feature.extraction.ontology.importance.PageRankVoaf;
import experiment.feature.extraction.term.importance.*;
import experiment.feature.scoring.TFIDFScorer;
import experiment.feature.scoring.TermStatsScorer;
import experiment.feature.scoring.graph.BetweennessScorer;
import experiment.model.Term;
import experiment.model.query.TermQuery;
import experiment.configuration.ExperimentConfiguration;
import experiment.repository.file.FeatureSetScores;
import experiment.repository.triplestore.AbstractOntologyMetadataRepository;
import experiment.repository.triplestore.AbstractOntologyRepository;
import experiment.repository.triplestore.connector.JenaConnector;
import org.elasticsearch.index.mapper.SourceToParse;

import java.util.List;

public class ExtractFeaturesElasticsearch extends CmdGeneral {

    private String clusterName;
    private String hostName;
    private String classIndexName;
    private String propertyIndexName;
    private String lovNqFile;
    private List<String> features;

    public static void main(String... args) {
        new ExtractFeaturesElasticsearch(args).mainRun();
    }

    public ExtractFeaturesElasticsearch(String[] argv) {
        super(argv);
        getUsage().startCategory("Arguments");
        getUsage().addUsage("clusterName", "ElasticSearch cluster name (e.g., elasticsearch)");
        getUsage().addUsage("hostname", "ElasticSearch hostname (e.g., localhost)");
        getUsage().addUsage("classIndexName", "Target ElasticSearch CLASS index (e.g., classes)");
        getUsage().addUsage("propertyIndexName", "Target ElasticSearch PROPERTY index (e.g., properties)");
        getUsage().addUsage("lov.nq", "Filename or URL of the LOV N-Quads dump");
        getUsage().addUsage("features", "List of features to extract, only excepts importance and no relevance features");

    }

    @Override
    protected String getSummary() {
        return getCommandName() + " clusterName hostname classIndexName propertyIndexName lov.nq lov.n3 features...";
    }

    @Override
    protected void processModulesAndArgs() {
        if (getPositional().size() < 4) {
            doHelp();
        }
        clusterName = getPositionalArg(0);
        hostName = getPositionalArg(1);
        classIndexName = getPositionalArg(2);
        propertyIndexName = getPositionalArg(3);
        lovNqFile = getPositionalArg(4);
        features = getArgList().subList(5, getArgList().size());
    }

    @Override
    protected void exec() {

        AbstractOntologyRepository repository = ExperimentConfiguration.getInstance().getRepository();
        repository.setConnector(new JenaConnector(lovNqFile));

        AbstractOntologyMetadataRepository metadataRepository = ExperimentConfiguration.getInstance().getRepositoryMetadata();
        metadataRepository.setConnector(new JenaConnector(lovNqFile));

        // Initialize feature extractor
        FeatureExtractorTerms featureExtractorTerms = this.prepareFeatureExtractor(repository, metadataRepository);

        // Extract
        FeatureSetScores<TermQuery,Term> featureSetScores = featureExtractorTerms.extractImportance(repository.getAllTerms());
        featureSetScores.writeCsv();

        // Elasticsearch client


        // Update values
    }

    @Override
    protected String getCommandName() {
        return "extract-features-elasticsearch";
    }

    private FeatureExtractorTerms prepareFeatureExtractor(AbstractOntologyRepository repository, AbstractOntologyMetadataRepository metadataRepository) {
        FeatureExtractorTerms featureExtractorTerms = new FeatureExtractorTerms();

        TFIDFScorer tfidfScorer = new TFIDFScorer(repository);
        TermStatsScorer termStatsScorer = new TermStatsScorer(repository);
        for (String featureName : this.features) {
            switch (featureName) {
                case PageRankVoaf.FEATURE_NAME:
                    featureExtractorTerms.addOntologyFeature(new PageRankVoaf(repository, metadataRepository));
                    break;
                case BetweennessMeasureTerm.FEATURE_NAME:
                    BetweennessScorer betweennessScorer = new BetweennessScorer(repository);
                    featureExtractorTerms.addTermFeature(new BetweennessMeasureTerm(repository, betweennessScorer));
                    break;
                case IDFTerm.FEATURE_NAME:
                    featureExtractorTerms.addTermFeature(new IDFTerm(repository, tfidfScorer));
                    break;
                case TFTerm.FEATURE_NAME:
                    featureExtractorTerms.addTermFeature(new TFTerm(repository, tfidfScorer));
                    break;
                case TFIDFTerm.FEATURE_NAME:
                    featureExtractorTerms.addTermFeature(new TFIDFTerm(repository, tfidfScorer));
                    break;
                case Subclasses.FEATURE_NAME:
                    featureExtractorTerms.addTermFeature(new Subclasses(repository, termStatsScorer));
                    break;
                case Superclasses.FEATURE_NAME:
                    featureExtractorTerms.addTermFeature(new Superclasses(repository, termStatsScorer));
                    break;
                case Relations.FEATURE_NAME:
                    featureExtractorTerms.addTermFeature(new Relations(repository, termStatsScorer));
                    break;
                case Siblings.FEATURE_NAME:
                    featureExtractorTerms.addTermFeature(new Siblings(repository, termStatsScorer));
                    break;
                case Subproperties.FEATURE_NAME:
                    featureExtractorTerms.addTermFeature(new Subproperties(repository, termStatsScorer));
                    break;
                case Superproperties.FEATURE_NAME:
                    featureExtractorTerms.addTermFeature(new Superproperties(repository, termStatsScorer));
                    break;
                default:
                    System.out.println(featureName + " invalid.");
                    break;
            }
        }

        return featureExtractorTerms;

    }
}

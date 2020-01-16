package experiment;

import experiment.feature.extraction.FeatureExtractorOntologies;
import experiment.feature.extraction.FeatureExtractorTerms;
import experiment.feature.extraction.ontology.importance.*;
import experiment.feature.extraction.ontology.relevance.*;
import experiment.feature.extraction.term.importance.*;
import experiment.feature.extraction.term.relevance.*;
import experiment.feature.scoring.TFIDFScorer;
import experiment.feature.scoring.TermStatsScorer;
import experiment.feature.scoring.api.LOVScorer;
import experiment.feature.scoring.graph.BetweennessScorer;
import experiment.feature.scoring.graph.HITSScorer;
import experiment.feature.scoring.graph.HubDWRankScorer;
import experiment.repository.file.ExperimentConfiguration;
import experiment.repository.file.GroundTruthOntologyRanking;
import experiment.repository.file.GroundTruthTermRanking;
import experiment.repository.triplestore.AbstractOntologyMetadataRepository;
import experiment.repository.triplestore.AbstractOntologyRepository;
import experiment.repository.triplestore.AbstractOntologySearchRepository;
import experiment.repository.triplestore.LOVSearchRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *  This application extracts term and ontology features from a repository of ontologies.
 *  Dependencies and configurations can be viewed and changed through the experiment.repository.file.ExperimentConfiguration Singleton.
 *  The computed scores for query/term and query/ontology pairs are written to csv files in the output folder.
 *
 */
public class FeatureExperimentApplication {

    private static final Logger log = LoggerFactory.getLogger( FeatureExperimentApplication.class );

    /**
     *
     * @param args So far command line arguments are not parsed!
     */
    public static void main(String[] args) {

        ExperimentConfiguration.getInstance().writeCsv();

        // init repository
        AbstractOntologyRepository repository = ExperimentConfiguration.getInstance().getRepository();
        AbstractOntologyMetadataRepository metadataRepository = ExperimentConfiguration.getInstance().getRepositoryMetadata();
        AbstractOntologySearchRepository searchRepository = new LOVSearchRepository(ExperimentConfiguration.getInstance().getDbnameSearch());

        // init scorer objects
        HITSScorer hitsScorer = new HITSScorer(repository, repository.getOwlImports());
        BetweennessScorer betweennessScorer = new BetweennessScorer(repository);
        TFIDFScorer tfidfScorer = new TFIDFScorer(repository);
        HubDWRankScorer hubDWRankScorer = new HubDWRankScorer(repository);
        LOVScorer lovScorer = new LOVScorer();
        TermStatsScorer termStatsScorer = new TermStatsScorer(repository);

        // Ontology Importance Features
        AuthorativenessDWRank authorativenessDWRank = new AuthorativenessDWRank(repository);
        MaxHubDWRank maxHubDWRank = new MaxHubDWRank(repository, hubDWRankScorer);
        MinHubDWRank minHubDWRank = new MinHubDWRank(repository, hubDWRankScorer);
        PageRankImports pageRankImports = new PageRankImports(repository);
        PageRankImplicit pageRankImplicit = new PageRankImplicit(repository);
        PageRankVoaf pageRankVoaf = new PageRankVoaf(repository, metadataRepository);

        // Ontology Relevance Features
        BetweennessMeasure betweennessMeasure = new BetweennessMeasure(repository, betweennessScorer);
        ClassMatchMeasure classMatchMeasure = new ClassMatchMeasure(repository);
        DensityMeasure densityMeasure = new DensityMeasure(repository, termStatsScorer);
        SemanticSimilarityMeasure semanticSimilarityMeasure = new SemanticSimilarityMeasure(repository);
        TFOntology tfOntology = new TFOntology(repository, tfidfScorer);
        IDFOntology idfOntology = new IDFOntology(repository, tfidfScorer);
        TFIDFOntology tfidfOntology = new TFIDFOntology(repository, tfidfScorer);
        BM25Ontology bm25Ontology = new BM25Ontology(repository,tfidfScorer);
        HITSAuthorityImports hitsAuthorityImports = new HITSAuthorityImports(repository, hitsScorer);
        HITSHubImports hitsHubImports = new HITSHubImports(repository, hitsScorer);
        LOVOntologyMatch lovOntologyMatch = new LOVOntologyMatch(repository, lovScorer);
        VSMOntology vsmOntology = new VSMOntology(repository,tfidfScorer);

        // Term Importance Features
        HubDWRank hubDWRank = new HubDWRank(repository, hubDWRankScorer);
        BetweennessMeasureTerm betweennessMeasureTerm = new BetweennessMeasureTerm(repository, betweennessScorer);
        DensityMeasureTerm densityMeasureTerm = new DensityMeasureTerm(repository, termStatsScorer);
        TFTerm tfTerm = new TFTerm(repository, tfidfScorer);
        IDFTerm idfTerm = new IDFTerm(repository, tfidfScorer);
        TFIDFTerm tfidfTerm = new TFIDFTerm(repository, tfidfScorer);
        Subproperties subproperties = new Subproperties(repository, termStatsScorer);
        Superproperties superproperties = new Superproperties(repository, termStatsScorer);
        Subclasses subclasses = new Subclasses(repository, termStatsScorer);
        Superclasses superclasses = new Superclasses(repository, termStatsScorer);
        Relations relations = new Relations(repository, termStatsScorer);
        Siblings siblings = new Siblings(repository, termStatsScorer);

        // Term Relevance Features
        TextRelevancy textRelevancy = new TextRelevancy(repository);
        LOVTermMatch lovTermMatch = new LOVTermMatch(repository,lovScorer);
        LOVTermPopularity lovTermPopularity = new LOVTermPopularity(repository,lovScorer);
        LabelSearch labelSearch = new LabelSearch(repository, searchRepository);

        FeatureExtractorTerms featureExtractorTerms = new FeatureExtractorTerms();
        featureExtractorTerms.addOntologyFeatures(authorativenessDWRank, maxHubDWRank, minHubDWRank, pageRankImports, pageRankImplicit, pageRankVoaf);
        featureExtractorTerms.addOntologyFeatures(betweennessMeasure, classMatchMeasure, densityMeasure, semanticSimilarityMeasure, tfOntology, idfOntology, tfidfOntology, bm25Ontology, hitsAuthorityImports, hitsHubImports, lovOntologyMatch, vsmOntology);
        featureExtractorTerms.addTermFeatures(hubDWRank, betweennessMeasureTerm, densityMeasureTerm, tfTerm, idfTerm, tfidfTerm, subproperties, superproperties, subclasses, superclasses, relations, siblings);
        featureExtractorTerms.addTermFeatures(textRelevancy, lovTermMatch, lovTermPopularity, labelSearch);

        GroundTruthTermRanking groundTruthTermRanking = GroundTruthTermRanking.parse(ExperimentConfiguration.getInstance().getGroundTruthTermsFilePath());
        featureExtractorTerms.extract(groundTruthTermRanking);

    }

}

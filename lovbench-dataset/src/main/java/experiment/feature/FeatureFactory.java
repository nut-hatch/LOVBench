package experiment.feature;

import edu.uci.ics.jung.algorithms.scoring.PageRank;
import experiment.configuration.ExperimentConfiguration;
import experiment.feature.extraction.AbstractFeature;
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
import experiment.repository.triplestore.AbstractOntologyMetadataRepository;
import experiment.repository.triplestore.AbstractOntologyRepository;
import experiment.repository.triplestore.LOVSearchRepository;

public class FeatureFactory {

    AbstractOntologyRepository repository = ExperimentConfiguration.getInstance().getRepository();
    AbstractOntologyMetadataRepository metaDataRepository = ExperimentConfiguration.getInstance().getRepositoryMetadata();

    LOVScorer lovScorer;
    TFIDFScorer tfidifScorer;
    BetweennessScorer betweennessScorer;
    HubDWRankScorer hubDWRankScorer;
    TermStatsScorer termStatsScorer;
    HITSScorer hitsScorer;

    public AbstractFeature getFeature(String featureName) {
        switch (featureName) {
            case BooleanMatch.FEATURE_NAME:
                return new BooleanMatch(repository);
            case LabelSearch.FEATURE_NAME:
                return new LabelSearch(repository, new LOVSearchRepository(ExperimentConfiguration.getInstance().getDbnameSearch()));
            case LOVTermMatch.FEATURE_NAME:
                return new LOVTermMatch(repository, this.getLovScorer());
            case LOVTermPopularity.FEATURE_NAME:
                return new LOVTermPopularity(repository, this.getLovScorer());
            case QueryLength.FEATURE_NAME:
                return new QueryLength(repository);
            case VSMTerm.FEATURE_NAME:
                return new VSMTerm(repository, this.getTfidifScorer());
            case BetweennessMeasureTerm.FEATURE_NAME:
                return new BetweennessMeasureTerm(repository, this.getBetweennessScorer());
            case BM25Term.FEATURE_NAME:
                return new BM25Term(repository, this.getTfidifScorer());
            case DensityMeasureTerm.FEATURE_NAME:
                return new DensityMeasureTerm(repository, this.getTermStatsScorer());
            case HubDWRank.FEATURE_NAME:
                return new HubDWRank(repository, this.getHubDWRankScorer());
            case IDFTerm.FEATURE_NAME:
                return new IDFTerm(repository, this.getTfidifScorer());
            case Relations.FEATURE_NAME:
                return new Relations(repository, this.getTermStatsScorer());
            case Siblings.FEATURE_NAME:
                return new Siblings(repository, this.getTermStatsScorer());
            case Subclasses.FEATURE_NAME:
                return new Subclasses(repository, this.getTermStatsScorer());
            case Subproperties.FEATURE_NAME:
                return new Subproperties(repository, this.getTermStatsScorer());
            case Superclasses.FEATURE_NAME:
                return new Superclasses(repository, this.getTermStatsScorer());
            case Superproperties.FEATURE_NAME:
                return new Superproperties(repository, this.getTermStatsScorer());
            case TFIDFTerm.FEATURE_NAME:
                return new TFIDFTerm(repository, this.getTfidifScorer());
            case TFTerm.FEATURE_NAME:
                return new TFTerm(repository, this.getTfidifScorer());
            case BetweennessMeasure.FEATURE_NAME:
                return new BetweennessMeasure(repository, this.getBetweennessScorer());
            case BM25Ontology.FEATURE_NAME:
                return new BM25Ontology(repository, this.getTfidifScorer());
            case ClassMatchMeasure.FEATURE_NAME:
                return new ClassMatchMeasure(repository);
            case DensityMeasure.FEATURE_NAME:
                return new DensityMeasure(repository, this.getTermStatsScorer());
            case HITSAuthorityImports.FEATURE_NAME:
                return new HITSAuthorityImports(repository, this.getHitsScorer());
            case HITSHubImports.FEATURE_NAME:
                return new HITSHubImports(repository, this.getHitsScorer());
            case IDFOntology.FEATURE_NAME:
                return new IDFOntology(repository, this.getTfidifScorer());
            case LOVOntologyMatch.FEATURE_NAME:
                return new LOVOntologyMatch(repository, this.getLovScorer());
            case PropertyMatchMeasure.FEATURE_NAME:
                return new PropertyMatchMeasure(repository);
            case SemanticSimilarityMeasure.FEATURE_NAME:
                return new SemanticSimilarityMeasure(repository);
            case TFIDFOntology.FEATURE_NAME:
                return new TFIDFOntology(repository, this.getTfidifScorer());
            case TFOntology.FEATURE_NAME:
                return new TFOntology(repository, this.getTfidifScorer());
            case VSMOntology.FEATURE_NAME:
                return new VSMOntology(repository, this.getTfidifScorer());
            case AuthorativenessDWRank.FEATURE_NAME:
                return new AuthorativenessDWRank(repository);
            case MaxHubDWRank.FEATURE_NAME:
                return new MaxHubDWRank(repository, this.getHubDWRankScorer());
            case MinHubDWRank.FEATURE_NAME:
                return new MinHubDWRank(repository, this.getHubDWRankScorer());
            case PageRankImplicit.FEATURE_NAME:
                return new PageRankImplicit(repository);
            case PageRankImports.FEATURE_NAME:
                return new PageRankImports(repository);
            case PageRankVoaf.FEATURE_NAME:
                return new PageRankVoaf(repository, ExperimentConfiguration.getInstance().getRepositoryMetadata());
            default:
                return null;
        }
    }


    public LOVScorer getLovScorer() {
        if (this.lovScorer == null) {
            this.lovScorer = new LOVScorer();
        }
        return this.lovScorer;
    }

    public TFIDFScorer getTfidifScorer() {
        if (this.tfidifScorer == null) {
            this.tfidifScorer = new TFIDFScorer(repository);
        }
        return this.tfidifScorer;
    }

    public BetweennessScorer getBetweennessScorer() {
        if (this.betweennessScorer == null) {
            this.betweennessScorer = new BetweennessScorer(repository);
        }
        return this.betweennessScorer;
    }

    public HubDWRankScorer getHubDWRankScorer() {
        if (this.hubDWRankScorer == null) {
            this.hubDWRankScorer = new HubDWRankScorer(repository);
        }
        return this.hubDWRankScorer;
    }

    public TermStatsScorer getTermStatsScorer() {
        if (this.termStatsScorer == null) {
            this.termStatsScorer = new TermStatsScorer(repository);
        }
        return this.termStatsScorer;
    }

    public HITSScorer getHitsScorer() {
        if (this.hitsScorer == null) {
            this.hitsScorer = new HITSScorer(repository, repository.getOwlImports());
        }
        return this.hitsScorer;
    }
}

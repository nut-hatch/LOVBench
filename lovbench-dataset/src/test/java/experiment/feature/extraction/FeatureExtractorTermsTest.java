package experiment.feature.extraction;

import experiment.TestUtil;
import experiment.configuration.ExperimentConfiguration;
import experiment.feature.extraction.ontology.importance.PageRankVoaf;
import experiment.feature.extraction.ontology.relevance.BetweennessMeasure;
import experiment.feature.extraction.term.importance.DensityMeasureTerm;
import experiment.feature.extraction.term.importance.HubDWRank;
import experiment.feature.extraction.term.relevance.TextRelevancy;
import experiment.feature.scoring.TermStatsScorer;
import experiment.feature.scoring.graph.BetweennessScorer;
import experiment.feature.scoring.graph.HubDWRankScorer;
import experiment.model.Ontology;
import experiment.model.Term;
import experiment.model.query.TermQuery;
import experiment.repository.file.FeatureSetScores;
import experiment.repository.triplestore.AbstractOntologyMetadataRepository;
import experiment.repository.triplestore.AbstractOntologyRepository;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static org.junit.Assert.*;

public class FeatureExtractorTermsTest {

    AbstractOntologyRepository repository = ExperimentConfiguration.getInstance().getRepository();
    AbstractOntologyMetadataRepository metadataRepository = ExperimentConfiguration.getInstance().getRepositoryMetadata();

    private static final Logger log = LoggerFactory.getLogger( FeatureExtractorTermsTest.class );

    @Before
    public void setUp() throws Exception {
        new TestUtil().setNqFileConfiguration();
    }

    @Test
    public void extractImportance() {
        // Compute scores with extractor
        FeatureExtractorTerms extractorTerms = new FeatureExtractorTerms();
        extractorTerms.addOntologyFeature(new PageRankVoaf(repository, metadataRepository));
        extractorTerms.addTermFeature(new HubDWRank(repository, new HubDWRankScorer(repository)));

        Map<Ontology,Set<Term>> ontAndTerms = new HashMap<>();
        ontAndTerms.put(new Ontology("http://schema.org/"), new HashSet<>(Arrays.asList(
                new Term("http://schema.org/House"),
                new Term("http://schema.org/Person")
        )));
        FeatureSetScores<TermQuery, Term> scores = extractorTerms.extractImportance(ontAndTerms);

        // Compute scores directly with features
        PageRankVoaf pr = new PageRankVoaf(repository, metadataRepository);
        pr.computeAllScores();

        HubDWRank hub = new HubDWRank(repository, new HubDWRankScorer(repository));
        hub.computeAllScores();

        // Compare results
        for (Map.Entry<Pair<TermQuery, Term>, Map<AbstractFeature, Double>> score : scores.getFeatureScores().entrySet()) {
            Term term = score.getKey().getRight();
            for (Map.Entry<AbstractFeature, Double> featureScore : score.getValue().entrySet()) {
                AbstractFeature feature = featureScore.getKey();
                Double myScore = featureScore.getValue();
                log.debug(feature.getFeatureName()+": "+myScore);
                if (feature.getFeatureName().equals(pr.getFeatureName())) {
                    assertEquals(0, Double.compare(pr.getScore(new Ontology(term.getOntologyUriOfTerm())), myScore));
                }

                if (feature.getFeatureName().equals(hub.getFeatureName())) {
                    assertEquals(0, Double.compare(hub.getScore(term), myScore));
                }
            }
        }



    }
}
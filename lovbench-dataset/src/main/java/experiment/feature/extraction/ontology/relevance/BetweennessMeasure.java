package experiment.feature.extraction.ontology.relevance;

import experiment.feature.scoring.graph.BetweennessScorer;
import experiment.model.Ontology;
import experiment.model.Term;
import experiment.model.query.AbstractQuery;
import experiment.model.query.enums.TermType;
import experiment.repository.triplestore.AbstractOntologyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * Computes the betweenness measure as specified in AKTiveRank for an ontology graph.
 *
 * Betweenness is defined by the number of shortest paths between all nodes in a graph that go through the node to score.
 */
public class BetweennessMeasure extends AbstractOntologyRelevanceFeature {

    BetweennessScorer betweennessScorer;

    public static final String FEATURE_NAME = "Betweenness_O";

    private static final Logger log = LoggerFactory.getLogger( BetweennessMeasure.class );

    public BetweennessMeasure(AbstractOntologyRepository repository, BetweennessScorer betweennessScorer) {
        super(repository);
        this.betweennessScorer = betweennessScorer;
    }

    @Override
    public double getScore(AbstractQuery query, Ontology ontology) {
        Set<Term> classMatches = this.repository.getTermQueryMatch(query, ontology, TermType.CLASS);

        double betweennessScore = 0.0;
        if (classMatches != null && !classMatches.isEmpty()) {
            for (Term classMatch : classMatches) {
                double betweennessScoreForClass = this.betweennessScorer.betweenness(classMatch, ontology);
                betweennessScore += betweennessScoreForClass;
            }
            log.debug(String.format("Betweenness measure for query %s in ontology %s - sum: %s - no of matches: %s - total score: %s", query, ontology, betweennessScore, classMatches.size(), betweennessScore /= classMatches.size()));
            betweennessScore /= classMatches.size();
        }
        return betweennessScore;
    }

    @Override
    public String getFeatureName() {
        return BetweennessMeasure.FEATURE_NAME;
    }
}

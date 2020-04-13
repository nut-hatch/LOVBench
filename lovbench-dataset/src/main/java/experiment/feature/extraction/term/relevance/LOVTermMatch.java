package experiment.feature.extraction.term.relevance;

import experiment.feature.scoring.api.LOVScorer;
import experiment.model.Term;
import experiment.model.query.TermQuery;
import experiment.repository.triplestore.AbstractOntologyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Computes the LOV term match score.
 */
public class LOVTermMatch extends AbstractTermRelevanceFeature {

    LOVScorer lovScorer;

    public static final String FEATURE_NAME = "LOV_Match_T";

    private static final Logger log = LoggerFactory.getLogger( LOVTermMatch.class );

    public LOVTermMatch(AbstractOntologyRepository repository, LOVScorer lovScorer) {
        super(repository);
        this.lovScorer = lovScorer;
    }

    @Override
    public double getScore(TermQuery query, Term term) {
        return this.lovScorer.getTermMatchScore(query,term);
    }

    @Override
    public String getFeatureName() {
        return LOVTermMatch.FEATURE_NAME;
    }
}

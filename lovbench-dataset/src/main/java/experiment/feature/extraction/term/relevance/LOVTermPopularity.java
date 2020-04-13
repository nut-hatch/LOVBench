package experiment.feature.extraction.term.relevance;

import experiment.feature.scoring.api.LOVScorer;
import experiment.model.Term;
import experiment.model.query.TermQuery;
import experiment.repository.triplestore.AbstractOntologyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Computes the LOV term popularity feature.
 */
public class LOVTermPopularity extends AbstractTermRelevanceFeature {

    LOVScorer lovScorer;

    public static final String FEATURE_NAME = "LOV_Popularity_T";
    
    private static final Logger log = LoggerFactory.getLogger( LOVTermPopularity.class );

    public LOVTermPopularity(AbstractOntologyRepository repository, LOVScorer lovScorer) {
        super(repository);
        this.lovScorer = lovScorer;
    }

    @Override
    public double getScore(TermQuery query, Term term) {
        return this.lovScorer.getTermPopularityScore(query,term);
    }

    @Override
    public String getFeatureName() {
        return LOVTermPopularity.FEATURE_NAME;
    }
}

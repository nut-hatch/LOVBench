package experiment.feature.extraction.term.relevance;

import experiment.model.Term;
import experiment.model.query.TermQuery;
import experiment.repository.triplestore.AbstractOntologyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QueryLength extends AbstractTermRelevanceFeature {

    public static final String FEATURE_NAME = "Query_Length_Q";

    private static final Logger log = LoggerFactory.getLogger( QueryLength.class );

    public QueryLength(AbstractOntologyRepository repository) {
        super(repository);
    }

    @Override
    public double getScore(TermQuery query, Term term) {
        return query.getSearchWords().size();
    }

    @Override
    public String getFeatureName() {
        return QueryLength.FEATURE_NAME;
    }
}

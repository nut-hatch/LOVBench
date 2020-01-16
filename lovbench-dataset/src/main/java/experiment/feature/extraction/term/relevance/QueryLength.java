package experiment.feature.extraction.term.relevance;

import experiment.model.Term;
import experiment.model.query.TermQuery;
import experiment.repository.triplestore.AbstractOntologyRepository;

public class QueryLength extends AbstractTermRelevanceFeature {

    public QueryLength(AbstractOntologyRepository repository) {
        super(repository);
    }

    @Override
    public double getScore(TermQuery query, Term term) {
        return query.getSearchWords().size();
    }

    @Override
    public String getFeatureName() {
        return "Query_Length_Q";
    }
}

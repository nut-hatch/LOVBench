package experiment.feature.extraction.term.relevance;

import experiment.model.Ontology;
import experiment.model.Term;
import experiment.model.query.TermQuery;
import experiment.repository.triplestore.AbstractOntologyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

public class BooleanMatch extends AbstractTermRelevanceFeature {

    public static final String FEATURE_NAME = "Boolean_Match_T";

    private static final Logger log = LoggerFactory.getLogger( BooleanMatch.class );

    public BooleanMatch(AbstractOntologyRepository repository) {
        super(repository);
    }

    @Override
    public double getScore(TermQuery query, Term term) {
        Set<Term> termMatches = this.repository.getTermQueryMatch(query,new Ontology(term.getOntologyUriOfTerm()));
        return (termMatches.contains(term)) ? 1.0 : 0.0;
    }

    @Override
    public String getFeatureName() {
        return BooleanMatch.FEATURE_NAME;
    }
}

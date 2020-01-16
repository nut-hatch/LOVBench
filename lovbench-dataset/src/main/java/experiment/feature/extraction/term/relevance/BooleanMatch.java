package experiment.feature.extraction.term.relevance;

import experiment.model.Ontology;
import experiment.model.Term;
import experiment.model.query.TermQuery;
import experiment.repository.triplestore.AbstractOntologyRepository;

import java.util.Set;

public class BooleanMatch extends AbstractTermRelevanceFeature {

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
        return "Boolean_Match_T";
    }
}

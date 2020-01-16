package experiment.feature.extraction.term.relevance;

import experiment.model.Term;
import experiment.model.query.TermQuery;
import experiment.repository.triplestore.AbstractOntologyRepository;
import experiment.repository.triplestore.AbstractOntologySearchRepository;
import javafx.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class LabelSearch extends AbstractTermRelevanceFeature {

    AbstractOntologySearchRepository searchRepository;

    Map<TermQuery,Map<Term,Double>> searchScores = new HashMap<>();

    private static final Logger log = LoggerFactory.getLogger( LabelSearch.class );

    public LabelSearch(AbstractOntologyRepository repository, AbstractOntologySearchRepository searchRepository) {
        super(repository);
        this.searchRepository = searchRepository;
    }

    @Override
    public double getScore(TermQuery query, Term term) {
        if (!this.searchScores.containsKey(query)) {
            this.searchScores.put(query, this.searchRepository.search(query));
        }

        if (this.searchScores.get(query).containsKey(term)) {
            log.info(String.format("Query %s term %s score %s",query,term,this.searchScores.get(query).get(term)));
            return this.searchScores.get(query).get(term);
        } else {
            log.info(String.format("Query %s term %s score %s",query,term,"ZERO"));
            return 0.0;
        }

    }

    @Override
    public String getFeatureName() {
        return "LabelSearch_T";
    }
}

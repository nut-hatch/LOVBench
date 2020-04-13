package experiment.feature.extraction.term.relevance;

import experiment.model.Term;
import experiment.model.query.TermQuery;
import experiment.repository.triplestore.AbstractOntologyRepository;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * Computes the text relevance feature of DWRank.
 *
 * It counts how many keywords in the query match a given term.
 */
public class TextRelevancy extends AbstractTermRelevanceFeature {

    public static final String FEATURE_NAME = "Text_Relevancy_T";

    private static final Logger log = LoggerFactory.getLogger( TextRelevancy.class );

    public TextRelevancy(AbstractOntologyRepository repository) {
        super(repository);
    }

    @Override
    public double getScore(TermQuery query, Term term) {
        // counts the number of searchwords that have a match in a label of the term
        int matchCount = 0;
        Set<String> matchedLabels = this.repository.getTermQueryMatchLabels(query,term);

        if (matchedLabels != null && !matchedLabels.isEmpty()) {
            for (String searchWord : query.getSearchWords()) {
                for (String matchedLabel : matchedLabels) {
                    if (StringUtils.containsIgnoreCase(matchedLabel, searchWord)) {
                        matchCount++;
                        break;
                    }
                }
            }
        }
        return matchCount;
    }

    @Override
    public String getFeatureName() {
        return TextRelevancy.FEATURE_NAME;
    }
}

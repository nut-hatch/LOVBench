package experiment.feature.extraction.ontology.relevance;

import experiment.model.Ontology;
import experiment.model.Term;
import experiment.model.query.AbstractQuery;
import experiment.repository.triplestore.AbstractOntologyRepository;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;

/**
 * Computes the ClassMatchMeasure as specified by AKTiveRank.
 *
 * The score is based on a weighted sum exact and partial matched of a query in labels for classes in an ontology.
 */
public class PropertyMatchMeasure extends AbstractOntologyRelevanceFeature {

    private double alpha = 0.6;

    private double beta = 0.4;

    public static final String FEATURE_NAME = "Property_Match_O";

    private static final Logger log = LoggerFactory.getLogger( PropertyMatchMeasure.class );

    public PropertyMatchMeasure(AbstractOntologyRepository repository) {
        super(repository);
    }

    public PropertyMatchMeasure(AbstractOntologyRepository repository, double alpha, double beta) {
        super(repository);
        this.alpha = alpha;
        this.beta = beta;
    }

    @Override
    public double getScore(AbstractQuery query, Ontology ontology) {
        // get all matching classes and their label
        Map<Term,Set<String>> matchedProperties = this.repository.getPropertyQueryMatchRDFSLabels(query,ontology);

        int exactMatchCount = 0;
        int partialMatchCount = 0;

        // for each query word:
        for (String searchWord : query.getSearchWords()) {
            // for each matched property
            for (Map.Entry<Term,Set<String>> matchedProperty : matchedProperties.entrySet()) {
                // sum: exact matches property
                if (matchedProperty.getValue().contains(searchWord.toLowerCase())) {
                    exactMatchCount++;
                } else {
                    // sum: partial matched property
                    for (String matchedLabel : matchedProperty.getValue()) {
                        if (StringUtils.containsIgnoreCase(matchedLabel, searchWord)) {
                            partialMatchCount++;
                            break;
                        }
                    }
                }
            }

        }

        return this.alpha * exactMatchCount + this.beta * partialMatchCount;
    }

    @Override
    public String getFeatureName() {
        return PropertyMatchMeasure.FEATURE_NAME;
    }
}

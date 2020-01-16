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
public class ClassMatchMeasure extends AbstractOntologyRelevanceFeature {

    private double alpha = 0.6;

    private double beta = 0.4;

    private static final Logger log = LoggerFactory.getLogger( ClassMatchMeasure.class );

    public ClassMatchMeasure(AbstractOntologyRepository repository) {
        super(repository);
    }

    public ClassMatchMeasure(AbstractOntologyRepository repository, double alpha, double beta) {
        super(repository);
        this.alpha = alpha;
        this.beta = beta;
    }

    @Override
    public double getScore(AbstractQuery query, Ontology ontology) {
        // get all matching classes and their label
        Map<Term,Set<String>> matchedClasses = this.repository.getClassQueryMatchRDFSLabels(query,ontology);

        int exactMatchCount = 0;
        int partialMatchCount = 0;

        // for each query word:
        for (String searchWord : query.getSearchWords()) {
            // for each matched class
            for (Map.Entry<Term,Set<String>> matchedClass : matchedClasses.entrySet()) {
                // sum: exact matches classes
                if (matchedClass.getValue().contains(searchWord.toLowerCase())) {
                    exactMatchCount++;
                } else {
                    // sum: partial matched classes
                    for (String matchedLabel : matchedClass.getValue()) {
                        if (StringUtils.containsIgnoreCase(matchedLabel, searchWord)) {
                            partialMatchCount++;
                            break;
                        }
                    }
                }
            }

        }
        log.info(exactMatchCount+"");
        log.info(partialMatchCount+"");
        // compute CMM.
        return this.alpha * exactMatchCount + this.beta * partialMatchCount;
    }

    @Override
    public String getFeatureName() {
        return "Class_Match_O";
    }
}

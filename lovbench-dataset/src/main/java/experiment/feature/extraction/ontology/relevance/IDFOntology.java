package experiment.feature.extraction.ontology.relevance;

import experiment.feature.scoring.TFIDFScorer;
import experiment.model.Ontology;
import experiment.model.Term;
import experiment.model.query.AbstractQuery;
import experiment.repository.triplestore.AbstractOntologyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * Feature that extracts the IDF score for an ontology. It sums the IDF score for all terms in the ontology that match the query.
 *
 */
public class IDFOntology extends AbstractOntologyRelevanceFeature {

    /**
     * TFIDF scorer object.
     */
    TFIDFScorer tfidfScorer;

    public static final String FEATURE_NAME = "IDF_O";

    private static final Logger log = LoggerFactory.getLogger( IDFOntology.class );

    public IDFOntology(AbstractOntologyRepository repository, TFIDFScorer tfidfScorer) {
        super(repository);
        this.tfidfScorer = tfidfScorer;
    }

    @Override
    public double getScore(AbstractQuery query, Ontology ontology) {
        double idf = 0.0;

        Set<Term> matchedTerms = this.repository.getTermQueryMatch(query, ontology);

        if (matchedTerms != null && !matchedTerms.isEmpty()) {
            for (Term matchedTerm : matchedTerms) {
                // sum IDF for each matching URI term
                double idfForTerm = this.tfidfScorer.idf(matchedTerm);
                log.debug(String.format("IDF score for %s in ontology %s: %s", matchedTerm, ontology, idfForTerm));
                idf += idfForTerm;
            }
        } else {
            log.debug(String.format("Not matches for query %s and ontology %s. Score = 0", query.toString(), ontology));
        }

        return idf;
    }

    @Override
    public String getFeatureName() {
        return IDFOntology.FEATURE_NAME;
    }
}

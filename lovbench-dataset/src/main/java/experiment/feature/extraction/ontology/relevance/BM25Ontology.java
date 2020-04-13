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
 * Computes the BM25 score for an onotlogy.
 */
public class BM25Ontology extends AbstractOntologyRelevanceFeature {

    /**
     * The TFIDF scorer.
     */
    TFIDFScorer tfidfScorer;

    double k = 2.0;

    double b = 0.75;

    public static final String FEATURE_NAME = "BM25_O";

    private static final Logger log = LoggerFactory.getLogger( BM25Ontology.class );

    public BM25Ontology(AbstractOntologyRepository repository, TFIDFScorer tfidfScorer) {
        super(repository);
        this.tfidfScorer = tfidfScorer;
    }

    @Override
    public double getScore(AbstractQuery query, Ontology ontology) {
        double bm25 = 0.0;

        Set<Term> matchedTerms = this.repository.getTermQueryMatch(query, ontology);

        if (matchedTerms != null && !matchedTerms.isEmpty()) {
            for (Term matchedTerm : matchedTerms) {
                // sum IDF for each URI term
                double bm25ForTerm = this.tfidfScorer.idf(matchedTerm) *
                        (       (this.tfidfScorer.tf(matchedTerm, ontology) * this.k + 1 ) /
                                (this.tfidfScorer.tf(matchedTerm, ontology) + this.k * (1 - this.b + this.b * (this.repository.ontologySize(ontology) / this.repository.averageOntologySize())))
                        );
                log.debug(String.format("BM25 score for %s in ontology %s: %s", matchedTerm, ontology, bm25ForTerm));
                bm25 += bm25ForTerm;
            }
        } else {
            log.debug(String.format("No matches for query %s and ontology %s. BM25 score = 0", query.toString(), ontology));
        }

        return bm25;
    }

    @Override
    public String getFeatureName() {
        return BM25Ontology.FEATURE_NAME;
    }
}

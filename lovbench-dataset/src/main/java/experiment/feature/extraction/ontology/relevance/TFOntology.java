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
 * Feature that extracts the TF score for an ontology. It sums the TF score for all terms in the ontology that match the query.
 *
 */
public class TFOntology extends AbstractOntologyRelevanceFeature {

    /**
     * TFIDF scorer object.
     */
    TFIDFScorer tfidfScorer;

    private static final Logger log = LoggerFactory.getLogger( TFOntology.class );

    public TFOntology(AbstractOntologyRepository repository, TFIDFScorer tfidfScorer) {
        super(repository);
        this.tfidfScorer = tfidfScorer;
    }

    @Override
    public double getScore(AbstractQuery query, Ontology ontology) {
        double tf = 0.0;
        Set<Term> matchedTerms = this.repository.getTermQueryMatch(query, ontology);

        if (matchedTerms != null && !matchedTerms.isEmpty()) {
            for (Term matchedTerm : matchedTerms) {
                // sum TF for each URI term
                double tfForTerm = this.tfidfScorer.tf(matchedTerm, ontology);
                log.debug(String.format("TF score for %s in ontology %s: %s", matchedTerm, ontology, tfForTerm));
                tf += tfForTerm;
            }
        } else {
            log.debug(String.format("No matches for query %s and ontology %s. Score = 0", query.toString(), ontology));
        }

        return tf;
    }

    @Override
    public String getFeatureName() {
        return "TF_O";
    }

}

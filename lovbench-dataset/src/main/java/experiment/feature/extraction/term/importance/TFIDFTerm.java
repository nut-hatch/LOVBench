package experiment.feature.extraction.term.importance;

import experiment.feature.scoring.TFIDFScorer;
import experiment.model.Ontology;
import experiment.model.Term;
import experiment.repository.triplestore.AbstractOntologyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Class to compute the TD-IDF score for a term.
 *
 */
public class TFIDFTerm extends AbstractTermImportanceFeature {

    /**
     * The TDIDF scorer object.
     */
    TFIDFScorer tfidfScorer;

    private static final Logger log = LoggerFactory.getLogger( TFIDFTerm.class );

    public TFIDFTerm(AbstractOntologyRepository repository, TFIDFScorer tfidfScorer) {
        super(repository);
        this.tfidfScorer = tfidfScorer;
    }

    @Override
    public Map<Term, Double> computeScores(Set<Term> termSet) {
        Map<Term, Double> scores = new HashMap<>();
        for (Term term : termSet) {
            Ontology ontology = new Ontology(term.getOntologyUriOfTerm());
            double tfidfScore = this.tfidfScorer.tf(term, ontology) * this.tfidfScorer.idf(term);
            scores.put(term, tfidfScore);
        }
        this.setScores(scores);
        return scores;
    }

    @Override
    protected void computeAllScores() {
        for (Map.Entry<Ontology, Set<Term>> ontologyTerms : this.repository.getAllTerms().entrySet()) {
            Ontology ontology = ontologyTerms.getKey();
            for (Term term : ontologyTerms.getValue()) {
                double tfidfScore = this.tfidfScorer.tf(term, ontology) * this.tfidfScorer.idf(term);
                this.scores.put(term, tfidfScore);
            }
        }
    }

    //    @Override
//    public double getScore(TermQuery query, Term term) {
//        // For Term TFIDF: check if the term we want to score is in query match, if yes return tfidf, if not return 0
//        Map<Ontology, List<Term>> matchedOntologies = this.repository.getQueryMatch(query);
//        if (matchedOntologies != null && !matchedOntologies.isEmpty()) {
//            for (Map.Entry<Ontology, List<Term>> matchedTermsForOntology : matchedOntologies.entrySet()) {
//                if (matchedTermsForOntology.getValue().contains(term)) {
//                    log.debug(String.format("Term contained in match for query %s! Computing tf-idf score for term %s and ontologyuri %s", query.toString(), term.getTermUri(), matchedTermsForOntology.getKey()));
//                    return this.tfidfScorer.tf(term, matchedTermsForOntology.getKey()) * this.tfidfScorer.idf(term);
//                }
//            }
//        }
//        log.debug(term.getTermUri() + "not in match!! Score = 0.");
//        return 0.0;
//    }

    @Override
    public String getFeatureName() {
        return "TF_IDF_T";
    }

}

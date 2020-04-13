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
 * Class to extract the IDF feature of a term.
 *
 */
public class IDFTerm extends AbstractTermImportanceFeature {

    /**
     * The TDIDF scorer object.
     */
    TFIDFScorer tfidfScorer;

    public static final String FEATURE_NAME = "IDF_T";

    private static final Logger log = LoggerFactory.getLogger( IDFTerm.class );

    public IDFTerm(AbstractOntologyRepository repository, TFIDFScorer tfidfScorer) {
        super(repository);
        this.tfidfScorer = tfidfScorer;
    }

    @Override
    public Map<Term, Double> computeScores(Set<Term> termSet, Ontology ontology) {
        Map<Term, Double> scores = new HashMap<>();
        for (Term term : termSet) {
            double idfScore = this.tfidfScorer.idf(term);
            scores.put(term, idfScore);
        }
        this.scores.putAll(scores);
        return scores;
    }

//    @Override
//    protected void computeAllScores() {
//        this.scores = new HashMap<>();
//        for (Map.Entry<Ontology, Set<Term>> ontologyTerms : this.repository.getAllTerms().entrySet()) {
//            for (Term term : ontologyTerms.getValue()) {
//                double tfidfScore = this.tfidfScorer.idf(term);
//                this.scores.put(term, tfidfScore);
//            }
//        }
//    }
    //    @Override
//    public double getScore(TermQuery query, Term term) {
//        // For Term IDF: check if the term we want to score is in query match, if yes return idf, if not return 0
//        Map<Ontology, List<Term>> matchedOntologies = this.repository.getQueryMatch(query);
//        if (matchedOntologies != null && !matchedOntologies.isEmpty()) {
//            for (Map.Entry<Ontology, List<Term>> matchedTermsForOntology : matchedOntologies.entrySet()) {
//                if (matchedTermsForOntology.getValue().contains(term)) {
//                    log.debug(String.format("Term contained in match for query %s! Computing tf-idf score for term %s and ontologyuri %s", query.toString(), term.getTermUri(), matchedTermsForOntology.getKey()));
//                    return this.tfidfScorer.idf(term);
//                }
//            }
//        }
//        log.debug(term.getTermUri() + "not in match!! Score = 0.");
//        return 0.0;
//    }

    @Override
    public String getFeatureName() {
        return IDFTerm.FEATURE_NAME;
    }
}

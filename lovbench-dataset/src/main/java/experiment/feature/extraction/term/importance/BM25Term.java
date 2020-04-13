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

public class BM25Term extends AbstractTermImportanceFeature {

    /**
     * The TFIDF scorer.
     */
    TFIDFScorer tfidfScorer;

    double k = 2.0;

    double b = 0.75;

    public static final String FEATURE_NAME = "BM25_T";

    private static final Logger log = LoggerFactory.getLogger( BM25Term.class );

    public BM25Term(AbstractOntologyRepository repository, TFIDFScorer tfidfScorer) {
        super(repository);
        this.tfidfScorer = tfidfScorer;
    }

    public BM25Term(AbstractOntologyRepository repository, TFIDFScorer tfidfScorer, double k, double b) {
        super(repository);
        this.tfidfScorer = tfidfScorer;
        this.k = k;
        this.b = b;
    }

    @Override
    public Map<Term, Double> computeScores(Set<Term> termSet, Ontology ontology) {
        Map<Term, Double> scores = new HashMap<>();
        for (Term term : termSet) {
            if (ontology == null) {
                ontology = new Ontology(term.getOntologyUriOfTerm());
            }
            double bm25Score = this.tfidfScorer.idf(term) *
                    (       (this.tfidfScorer.tf(term, ontology) * this.k + 1 ) /
                            (this.tfidfScorer.tf(term, ontology) + this.k * (1 - this.b + this.b * (this.repository.ontologySize(ontology) / this.repository.averageOntologySize())))
                    );
            scores.put(term, bm25Score);
        }
        this.scores.putAll(scores);
        return scores;
    }

    @Override
    public String getFeatureName() {
        return BM25Term.FEATURE_NAME;
    }
}

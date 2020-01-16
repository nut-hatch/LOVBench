package experiment.feature.extraction.term.importance;

import experiment.feature.scoring.TFIDFScorer;
import experiment.model.Ontology;
import experiment.model.Term;
import experiment.repository.triplestore.AbstractOntologyRepository;

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
    protected void computeAllScores() {
        for (Map.Entry<Ontology, Set<Term>> ontologyTerms : this.repository.getAllTerms().entrySet()) {
            Ontology ontology = ontologyTerms.getKey();
            for (Term term : ontologyTerms.getValue()) {
                double bm25Score = this.tfidfScorer.idf(term) *
                    (       (this.tfidfScorer.tf(term, ontology) * this.k + 1 ) /
                            (this.tfidfScorer.tf(term, ontology) + this.k * (1 - this.b + this.b * (this.repository.ontologySize(ontology) / this.repository.averageOntologySize())))
                    );
                this.scores.put(term, bm25Score);
            }
        }
    }

    @Override
    public Map<Term, Double> computeScores(Set<Term> termSet) {
        Map<Term, Double> scores = new HashMap<>();
        for (Term term : termSet) {
            Ontology ontology = new Ontology(term.getOntologyUriOfTerm());
            double bm25Score = this.tfidfScorer.idf(term) *
                    (       (this.tfidfScorer.tf(term, ontology) * this.k + 1 ) /
                            (this.tfidfScorer.tf(term, ontology) + this.k * (1 - this.b + this.b * (this.repository.ontologySize(ontology) / this.repository.averageOntologySize())))
                    );
            scores.put(term, bm25Score);
        }
        this.setScores(scores);
        return scores;
    }

    @Override
    public String getFeatureName() {
        return "BM25_T";
    }
}

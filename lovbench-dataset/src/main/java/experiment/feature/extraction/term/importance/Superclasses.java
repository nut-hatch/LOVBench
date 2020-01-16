package experiment.feature.extraction.term.importance;

import experiment.feature.scoring.TermStatsScorer;
import experiment.model.Term;
import experiment.repository.triplestore.AbstractOntologyRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Superclasses extends AbstractTermImportanceFeature {

    TermStatsScorer termStatsScorer;

    public Superclasses(AbstractOntologyRepository repository, TermStatsScorer termStatsScorer) {
        super(repository);
        this.termStatsScorer = termStatsScorer;
    }

    @Override
    protected void computeAllScores() {

    }

    @Override
    public Map<Term, Double> computeScores(Set<Term> termSet) {
        Map<Term, Double> scores = new HashMap<>();
        for (Term term : termSet) {
            scores.put(term, (double)this.termStatsScorer.countSuperclasses(term));
        }
        this.setScores(scores);
        return scores;
    }

    @Override
    public String getFeatureName() {
        return "Superclasses_T";
    }
}

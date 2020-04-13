package experiment.feature.extraction.term.importance;

import experiment.feature.scoring.TermStatsScorer;
import experiment.model.Ontology;
import experiment.model.Term;
import experiment.repository.triplestore.AbstractOntologyRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Superclasses extends AbstractTermImportanceFeature {

    TermStatsScorer termStatsScorer;

    public static final String FEATURE_NAME = "Superclasses_T";

    public Superclasses(AbstractOntologyRepository repository, TermStatsScorer termStatsScorer) {
        super(repository);
        this.termStatsScorer = termStatsScorer;
    }

    @Override
    public Map<Term, Double> computeScores(Set<Term> termSet, Ontology ontology) {
        Map<Term, Double> scores = new HashMap<>();
        for (Term term : termSet) {
            double score = (double)this.termStatsScorer.countSuperclasses(term);
            scores.put(term, score);
        }
        this.scores.putAll(scores);
        return scores;
    }

    @Override
    public String getFeatureName() {
        return Superclasses.FEATURE_NAME;
    }
}

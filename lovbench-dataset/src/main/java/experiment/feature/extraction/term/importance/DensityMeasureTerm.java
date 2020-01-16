package experiment.feature.extraction.term.importance;

import experiment.feature.scoring.TermStatsScorer;
import experiment.model.Ontology;
import experiment.model.Term;
import experiment.repository.triplestore.AbstractOntologyRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Adopts the density measure for terms.
 */
public class DensityMeasureTerm extends AbstractTermImportanceFeature {

    private double weightSubClass = 1.0;

    private double weightSuperClass = 0.25;

    private double weightRelations = 0.5;

    private double weightSiblings = 0.5;

    TermStatsScorer termStatsScorer;

    public DensityMeasureTerm(AbstractOntologyRepository repository, TermStatsScorer termStatsScorer) {
        super(repository);
        this.termStatsScorer = termStatsScorer;
    }

    public DensityMeasureTerm(AbstractOntologyRepository repository, TermStatsScorer termStatsScorer, double weightSubClass, double weightSuperClass, double weightRelations, double weightSiblings) {
        super(repository);
        this.termStatsScorer = termStatsScorer;
        this.weightSubClass = weightSubClass;
        this.weightSuperClass = weightSuperClass;
        this.weightRelations = weightRelations;
        this.weightSiblings = weightSiblings;
    }

    @Override
    public Map<Term, Double> computeScores(Set<Term> termSet) {
        Map<Term, Double> scores = new HashMap<>();
        for (Term term : termSet) {
            int countSubClasses = this.termStatsScorer.countSubclasses(term);
            int countSuperClasses = this.termStatsScorer.countSuperclasses(term);
            int countRelations = this.termStatsScorer.countRelations(term);
            int countSiblings = this.termStatsScorer.countSiblings(term);
            double densityScore = (weightSubClass * countSubClasses + weightSuperClass * countSuperClasses + weightRelations * countRelations + weightSiblings * countSiblings);
            scores.put(term, densityScore);
        }
        this.setScores(scores);
        return scores;
    }

    @Override
    protected void computeAllScores() {
        Map<Ontology, Set<Term>> allTerms = this.repository.getAllTerms();
        for (Map.Entry<Ontology, Set<Term>> ontologyTerms : allTerms.entrySet()) {
            for (Term term : ontologyTerms.getValue()) {
                int countSubClasses = this.termStatsScorer.countSubclasses(term);
                int countSuperClasses = this.termStatsScorer.countSuperclasses(term);
                int countRelations = this.termStatsScorer.countRelations(term);
                int countSiblings = this.termStatsScorer.countSiblings(term);
                double densityScore = (weightSubClass * countSubClasses + weightSuperClass * countSuperClasses + weightRelations * countRelations + weightSiblings * countSiblings);
                this.scores.put(term,densityScore);
            }
        }
    }

    @Override
    public String getFeatureName() {
        return "Density_T";
    }
}

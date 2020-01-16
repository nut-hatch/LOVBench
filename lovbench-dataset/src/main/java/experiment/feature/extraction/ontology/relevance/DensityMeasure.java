package experiment.feature.extraction.ontology.relevance;

import experiment.feature.scoring.TermStatsScorer;
import experiment.model.Ontology;
import experiment.model.Term;
import experiment.model.query.AbstractQuery;
import experiment.model.query.enums.TermType;
import experiment.repository.triplestore.AbstractOntologyRepository;

import java.util.Set;

/**
 * Computes the density measure as specified by AKTiveRank.
 *
 * The score is based on a weighted sum of the count of subclasses, superclasses, relations and siblings for all matched classes for a query in an ontology, divided by the number of matches.
 */
public class DensityMeasure extends AbstractOntologyRelevanceFeature {

    private double weightSubClass = 1.0;

    private double weightSuperClass = 0.25;

    private double weightRelations = 0.5;

    private double weightSiblings = 0.5;

    TermStatsScorer termStatsScorer;

    public DensityMeasure(AbstractOntologyRepository repository, TermStatsScorer termStatsScorer) {
        super(repository);
        this.termStatsScorer = termStatsScorer;
    }

    @Override
    public double getScore(AbstractQuery query, Ontology ontology) {
        double densityScore = 0.0;
        Set<Term> queryMatch = this.repository.getTermQueryMatch(query,ontology, TermType.CLASS);

        for (Term term : queryMatch) {
            int countSubClasses = this.termStatsScorer.countSubclasses(term);
            int countSuperClasses = this.termStatsScorer.countSuperclasses(term);
            int countRelations = this.termStatsScorer.countRelations(term);
            int countSiblings = this.termStatsScorer.countSiblings(term);
            densityScore += (weightSubClass * countSubClasses + weightSuperClass * countSuperClasses + weightRelations * countRelations + weightSiblings * countSiblings);
        }

        if (queryMatch.size() > 0) {
            densityScore /= queryMatch.size();
        }
        return densityScore;
//        int countQueryMatches = this.repository.countClassMatches(query,ontology);
//        // if the query has no matches, no need to compute the values (score = 0)
//        if (countQueryMatches > 0) {
//            int countSubClasses = this.repository.countSubClassesOfQueryMatches(query, ontology);
//            int countSuperClasses = this.repository.countSuperClassesOfQueryMatches(query, ontology);
//            int countRelations = this.repository.countRelationsOfQueryMatches(query, ontology);
//            int countSiblings = this.repository.countSiblingsOfQueryMatches(query, ontology);
//            densityScore = (weightSubClass * countSubClasses + weightSuperClass * countSuperClasses + weightRelations * countRelations + weightSiblings * countSiblings) / countQueryMatches;
//        }
//        return densityScore;
    }

    @Override
    public String getFeatureName() {
        return "Density_O";
    }
}

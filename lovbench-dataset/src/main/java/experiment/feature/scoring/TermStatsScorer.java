package experiment.feature.scoring;

import experiment.model.Ontology;
import experiment.model.Term;
import experiment.repository.triplestore.AbstractOntologyRepository;

import java.util.HashMap;
import java.util.Map;

public class TermStatsScorer extends AbstractScorer {

    Map<Term, Integer> subclasses = new HashMap<>();

    Map<Term, Integer> superclasses = new HashMap<>();

    Map<Term, Integer> siblings = new HashMap<>();

    Map<Term, Integer> relations = new HashMap<>();

    Map<Term, Integer> subproperties = new HashMap<>();

    Map<Term, Integer> superproperties = new HashMap<>();

    /**
     * The repository for the ontology collection.
     */
    AbstractOntologyRepository repository;

    public TermStatsScorer(AbstractOntologyRepository repository) {
        this.repository = repository;
    }

    public int countSubclasses(Term term) {
        if (!this.subclasses.containsKey(term)) {
            this.subclasses.put(term, this.repository.countSubClasses(term, new Ontology(term.getOntologyUriOfTerm())));
        }
        return this.subclasses.get(term);
    }

    public int countSuperclasses(Term term) {
        if (!this.superclasses.containsKey(term)) {
            this.superclasses.put(term, this.repository.countSuperClasses(term, new Ontology(term.getOntologyUriOfTerm())));
        }
        return this.superclasses.get(term);
    }

    public int countSiblings(Term term) {
        if (!this.siblings.containsKey(term)) {
            this.siblings.put(term, this.repository.countSiblings(term, new Ontology(term.getOntologyUriOfTerm())));
        }
        return siblings.get(term);
    }

    public int countRelations(Term term) {
        if (!this.relations.containsKey(term)) {
            this.relations.put(term, this.repository.countRelations(term, new Ontology(term.getOntologyUriOfTerm())));
        }
        return relations.get(term);
    }

    public int countSubproperties(Term term) {
        if (!this.subproperties.containsKey(term)) {
            this.subproperties.put(term, this.repository.countSubProperties(term, new Ontology(term.getOntologyUriOfTerm())));
        }
        return subproperties.get(term);
    }

    public int countSuperproperties(Term term) {
        if (!this.superproperties.containsKey(term)) {
            this.superproperties.put(term, this.repository.countSuperProperties(term, new Ontology(term.getOntologyUriOfTerm())));
        }
        return superproperties.get(term);
    }
}

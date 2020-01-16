package experiment.repository.triplestore;

import experiment.model.Ontology;
import experiment.model.Term;
import experiment.model.query.AbstractQuery;
import experiment.model.query.TermQuery;
import experiment.model.query.enums.TermType;
import experiment.repository.file.ExperimentConfiguration;
import experiment.repository.triplestore.connector.AbstractConnector;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The abstract repository defines all functions that query the underlying ontology collection required to compute the features.
 * Individual implementations can use different connectors to different triple stores.
 *
 */
public abstract class AbstractOntologyRepository extends AbstractRepository {

    /**
     * String containing all classes that describe a property in the vocabulary.
     */
    public static final String TYPE_PROPERTY_VALUES = " rdf:Property rdfs:Property owl:DatatypeProperty owl:ObjectProperty owl:AnnotationProperty owl:OntologyProperty ";

    /**
     * String containing all classes that describe a class in the vocabulary.
     */
    public static final String TYPE_CLASS_VALUES = " rdfs:Class owl:Class ";

    /**
     * Query match condition for AKTIVERank.
     */
    public static final String QUERY_MATCH_AKTIVERANK = " rdfs:label ";

    /**
     * Query match condition for DWRank.
     */
    public static final String QUERY_MATCH_DWRANK = " rdfs:label|rdfs:comment|rdfs:description ";

    /**
     * Query match condition for LOV.
     */
    public static final String QUERY_MATCH_LOV = " rdfs:label|dce:title|dcterms:title|skos:prefLabel|rdfs:comment|rdfs:description|dce:description|dcterms:description|skos:altLabel ";

    private static final Logger log = LoggerFactory.getLogger( AbstractOntologyRepository.class );

    /**
     *
     * @param dbName database name.
     */
    public AbstractOntologyRepository(String dbName) {
        super(dbName);
    }


    /**
     * Returns the sparql value string for properties.
     *
     * @return
     */
    public static String getTypePropertyValuesString() {
        return "{" + TYPE_PROPERTY_VALUES + "}";
    }

    /**
     * Returns the sparql value string for classes.
     *
     * @return
     */
    public static String getTypeClassValuesString() {
        return "{" + TYPE_CLASS_VALUES + "}";
    }

    /**
     * Returns the sparql value string for properties and classes.
     *
     * @return
     */
    public static String getAllTypesValuesString() {
        return "{" + TYPE_PROPERTY_VALUES + TYPE_CLASS_VALUES + "}";
    }

    /**
     * Return the query match constraint sparql string based on the experiment configuration.
     *
     * @param bindingSubject
     * @param bindingObject
     * @return
     */
    public String getQueryMatchConstraint(String bindingSubject, String bindingObject) {
        switch (ExperimentConfiguration.getInstance().getQueryMatch()) {
            case AKTIVERANK: return bindingSubject + QUERY_MATCH_AKTIVERANK + bindingObject + " .";
            case DWRANK: return bindingSubject + QUERY_MATCH_DWRANK + bindingObject + " .";
            case LOV: return bindingSubject + QUERY_MATCH_LOV + bindingObject + " .";
        }
        log.error("Missing or false query match constraint configuration!!");
        return "";
    }

    /**
     * Returns all owl:imports triples in the collection.
     *
     * @return List<BindingSet>
     */
    public Set<Pair<Ontology, Ontology>> getOwlImports() {
        return this.getOwlImports(null);
    }

    /**
     * Returns all owl:imports triples for ontologies that match the query.
     *
     * @param query
     * @return List<BindingSet>
     */
    public Set<Pair<Ontology, Ontology>> getOwlImports(AbstractQuery query) {
        return this.getOwlImports(query, false);
    }

    /**
     * Returns all imports triples for all uses of external ontologies, even when no explicit owl:imports statement is given.
     *
     * @param query
     * @param bolImplicitImports
     * @return
     */
    public abstract Set<Pair<Ontology, Ontology>> getOwlImports(AbstractQuery query, boolean bolImplicitImports);

    /**
     * Counts the occuerences of a term in an ontology.
     *
     * @param term
     * @param ontology
     * @return int
     */
    public abstract int termFrequency(Term term, Ontology ontology);

    /**
     * Counts occurences of all terms of an ontology and returns the maximum.
     *
     * @param ontology
     * @return int
     */
    public abstract int maximumFrequency(Ontology ontology);

    /**
     * Counts the number of ontologies in the collection.
     *
     * @return int
     */
    public abstract int countOntologies();

    /**
     * Counts the number of ontologies that contain a term.
     *
     * @param term
     * @return int
     */
    public abstract int countOntologiesContainingTerm(Term term);

    /**
     * Gets all matching terms and the ontology in which they are defined of a query.
     *
     * @param query
     * @return Map<Ontology, List<Term>>
     * @deprecated inefficient, use getTermQueryMatch or getOntolgoyQueryMatch instead.
     */
    public abstract Map<Ontology, Set<Term>> getQueryMatch(AbstractQuery query);

    /**
     * Gets all matching terms in a specified ontology of a query.
     *
     * @param query
     * @param ontology
     * @return List<Term>
     */
    public abstract Set<Term> getTermQueryMatch(AbstractQuery query, Ontology ontology);

    /**
     * Gets all matching classes for a specified ontology based on the configured query match constraint.
     *
     * @param query
     * @param ontology
     * @return
     */
    public abstract Set<Term> getTermQueryMatch(AbstractQuery query, Ontology ontology, TermType termType);
//    public abstract List<Term> getMatchingClassesForQueryAndOntology(AbstractQuery query, Ontology ontology);

    public abstract Set<Ontology> getOntologyQueryMatch(AbstractQuery query);

    /**
     * Returns all the labels - based on the configured query match constraint - of specified term for a query.
     *
     * @param query
     * @param term
     * @return
     */
    public abstract Set<String> getTermQueryMatchLabels(TermQuery query, Term term);

    public abstract Map<Term,Set<String>> getClassQueryMatchRDFSLabels(AbstractQuery query, Ontology ontology);

    public abstract Map<Term,Set<String>> getPropertyQueryMatchRDFSLabels(AbstractQuery query, Ontology ontology);

    /**
     * Counts how many classes in an ontology match the query, only with exact matches on query match configuration.
     * @param query
     * @param ontology
     * @return
     */
    public abstract int countExactClassLabelMatches(AbstractQuery query, Ontology ontology);

    /**
     * Counts how many classes in an ontology match the query, only with partial matches on query match configuration.
     *
     * @param query
     * @param ontology
     * @return
     */
    public abstract int countPartialClassLabelMatches(AbstractQuery query, Ontology ontology);

    /**
     * Measures the ontology size as |axioms| * 3.
     * @param ontology
     * @return
     */
    public abstract int ontologySize(Ontology ontology);

    /**
     * Returns the average ontology size in the repository.
     * @return
     */
    public abstract double averageOntologySize();


    /**
     * Counts how many classes match the query based on the configured query match constraint.
     *
     * @param query
     * @param ontology
     * @return
     */
    public abstract int countClassMatches(AbstractQuery query, Ontology ontology);

    /**
     * Counts the number of sub classes of classes that match the query based on the configured query match constraint.
     * @param query
     * @param ontology
     * @return
     */
    public abstract int countSubClassesOfQueryMatches(AbstractQuery query, Ontology ontology);

    /**
     * Counts the number of super classes of classes that match the query based on the configured query match constraint.
     *
     * @param query
     * @param ontology
     * @return
     */
    public abstract int countSuperClassesOfQueryMatches(AbstractQuery query, Ontology ontology);

    /**
     *
     * Counts the number of relations defined for classes that match the query based on the configured query match constraint.
     *
     * @param query
     * @param ontology
     * @return
     */
    public abstract int countRelationsOfQueryMatches(AbstractQuery query, Ontology ontology);

    /**
     *
     * Counts the number of siblings of classes that match the query based on the configured query match constraint.
     *
     * @param query
     * @param ontology
     * @return
     */
    public abstract int countSiblingsOfQueryMatches(AbstractQuery query, Ontology ontology);

    /**
     * Count subclasses of a term in an ontology.
     *
     * @param term
     * @param ontology
     * @return
     */
    public abstract int countSubClasses(Term term, Ontology ontology);

    /**
     * Counts superclasses of a term in an ontology.
     *
     * @param term
     * @param ontology
     * @return
     */
    public abstract int countSuperClasses(Term term, Ontology ontology);

    /**
     * Counts relations of a term in an ontology.
     *
     * @param term
     * @param ontology
     * @return
     */
    public abstract int countRelations(Term term, Ontology ontology);

    /**
     * Counts siblings of a term in an ontology.
     *
     * @param term
     * @param ontology
     * @return
     */
    public abstract int countSiblings(Term term, Ontology ontology);

    public abstract int countSubProperties(Term term, Ontology ontology);

    public abstract int countSuperProperties(Term term, Ontology ontology);

    /**
     * Returns the length of the shortest path of two terms in an ontology.
     *
     * @param classQueryMatchStart
     * @param classQueryMatchEnd
     * @return
     */
    public abstract int getShortestPathLength(Term classQueryMatchStart, Term classQueryMatchEnd);

    /**
     * Gets the ontology graph in the form of triples from the RDF graph: node -> property -> node.
     *
     * @param ontology
     * @return
     */
    public List<Triple<Term, Term, Term>> getOntologyGraphTriples(Ontology ontology) {
        return this.getOntologyGraphTriples(ontology, false);
    }

    /**
     * Gets the ontology graph in the form of triples from the RDF graph: node -> property -> node, optionally in reverse.
     *
     * @param ontology
     * @param reversed
     * @return
     */
    public abstract List<Triple<Term, Term, Term>> getOntologyGraphTriples(Ontology ontology, boolean reversed);

    /**
     * Gets all ontologies contained in the repository.
     *
     * @return
     */
    public abstract Set<Ontology> getAllOntologies();

    /**
     * Gets all Terms contained in the repository.
     *
     * @return
     */
    public abstract Map<Ontology,Set<Term>> getAllTerms();

    /**
     * Gets all Terms contained in the repository.
     *
     * @return
     */
    public abstract Set<Term> getAllTerms(Ontology ontology);

}

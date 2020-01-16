package experiment.repository.triplestore;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import experiment.model.Ontology;
import experiment.model.Term;
import experiment.model.query.AbstractQuery;
import experiment.model.query.TermQuery;
import experiment.model.query.enums.TermType;
import experiment.model.query.enums.TypeFilter;
import experiment.repository.file.ExperimentConfiguration;
import experiment.repository.file.LOVPrefixes;
import experiment.repository.triplestore.connector.AbstractConnector;
import experiment.repository.triplestore.connector.StardogConnector;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.openrdf.query.BindingSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Implementation of a repository for the LOV collection in a Stardog triple store.
 *
 */
public class LOVRepository extends AbstractOntologyRepository {

    /**
     * Singleton object
     */
    private static LOVRepository lovRepository = null;

    /**
     * Caches the number of ontologies in the corpus.
     */
    int countOntologiesCache = 0;

    /**
     * Caches the count of ontologies that contain a term.
     */
    Map<Term,Integer> countOntologiesContainingTermCache = new HashMap<>();

    /**
     * Caches term matches for a query.
     */
    private Table<AbstractQuery, Ontology, List<Term>> termMatchesCache = HashBasedTable.create();

    /**
     * Caches the size of ontologies.
     */
    private Map<Ontology,Integer> ontologySizeCache = new HashMap<>();

    /**
     * Cahces the average size of ontologies in the repository.
     */
    private double averageOntologySizeCache;

    private static final Logger log = LoggerFactory.getLogger( LOVRepository.class );

    /**
     * Singleton object, has to be initialized with the database name.
     * @param dbName
     */
    private LOVRepository(String dbName) {
        super(dbName);
    }

    /**
     * Access to the Singleton object, has to be initialized with the database name.
     * @return LOVRepository
     */
    public static LOVRepository getInstance() {
        if (lovRepository == null) {
            log.error("Repository needs to be initiated with db name");
        }
        return lovRepository;
    }

    /**
     * Instantiates singleton object.
     *
     * @param dbname
     * @return LOVRepository
     */
    public static LOVRepository getInstance(String dbname) {
        if (lovRepository == null) {
            lovRepository = new LOVRepository(dbname);
        }
        return lovRepository;
    }

    /**
     * Finds query matches and inserts them in to the cache table.
     *
     * @param query query
     * @param sparql sparql formulation for match
     */
    private void addQueryMatchesToCache(AbstractQuery query, String sparql) {
        List<BindingSet> uriMatches = this.getConnector().selectQuery(sparql);

        // No match found at all
        if (uriMatches.isEmpty()) {
            log.info(String.format("Zero matches for query %s. Adding empty result to cache", query.toString()));
            this.termMatchesCache.put(query,new Ontology(""),new ArrayList<>());
        } else {
            // Adding term matches per ontology to cache
            for (BindingSet uriMatch : uriMatches) {
                Ontology matchedOntology = new Ontology(uriMatch.getBinding("g").getValue().stringValue());
                Term matchedTerm = new Term(uriMatch.getBinding("uri").getValue().stringValue());

                List<Term> termList = new ArrayList<>();
                termList.add(matchedTerm);

                // Insert to cache table
                if (!this.termMatchesCache.containsRow(query)) {
                    this.termMatchesCache.put(query, matchedOntology, termList);
                } else {
                    if (!this.termMatchesCache.contains(query, matchedOntology)) {
                        this.termMatchesCache.put(query, matchedOntology, termList);
                    } else {
                        this.termMatchesCache.get(query, matchedOntology).add(matchedTerm);
                    }
                }
            }
        }
    }

    @Override
    public AbstractConnector getConnector() {
        if (this.connector == null) {
            this.connector = new StardogConnector(this.dbName, ExperimentConfiguration.getInstance().getDbServer(), ExperimentConfiguration.getInstance().getDbUser(), ExperimentConfiguration.getInstance().getDbPassword());
        }
        return connector;
    }

//    @Override
//    public Map<Ontology, List<Term>> getQueryMatch(AbstractQuery query) {
//        if (!this.termMatchesCache.containsRow(query)) {
////            String sparql = "SELECT DISTINCT ?g ?uri WHERE { GRAPH ?g { ?uri rdfs:label|rdfs:comment|rdfs:description ?value . " + query.getQueryFilterString( "?uri", "?value") + "  FILTER (isuri(?uri)) } }";
//            String sparql = "SELECT DISTINCT ?g ?uri WHERE { GRAPH ?g { " + this.getQueryMatchConstraint("?uri", "?value") + " ?uri a ?valueType . " + query.getQueryFilterString( "<http://jena.hpl.hp.com/ARQ/function#localname>(?uri)", "?value") + "  FILTER (isuri(?uri)) . VALUES ?valueType " + AbstractOntologyRepository.getAllTypesValuesString() + " . } }";
//            this.addQueryMatchesToCache(query, sparql);
//        }
//        return this.termMatchesCache.row(query);
//    }

    @Override
    public Map<Ontology, Set<Term>> getQueryMatch(AbstractQuery query) {
        Map<Ontology, Set<Term>> queryMatch = new HashMap<>();
        String sparql = "SELECT DISTINCT ?g ?uri WHERE { GRAPH ?g { ?uri a ?valueType . OPTIONAL { " + this.getQueryMatchConstraint("?uri", "?value") + " } " + query.getQueryFilterString( "<http://jena.hpl.hp.com/ARQ/function#localname>(?uri)", "?value") + "  FILTER (isuri(?uri)) . VALUES ?valueType " + AbstractOntologyRepository.getAllTypesValuesString() + " . } }";
        List<BindingSet> uriMatches = this.getConnector().selectQuery(sparql);
        for (BindingSet uriMatch : uriMatches) {
            Ontology matchedOntology = new Ontology(uriMatch.getBinding("g").getValue().stringValue());
            Term matchedTerm = new Term(uriMatch.getBinding("uri").getValue().stringValue());
            if (queryMatch.containsKey(matchedOntology)) {
                queryMatch.get(matchedOntology).add(matchedTerm);
            } else {
                Set<Term> termSet = new HashSet<>();
                termSet.add(matchedTerm);
                queryMatch.put(matchedOntology,termSet);
            }
        }
        return queryMatch;
    }

//    @Override
//    public List<Term> getTermQueryMatch(AbstractQuery query, Ontology ontology) {
//        this.getQueryMatch(query);
//        return this.termMatchesCache.get(query, ontology);
//    }

    @Override
    public Set<Term> getTermQueryMatch(AbstractQuery query, Ontology ontology) {
//        Set<Term> queryMatch = new HashSet<>();
//        String sparql = "SELECT DISTINCT ?uri WHERE { GRAPH <"+ontology.getOntologyUri()+"> { ?uri a ?valueType . OPTIONAL { " + this.getQueryMatchConstraint("?uri", "?value") + " } " + query.getQueryFilterString( "<http://jena.hpl.hp.com/ARQ/function#localname>(?uri)", "?value") + "  FILTER (isuri(?uri)) . VALUES ?valueType " + AbstractOntologyRepository.getAllTypesValuesString() + " . } }";
//        List<BindingSet> uriMatches = this.getConnector().selectQuery(sparql);
//        for (BindingSet uriMatch : uriMatches) {
//            Term matchedTerm = new Term(uriMatch.getBinding("uri").getValue().stringValue());
//            queryMatch.add(matchedTerm);
//        }
//        return queryMatch;
        return this.getTermQueryMatch(query,ontology,TermType.ANY);
    }

    @Override
    public Set<Term> getTermQueryMatch(AbstractQuery query, Ontology ontology, TermType termType) {
        Set<Term> queryMatch = new HashSet<>();
        String termTypes = AbstractOntologyRepository.getAllTypesValuesString();
        switch (termType) {
            case CLASS:
                termTypes = AbstractOntologyRepository.getTypeClassValuesString();
                break;
            case PROPERTY:
                termTypes = AbstractOntologyRepository.getTypePropertyValuesString();
                break;
        }
        String sparql = "SELECT DISTINCT ?uri WHERE { GRAPH <"+ontology.getOntologyUri()+"> { ?uri a ?termType . OPTIONAL { " + this.getQueryMatchConstraint("?uri", "?value") + " } " + query.getQueryFilterString( "<http://jena.hpl.hp.com/ARQ/function#localname>(?uri)", "?value") + "  FILTER (isuri(?uri)) . VALUES ?termType " + termTypes + " . } }";
        List<BindingSet> uriMatches = this.getConnector().selectQuery(sparql);
        for (BindingSet uriMatch : uriMatches) {
            Term matchedTerm = new Term(uriMatch.getBinding("uri").getValue().stringValue());
            queryMatch.add(matchedTerm);
        }
        return queryMatch;
    }

    @Override
    public Set<Ontology> getOntologyQueryMatch(AbstractQuery query) {
        Set<Ontology> queryMatch = new HashSet<>();
        String sparql = "SELECT DISTINCT ?g WHERE { GRAPH ?g { ?uri a ?termType . OPTIONAL { " + this.getQueryMatchConstraint("?uri", "?value") + " } " + query.getQueryFilterString( "<http://jena.hpl.hp.com/ARQ/function#localname>(?uri)", "?value") + "  FILTER (isuri(?uri)) . VALUES ?termType " + AbstractOntologyRepository.getAllTypesValuesString() + " . } }";
        List<BindingSet> uriMatches = this.getConnector().selectQuery(sparql);
        for (BindingSet uriMatch : uriMatches) {
            Ontology matchedOntology = new Ontology(uriMatch.getBinding("g").getValue().stringValue());
            queryMatch.add(matchedOntology);
        }
        return queryMatch;
    }

    @Override
    public Set<String> getTermQueryMatchLabels(TermQuery query, Term term) {
        Set<String> matchingLabels = new HashSet<>();
//        String sparql = "SELECT DISTINCT ?label WHERE { GRAPH <" + term.getOntologyUriOfTerm() + "> { " + getQueryMatchConstraint("<" + term.getTermUri() + ">",  "?label") + " } " + query.getQueryFilterString("?label") + " }";
        String sparql = "SELECT DISTINCT (str(?label) as ?str_label) WHERE { GRAPH <" + term.getOntologyUriOfTerm() + "> { { BIND(<http://jena.hpl.hp.com/ARQ/function#localname>(<"+term.getTermUri()+">) AS ?label) . } UNION { " + getQueryMatchConstraint("<" + term.getTermUri() + ">",  "?label") + " } } "+ query.getQueryFilterString("?label") + " }";
        List<BindingSet> matchedLabels = this.getConnector().selectQuery(sparql);
        for (BindingSet matchedLabel : matchedLabels) {
            String label = matchedLabel.getBinding("str_label").getValue().stringValue();
            matchingLabels.add(label);
        }
        return matchingLabels;
    }


//    @Override
//    public List<Term> getMatchingClassesForQueryAndOntology(AbstractQuery query, Ontology ontology) {
//        // TODO: FIX! Merge with getTermQueryMatch => Done
//        List<Term> classMatches = new ArrayList<>();
//        if (!(query instanceof TermQuery && ( ((TermQuery) query).getFilterTypes() != null && !((TermQuery) query).getFilterTypes().equals(TypeFilter.CLASS)) )) {
//            String sparql = "SELECT distinct ?class WHERE { GRAPH <"+ontology.getOntologyUri()+"> { ?class a ?classType . " + this.getQueryMatchConstraint("?class", "?label") + " } VALUES ?classType " + AbstractOntologyRepository.getTypeClassValuesString() + " . " + query.getQueryFilterString("<http://jena.hpl.hp.com/ARQ/function#localname>(?class)", "?label") + " }";
//            List<BindingSet> matchedClasses = this.getConnector().selectQuery(sparql);
//            for (BindingSet matchedClass : matchedClasses) {
//                String strTerm = matchedClass.getBinding("class").getValue().stringValue();
//                log.info(String.format("Matched class: %s", strTerm));
//                Term term = new Term(strTerm);
//                log.info(term.getOntologyUriOfTerm());
//                // sometimes owl classes etc. come up as matches... because they have been pasted into the ontology? so we filter them out here
//                if (term.getOntologyUriOfTerm().equals(ontology.getOntologyUri())) {
//                    classMatches.add(term);
//                }
//            }
//        }
//        return classMatches;
//    }

    @Override
    public Map<Term,Set<String>> getClassQueryMatchRDFSLabels(AbstractQuery query, Ontology ontology) {
        Map<Term,Set<String>> matchingClassLabels = new HashMap<>();
        String sparql = "SELECT DISTINCT ?uri (str(?classLabel) as ?str_classLabel) WHERE { GRAPH <"+ontology.getOntologyUri()+"> { ?uri a ?classType . ?uri rdfs:label ?classLabel . "+ query.getQueryFilterString("?classLabel") + " FILTER (isuri(?uri)) . VALUES ?classType " + AbstractOntologyRepository.getTypeClassValuesString() + " . } }";
        List<BindingSet> uriMatches = this.getConnector().selectQuery(sparql);
        for (BindingSet uriMatch : uriMatches) {
            Term matchedClass = new Term(uriMatch.getBinding("uri").getValue().stringValue());
            String matchedLabel = uriMatch.getBinding("str_classLabel").getValue().stringValue().toLowerCase();
            if (!matchingClassLabels.containsKey(matchedClass)) {
                matchingClassLabels.put(matchedClass, new HashSet<>());
            }
            matchingClassLabels.get(matchedClass).add(matchedLabel);
        }
        return matchingClassLabels;
    }

    @Override
    public Map<Term,Set<String>> getPropertyQueryMatchRDFSLabels(AbstractQuery query, Ontology ontology) {
        Map<Term,Set<String>> matchingPropertyLabels = new HashMap<>();
        String sparql = "SELECT DISTINCT ?uri (str(?propertyLabel) as ?str_propertyLabel) WHERE { GRAPH <"+ontology.getOntologyUri()+"> { ?uri a ?propertyType . ?uri rdfs:label ?propertyLabel . "+ query.getQueryFilterString("?propertyLabel") + " FILTER (isuri(?uri)) . VALUES ?propertyType " + AbstractOntologyRepository.getTypePropertyValuesString() + " . } }";
        List<BindingSet> uriMatches = this.getConnector().selectQuery(sparql);
        for (BindingSet uriMatch : uriMatches) {
            Term matchedProperty = new Term(uriMatch.getBinding("uri").getValue().stringValue());
            String matchedLabel = uriMatch.getBinding("str_propertyLabel").getValue().stringValue().toLowerCase();
            if (!matchingPropertyLabels.containsKey(matchedProperty)) {
                matchingPropertyLabels.put(matchedProperty, new HashSet<>());
            }
            matchingPropertyLabels.get(matchedProperty).add(matchedLabel);
        }
        return matchingPropertyLabels;
    }


    @Override
    public Set<Pair<Ontology, Ontology>> getOwlImports(AbstractQuery query, boolean bolImplicitImports) {
        Set<Pair<Ontology, Ontology>> importTriples = new HashSet<>();
        String sparql = "SELECT DISTINCT ?importingOntology ?importedOntology WHERE { ?importingOntology owl:imports ?importedOntology . }";

        if (query != null) {
            //@TODO add query match constraint with OR filter for a b c
            sparql = "SELECT DISTINCT ?importingOntology ?importedOntology WHERE { ?importingOntology owl:imports ?importedOntology . { SELECT DISTINCT ?importedOntology WHERE { GRAPH ?importedOntology { ?a ?b ?c . " + query.getQueryFilterString("?a", "?b", "?c") + " } } } } ";
        }

//        String sparql = "SELECT DISTINCT ?importingOntology ?importedOntology WHERE { ?importingOntology owl:imports ?importedOntology . { SELECT DISTINCT ?importedOntology WHERE { GRAPH ?importedOntology { ?a ?b ?c . " + queryFilter + " } } } } ";

        List<BindingSet> importResults = this.getConnector().selectQuery(sparql);

        for (BindingSet importResult : importResults) {
            String fromOntology = importResult.getValue("importingOntology").stringValue();
            String toOntology = importResult.getValue("importedOntology").stringValue();
            importTriples.add(Pair.of(new Ontology(fromOntology), new Ontology(toOntology)));
        }

        int countExplicit = importTriples.size();
        log.info(String.format("Count of explicit import statements: %s", importTriples.size()));

        if (bolImplicitImports) {
            for (Ontology ontology : this.getAllOntologies()) {
                String ontologyTermPrefix = LOVPrefixes.getInstance().getTermPrefixForOntologyPrefix(ontology.getOntologyPrefix());
                String implicitImportsSparql = "SELECT DISTINCT ?a ?b ?c WHERE { Graph <" + ontology.getOntologyUri() + "> { ?a ?b ?c . filter ( ( isiri(?a) && !strstarts(str(?a), \"" + ontologyTermPrefix + "\") && !strstarts(str(?a), \"" + ontology.getOntologyUri() + "\") && !strstarts(str(?a), \"http://www.w3.org/1999/02/22-rdf-syntax-ns\") && !strstarts(str(?a), \"http://www.w3.org/2000/01/rdf-schema\") && !strstarts(str(?a), \"http://www.w3.org/2002/07/owl\") && !strstarts(str(?a), \"http://www.w3.org/2001/XMLSchema\") ) || ( isiri(?b) && !strstarts(str(?b), \"" + ontologyTermPrefix + "\") && !strstarts(str(?b), \"" + ontology.getOntologyUri() + "\") && !strstarts(str(?b), \"http://www.w3.org/1999/02/22-rdf-syntax-ns\") && !strstarts(str(?b), \"http://www.w3.org/2000/01/rdf-schema\") && !strstarts(str(?b), \"http://www.w3.org/2002/07/owl\")  && !strstarts(str(?b), \"http://www.w3.org/2001/XMLSchema\") ) || ( isiri(?c) && !strstarts(str(?c), \"" + ontologyTermPrefix + "\") && !strstarts(str(?c), \"" + ontology.getOntologyUri() + "\") && !strstarts(str(?c), \"http://www.w3.org/1999/02/22-rdf-syntax-ns\") && !strstarts(str(?c), \"http://www.w3.org/2000/01/rdf-schema\") && !strstarts(str(?c), \"http://www.w3.org/2002/07/owl\") && !strstarts(str(?c), \"http://www.w3.org/2001/XMLSchema\") ) ) . } }";
                List<BindingSet> implicitImports = this.getConnector().selectQuery(implicitImportsSparql);
                if (implicitImports != null && !implicitImports.isEmpty()) {
                    for (BindingSet implicitImport : implicitImports) {
                        Term subject = new Term(implicitImport.getValue("a").stringValue());
                        Term predicate = new Term(implicitImport.getValue("b").stringValue());
                        Term object = new Term(implicitImport.getValue("c").stringValue());
                        if (LOVPrefixes.getInstance().isFullUri(subject.getTermUri()) && !subject.getOntologyUriOfTerm().equals(ontology.getOntologyUri()) && !subject.getTermUri().equals(ontology.getOntologyUri())) {
                            Pair<Ontology,Ontology> newImplicitImport = Pair.of(ontology, new Ontology(subject.getOntologyUriOfTerm()));
                            if (!importTriples.contains(newImplicitImport)) {
                                importTriples.add(newImplicitImport);
                            }
                        }
                        if (LOVPrefixes.getInstance().isFullUri(predicate.getTermUri()) && !predicate.getOntologyUriOfTerm().equals(ontology.getOntologyUri()) && !predicate.getTermUri().equals(ontology.getOntologyUri())) {
                            Pair<Ontology,Ontology> newImplicitImport = Pair.of(ontology, new Ontology(predicate.getOntologyUriOfTerm()));
                            if (!importTriples.contains(newImplicitImport)) {
                                importTriples.add(newImplicitImport);
                            }
                        }
                        if (LOVPrefixes.getInstance().isFullUri(object.getTermUri()) && !object.getOntologyUriOfTerm().equals(ontology.getOntologyUri()) && !object.getTermUri().equals(ontology.getOntologyUri())) {
                            Pair<Ontology,Ontology> newImplicitImport = Pair.of(ontology, new Ontology(object.getOntologyUriOfTerm()));
                            if (!importTriples.contains(newImplicitImport)) {
                                importTriples.add(newImplicitImport);
                            }
                        }
                    }
                }
            }
            log.info(String.format("Count of implicit import statements: %s", importTriples.size()-countExplicit));
        }

        return importTriples;
    }

    @Override
    public int termFrequency(Term term, Ontology ontology) {
        String sparql = "SELECT (COUNT(*) AS ?termFrequency) WHERE { GRAPH <"+ontology.getOntologyUri()+"> { ?a ?b ?c . FILTER (?a=<"+term.getTermUri()+"> || ?b=<"+term.getTermUri()+"> || ?c=<"+term.getTermUri()+">) . } } ";
        if (!term.getAlternativeUri().isEmpty()) {
            sparql = "SELECT (COUNT(*) AS ?termFrequency) WHERE { GRAPH <"+ontology.getOntologyUri()+"> { ?a ?b ?c . FILTER (?a=<"+term.getTermUri()+"> || ?b=<"+term.getTermUri()+"> || ?c=<"+term.getTermUri()+"> || ?a=<"+term.getAlternativeUri()+"> || ?b=<"+term.getAlternativeUri()+"> || ?c=<"+term.getAlternativeUri()+">) . } } ";
        }
        int termFrequency = Integer.parseInt(this.getConnector().selectQuery(sparql).get(0).getBinding("termFrequency").getValue().stringValue());
        return termFrequency;
    }

    @Override
    public int maximumFrequency(Ontology ontology) {
//        String sparql = "SELECT (max(?termFrequencies) as ?maximumFrequency) WHERE { SELECT ?uri (COUNT(*) as ?termFrequencies) WHERE { GRAPH <"+ontology.getOntologyUri()+"> { ?a ?b ?c . { SELECT DISTINCT ?uri WHERE { GRAPH <"+ontology.getOntologyUri() +"> { ?uri a ?type . } VALUES ?type { rdf:Property rdfs:Property owl:DatatypeProperty owl:ObjectProperty rdfs:Class owl:Class } } } filter(regex(str(?a), CONCAT(\"^\",str(?uri),\"$\")) || regex(str(?b), CONCAT(\"^\",str(?uri),\"$\")) || regex(str(?c), CONCAT(\"^\",str(?uri),\"$\"))) . } } group by ?uri }";
        String sparql = "SELECT (max(?termFrequencies) as ?maximumFrequency) WHERE { SELECT ?uri (COUNT(*) as ?termFrequencies) WHERE { GRAPH <"+ontology.getOntologyUri()+"> { ?a ?b ?c . { SELECT DISTINCT ?uri WHERE { GRAPH <"+ontology.getOntologyUri() +"> { ?uri a ?type . } VALUES ?type " + AbstractOntologyRepository.getAllTypesValuesString() + " } } filter(?a=?uri || ?b=?uri || ?c=?uri) . } } group by ?uri }";
        return Integer.parseInt(this.getConnector().selectQuery(sparql).get(0).getBinding("maximumFrequency").getValue().stringValue());
    }

    @Override
    public int countOntologies() {
        if (this.countOntologiesCache == 0) {
            String sparql = "SELECT (count(distinct ?g) as ?ontologyCount) WHERE { GRAPH ?g { ?a ?b ?c } } ";
            this.countOntologiesCache = Integer.parseInt(this.getConnector().selectQuery(sparql).get(0).getBinding("ontologyCount").getValue().stringValue());
        }
        return this.countOntologiesCache;
    }

    @Override
    public int countOntologiesContainingTerm(Term term) {
        if (!this.countOntologiesContainingTermCache.containsKey(term)) {
            String sparql = "SELECT (count(distinct ?g) as ?ontologyCount) WHERE { GRAPH ?g { ?a ?b ?c . FILTER (?a=<"+term.getTermUri()+"> || ?b=<"+term.getTermUri()+"> || ?c=<"+term.getTermUri()+">) . } } ";
            if (!term.getAlternativeUri().isEmpty()) {
                sparql = "SELECT (count(distinct ?g) as ?ontologyCount) WHERE { GRAPH ?g { ?a ?b ?c . FILTER (?a=<"+term.getTermUri()+"> || ?b=<"+term.getTermUri()+"> || ?c=<"+term.getTermUri()+"> || ?a=<"+term.getAlternativeUri()+"> || ?b=<"+term.getAlternativeUri()+"> || ?c=<"+term.getAlternativeUri()+">) . } } ";
            }
            int count = Integer.parseInt(this.getConnector().selectQuery(sparql).get(0).getBinding("ontologyCount").getValue().stringValue());
            this.countOntologiesContainingTermCache.put(term,count);
        }
        return this.countOntologiesContainingTermCache.get(term);
    }

    @Override
    public int ontologySize(Ontology ontology) {
        if (!this.ontologySizeCache.containsKey(ontology)) {
            String sparql = "SELECT (COUNT(?s)*3 AS ?ontologySize) WHERE { GRAPH <"+ontology.getOntologyUri()+"> { ?s ?p ?o } }";
            int size = Integer.parseInt(this.getConnector().selectQuery(sparql).get(0).getBinding("ontologySize").getValue().stringValue());
            this.ontologySizeCache.put(ontology,size);
        }
        return this.ontologySizeCache.get(ontology);
    }

    @Override
    public double averageOntologySize() {
        if (this.averageOntologySizeCache == 0) {
            String sparql = "SELECT (COUNT(?s)*3 AS ?corpusSize) WHERE { GRAPH ?g { ?s ?p ?o } } ";
            int corpusSize = Integer.parseInt(this.getConnector().selectQuery(sparql).get(0).getBinding("corpusSize").getValue().stringValue());
            this.averageOntologySizeCache = corpusSize / this.countOntologies();
        }
        return this.averageOntologySizeCache;
    }

    @Override
    public int countExactClassLabelMatches(AbstractQuery query, Ontology ontology) {
        int exactClassLabelMatches = 0;
        // If it is a term query with a filter that does not correspond to a class, then there are no matches! => skip query
        if (!(query instanceof TermQuery && ( ((TermQuery) query).getFilterTypes() != null && !((TermQuery) query).getFilterTypes().equals(TypeFilter.CLASS)) )) {
            for (String searchWord : query.getSearchWords()) {
                String sparql = "SELECT (COUNT(DISTINCT ?class) as ?exactClassLabelMatches) WHERE { GRAPH <"+ontology.getOntologyUri()+"> { ?class a ?classType . " + this.getQueryMatchConstraint("?class", "?label") + " } VALUES ?classType " + AbstractOntologyRepository.getTypeClassValuesString() + " . filter(lcase(str(?label))=lcase(\""+searchWord+"\")) }";
                int exactClassLabelMatchesForSearchWord = Integer.parseInt(this.getConnector().selectQuery(sparql).get(0).getBinding("exactClassLabelMatches").getValue().stringValue());
                log.debug(String.format("EMM score for searchWord %s in ontology %s: %s", searchWord, ontology.getOntologyUri(), exactClassLabelMatchesForSearchWord));
                exactClassLabelMatches += exactClassLabelMatchesForSearchWord;
            }
        }

        log.debug(String.format("Total EMM score for query %s in ontology %s: %s", query.toString(), ontology.getOntologyUri(), exactClassLabelMatches));
        return exactClassLabelMatches;
    }

    @Override
    public int countPartialClassLabelMatches(AbstractQuery query, Ontology ontology) {
        int partialClassLabelMatches = 0;

        // If it is a term query with a filter that does not correspond to a class, then there are no class matches! => skip query
        if (!(query instanceof TermQuery && ( ((TermQuery) query).getFilterTypes() != null && !((TermQuery) query).getFilterTypes().equals(TypeFilter.CLASS)) )) {
            // If multiple searchWords match a class the count goes up!
            for (String searchWord : query.getSearchWords()) {
                String sparql = "SELECT (COUNT(DISTINCT ?class) as ?partialClassLabelMatches) WHERE { GRAPH <"+ontology.getOntologyUri()+"> { ?class a ?classType . " + this.getQueryMatchConstraint("?class", "?label") + " } VALUES ?classType " + AbstractOntologyRepository.getTypeClassValuesString() + " . filter(contains(lcase(str(?label)),lcase(\""+searchWord+"\"))) }";
                int exactClassLabelMatchesForSearchWord = Integer.parseInt(this.getConnector().selectQuery(sparql).get(0).getBinding("partialClassLabelMatches").getValue().stringValue());
                log.debug(String.format("PMM score for searchWord %s in ontology %s: %s", searchWord, ontology.getOntologyUri(), exactClassLabelMatchesForSearchWord));
                partialClassLabelMatches += exactClassLabelMatchesForSearchWord;
            }
        }
        log.debug(String.format("Total PMM score for query %s in ontology %s: %s", query.toString(), ontology.getOntologyUri(), partialClassLabelMatches));
        return partialClassLabelMatches;
    }

    @Override
    public int countClassMatches(AbstractQuery query, Ontology ontology) {
        int countClassMatches = 0;

        // If it is a term query with a filter that does not correspond to a class, then there are no class matches! => skip query
        if (!(query instanceof TermQuery && ( ((TermQuery) query).getFilterTypes() != null && !((TermQuery) query).getFilterTypes().equals(TypeFilter.CLASS)) )) {
            String sparql = "SELECT (COUNT(DISTINCT ?class) as ?countClassMatches) WHERE { GRAPH <"+ontology.getOntologyUri()+"> { ?class a ?classType . " + this.getQueryMatchConstraint("?class", "?label") + " } VALUES ?classType " + AbstractOntologyRepository.getTypeClassValuesString() + " . " + query.getQueryFilterString("<http://jena.hpl.hp.com/ARQ/function#localname>(?class)", "?label") + " }";
            countClassMatches = Integer.parseInt(this.getConnector().selectQuery(sparql).get(0).getBinding("countClassMatches").getValue().stringValue());
        }
        log.debug(String.format("Count of class matches for query %s in ontology %s: %s", query, ontology.getOntologyUri(), countClassMatches));
        return countClassMatches;
    }

    @Override
    public int countSubClasses(Term term, Ontology ontology) {
        String sparql = "SELECT (count(distinct ?subClass) as ?countSubClass) WHERE { GRAPH <"+ontology.getOntologyUri()+"> { ?subClass rdfs:subClassOf+ <"+term.getTermUri()+">  . } }";
        return Integer.parseInt(this.getConnector().selectQuery(sparql).get(0).getBinding("countSubClass").getValue().stringValue());
    }

    @Override
    public int countSubClassesOfQueryMatches(AbstractQuery query, Ontology ontology) {
        int countSubClasses = 0;

        // If it is a term query with a filter that does not correspond to a class, then there are no class matches! => skip query
        if (!(query instanceof TermQuery && ( ((TermQuery) query).getFilterTypes() != null && !((TermQuery) query).getFilterTypes().equals(TypeFilter.CLASS)) )) {
            String sparql = "SELECT (sum(?countSubClass) as ?countSubClassSum) WHERE { SELECT ?class (count(distinct ?subClass) as ?countSubClass) WHERE { GRAPH <"+ontology.getOntologyUri()+"> { ?class a ?classType . ?subClass rdfs:subClassOf+ ?class . " + this.getQueryMatchConstraint("?class", "?label") + " } VALUES ?classType " + AbstractOntologyRepository.getTypeClassValuesString() + " . " + query.getQueryFilterString("?label", "<http://jena.hpl.hp.com/ARQ/function#localname>(?class)") + " } group by ?class }";
            countSubClasses = Integer.parseInt(this.getConnector().selectQuery(sparql).get(0).getBinding("countSubClassSum").getValue().stringValue());
        }
        log.debug(String.format("Total count of sub classes for query %s in ontology %s: %s", query.toString(), ontology.getOntologyUri(), countSubClasses));
        return countSubClasses;
    }


    @Override
    public int countSuperClasses(Term term, Ontology ontology) {
        String sparql = "SELECT (count(distinct ?superClass) as ?countSuperClass) WHERE { GRAPH <"+ontology.getOntologyUri()+"> { <"+term.getTermUri()+"> rdfs:subClassOf+ ?superClass . } }";
        return Integer.parseInt(this.getConnector().selectQuery(sparql).get(0).getBinding("countSuperClass").getValue().stringValue());
    }

    @Override
    public int countSuperClassesOfQueryMatches(AbstractQuery query, Ontology ontology) {
        int countSuperClasses = 0;

        // If it is a term query with a filter that does not correspond to a class, then there are no class matches! => skip query
        if (!(query instanceof TermQuery && ( ((TermQuery) query).getFilterTypes() != null && !((TermQuery) query).getFilterTypes().equals(TypeFilter.CLASS)) )) {
            String sparql = "SELECT (sum(?countSuperClass) as ?countSuperClassSum) WHERE { SELECT ?class (count(distinct ?superClass) as ?countSuperClass) WHERE { GRAPH <"+ontology.getOntologyUri()+"> { ?class a ?classType . ?class rdfs:subClassOf+ ?superClass . " + this.getQueryMatchConstraint("?class", "?label") + " } VALUES ?classType " + AbstractOntologyRepository.getTypeClassValuesString() + " . " + query.getQueryFilterString("?label", "<http://jena.hpl.hp.com/ARQ/function#localname>(?class)") + " } group by ?class }";
            countSuperClasses = Integer.parseInt(this.getConnector().selectQuery(sparql).get(0).getBinding("countSuperClassSum").getValue().stringValue());
        }
        log.debug(String.format("Total count of super classes for query %s in ontology %s: %s", query.toString(), ontology.getOntologyUri(), countSuperClasses));
        return countSuperClasses;
    }


    @Override
    public int countRelations(Term term, Ontology ontology) {
        String sparql = "SELECT distinct (count(distinct ?classRelation) as ?countClassRelations)  WHERE { GRAPH <"+ontology.getOntologyUri()+"> { ?classRelation a ?propertyType . ?classRelation rdf:domain|rdfs:domain|<http://schema.org/domainIncludes> <"+term.getTermUri()+"> . } VALUES ?propertyType " + AbstractOntologyRepository.getTypePropertyValuesString() + " . }";
        return Integer.parseInt(this.getConnector().selectQuery(sparql).get(0).getBinding("countClassRelations").getValue().stringValue());
    }

    @Override
    public int countRelationsOfQueryMatches(AbstractQuery query, Ontology ontology) {
        int countRelations = 0;
        // If it is a term query with a filter that does not correspond to a class, then there are no class matches! => skip query
        if (!(query instanceof TermQuery && ( ((TermQuery) query).getFilterTypes() != null && !((TermQuery) query).getFilterTypes().equals(TypeFilter.CLASS)) )) {
//            String sparql = "SELECT (sum(?countRelations) as ?countRelationsSum) WHERE { SELECT ?class (count(distinct ?relation) as ?countRelation) WHERE { GRAPH <"+ontology.getOntologyUri()+"> { ?class a ?classType . ?class ?relation ?anotherClass . " + this.getQueryMatchConstraint("?class", "?label") + " } VALUES ?classType " + AbstractRepository.getTypeClassValuesString() + " . " + query.getQueryFilterString("?class") + " } group by ?class }";
            String sparql = "select (sum(?countClassRelations) as ?countClassRelationsSum) WHERE { SELECT ?class (count(distinct ?classRelation) as ?countClassRelations) WHERE { GRAPH <"+ontology.getOntologyUri()+"> { ?class a ?classType . ?classRelation a ?propertyType . ?class rdfs:subClassOf* ?superClass . ?classRelation rdf:domain|rdfs:domain|<http://schema.org/domainIncludes> ?superClass . " + this.getQueryMatchConstraint("?class", "?label") + " } VALUES ?classType " + AbstractOntologyRepository.getTypeClassValuesString() + " VALUES ?propertyType " + AbstractOntologyRepository.getTypePropertyValuesString() + " . " + query.getQueryFilterString("?label", "<http://jena.hpl.hp.com/ARQ/function#localname>(?class)") + " } group by ?class } ";
            countRelations = Integer.parseInt(this.getConnector().selectQuery(sparql).get(0).getBinding("countClassRelationsSum").getValue().stringValue());
        }
        log.debug(String.format("Count of class relations for query %s in ontology %s: %s", query, ontology.getOntologyUri(), countRelations));
        return countRelations;
    }


    @Override
    public int countSiblings(Term term, Ontology ontology) {
        String sparql = "SELECT (count(distinct ?siblingClasses) as ?countClassSiblings) WHERE { GRAPH <"+ontology.getOntologyUri()+"> { <"+term.getTermUri()+"> rdfs:subClassOf ?superClass . ?siblingClasses rdfs:subClassOf ?superClass . } FILTER ( ?superClass not in ( owl:Thing )) }";
        return Integer.parseInt(this.getConnector().selectQuery(sparql).get(0).getBinding("countClassSiblings").getValue().stringValue());
    }

    @Override
    public int countSiblingsOfQueryMatches(AbstractQuery query, Ontology ontology) {
        int countSiblings = 0;
        // If it is a term query with a filter that does not correspond to a class, then there are no class matches! => skip query
        if (!(query instanceof TermQuery && ( ((TermQuery) query).getFilterTypes() != null && !((TermQuery) query).getFilterTypes().equals(TypeFilter.CLASS)) )) {
            String sparql = "select (sum(?countClassSiblings) as ?countClassSiblingsSum) WHERE { SELECT ?class (count(distinct ?siblingClasses) as ?countClassSiblings) WHERE { GRAPH <"+ontology.getOntologyUri()+"> { ?class a ?classType . ?class rdfs:subClassOf ?superClass . ?siblingClasses rdfs:subClassOf ?superClass . " + this.getQueryMatchConstraint("?class", "?label") + " } VALUES ?classType " + AbstractOntologyRepository.getTypeClassValuesString() + " . FILTER ( ?superClass not in ( owl:Thing )) . " + query.getQueryFilterString("?label", "<http://jena.hpl.hp.com/ARQ/function#localname>(?class)") + " } group by ?class } ";
            countSiblings = Integer.parseInt(this.getConnector().selectQuery(sparql).get(0).getBinding("countClassSiblingsSum").getValue().stringValue());
        }
        log.debug(String.format("Count of class siblings for query %s in ontology %s: %s", query, ontology.getOntologyUri(), countSiblings));
        return countSiblings;
    }

    @Override
    public int countSubProperties(Term term, Ontology ontology) {
        String sparql = "SELECT (count(distinct ?subproperty) as ?count_subproperties) WHERE { GRAPH <"+ontology.getOntologyUri()+"> { ?subproperty rdfs:subPropertyOf+ <"+term.getTermUri()+">  .  } }";
        return Integer.parseInt(this.getConnector().selectQuery(sparql).get(0).getBinding("count_subproperties").getValue().stringValue());
    }

    @Override
    public int countSuperProperties(Term term, Ontology ontology) {
        String sparql = "SELECT (count(distinct ?superproperty) as ?count_superproperties) WHERE { GRAPH <"+ontology.getOntologyUri()+"> { <"+term.getTermUri()+"> rdfs:subPropertyOf+ ?superproperty  .  } }";
        return Integer.parseInt(this.getConnector().selectQuery(sparql).get(0).getBinding("count_superproperties").getValue().stringValue());
    }

    @Override
    public int getShortestPathLength(Term classQueryMatchStart, Term classQueryMatchEnd) {
        String sparql = "paths shortest start ?x = <"+classQueryMatchStart.getTermUri()+"> END ?y = <"+classQueryMatchEnd.getTermUri()+"> via { {?x ?p ?y} UNION {?y ?p ?x} filter (?y not in (rdfs:Class, owl:Class)) }";
        List<BindingSet> shortestPaths = this.getConnector().selectQuery(sparql);
        // multiple path are separated by an empty row
        int shortestPathLength = 0;
        if (shortestPaths != null) {
            for (BindingSet pathElement : shortestPaths) {
                if (!pathElement.hasBinding("p")) {
                    break;
                }
                shortestPathLength++;
            }
        }
        log.info(String.format("Shortest path for start term: %s and end term %s: %s", classQueryMatchStart.getTermUri(), classQueryMatchEnd.getTermUri(), shortestPathLength));
        return shortestPathLength;
    }

    @Override
    public List<Triple<Term, Term, Term>> getOntologyGraphTriples(Ontology ontology, boolean reversed) {
        List<Triple<Term, Term, Term>> tripleList = new ArrayList<>();

//        String sparql = "Select distinct ?subject ?predicate ?object where { graph <" + ontology.getOntologyUri() + "> { ?subject ?predicate ?object } } ";
        String sparql = "SELECT distinct ?subject_all ?predicate ?object_all WHERE { GRAPH <" + ontology.getOntologyUri() + "> { ?predicate a ?propertyType . OPTIONAL { ?predicate rdfs:domain|<http://schema.org/domainIncludes> ?subject } . bind(if(bound(?subject), ?subject, :source) as ?subject_all ) . OPTIONAL { ?predicate rdfs:range|<http://schema.org/rangeIncludes> ?object } . bind(if(bound(?object), ?object, :sink) as ?object_all ) . } VALUES ?propertyType "+ AbstractOntologyRepository.getTypePropertyValuesString() + " . }";

        List<BindingSet> triples = this.getConnector().selectQuery(sparql);

        for (BindingSet triple : triples) {
            String bindingSubject = "subject_all";
            String bindingObject = "object_all";
            if (reversed) {
                bindingSubject = "object_all";
                bindingObject = "subject_all";
            }
            Term subject = new Term(triple.getBinding(bindingSubject).getValue().stringValue());
            Term predicate = new Term(triple.getBinding("predicate").getValue().stringValue());
            Term object = new Term(triple.getBinding(bindingObject).getValue().stringValue());
            tripleList.add(Triple.of(subject, predicate, object));
        }
        return tripleList;
    }

    @Override
    public Set<Ontology> getAllOntologies() {
        Set<Ontology> ontologies = new HashSet<>();

        String sparql = "select distinct ?g where { graph ?g { ?a ?b ?c } } ";
        List<BindingSet> graphs = this.getConnector().selectQuery(sparql);

        for (BindingSet graph : graphs) {
            ontologies.add(new Ontology(graph.getBinding("g").getValue().stringValue()));
        }

        return ontologies;
    }

    @Override
    public Map<Ontology, Set<Term>> getAllTerms() {
        Map<Ontology, Set<Term>> allTerms = new HashMap<>();

        for (Ontology ontology : this.getAllOntologies()) {
            String sparql = "SELECT distinct ?term WHERE { GRAPH <" + ontology.getOntologyUri() + "> { ?term a ?termType . } VALUES ?termType " + AbstractOntologyRepository.getAllTypesValuesString() + " . filter (strstarts(str(?term), \"" + ontology.getOntologyTermPrefix() + "\")) . }";
            if (!ontology.getOntologyAlternativeTermPrefix().isEmpty()) {
                sparql = "SELECT distinct ?term WHERE { GRAPH <" + ontology.getOntologyUri() + "> { ?term a ?termType . } VALUES ?termType " + AbstractOntologyRepository.getAllTypesValuesString() + " . filter (strstarts(str(?term), \"" + ontology.getOntologyTermPrefix() + "\") || strstarts(str(?term), \"" + ontology.getOntologyAlternativeTermPrefix() + "\")) . }";
            }
            List<BindingSet> termResults = this.getConnector().selectQuery(sparql);
            for (BindingSet termResult : termResults) {
                if (!allTerms.containsKey(ontology)) {
                    allTerms.put(ontology, new HashSet<>());
                }
                allTerms.get(ontology).add(new Term(termResult.getBinding("term").getValue().stringValue()));
            }
        }

        return allTerms;
    }

    @Override
    public Set<Term> getAllTerms(Ontology ontology) {
        Set<Term> allTerms = new HashSet<>();

        String sparql = "SELECT distinct ?term WHERE { GRAPH <" + ontology.getOntologyUri() + "> { ?term a ?termType . } VALUES ?termType " + AbstractOntologyRepository.getAllTypesValuesString() + " . filter (strstarts(str(?term), \"" + ontology.getOntologyTermPrefix() + "\")) . }";
        if (!ontology.getOntologyAlternativeTermPrefix().isEmpty()) {
            sparql = "SELECT distinct ?term WHERE { GRAPH <" + ontology.getOntologyUri() + "> { ?term a ?termType . } VALUES ?termType " + AbstractOntologyRepository.getAllTypesValuesString() + " . filter (strstarts(str(?term), \"" + ontology.getOntologyTermPrefix() + "\") || strstarts(str(?term), \"" + ontology.getOntologyAlternativeTermPrefix() + "\")) . }";
        }
        List<BindingSet> termResults = this.getConnector().selectQuery(sparql);

        for (BindingSet termResult : termResults) {
            Term term = new Term(termResult.getBinding("term").getValue().stringValue());
            if (!allTerms.contains(term)) {
                allTerms.add(term);
            }
        }

        return allTerms;
    }
}

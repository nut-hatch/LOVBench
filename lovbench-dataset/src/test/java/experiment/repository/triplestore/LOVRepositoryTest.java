package experiment.repository.triplestore;

import com.complexible.stardog.plan.filter.expr.ValueOrError;
import experiment.TestUtil;
import experiment.feature.scoring.graph.util.JungGraphUtil;
import experiment.model.Ontology;
import experiment.model.Term;
import experiment.configuration.ExperimentConfiguration;
import experiment.model.query.TermQuery;
import experiment.model.query.enums.TermType;
import experiment.repository.triplestore.connector.JenaConnector;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static org.junit.Assert.*;

public class LOVRepositoryTest {

    AbstractOntologyRepository repository = ExperimentConfiguration.getInstance().getRepository();


    // Handling some irregularities of the LOV dump used for testing
    String[] knownMissingOrEmptyVocabsInDump = {
            "http://ontology.eil.utoronto.ca/open311.owl",
            "https://w3id.org/opm",
            "https://www.w3.org/2019/wot/td",
            "http://www.w3.org/2001/XMLSchema" };
    String[] knownVocabsNoURIs = {
            "http://ontology.eil.utoronto.ca/GCI/Foundation/GCI-Foundation.owl",
            "http://ns.inria.fr/ludo"
    };
    String[] knownVocabsNoTermsWithVocabPrefix = {
            "http://purl.org/iot/ontology/fiesta-iot",
            "http://purl.oclc.org/NET/ssnx/cf/cf-property",
            "https://w3id.org/seas/QUDTAlignment",
            "http://linked.opendata.cz/ontology/ldvm/",
            "http://data.ordnancesurvey.co.uk/ontology/50kGazetteer/",
            "http://purl.org/twc/vocab/conversion/",
            "https://w3id.org/seas/",
            "http://semwebquality.org/ontologies/dq-constraints"
    };

    List<Term> knownWronglySpecifiedDuplicatesInDump;
    Map<Ontology,Integer> missmatchFix;

    private static final Logger log = LoggerFactory.getLogger( LOVRepositoryTest.class );

    @Before
    public void setUp() throws Exception {
        new TestUtil().setNqFileConfiguration();
        knownWronglySpecifiedDuplicatesInDump = Arrays.asList(
                new Term("http://purl.org/vocab/aiiso/schema#organizationalUnit")  // specified as class AND property .... why?!
        );
        missmatchFix = new HashMap<>();
        for (Term badTerm : knownWronglySpecifiedDuplicatesInDump) {
            Ontology ont = new Ontology(badTerm.getOntologyUriOfTerm());
            if (!missmatchFix.containsKey(ont)) {
                missmatchFix.put(ont, 1);
            } else {
                missmatchFix.put(ont, missmatchFix.get(ont)+1);
            }
        }
    }

    @Test
    public void setupTest() {
        JenaConnector connector = ((JenaConnector)repository.getConnector());
        long graphCount = 1;
        long tripleCount = connector.getDataset().getDefaultModel().size();
        Iterator<String> it = connector.getDataset().listNames();
        while (it.hasNext()) {
            graphCount++;
            tripleCount += connector.getDataset().getNamedModel(it.next()).size();
        }
        System.out.println("Read " + tripleCount + " triples in " + graphCount + " graphs");
    }


    @Test
    public void countOntologies() {
        assertEquals(694,repository.countOntologies());
    }

    @Test
    public void ontologySize() {
//        System.out.println("size: " + repository.ontologySize(new Ontology("http://www.w3.org/2006/vcard/ns")));
        assertNotEquals(0,repository.ontologySize(new Ontology("http://www.w3.org/2006/vcard/ns")));
    }

    @Test
    public void countSubClasses() {
//        System.out.println("subclasses: " + repository.countSubClasses(new Term("vcard:RelatedType"), new Ontology("http://www.w3.org/2006/vcard/ns")));
        assertNotEquals(0,repository.countSubClasses(new Term("vcard:RelatedType"), new Ontology("http://www.w3.org/2006/vcard/ns")));

    }

    @Test
    public void countSuperClasses() {
        assertNotEquals(0,repository.countSuperClasses(new Term("vcard:Agent"), new Ontology("http://www.w3.org/2006/vcard/ns")));
    }

    @Test
    public void countSiblings() {
        assertNotEquals(0,repository.countSiblings(new Term("vcard:Agent"), new Ontology("http://www.w3.org/2006/vcard/ns")));
    }

    @Test
    public void countSubProperties() {
        assertNotEquals(0,repository.countSubProperties(new Term("vcard:organization-name"), new Ontology("http://www.w3.org/2006/vcard/ns")));
    }

    @Test
    public void countSuperProperties() {
        assertNotEquals(0,repository.countSuperProperties(new Term("vcard:organization-unit"), new Ontology("http://www.w3.org/2006/vcard/ns")));
    }

    @Test
    public void getAllOntologies() {
        assertEquals(repository.getAllOntologies().size(),repository.countOntologies());
    }


    @Test
    public void getOntologyGraphTriples() {
        for (Ontology ontology : this.repository.getAllOntologies()) {
            List<Triple<Term, Term, Term>> triples = this.repository.getOntologyGraphTriples(ontology);
            Set<Term> termSet = this.repository.getAllTerms(ontology);
            if (!termSet.isEmpty()) {
                int propertyCount = this.repository.getPropertyCount(ontology);
                log.debug(triples.size() + "");
                log.debug(propertyCount+"");
                assertTrue(triples.size() >= propertyCount);
            }
        }
    }

    @Test
    public void getAllTerms() {

        Map<Ontology,Set<Term>> all = repository.getAllTerms();
        int i = 0;
        for (Set<Term> terms : all.values()) {
            i += terms.size();
        }
        log.debug("Terms total: " + i);
        assertEquals(68820, i);

        // assure that there is no "empty" ontology
        for (Map.Entry<Ontology,Set<Term>> ontologyTermSet : all.entrySet()) {
            Ontology ontology = ontologyTermSet.getKey();
            Set<Term> allOntologyTerms = ontologyTermSet.getValue();

            log.debug(ontology.getOntologyUri() + ": " + allOntologyTerms.size());
            if (!Arrays.asList(knownVocabsNoTermsWithVocabPrefix).contains(ontology.getOntologyUri()) && !Arrays.asList(knownVocabsNoURIs).contains(ontology.getOntologyUri())) {
                assertTrue(allOntologyTerms.size() > 0);

                Set<Term> allOntologyClasses = repository.getAllTerms(ontology, TermType.CLASS);
                Set<Term> allOntologyProperties = repository.getAllTerms(ontology, TermType.PROPERTY);
                int termSize = allOntologyTerms.size();
                if (missmatchFix.containsKey(ontology)) {
                    termSize += missmatchFix.get(ontology);
                }
                assertEquals(termSize,allOntologyClasses.size()+allOntologyProperties.size());
            }
        }
        log.debug(all.keySet().size()+"");
        log.debug(this.repository.countOntologies()+"");
        log.debug(this.repository.getAllOntologies().size()+"");
        assertEquals(all.keySet().size(), this.repository.countOntologies());
        assertEquals(all.keySet().size(), this.repository.getAllOntologies().size());


    }

    @Test
    public void termCounts() {
        for (Ontology ontology : this.repository.getAllOntologies()) {
            int props = this.repository.getPropertyCount(ontology);
            int classes = this.repository.getClassCount(ontology);
            Set<Term> allOntTerms = this.repository.getAllTerms(ontology);
            int termSize = allOntTerms.size();
            if (missmatchFix.containsKey(ontology)) {
                termSize += missmatchFix.get(ontology);
            }
            log.debug(ontology.getOntologyUri());
            log.debug(props+"");
            log.debug(classes+"");
            log.debug(termSize+"");
            assertEquals(termSize,props+classes);
        }
    }

    @Test
    public void countAppearanceOfTermPrefix() {
        int cntSlash = this.repository.countAppearanceOfTermPrefix("http://ns.inria.fr/ludo","http://nr.inria.fr/ludo/");
        int cntHash = this.repository.countAppearanceOfTermPrefix("http://ns.inria.fr/ludo","http://nr.inria.fr/ludo#");
        int cntSlashHash = this.repository.countAppearanceOfTermPrefix("http://ns.inria.fr/ludo","http://nr.inria.fr/ludo/#");
        log.debug(cntSlash+"");
        log.debug(cntHash+"");
        log.debug(cntSlashHash+"");
        assertEquals(0,cntSlash);
        assertEquals(0,cntHash);
        assertEquals(0,cntSlashHash);
    }

    @Test
    public void getOwlImports() {
        Set<Pair<Ontology, Ontology>> imports = this.repository.getOwlImports();
        log.debug(imports.size()+"");
        assertTrue(imports.size() > 0);

        Set<Pair<Ontology, Ontology>> importsImplicit = this.repository.getOwlImports(null,true);
        log.debug(importsImplicit.size()+"");
        assertTrue(importsImplicit.size() > 0);
        assertTrue(importsImplicit.size() > imports.size());
    }

    @Test
    public void getTermQueryMatch() {
        TermQuery query = new TermQuery("person");
        Ontology ontology = new Ontology("http://schema.org/");
        Set<Term> classMatches = this.repository.getTermQueryMatch(query, ontology, TermType.CLASS);
        for (Term term : classMatches) {
            log.debug(term.getTermUri());
        }
        assertEquals(20, classMatches.size());
        assertTrue(classMatches.contains(new Term("http://schema.org/Person")));

        Set<Term> propertyMatches = this.repository.getTermQueryMatch(query, ontology, TermType.PROPERTY);
        for (Term term : propertyMatches) {
            log.debug(term.getTermUri());
        }
        assertEquals(79, propertyMatches.size());

        Set<Term> allMatches = this.repository.getTermQueryMatch(query, ontology);
        assertEquals(allMatches.size(),propertyMatches.size()+classMatches.size());
    }

    @Test
    public void getClassQueryMatchRDFSLabels() {
        TermQuery query = new TermQuery("building");
        Ontology ontology = new Ontology("http://schema.org/");
        Map<Term,Set<String>> rdfsClassMatches = this.repository.getClassQueryMatchRDFSLabels(query, ontology);
        for (Map.Entry<Term,Set<String>> entry : rdfsClassMatches.entrySet()) {
            log.debug(entry.getKey() + ": " + String.join(";",entry.getValue()));
        }
        assertTrue(rdfsClassMatches.keySet().containsAll(new HashSet<>(Arrays.asList(
                new Term("http://schema.org/LandmarksOrHistoricalBuildings"),
                new Term("http://schema.org/GovernmentBuilding"),
                new Term("http://schema.org/LegislativeBuilding")
        ))));
    }
}
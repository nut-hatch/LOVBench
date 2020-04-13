package experiment.feature.extraction.term.importance;

import experiment.TestUtil;
import experiment.configuration.ExperimentConfiguration;
import experiment.feature.scoring.graph.HubDWRankScorer;
import experiment.model.Ontology;
import experiment.model.Term;
import experiment.model.query.enums.TermType;
import experiment.repository.triplestore.AbstractOntologyRepository;
import experiment.repository.triplestore.connector.JenaConnector;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

import static org.junit.Assert.*;

public class HubDWRankTest {

    AbstractOntologyRepository repository = ExperimentConfiguration.getInstance().getRepository();

    int knownOntologiesWithoutHubs = 116;

    private static final Logger log = LoggerFactory.getLogger(HubDWRankTest.class);

    @Before
    public void setUp() throws Exception {
        new TestUtil().setNqFileConfiguration();
    }

    @Test
    public void computeAllScores() {
        if (ExperimentConfiguration.getInstance().isMakeExtensiveTests()) {
            HubDWRank hub_t = new HubDWRank(repository, new HubDWRankScorer(repository));
            hub_t.computeAllScores();

            int countOntsNoHub = 0;
            for (Ontology ontology : repository.getAllOntologies()) {
                Set<Term> termSet = repository.getAllTerms(ontology);
                boolean allZero = true;
                if (!termSet.isEmpty()) {
                    for (Term term : termSet) {
                        if (Double.compare(0.0,hub_t.getScore(term)) == -1) {
                            log.debug(term.getTermUri() + ": " + hub_t.getScore(term));
                            allZero = false;
                            break;
                        }
                    }
                    log.debug(ontology.getOntologyUri());
                }
                if (allZero) {
                    countOntsNoHub++;
                    log.debug(ontology.getOntologyUri());
                }
            }
            log.debug(countOntsNoHub + "");
            assertEquals(knownOntologiesWithoutHubs,countOntsNoHub);
        }
    }

    @Test
    public void computeScore() {
        HubDWRank hub_t = new HubDWRank(repository, new HubDWRankScorer(repository));

        Ontology maxPerf = new Ontology("http://mex.aksw.org/mex-perf");
        Set<Term> maxPerfTermSet = repository.getAllTerms(maxPerf);
        hub_t.computeScores(maxPerfTermSet, maxPerf);


        log.debug(maxPerfTermSet.size() + "");
        double sum = 0.0;
        for (Term term : maxPerfTermSet) {
            log.debug(term.getTermUri() + ": " + hub_t.getScore(term));
            sum += hub_t.getScore(term);
        }

        assertEquals(-1, Double.compare(0.0, sum));

        Ontology rdfs = new Ontology("http://www.w3.org/1999/02/22-rdf-syntax-ns#");
        Set<Term> termSetRdfs = repository.getAllTerms(rdfs);
        hub_t.computeScores(termSetRdfs, rdfs);
        for (Term term : termSetRdfs) {
            log.debug(term.getTermUri() + ": " + hub_t.getScore(term));
        }

        assertEquals(0, Double.compare(0.0, hub_t.getScore(new Term("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"))));
        assertEquals(-1, Double.compare(0.0, hub_t.getScore(new Term("http://www.w3.org/1999/02/22-rdf-syntax-ns#List"))));

        // Hub only scores classes! check that property scores are zero.
        Ontology obo = new Ontology("http://purl.obolibrary.org/obo/obi.owl");
        Set<Term> propertySetObo = repository.getAllTerms(obo, TermType.PROPERTY);
        hub_t.computeScores(propertySetObo, obo);
        for (Term term : propertySetObo) {
            log.debug(term.getTermUri() + ": " + hub_t.getScore(term));
            assertEquals(0, Double.compare(0.0,hub_t.getScore(term)));
        }

        Ontology sf = new Ontology("http://www.opengis.net/ont/sf");
        Set<Term> termSetSf = repository.getAllTerms(sf);
        hub_t.computeScores(termSetSf, sf);
        for (Term term : termSetSf) {
            log.debug(term.getTermUri() + ": " + hub_t.getScore(term));
            assertEquals(0, Double.compare(0.0,hub_t.getScore(term)));
        }
//        Graph<Term, String> graph = JungGraphUtil.createOntologyGraph(this.repository.getOntologyGraphTriples(sf, true), EdgeType.DIRECTED);
//        for (String edge : graph.getEdges()) {
//            log.info(graph.getSource(edge) + " --" + edge + "--> " + graph.getDest(edge));
//        }
//        log.info(graph.getEdgeCount() + "");
//        log.info(graph.getVertexCount() + "");

    }

}
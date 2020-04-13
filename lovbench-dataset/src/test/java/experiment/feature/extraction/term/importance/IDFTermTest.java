package experiment.feature.extraction.term.importance;

import experiment.TestUtil;
import experiment.configuration.ExperimentConfiguration;
import experiment.feature.scoring.TFIDFScorer;
import experiment.model.Ontology;
import experiment.model.Term;
import experiment.repository.triplestore.AbstractOntologyRepository;
import experiment.repository.triplestore.connector.JenaConnector;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

import static org.junit.Assert.*;

public class IDFTermTest {

    AbstractOntologyRepository repository = ExperimentConfiguration.getInstance().getRepository();
    
    private static final Logger log = LoggerFactory.getLogger( IDFTermTest.class );

    @Before
    public void setUp() throws Exception {
        new TestUtil().setNqFileConfiguration();
    }

    @Test
    public void computeAllScores() {
        if (ExperimentConfiguration.getInstance().isMakeExtensiveTests()) {
            IDFTerm idf_t = new IDFTerm(repository, new TFIDFScorer(repository));
            idf_t.computeAllScores();

            // It is unlikely that there exists one ontology of which all its terms are used in all other ontologies (i.e., an ontology idf score of 0).
            // Let's check.
            for (Ontology ontology : repository.getAllOntologies()) {
                Set<Term> termSet = repository.getAllTerms(ontology);
                if (termSet.isEmpty()) {
                    log.debug("No terms found for ontology " + ontology.getOntologyUri());
                } else {
                    boolean allZero = true;
                    for (Term term : termSet) {
                        if (idf_t.getScore(term) > 0) {
                            log.debug(term.getTermUri());
                            allZero = false;
                            break;
                        }
                    }
                    assertFalse(allZero);
                }
            }
        }
    }

    @Test
    public void computeScore() {
        IDFTerm idf_t = new IDFTerm(repository, new TFIDFScorer(repository));

        Ontology maxPerf = new Ontology("http://mex.aksw.org/mex-perf");
        Set<Term> maxPerfTermSet =  repository.getAllTerms(maxPerf);
        idf_t.computeScores(maxPerfTermSet, maxPerf);

        double sum = 0.0;
        for (Term term : maxPerfTermSet) {
            sum += idf_t.getScore(term);
        }
        assertEquals(-1, Double.compare(0.0,sum));

        Ontology rdfs = new Ontology("http://www.w3.org/1999/02/22-rdf-syntax-ns#");
        Set<Term> termSetRdfs =  repository.getAllTerms(rdfs);
        idf_t.computeScores(termSetRdfs, rdfs);

        // rdf type should occur in each ontology, and that's why the IDF score should be zero (rare case).
        assertEquals(0,Double.compare(0.0, idf_t.getScore(new Term("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"))));

    }
}
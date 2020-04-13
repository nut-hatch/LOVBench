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

public class TFIDFTermTest {

    AbstractOntologyRepository repository = ExperimentConfiguration.getInstance().getRepository();

    private static final Logger log = LoggerFactory.getLogger( TFIDFTermTest.class );

    @Before
    public void setUp() throws Exception {
        new TestUtil().setNqFileConfiguration();
    }

    @Test
    public void computeAllScores() {
        if (ExperimentConfiguration.getInstance().isMakeExtensiveTests()) {
            TFIDFTerm tfidf_t = new TFIDFTerm(repository, new TFIDFScorer(repository));
            tfidf_t.computeAllScores();

            for (Ontology ontology : repository.getAllOntologies()) {

                Set<Term> termSet = repository.getAllTerms(ontology);
                if (termSet.isEmpty()) {
                    log.debug("No terms found for ontology " + ontology.getOntologyUri());
                } else {
                    boolean allZero = true;
                    for (Term term : termSet) {
                        if (tfidf_t.getScore(term) > 0) {
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
    public void computeScores() {
        TFIDFTerm tfidf_t = new TFIDFTerm(repository, new TFIDFScorer(repository));

        Ontology rdfOntolgy = new Ontology("http://www.w3.org/1999/02/22-rdf-syntax-ns#");
        tfidf_t.computeScores(this.repository.getAllTerms(rdfOntolgy), rdfOntolgy);

        assertEquals(0,Double.compare(0, tfidf_t.getScore(new Term("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"))));

        Ontology d2rqOntology = new Ontology("http://www.wiwiss.fu-berlin.de/suhl/bizer/D2RQ/0.1");
        Set<Term> d2rqTerms = this.repository.getAllTerms(d2rqOntology);
        tfidf_t.computeScores(d2rqTerms, d2rqOntology);

        for (Term term : d2rqTerms) {
            assertEquals(-1,Double.compare(0.0,tfidf_t.getScore(term)));
        }
    }

}
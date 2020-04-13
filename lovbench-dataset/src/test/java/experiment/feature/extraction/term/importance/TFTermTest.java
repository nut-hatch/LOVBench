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
import org.openrdf.query.BindingSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

public class TFTermTest {

    AbstractOntologyRepository repository = ExperimentConfiguration.getInstance().getRepository();

    private static final Logger log = LoggerFactory.getLogger( TFTermTest.class );

    @Before
    public void setUp() throws Exception {
        new TestUtil().setNqFileConfiguration();
    }

    @Test
    public void computeAllScores() {
        if (ExperimentConfiguration.getInstance().isMakeExtensiveTests()) {
            TFTerm tf_t = new TFTerm(repository, new TFIDFScorer(repository));
            tf_t.computeAllScores();

            // A good sanity check is that there is no tf equals zero - since each term should appear at least once, the score has to be > 0.
            for (Ontology ontology : this.repository.getAllOntologies()) {
                Set<Term> termSet = repository.getAllTerms(ontology);
                if (termSet.isEmpty()) {
                    log.debug("No terms found for ontology " + ontology.getOntologyUri());
                } else {
                    boolean allZero = true;
                    for (Term term : repository.getAllTerms(ontology)) {
                        if (tf_t.getScore(term) > 0) {
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
        TFTerm tf_t = new TFTerm(repository, new TFIDFScorer(repository));
        Set<Term> termSet =  repository.getAllTerms(new Ontology("http://mex.aksw.org/mex-perf"));

        tf_t.computeScores(termSet, new Ontology("http://mex.aksw.org/mex-perf"));

        double sum = 0.0;
        for (Term term : termSet) {
            sum += tf_t.getScore(term);
        }

        assertEquals(-1, Double.compare(0.0,sum));


        termSet = repository.getAllTerms(new Ontology("http://qudt.org/schema/qudt"));
        log.debug(termSet.size()+"");

        tf_t.computeScores(termSet, new Ontology("http://qudt.org/schema/qudt"));
        sum = 0.0;
        for (Term term : termSet) {
            sum += tf_t.getScore(term);
        }

        assertEquals(-1, Double.compare(0.0,sum));
    }
}
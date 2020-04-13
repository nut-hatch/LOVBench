package experiment.feature.extraction.ontology.relevance;

import experiment.TestUtil;
import experiment.configuration.ExperimentConfiguration;
import experiment.feature.extraction.term.importance.BM25Term;
import experiment.feature.extraction.term.importance.IDFTerm;
import experiment.feature.scoring.TFIDFScorer;
import experiment.model.Ontology;
import experiment.model.Term;
import experiment.model.query.TermQuery;
import experiment.repository.triplestore.AbstractOntologyRepository;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

import static org.junit.Assert.*;

public class IDFOntologyTest {

    AbstractOntologyRepository repository = ExperimentConfiguration.getInstance().getRepository();

    private static final Logger log = LoggerFactory.getLogger( IDFOntologyTest.class );

    @Before
    public void setUp() throws Exception {
        new TestUtil().setNqFileConfiguration();
    }

    @Test
    public void getScore() {
        IDFOntology idf = new IDFOntology(repository, new TFIDFScorer(repository));
        TermQuery query = new TermQuery("person");
        Ontology ontology = new Ontology("http://schema.org/");
        double score = idf.getScore(query, ontology);
        log.debug(score+"");
        assertEquals(-1, Double.compare(0.0, score));

        IDFTerm idf_t = new IDFTerm(repository, new TFIDFScorer(repository));
        Set<Term> termSet = this.repository.getTermQueryMatch(query, ontology);
        idf_t.computeScores(termSet,ontology);
        double sum = 0.0;
        for (Term term : termSet) {
            sum += idf_t.getScore(term);
        }
        log.debug(sum+"");
        assertEquals(0, Double.compare(score,sum));
    }


}
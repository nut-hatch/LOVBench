package experiment.feature.extraction.term.relevance;

import experiment.TestUtil;
import experiment.configuration.ExperimentConfiguration;
import experiment.model.query.TermQuery;
import experiment.repository.triplestore.AbstractOntologyRepository;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.*;

public class QueryLengthTest {

    AbstractOntologyRepository repository = ExperimentConfiguration.getInstance().getRepository();

    private static final Logger log = LoggerFactory.getLogger( QueryLengthTest.class );

    @Before
    public void setUp() throws Exception {
        new TestUtil().setNqFileConfiguration();
    }

    @Test
    public void getScore() {
        QueryLength ql = new QueryLength(repository);

        assertEquals(0,Double.compare(1.0, ql.getScore(new TermQuery("person"), null)));
        assertEquals(0,Double.compare(3.0, ql.getScore(new TermQuery("internet of things"), null)));
    }

}
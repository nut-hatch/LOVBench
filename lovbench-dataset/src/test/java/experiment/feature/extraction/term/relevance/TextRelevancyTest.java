package experiment.feature.extraction.term.relevance;

import experiment.TestUtil;
import experiment.configuration.ExperimentConfiguration;
import experiment.model.Term;
import experiment.model.query.TermQuery;
import experiment.repository.triplestore.AbstractOntologyRepository;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.*;

public class TextRelevancyTest {

    AbstractOntologyRepository repository = ExperimentConfiguration.getInstance().getRepository();

    private static final Logger log = LoggerFactory.getLogger( TextRelevancyTest.class );

    @Before
    public void setUp() throws Exception {
        new TestUtil().setNqFileConfiguration();
    }

    @Test
    public void getScore() {
        TextRelevancy tr = new TextRelevancy(repository);
        TermQuery query = new TermQuery("house building");
        Term term = new Term("http://schema.org/House");

        double score = tr.getScore(query,term);
        log.debug(score+"");
        assertEquals(0,Double.compare(2.0, score));

        score = tr.getScore(new TermQuery("building"),term);
        log.debug(score+"");
        assertEquals(0,Double.compare(1.0, score));

        score = tr.getScore(new TermQuery("person"),term);
        log.debug(score+"");
        assertEquals(0,Double.compare(0.0, score));
//        log.debug(Double.compare(3*0.4, score)+"");
//        assertEquals(0,Double.compare(3*0.4, score));
    }
}
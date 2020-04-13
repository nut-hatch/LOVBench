package experiment.feature.extraction.term.relevance;

import experiment.TestUtil;
import experiment.configuration.ExperimentConfiguration;
import experiment.model.Ontology;
import experiment.model.Term;
import experiment.model.query.TermQuery;
import experiment.repository.triplestore.AbstractOntologyRepository;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class BooleanMatchTest {

    AbstractOntologyRepository repository = ExperimentConfiguration.getInstance().getRepository();

    @Before
    public void setUp() throws Exception {
        new TestUtil().setNqFileConfiguration();
    }
    @Test
    public void getScore() {
        BooleanMatch bolMatch = new BooleanMatch(repository);

        TermQuery query = new TermQuery("person");
        Term term = new Term("http://schema.org/Person");
        assertEquals(0,Double.compare(1.0, bolMatch.getScore(query,term)));

        query = new TermQuery("building");
        assertEquals(0,Double.compare(0.0, bolMatch.getScore(query,term)));
    }
}
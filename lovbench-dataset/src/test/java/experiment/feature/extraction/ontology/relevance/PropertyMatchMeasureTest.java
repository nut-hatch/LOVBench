package experiment.feature.extraction.ontology.relevance;

import experiment.TestUtil;
import experiment.configuration.ExperimentConfiguration;
import experiment.model.Ontology;
import experiment.model.query.TermQuery;
import experiment.repository.triplestore.AbstractOntologyRepository;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.*;

public class PropertyMatchMeasureTest {

    AbstractOntologyRepository repository = ExperimentConfiguration.getInstance().getRepository();

    private static final Logger log = LoggerFactory.getLogger( PropertyMatchMeasureTest.class );

    @Before
    public void setUp() throws Exception {
        new TestUtil().setNqFileConfiguration();
    }

    @Test
    public void getScore() {
        PropertyMatchMeasure cmm = new PropertyMatchMeasure(repository);
        TermQuery query = new TermQuery("person");
        Ontology ontology = new Ontology("http://schema.org/");

        double score = cmm.getScore(query,ontology);
        log.debug(score+"");
        assertEquals(-1,Double.compare(0.0, score));

        score = cmm.getScore(new TermQuery("building"),ontology);
        log.debug(score+"");
        assertEquals(0,Double.compare(0.0, score));

        score = cmm.getScore(new TermQuery("brand"),ontology);
        log.debug(score+"");
        assertEquals(-1,Double.compare(0.0, score));
    }
}
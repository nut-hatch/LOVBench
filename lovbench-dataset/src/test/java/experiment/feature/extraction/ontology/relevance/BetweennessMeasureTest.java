package experiment.feature.extraction.ontology.relevance;

import experiment.TestUtil;
import experiment.configuration.ExperimentConfiguration;
import experiment.feature.scoring.graph.BetweennessScorer;
import experiment.model.Ontology;
import experiment.model.query.TermQuery;
import experiment.repository.triplestore.AbstractOntologyRepository;
import experiment.repository.triplestore.connector.JenaConnector;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.*;

public class BetweennessMeasureTest {

    AbstractOntologyRepository repository = ExperimentConfiguration.getInstance().getRepository();

    private static final Logger log = LoggerFactory.getLogger( BetweennessMeasureTest.class );

    @Before
    public void setUp() throws Exception {
        new TestUtil().setNqFileConfiguration();
    }

    @Test
    public void getScore() {
        BetweennessMeasure bm = new BetweennessMeasure(repository, new BetweennessScorer(repository));
        double score = bm.getScore(new TermQuery("person"), new Ontology("http://schema.org/"));
        log.debug(score+"");
        assertEquals(-1, Double.compare(0.0, score));
    }

}
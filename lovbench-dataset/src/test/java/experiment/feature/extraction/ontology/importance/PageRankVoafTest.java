package experiment.feature.extraction.ontology.importance;

import experiment.TestUtil;
import experiment.model.Ontology;
import experiment.configuration.ExperimentConfiguration;
import experiment.repository.triplestore.AbstractOntologyMetadataRepository;
import experiment.repository.triplestore.AbstractOntologyRepository;
import experiment.repository.triplestore.connector.JenaConnector;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashSet;

import static org.junit.Assert.*;

public class PageRankVoafTest {

    AbstractOntologyRepository repository = ExperimentConfiguration.getInstance().getRepository();
    AbstractOntologyMetadataRepository metadataRepository = ExperimentConfiguration.getInstance().getRepositoryMetadata();

    private static final Logger log = LoggerFactory.getLogger(PageRankVoafTest.class);


    @Before
    public void setUp() throws Exception {
        new TestUtil().setNqFileConfiguration();
    }

    @Test
    public void computeAllScores() {
        PageRankVoaf pr = new PageRankVoaf(repository, metadataRepository);
        pr.computeAllScores();

        for (Ontology ontology : repository.getAllOntologies()) {
            log.debug("Ontology: " + ontology.getOntologyUri() + ", " + pr.getFeatureName() + ": " + pr.getScore(ontology));
            assertNotEquals(0, pr.getScore(ontology));
        }
    }

    @Test
    public void computeScores() {
        Ontology rdfs = new Ontology("http://www.w3.org/2000/01/rdf-schema#");
        Ontology owl = new Ontology("http://www.w3.org/2002/07/owl");

        PageRankVoaf pr = new PageRankVoaf(repository, metadataRepository);
        pr.computeScores(new HashSet<>(Arrays.asList(rdfs, owl)));

        assertNotEquals(0, pr.getScore(rdfs));
        assertNotEquals(0, pr.getScore(owl));
        assertEquals(1, Double.compare(pr.getScore(rdfs), pr.getScore(owl)));
    }

}
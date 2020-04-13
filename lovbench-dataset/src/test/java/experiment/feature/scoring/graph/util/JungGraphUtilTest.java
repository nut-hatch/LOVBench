package experiment.feature.scoring.graph.util;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.EdgeType;
import experiment.TestUtil;
import experiment.configuration.ExperimentConfiguration;
import experiment.model.Ontology;
import experiment.model.Term;
import experiment.repository.triplestore.AbstractOntologyMetadataRepository;
import experiment.repository.triplestore.AbstractOntologyRepository;
import experiment.repository.triplestore.connector.JenaConnector;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

public class JungGraphUtilTest {

    AbstractOntologyRepository repository = ExperimentConfiguration.getInstance().getRepository();
    AbstractOntologyMetadataRepository metadataRepository = ExperimentConfiguration.getInstance().getRepositoryMetadata();


    private static final Logger log = LoggerFactory.getLogger( JungGraphUtilTest.class );

    @Before
    public void setUp() throws Exception {
        new TestUtil().setNqFileConfiguration();
    }

    @Test
    public void createRepositoryGraph() {
        Set<Pair<Ontology, Ontology>> voafRelations = this.metadataRepository.getAllVoafRelations();
        Graph<Ontology,String> graph = JungGraphUtil.createRepositoryGraph(voafRelations, EdgeType.DIRECTED);

        Set<Ontology> knownVocabsNoWithNoRelations = new HashSet<>();
        knownVocabsNoWithNoRelations.add(new Ontology("http://www.daml.org/2001/09/countries/iso-3166-ont"));

        Set<Ontology> allOntologies = repository.getAllOntologies();
        allOntologies.removeAll(knownVocabsNoWithNoRelations);

        assertEquals(allOntologies.size(),graph.getVertexCount());
        assertEquals(voafRelations.size(),graph.getEdgeCount());
    }

    @Test
    public void createOntologyGraph() {
        for (Ontology ontology : this.repository.getAllOntologies()) {
            List<Triple<Term, Term, Term>> triples = this.repository.getOntologyGraphTriples(ontology);
            Graph<Term, String> graph = JungGraphUtil.createOntologyGraph(triples, EdgeType.UNDIRECTED);
            log.debug(triples.size()+"");
            log.debug(graph.getEdgeCount()+"");
            assertEquals(triples.size(), graph.getEdgeCount());
        }
    }
}
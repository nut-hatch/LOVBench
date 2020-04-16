package experiment.repository.triplestore.connector;

import experiment.TestUtil;
import experiment.configuration.ExperimentConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.query.BindingSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.*;

public class JenaConnectorTest {

    private static final Logger log = LoggerFactory.getLogger( JenaConnectorTest.class );

    @Before
    public void setUp() throws Exception {
        new TestUtil().setNqFileConfiguration();
    }

    @Test
    public void loadDataset() {
        JenaConnector connector = (JenaConnector)ExperimentConfiguration.getInstance().getRepository().getConnector();
        long graphCount = 1;
        long tripleCount = connector.getDataset().getDefaultModel().size();
        Iterator<String> it = connector.getDataset().listNames();
        while (it.hasNext()) {
            graphCount++;
            tripleCount += connector.getDataset().getNamedModel(it.next()).size();
        }
        log.debug("Read " + tripleCount + " triples in " + graphCount + " graphs");

    }


    @Test
    public void selectQuery() {
        JenaConnector connector = (JenaConnector)ExperimentConfiguration.getInstance().getRepository().getConnector();
        String selectQuery = "SELECT DISTINCT ?class {" +
                " GRAPH ?g { {" +
                "    ?class a rdfs:Class" +
                "  } UNION {" +
                "    ?class a owl:Class" +
                "  }" +
                "  FILTER (isURI(?class))" +
                " } }";
        List<BindingSet> result = connector.selectQuery(selectQuery, true);
        log.debug(result.size()+"");
        assertNotEquals(0, result.size());

        selectQuery = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                "PREFIX voaf: <http://purl.org/vocommons/voaf#>\n" +
                "PREFIX bibo: <http://purl.org/ontology/bibo/>\n" +
                "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n" +
                "PREFIX dct:  <http://purl.org/dc/terms/>\n" +
                "PREFIX vann: <http://purl.org/vocab/vann/>\n" +
                "\n" +
                "SELECT DISTINCT ?vocab {\n" +
                "\n" +
                "  GRAPH <https://lov.linkeddata.es/dataset/lov> {\n" +
                "    ?vocab a voaf:Vocabulary .\n" +
                "    ?vocab vann:preferredNamespacePrefix ?prefix .\n" +
                "    ?vocab dct:title ?title .\n" +
                "  }\n" +
                "}";

    }

}
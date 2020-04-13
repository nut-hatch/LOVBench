package experiment.feature.extraction.ontology.importance;

import experiment.TestUtil;
import experiment.configuration.ExperimentConfiguration;
import experiment.model.Ontology;
import experiment.repository.triplestore.AbstractOntologyMetadataRepository;
import experiment.repository.triplestore.AbstractOntologyRepository;
import experiment.repository.triplestore.connector.JenaConnector;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

public class PageRankImportsTest {

    AbstractOntologyRepository repository = ExperimentConfiguration.getInstance().getRepository();

    private static final Logger log = LoggerFactory.getLogger( PageRankImportsTest.class );


    @Before
    public void setUp() throws Exception {
        new TestUtil().setNqFileConfiguration();
    }

    @Test
    public void computeAllScores() {
        PageRankImports pr = new PageRankImports(repository);
        pr.computeAllScores();

        Set<Pair<Ontology, Ontology>> imports = this.repository.getOwlImports();
        Set<Ontology> importSet = new HashSet<>();
        for (Pair<Ontology, Ontology> importPair : imports) {
            importSet.add(importPair.getLeft());
            importSet.add(importPair.getRight());
        }

        for (Ontology ontology : repository.getAllOntologies()) {
            log.debug("Ontology: " + ontology.getOntologyUri() + ", " + pr.getFeatureName() + ": " + pr.getScore(ontology));
            if (importSet.contains(ontology)) {
                assertEquals(-1, Double.compare(0.0, pr.getScore(ontology)));
            } else {
                assertEquals(0, Double.compare(0.0, pr.getScore(ontology)));
            }
        }
    }

    @Test
    public void computeScores() {
        Ontology rdfs = new Ontology("http://www.w3.org/2000/01/rdf-schema#");
        Ontology owl = new Ontology("http://www.w3.org/2002/07/owl");

        PageRankImports pr = new PageRankImports(repository);
        pr.computeScores(new HashSet<>(Arrays.asList(rdfs, owl)));

        log.debug(pr.getScore(rdfs)+"");
        log.debug(pr.getScore(owl)+"");
        assertEquals(-1, Double.compare(0.0, pr.getScore(rdfs)));
        assertEquals(-1, Double.compare(0.0, pr.getScore(owl)));
        assertEquals(0, Double.compare(pr.getScore(rdfs), pr.getScore(owl)));
    }


}
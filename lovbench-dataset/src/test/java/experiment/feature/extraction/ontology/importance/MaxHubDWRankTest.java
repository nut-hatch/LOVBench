package experiment.feature.extraction.ontology.importance;

import experiment.TestUtil;
import experiment.configuration.ExperimentConfiguration;
import experiment.feature.extraction.term.importance.HubDWRank;
import experiment.feature.scoring.graph.HubDWRankScorer;
import experiment.model.Ontology;
import experiment.model.Term;
import experiment.repository.triplestore.AbstractOntologyRepository;
import experiment.repository.triplestore.connector.JenaConnector;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static org.junit.Assert.*;

public class MaxHubDWRankTest {

    AbstractOntologyRepository repository;

    private static final Logger log = LoggerFactory.getLogger( MaxHubDWRankTest.class );

    @Before
    public void setUp() throws Exception {
        new TestUtil().setNqFileConfiguration();
    }

    @Test
    public void computeAllScores() {
        if (ExperimentConfiguration.getInstance().isMakeExtensiveTests()) {
            HubDWRankScorer scorer = new HubDWRankScorer(repository);
            HubDWRank hub_t = new HubDWRank(repository, scorer);
            MaxHubDWRank maxhub_o = new MaxHubDWRank(repository, scorer);

            for (Ontology ontology : repository.getAllOntologies()) {
                Set<Term> termSet = this.repository.getAllTerms(ontology);

                hub_t.computeScores(termSet, ontology);
                maxhub_o.computeScores(new HashSet<>(Arrays.asList(ontology)));

                double max = 0.0;
                for (Term term : termSet) {
                    if (Double.compare(hub_t.getScore(term), max) == 1) {
                        max = hub_t.getScore(term);
                    }
                }
                assertEquals(0,Double.compare(max, maxhub_o.getScore(ontology)));
            }
        }
    }

    @Test
    public void computeScore() {
        HubDWRankScorer scorer = new HubDWRankScorer(repository);
        HubDWRank hub_t = new HubDWRank(repository, scorer);
        MaxHubDWRank maxhub_o = new MaxHubDWRank(repository, scorer);

        Ontology obo = new Ontology("http://purl.obolibrary.org/obo/obi.owl");
        Set<Term> termSetObo = repository.getAllTerms(obo);

        hub_t.computeScores(termSetObo, obo);
        maxhub_o.computeScores(new HashSet<>(Arrays.asList(obo)));

        double max = 0.0;
        for (Term term : termSetObo) {
            if (Double.compare(hub_t.getScore(term), max) == 1) {
                max = hub_t.getScore(term);
            }
        }

        assertEquals(0,Double.compare(max, maxhub_o.getScore(obo)));


        Ontology ont = new Ontology("http://www.wsmo.org/ns/wsmo-lite#");
        Set<Term> termSet = repository.getAllTerms(ont);
        hub_t.computeScores(termSet, ont);

        max = 0.0;
        for (Term term : termSet) {
            log.debug(term.getTermUri());
            if (Double.compare(hub_t.getScore(term), max) == 1) {
                max = hub_t.getScore(term);
            }
        }

        maxhub_o.computeScores(new HashSet<>(Arrays.asList(ont)));

        log.debug(max+"");
        log.debug(maxhub_o.getScore(ont)+"");

        assertEquals(0,Double.compare(max, maxhub_o.getScore(ont)));

    }

}
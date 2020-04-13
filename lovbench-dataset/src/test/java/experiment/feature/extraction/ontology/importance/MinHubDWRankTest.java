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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

public class MinHubDWRankTest {

    AbstractOntologyRepository repository = ExperimentConfiguration.getInstance().getRepository();

    private static final Logger log = LoggerFactory.getLogger( MinHubDWRankTest.class );

    @Before
    public void setUp() throws Exception {
        new TestUtil().setNqFileConfiguration();
    }

    @Test
    public void computeAllScores() {
        if (ExperimentConfiguration.getInstance().isMakeExtensiveTests()) {
            HubDWRankScorer scorer = new HubDWRankScorer(repository);
            HubDWRank hub_t = new HubDWRank(repository, scorer);
            MinHubDWRank minhub_o = new MinHubDWRank(repository, scorer);

            for (Ontology ontology : repository.getAllOntologies()) {
                Set<Term> termSet = this.repository.getAllTerms(ontology);

                hub_t.computeScores(termSet, ontology);
                minhub_o.computeScores(new HashSet<>(Arrays.asList(ontology)));

                double min = Double.MAX_VALUE;
                for (Term term : termSet) {
                    if (Double.compare(hub_t.getScore(term), min) == -1) {
                        min = hub_t.getScore(term);
                    }
                }
                assertEquals(0,Double.compare(min, minhub_o.getScore(ontology)));
            }
        }
    }

    @Test
    public void computeScore() {
        HubDWRankScorer scorer = new HubDWRankScorer(repository);
        HubDWRank hub_t = new HubDWRank(repository, scorer);
        MinHubDWRank minhub_o = new MinHubDWRank(repository, scorer);

        Ontology obo = new Ontology("http://purl.obolibrary.org/obo/obi.owl");
        Set<Term> termSetObo = repository.getAllTerms(obo);

        hub_t.computeScores(termSetObo, obo);
        minhub_o.computeScores(new HashSet<>(Arrays.asList(obo)));

        double min = Double.MAX_VALUE;
        for (Term term : termSetObo) {
            if (Double.compare(hub_t.getScore(term), min) == -1) {
                min = hub_t.getScore(term);
            }
        }

        assertEquals(0,Double.compare(min, minhub_o.getScore(obo)));


        Ontology ont = new Ontology("http://www.wsmo.org/ns/wsmo-lite#");
        Set<Term> termSet = repository.getAllTerms(ont);
        hub_t.computeScores(termSet, ont);

        min = Double.MAX_VALUE;
        for (Term term : termSet) {
            log.debug(term.getTermUri());
            if (Double.compare(hub_t.getScore(term), min) == -1) {
                min = hub_t.getScore(term);
            }
        }

        minhub_o.computeScores(new HashSet<>(Arrays.asList(ont)));

        log.debug(min+"");
        log.debug(minhub_o.getScore(ont)+"");

        assertEquals(0,Double.compare(min, minhub_o.getScore(ont)));

    }
}
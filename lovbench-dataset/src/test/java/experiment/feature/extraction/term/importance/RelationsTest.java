package experiment.feature.extraction.term.importance;

import experiment.TestUtil;
import experiment.configuration.ExperimentConfiguration;
import experiment.feature.scoring.TermStatsScorer;
import experiment.model.Ontology;
import experiment.model.Term;
import experiment.model.query.enums.TermType;
import experiment.repository.triplestore.AbstractOntologyRepository;
import experiment.repository.triplestore.connector.JenaConnector;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

public class RelationsTest {

    AbstractOntologyRepository repository = ExperimentConfiguration.getInstance().getRepository();

    private static final Logger log = LoggerFactory.getLogger( RelationsTest.class );

    @Before
    public void setUp() throws Exception {
        new TestUtil().setNqFileConfiguration();
    }

    @Test
    public void computeAllScores() {
        if (ExperimentConfiguration.getInstance().isMakeExtensiveTests()) {
            Relations relations = new Relations(repository, new TermStatsScorer(repository));
            relations.computeAllScores();

            for (Ontology ontology : this.repository.getAllOntologies()) {
                Set<Term> termSet = this.repository.getAllTerms(ontology);
                if (!termSet.isEmpty()) {

                    // all properties should not have a single subclass, obviously
                    Set<Term> allOntProperties = this.repository.getAllTerms(ontology, TermType.PROPERTY);
                    for (Term property : allOntProperties) {
                        log.debug(property.getTermUri());
                        log.debug(relations.getScore(property) + "");
                        assertEquals(0, Double.compare(0.0, relations.getScore(property)));
                    }
                }
            }
        }
    }

    @Test
    public void computeScores() {
        Relations relations = new Relations(repository, new TermStatsScorer(repository));
        Ontology ontology = new Ontology("http://purl.obolibrary.org/obo/obi.owl");
        Map<Term, Double> scores = relations.computeScores(this.repository.getAllTerms(ontology), ontology);
        Set<Term> ontologyTerms = this.repository.getAllTerms(ontology, TermType.CLASS);
        if (!ontologyTerms.isEmpty()) {
            int relationsCount = 0;
            for (Map.Entry<Term, Double> score : scores.entrySet()) {
                relationsCount += score.getValue();
            }
            log.debug(relationsCount + "");
            assertEquals(-1, Double.compare(0.0, relationsCount));

            // all properties should not have a single class relation, obviously
            Set<Term> allOntProperties = this.repository.getAllTerms(ontology, TermType.PROPERTY);
            for (Term property : allOntProperties) {
                log.debug(property.getTermUri());
                log.debug(scores.get(property) + "");
                assertEquals(0, Double.compare(0.0, scores.get(property)));
            }
        }
    }
}
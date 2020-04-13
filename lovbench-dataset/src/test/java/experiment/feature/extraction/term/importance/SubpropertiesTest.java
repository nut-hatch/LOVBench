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

public class SubpropertiesTest {

    AbstractOntologyRepository repository = ExperimentConfiguration.getInstance().getRepository();

    private static final Logger log = LoggerFactory.getLogger( SubpropertiesTest.class );

    @Before
    public void setUp() throws Exception {
        new TestUtil().setNqFileConfiguration();
    }

    @Test
    public void computeAllScores() {
        if (ExperimentConfiguration.getInstance().isMakeExtensiveTests()) {
            Subproperties subproperties = new Subproperties(repository, new TermStatsScorer(repository));
            subproperties.computeAllScores();

            for (Ontology ontology : this.repository.getAllOntologies()) {
                Set<Term> termSet = this.repository.getAllTerms(ontology);
                if (!termSet.isEmpty()) {

                    // sanity check: all class should not have a single subproperty, obviously
                    Set<Term> allOntProperties = this.repository.getAllTerms(ontology, TermType.CLASS);
                    for (Term property : allOntProperties) {
                        log.debug(property.getTermUri());
                        log.debug(subproperties.getScore(property) + "");
                        assertEquals(0, Double.compare(0.0, subproperties.getScore(property)));
                    }
                }
            }
        }
    }

    @Test
    public void computeScores() {
        Subproperties subproperties = new Subproperties(repository, new TermStatsScorer(repository));
        Ontology ontology = new Ontology("http://purl.obolibrary.org/obo/obi.owl");
        Map<Term, Double> scores = subproperties.computeScores(this.repository.getAllTerms(ontology), ontology);
        Set<Term> ontologyTerms = this.repository.getAllTerms(ontology, TermType.PROPERTY);
        if (!ontologyTerms.isEmpty()) {
            int subpropertiesCount = 0;
            for (Map.Entry<Term, Double> score : scores.entrySet()) {
                subpropertiesCount += score.getValue();
            }
            log.debug(subpropertiesCount + "");
            assertEquals(-1, Double.compare(0.0, subpropertiesCount));

            // all classes should not have a single subproperty, obviously
            Set<Term> allOntClasses = this.repository.getAllTerms(ontology, TermType.CLASS);
            for (Term property : allOntClasses) {
                log.debug(property.getTermUri());
                log.debug(scores.get(property) + "");
                assertEquals(0, Double.compare(0.0, scores.get(property)));
            }
        }
    }
}
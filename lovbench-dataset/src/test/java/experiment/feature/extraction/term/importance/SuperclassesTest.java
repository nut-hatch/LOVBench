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

public class SuperclassesTest {

    AbstractOntologyRepository repository = ExperimentConfiguration.getInstance().getRepository();

    private static final Logger log = LoggerFactory.getLogger( SuperclassesTest.class );

    @Before
    public void setUp() throws Exception {
        new TestUtil().setNqFileConfiguration();
    }

    @Test
    public void computeAllScores() {
        if (ExperimentConfiguration.getInstance().isMakeExtensiveTests()) {
            Superclasses superClasses = new Superclasses(repository, new TermStatsScorer(repository));
            superClasses.computeAllScores();

            for (Ontology ontology : this.repository.getAllOntologies()) {
                Set<Term> termSet = this.repository.getAllTerms(ontology);
                if (!termSet.isEmpty()) {

                    // sanity check: all properties should not have a single superclass, obviously
                    Set<Term> allOntProperties = this.repository.getAllTerms(ontology, TermType.PROPERTY);
                    for (Term property : allOntProperties) {
                        log.debug(property.getTermUri());
                        log.debug(superClasses.getScore(property) + "");
                        assertEquals(0, Double.compare(0.0, superClasses.getScore(property)));
                    }
                }
            }
        }
    }

    @Test
    public void computeScores() {
        Superclasses superClasses = new Superclasses(repository, new TermStatsScorer(repository));
        Ontology ontology = new Ontology("http://purl.obolibrary.org/obo/obi.owl");
        Map<Term, Double> scores = superClasses.computeScores(this.repository.getAllTerms(ontology), ontology);
        int superClassesCount = 0;
        for (Map.Entry<Term,Double> score : scores.entrySet()) {
            superClassesCount += score.getValue();
        }
        log.debug(superClassesCount+"");
        assertEquals(-1, Double.compare(0.0,superClassesCount));

        // all properties should not have a single subclass, obviously
        Set<Term> allOntProperties = this.repository.getAllTerms(ontology, TermType.PROPERTY);
        for (Term property: allOntProperties) {
            log.debug(property.getTermUri());
            log.debug(scores.get(property)+"");
            assertEquals(0, Double.compare(0.0,scores.get(property)));
        }
    }
}
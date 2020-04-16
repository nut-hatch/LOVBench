package experiment.feature.extraction.term.importance;

import experiment.TestUtil;
import experiment.feature.scoring.graph.BetweennessScorer;
import experiment.model.Ontology;
import experiment.model.Term;
import experiment.configuration.ExperimentConfiguration;
import experiment.repository.triplestore.AbstractOntologyRepository;
import experiment.repository.triplestore.connector.JenaConnector;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

public class BetweennessMeasureTermTest {

    AbstractOntologyRepository repository = ExperimentConfiguration.getInstance().getRepository();

    private static final Logger log = LoggerFactory.getLogger( BetweennessMeasureTermTest.class );

    @Before
    public void setUp() throws Exception {
        new TestUtil().setNqFileConfiguration();
    }

    @Test
    public void computeAllScores() {
        if (ExperimentConfiguration.getInstance().isMakeExtensiveTests()) {
            BetweennessMeasureTerm bmt = new BetweennessMeasureTerm(repository, new BetweennessScorer(repository));
            bmt.computeAllScores();

            int countOntsZero = 0;
            for (Ontology ontology : this.repository.getAllOntologies()) {
                int countTermsNotZero = 0;
                boolean bolAllZero = true;
                Set<Term> termSet = repository.getAllTerms(ontology);
                if (termSet.isEmpty()) {
                    log.debug("No terms found for ontology " + ontology.getOntologyUri());
                } else {
                    for (Term term : termSet) {
                        log.info("Term: " + term.getTermUri() + ", " + bmt.getFeatureName() + ": " + bmt.getScore(term));
                        if (bmt.getScore(term) > 0) {
                            bolAllZero = false;
                            countTermsNotZero++;
                        }
                    }
                    log.info(countTermsNotZero+"");
                    if (bolAllZero) {
                        countOntsZero++;
                    }
                }
            }
            log.info(countOntsZero+"");
        }
    }

    @Test
    public void computeScores() {

        BetweennessMeasureTerm bmt = new BetweennessMeasureTerm(repository, new BetweennessScorer(repository));

        Ontology schemaOnt = new Ontology("http://schema.org/");
        Set<Term> schemaTerms = repository.getAllTerms(schemaOnt);

        Map<Term, Double> scores = bmt.computeScores(schemaTerms, schemaOnt);
        double sum = 0.0;
        for (Map.Entry<Term,Double> score : scores.entrySet()) {
            log.debug(score.getKey() + ": " + score.getValue());
            sum += score.getValue();
        }
        log.debug(sum+"");
        assertEquals(-1,Double.compare(0.0,sum));
    }
}
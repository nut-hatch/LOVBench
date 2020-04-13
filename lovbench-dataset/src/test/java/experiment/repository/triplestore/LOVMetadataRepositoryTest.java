package experiment.repository.triplestore;

import experiment.TestUtil;
import experiment.configuration.ExperimentConfiguration;
import experiment.model.Ontology;
import experiment.model.Prefix;
import experiment.repository.triplestore.connector.JenaConnector;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

public class LOVMetadataRepositoryTest {

    AbstractOntologyMetadataRepository metadataRepository = ExperimentConfiguration.getInstance().getRepositoryMetadata();
    AbstractOntologyRepository repository = ExperimentConfiguration.getInstance().getRepository();



    private static final Logger log = LoggerFactory.getLogger( LOVMetadataRepositoryTest.class );

    @Before
    public void setUp() throws Exception {
        new TestUtil().setNqFileConfiguration();
    }

    @Test
    public void getAllVoafRelations() {
        Set<Pair<Ontology, Ontology>> voafRelations = metadataRepository.getAllVoafRelations();
        log.debug(voafRelations.size()+"");
        Set<Ontology> allOntologies = repository.getAllOntologies();
        Set<Ontology> allOntologiesWithRelation = new HashSet<>();

        for (Pair<Ontology, Ontology> relation : voafRelations) {
            allOntologiesWithRelation.add(relation.getLeft());
            allOntologiesWithRelation.add(relation.getRight());
        }

        Set<Ontology> knownVocabsNoWithNoRelations = new HashSet<>();
        knownVocabsNoWithNoRelations.add(new Ontology("http://www.daml.org/2001/09/countries/iso-3166-ont"));

        log.debug(allOntologies.size()+"");
        log.debug(allOntologiesWithRelation.size()+"");
        log.debug(knownVocabsNoWithNoRelations.size()+"");
        allOntologies.removeAll(knownVocabsNoWithNoRelations);
        assertTrue(allOntologiesWithRelation.containsAll(allOntologies));
    }

    @Test
    public void getAllVocabPrefixes() {
        Set<Prefix> prefixes = metadataRepository.getAllVocabPrefixes();
        for (Prefix prefix : prefixes) {
            log.debug(prefix.getOntologyUri());
            log.debug(prefix.getTermPrefix());
            // checking a few problematic cases
            if (prefix.getOntologyUri().equals("http://ns.inria.fr/ludo/v1/gamepresentation#")) {
                assertEquals("http://ns.inria.fr/ludo/v1/gamemodel#", prefix.getTermPrefix());
            }
            if (prefix.getOntologyUri().equals("http://ns.inria.fr/ludo")) {
                assertEquals("http://nr.inria.fr/ludo/", prefix.getTermPrefix());
            }
            if (prefix.getOntologyUri().equals("http://purl.org/LiMo/0.1#")) {
                assertEquals("http://purl.org/LiMo/0.1/", prefix.getTermPrefix());
            }
            if (prefix.getOntologyUri().equals("https://decision-ontology.googlecode.com/svn/trunk/decision.owl")) {
                assertEquals("http://decision-ontology.googlecode.com/svn/trunk/decision.owl#", prefix.getTermPrefix());
            }
            if (prefix.getOntologyUri().equals("http://qudt.org/schema/qudt")) {
                assertEquals("http://qudt.org/schema/qudt/", prefix.getTermPrefix());
            }
            if (prefix.getOntologyUri().equals("http://loted.eu/ontology")) {
                assertEquals("http://www.loted.eu/ontology#", prefix.getTermPrefix());
            }
            if (prefix.getOntologyUri().equals("http://purl.org/twc/ontology/cdm.owl#")) {
                assertEquals("http://purl.org/twc/ontology/", prefix.getTermPrefix());
            }

        }
    }
}
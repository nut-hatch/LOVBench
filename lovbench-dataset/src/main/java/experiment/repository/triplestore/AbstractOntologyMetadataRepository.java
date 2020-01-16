package experiment.repository.triplestore;

import experiment.model.Ontology;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.Set;

/**
 * Abstract class for a database that contains the LOV.n3 metadata about the ontologies.
 */
public abstract class AbstractOntologyMetadataRepository extends AbstractRepository {

    public AbstractOntologyMetadataRepository(String dbName) {
        super(dbName);
    }

    /**
     * Returns all related ontologies as specified by the voaf vocabulary.
     *
     * @return
     */
    public abstract Set<Pair<Ontology, Ontology>> getAllVoafRelations();

}

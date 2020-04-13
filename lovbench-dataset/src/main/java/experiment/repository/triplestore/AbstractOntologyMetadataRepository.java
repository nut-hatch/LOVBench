package experiment.repository.triplestore;

import com.hp.hpl.jena.query.DatasetFactory;
import com.hp.hpl.jena.rdf.model.Model;
import experiment.configuration.ExperimentConfiguration;
import experiment.model.Ontology;
import experiment.model.Prefix;
import experiment.repository.triplestore.connector.AbstractConnector;
import experiment.repository.triplestore.connector.JenaConnector;
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


    public void setConnector(AbstractConnector connector) {
        this.connector = connector;
        // Make sure ONLY the VOAF graph is in the ontology metadata repository.
        if (connector instanceof JenaConnector) {
            Model voafModel = ((JenaConnector) this.getConnector()).getDataset().getNamedModel(ExperimentConfiguration.getInstance().getLOVgraph());
            ((JenaConnector) this.getConnector()).setDataset(DatasetFactory.create(voafModel));
        }
    }

    /**
     * Returns all related ontologies as specified by the voaf vocabulary.
     *
     * @return
     */
    public abstract Set<Pair<Ontology, Ontology>> getAllVoafRelations();

    public abstract Set<Prefix> getAllVocabPrefixes();


}

package experiment.repository.triplestore;

import experiment.model.Ontology;
import experiment.repository.file.ExperimentConfiguration;
import experiment.repository.file.LOVPrefixes;
import experiment.repository.triplestore.connector.AbstractConnector;
import experiment.repository.triplestore.connector.StardogConnector;
import org.apache.commons.lang3.tuple.Pair;
import org.openrdf.query.BindingSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Repository implementation for LOV.n3 database in stardog triple store.
 */
public class LOVMetadataRepository extends AbstractOntologyMetadataRepository {

    /**
     * Singleton object
     */
    private static LOVMetadataRepository lovMetadataRepository = null;

    private static final Logger log = LoggerFactory.getLogger( LOVMetadataRepository.class );

    private LOVMetadataRepository(String dbName) {
        super(dbName);
    }

    /**
     * Access to the Singleton object, has to be initialized with the database name.
     * @return LOVMetadataRepository
     */
    public static LOVMetadataRepository getInstance() {
        if (lovMetadataRepository == null) {
            log.error("Repository needs to be initiated with db name");
        }
        return lovMetadataRepository;
    }

    /**
     * Instantiates singleton object.
     *
     * @param dbname
     * @return LOVMetadataRepository
     */
    public static LOVMetadataRepository getInstance(String dbname) {
        if (lovMetadataRepository == null) {
            lovMetadataRepository = new LOVMetadataRepository(dbname);
        }
        return lovMetadataRepository;
    }

    @Override
    public AbstractConnector getConnector() {
        if (this.connector == null) {
            this.connector = new StardogConnector(this.dbName, ExperimentConfiguration.getInstance().getDbServer(), ExperimentConfiguration.getInstance().getDbUser(), ExperimentConfiguration.getInstance().getDbPassword());
        }
        return connector;
    }

    @Override
    public Set<Pair<Ontology, Ontology>> getAllVoafRelations() {
        Set<Pair<Ontology, Ontology>> ontologyRelations = new HashSet<>();

        String sparql = "SELECT DISTINCT ?vocabPrefix ?hasVoafRelationToPrefix { ?vocabURI a <http://purl.org/vocommons/voaf#Vocabulary>. ?vocabURI vann:preferredNamespacePrefix ?vocabPrefix. ?vocabURI <http://www.w3.org/ns/dcat#distribution> ?distribution . ?distribution <http://purl.org/dc/terms/issued> ?date . ?distribution <http://purl.org/vocommons/voaf#specializes>|<http://purl.org/vocommons/voaf#reliesOn>|<http://purl.org/vocommons/voaf#extends>|<http://purl.org/vocommons/voaf#metadataVoc>|<http://purl.org/vocommons/voaf#generalizes> ?hasVoafRelationTo . ?hasVoafRelationTo <http://purl.org/vocab/vann/preferredNamespacePrefix> ?hasVoafRelationToPrefix . filter not exists { ?vocabURI <http://www.w3.org/ns/dcat#distribution>/<http://purl.org/dc/terms/issued> ?dateForFilter filter (xsd:date(?date) < xsd:date(?dateForFilter)) } } ORDER BY ?vocabPrefix";
        List<BindingSet> relationResults = this.getConnector().selectQuery(sparql);

        for (BindingSet relationResult : relationResults) {
            String fromOntology = relationResult.getValue("vocabPrefix").stringValue();
            String toOntology = relationResult.getValue("hasVoafRelationToPrefix").stringValue();
            ontologyRelations.add(Pair.of(new Ontology(LOVPrefixes.getInstance().getOntologyUri(fromOntology)), new Ontology(LOVPrefixes.getInstance().getOntologyUri(toOntology))));
        }

        return ontologyRelations;
    }
}

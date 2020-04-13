package experiment.repository.triplestore;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.vocabulary.XSD;
import experiment.model.Ontology;
import experiment.configuration.ExperimentConfiguration;
import experiment.model.Prefix;
import experiment.model.Term;
import experiment.repository.file.LOVPrefixes;
import experiment.repository.triplestore.connector.AbstractConnector;
import experiment.repository.triplestore.connector.StardogConnector;
import org.apache.commons.lang3.tuple.Pair;
import org.openrdf.query.BindingSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

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
        AbstractOntologyRepository lovRepository = ExperimentConfiguration.getInstance().getRepository();
        Set<Ontology> allOntologies = lovRepository.getAllOntologies();
        Set<Pair<Ontology, Ontology>> ontologyRelations = new HashSet<>();

        String sparql = "SELECT DISTINCT ?vocabPrefix ?hasVoafRelationToPrefix ?vocabURI ?hasVoafRelationTo { ?vocabURI a <http://purl.org/vocommons/voaf#Vocabulary>. ?vocabURI <http://purl.org/vocab/vann/preferredNamespacePrefix> ?vocabPrefix. ?vocabURI <http://www.w3.org/ns/dcat#distribution> ?distribution . ?distribution <http://purl.org/dc/terms/issued> ?date . ?distribution <http://purl.org/vocommons/voaf#specializes>|<http://purl.org/vocommons/voaf#reliesOn>|<http://purl.org/vocommons/voaf#extends>|<http://purl.org/vocommons/voaf#metadataVoc>|<http://purl.org/vocommons/voaf#generalizes> ?hasVoafRelationTo . ?hasVoafRelationTo <http://purl.org/vocab/vann/preferredNamespacePrefix> ?hasVoafRelationToPrefix . filter not exists { ?vocabURI <http://www.w3.org/ns/dcat#distribution>/<http://purl.org/dc/terms/issued> ?dateForFilter filter (<"+ XSD.getURI()+"date>(?date) < <"+ XSD.getURI()+"date>(?dateForFilter)) } } ORDER BY ?vocabPrefix";
        List<BindingSet> relationResults = this.getConnector().selectQuery(sparql);

        for (BindingSet relationResult : relationResults) {
//            String fromOntology = relationResult.getValue("vocabPrefix").stringValue();
//            String toOntology = relationResult.getValue("hasVoafRelationToPrefix").stringValue();
            Ontology fromOntology = new Ontology(relationResult.getValue("vocabURI").stringValue());
            Ontology toOntology = new Ontology(relationResult.getValue("hasVoafRelationTo").stringValue());
//            log.info(relationResult.getValue("vocabURI").stringValue());
//            log.info(relationResult.getValue("vocabPrefix").stringValue());
//            log.info(relationResult.getValue("hasVoafRelationTo").stringValue());
//            log.info(relationResult.getValue("hasVoafRelationToPrefix").stringValue());
            if (allOntologies.contains(fromOntology) && allOntologies.contains(toOntology)) {
                ontologyRelations.add(Pair.of(fromOntology,toOntology));
            }
//            ontologyRelations.add(Pair.of(new Ontology(LOVPrefixes.getInstance().getOntologyUri(fromOntology)), new Ontology(LOVPrefixes.getInstance().getOntologyUri(toOntology))));
        }

        return ontologyRelations;
    }

    @Override
    public Set<Prefix> getAllVocabPrefixes() {
        AbstractOntologyRepository lovRepository = ExperimentConfiguration.getInstance().getRepository();

        String sparql = "SELECT DISTINCT ?vocabURI ?vocabPrefix ?termPrefix { ?vocabURI a <http://purl.org/vocommons/voaf#Vocabulary> . ?vocabURI <http://purl.org/vocab/vann/preferredNamespacePrefix> ?vocabPrefix . ?vocabURI <http://purl.org/vocab/vann/preferredNamespaceUri> ?termPrefix . }";
        List<BindingSet> vocabResults = this.getConnector().selectQuery(sparql);

        Set<Prefix> prefixes = new HashSet<>();
        for (BindingSet vocabResult : vocabResults) {
            // Values as in the NQ file
            String vocabURI = vocabResult.getValue("vocabURI").stringValue();
            String vocabPrefix = vocabResult.getValue("vocabPrefix").stringValue();
            String termPrefix = vocabResult.getValue("termPrefix").stringValue();

            // check if the termprefix is actually used
            int termPrefixAppearance = lovRepository.countAppearanceOfTermPrefix(vocabURI,termPrefix);

            // Handle incomplete term prefix with best effort
            Prefix prefix;
            if ((termPrefix.charAt(termPrefix.length()-1) != '#' && termPrefix.charAt(termPrefix.length()-1) != '/') || termPrefixAppearance == 0) {
                // We have to look into the actual ontology to see which prefix or prefixes are used...
                int countHash = lovRepository.countAppearanceOfTermPrefix(vocabURI,termPrefix+"#");
                int countSlash = lovRepository.countAppearanceOfTermPrefix(vocabURI,termPrefix+"/");
                int countSlashHash = lovRepository.countAppearanceOfTermPrefix(vocabURI,termPrefix+"/#"); // nothing is impossible...

                if ((countHash == 0 && countSlash == 0 && countSlashHash == 0) || termPrefixAppearance == 0) {
                    log.error("------- Next Entry -------");
                    log.error("No reliable term prefix found for " + vocabURI + "! This should be fixed in the LOV/VOAF graph.");
                    log.error("Trying our best to find the most likeliest actual term prefix......");

                    Set<Resource> allURIs = lovRepository.getAllURIs(vocabURI);
                    if (allURIs.size() > 0) {
                        Map<String, Integer> countNamespaceOccurences = new HashMap<>();
                        for (Resource resource : allURIs) {
                            if (!countNamespaceOccurences.containsKey(resource.getNameSpace())) {
                                countNamespaceOccurences.put(resource.getNameSpace(), 1);
                            } else {
                                countNamespaceOccurences.put(resource.getNameSpace(), countNamespaceOccurences.get(resource.getNameSpace()) + 1);
                            }
                        }
                        Map.Entry<String, Integer> max = null;
                        for (Map.Entry<String, Integer> entry : countNamespaceOccurences.entrySet()) {
                            if (max == null || entry.getValue() > max.getValue()) {
                                max = entry;
                            }
                        }
                        log.error("Our best guess for vocab " + vocabURI + " is: " + max.getKey());
                        prefix = new Prefix(vocabPrefix, vocabURI, max.getKey(), "");
                    } else {
                        log.error("ALERT FOR " + vocabURI + ": The ontology defined in the voaf graph is either missing in the dump or does not contain a single URI node and will be ignored.");
                        prefix = null;
                    }

                } else if (countHash >= countSlash && countHash >= countSlashHash) {
                    if (countHash == countSlash) {
                        prefix = new Prefix(vocabPrefix, vocabURI, termPrefix+"#", termPrefix+"/");
                    } else if (countHash == countSlashHash) {
                        prefix = new Prefix(vocabPrefix, vocabURI, termPrefix+"#", termPrefix+"/#");
                    } else {
                        prefix = new Prefix(vocabPrefix, vocabURI, termPrefix+"#", "");
                    }
                } else if (countSlash >= countSlashHash) {
                    if (countHash == countSlashHash) {
                        prefix = new Prefix(vocabPrefix, vocabURI, termPrefix + "/", termPrefix + "/#");
                    } else {
                        prefix = new Prefix(vocabPrefix, vocabURI, termPrefix + "/", "");
                    }
                } else {
                    prefix = new Prefix(vocabPrefix, vocabURI, termPrefix + "/#", "");
                }
            } else {
                prefix = new Prefix(vocabPrefix, vocabURI, termPrefix, "");
            }
            if (prefix != null) {
                prefixes.add(prefix);
            }
        }
        Prefix xsd = new Prefix("xsd", XSD.getURI(), XSD.getURI(), "");
        prefixes.add(xsd);
        return prefixes;
    }
}

package experiment.model;

/**
 * Class that keeps prefix information of ontology to resolve mismatches between terms and ontologies.
 *
 */
public class Prefix {

    /**
     * The prefix of the ontology.
     */
    String ontologyPrefix;

    /**
     * The URI of the ontology.
     */
    String ontologyUri;

    /**
     * The string that is used for terms' full URI in replacement for the ontology prefix.
     */
    String termPrefix;

    /**
     * An alternative string that might be used for terms' full URI in replacement for the ontology prefix.
     */
    String alternativeTermPrefix;

    public Prefix(String ontologyPrefix, String ontologyUri, String termPrefix, String alternativeTermPrefix) {
        this.ontologyPrefix = ontologyPrefix;
        this.ontologyUri = ontologyUri;
        this.termPrefix = termPrefix;
        this.alternativeTermPrefix = alternativeTermPrefix;
    }

    public String getOntologyPrefix() {
        return ontologyPrefix;
    }

    public void setOntologyPrefix(String ontologyPrefix) {
        this.ontologyPrefix = ontologyPrefix;
    }

    public String getOntologyUri() {
        return ontologyUri;
    }

    public void setOntologyUri(String ontologyUri) {
        this.ontologyUri = ontologyUri;
    }

    public String getTermPrefix() {
        return termPrefix;
    }

    public void setTermPrefix(String termPrefix) {
        this.termPrefix = termPrefix;
    }

    public String getAlternativeTermPrefix() {
        return alternativeTermPrefix;
    }

    public void setAlternativeTermPrefix(String alternativeTermPrefix) {
        this.alternativeTermPrefix = alternativeTermPrefix;
    }
}

package experiment.model;

import experiment.repository.file.LOVPrefixes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class that represents an ontology in the collection.
 */
public class Ontology extends RankingElement {

    /**
     * The URI of the ontology.
     */
    String ontologyUri;

    private static final Logger log = LoggerFactory.getLogger( Ontology.class );

    public Ontology(String ontologyUri) {
        this.ontologyUri = ontologyUri;
    }

    /**
     * Returns the prefix of the URI.
     *
     * @return String
     */
    public String getOntologyPrefix() {
        return LOVPrefixes.getInstance().getOntologyPrefix(this.ontologyUri);
    }

    /**
     * Return the prefix used for terms of this ontology.
     *
     * @return
     */
    public String getOntologyTermPrefix() {
        return LOVPrefixes.getInstance().getTermPrefixForOntologyPrefix(this.getOntologyPrefix());
    }

    public String getOntologyAlternativeTermPrefix() {
        return LOVPrefixes.getInstance().getAlternativeTermPrefixForOntologyPrefix(this.getOntologyPrefix());
    }

    public String getOntologyUri() {
        return ontologyUri;
    }

    public void setOntologyUri(String ontologyUri) {
        this.ontologyUri = ontologyUri;
    }

    @Override
    public int hashCode() {
        return this.ontologyUri.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Ontology) {
            return this.ontologyUri.equals(((Ontology) obj).getOntologyUri());
        }
        return false;
    }

    @Override
    public String toString() {
        return this.getOntologyUri();
    }
}

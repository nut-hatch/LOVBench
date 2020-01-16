package experiment.model;

import experiment.repository.file.LOVPrefixes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * Class that represents a term in the ontology collection.
 */
public class Term {

    /**
     * The full URI of the term.
     */
    String termUri;

    String localName;

    private static final Logger log = LoggerFactory.getLogger( Term.class );

    /**
     * Term URI will be transformed into its full form, except for blank nodes.
     *
     * @param termUri
     */
    public Term(String termUri) {
        if (!LOVPrefixes.getInstance().isFullUri(termUri) && !LOVPrefixes.getInstance().isBlankNode(termUri)) {
            log.debug(termUri);
            termUri = LOVPrefixes.getInstance().getFullUri(termUri);
            log.debug(termUri);
        }
        this.termUri = termUri;
        this.localName = LOVPrefixes.getInstance().getLocalName(termUri);
    }

    /**
     * Gets the ontology URI of the term.
     *
     * @return String
     */
    public String getOntologyUriOfTerm() {
        return LOVPrefixes.getInstance().getOntologyUriOfTermUri(this.termUri);
    }

    public String getTermUri() {
        return termUri;
    }

    public String getAlternativeUri() {
        String alternativeTermUri = "";
        String ontologyUri = LOVPrefixes.getInstance().getOntologyUriOfTermUri(this.termUri);
        log.debug(this.termUri+ ": "+ ontologyUri);
        if (!LOVPrefixes.getInstance().ontologyUri2prefixes.containsKey(ontologyUri)) {
            log.debug("CHECK PREFIX!");
        } else {
            String alternativePrefix = LOVPrefixes.getInstance().ontologyUri2prefixes.get(ontologyUri).getAlternativeTermPrefix();
            if (alternativePrefix != null && !alternativePrefix.isEmpty()) {
                alternativeTermUri = alternativePrefix + this.localName;
            }
        }
        return alternativeTermUri;
    }

    public void setTermUri(String termUri) {
        this.termUri = termUri;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Term term = (Term) o;
        return Objects.equals(termUri, term.termUri);
    }

    @Override
    public int hashCode() {

        return Objects.hash(termUri);
    }

    @Override
    public String toString() {
        return this.termUri;
    }
}

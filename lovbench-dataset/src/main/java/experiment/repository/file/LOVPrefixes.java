package experiment.repository.file;

import experiment.model.Prefix;
import experiment.model.Term;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Class that is responsible to resolve mismatches of term and ontology URIs in the LOV collection
 *
 */
public class LOVPrefixes {

    /**
     * Singleton object.
     */
    private static LOVPrefixes LOVPrefixesInstance = null;

    /**
     * Map from ontology prefixes to complete Prefix object.
     */
    public Map<String,Prefix> ontologyprefix2prefixes;

    /**
     * Maps from ontology URIs to complete Prefix object.
     */
    public Map<String,Prefix> ontologyUri2prefixes;

    private Set<String> alternativePrefixes;

    private static final Logger log = LoggerFactory.getLogger( LOVPrefixes.class );

    /**
     * Parses the LOV prefix file into Prefix objects.
     */
    private LOVPrefixes() {
        String filename = ExperimentConfiguration.getInstance().getLovPrefixesFile();
        this.ontologyprefix2prefixes = new HashMap<>();
        this.ontologyUri2prefixes = new HashMap<>();
        this.alternativePrefixes = new HashSet<>();

        JSONObject json = FileUtil.parseJSON(filename);
        try {
            JSONArray prefixesArray = json.getJSONObject("results").getJSONArray("bindings");
            for (int i = 0; i < prefixesArray.length(); i++) {

                JSONObject prefixObject = prefixesArray.getJSONObject(i);
                String ontologyPrefix = prefixObject.getJSONObject("vocabPrefix").getString("value");
                String ontologyUri = prefixObject.getJSONObject("vocabURI").getString("value");

                String termPrefix = prefixObject.getJSONObject("termPrefix").getString("value");
                String termPrefix2 = "";

                if (prefixObject.has("termPrefix2")) {
                    termPrefix2 = prefixObject.getJSONObject("termPrefix2").getString("value");
                    this.alternativePrefixes.add(termPrefix2);
                }
                Prefix prefix = new Prefix(ontologyPrefix, ontologyUri, termPrefix, termPrefix2);
                this.ontologyprefix2prefixes.put(ontologyPrefix, prefix);
                this.ontologyUri2prefixes.put(ontologyUri, prefix);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets singleton instance.
     *
     * @return LOVPrefixes
     */
    public static LOVPrefixes getInstance() {
        if (LOVPrefixesInstance == null) {
            LOVPrefixesInstance = new LOVPrefixes();
        }
        return LOVPrefixesInstance;
    }

    /**
     * Returns the full URI of a give term URI (which might come in the form of prefix:term)
     *
     * @param termUri
     * @return String
     */
    public String getFullUri(String termUri) {
        if (this.isFullUri(termUri)) {
            //Potentially match alternative term prefix to term prefix
            if (this.alternativePrefixes.contains(this.getOntologyUriOfTermUri(termUri))) {
                String preferredTermPrefix = this.getTermPrefixForOntologyPrefix(this.getOntologyPrefix(this.getOntologyUriOfTermUri(termUri)));
                String localname = this.getLocalName(termUri);
                return preferredTermPrefix + localname;
            }
            log.debug(String.format("Term uri %s already a full uri!", termUri));
            return termUri;
        }
        log.info("!!!!!!!!!!!!!!");
        log.info(termUri);
        String[] termParts = termUri.split(":",2);
        String ontologyPrefix = termParts[0];
        String termString = termParts[1];
        String termPrefix = this.getTermPrefixForOntologyPrefix(ontologyPrefix);

        if (termPrefix == null) {
            // If no term prefix is known, just return original - at least we tried
            log.debug(String.format("No term prefix found for ontology prefix %s extracted from term uri %s", ontologyPrefix, termUri));
            return termUri;
        }
        return termPrefix + termString;
    }

    /**
     * Evaluates whether a given term is in the form of a full URI.
     * Might require improvement.
     *
     * @param termUri
     * @return boolean
     */
    public boolean isFullUri(String termUri) {
        return (termUri.startsWith("http://") || termUri.startsWith("https://"));
    }

    /**
     * Evaluates whether a uri is a blank node.
     * Might require improvement.
     *
     * @param termUri
     * @return
     */
    public boolean isBlankNode(String termUri) {
        return !termUri.contains(":");
    }

    /**
     * Gets the default term prefix for an ontology given by its prefix.
     *
     * @param ontologyPrefix
     * @return String
     */
    public String getTermPrefixForOntologyPrefix(String ontologyPrefix) {
        String termPrefix = null;
        if (this.ontologyprefix2prefixes.containsKey(ontologyPrefix)) {
            termPrefix = this.ontologyprefix2prefixes.get(ontologyPrefix).getTermPrefix();
        }
        return termPrefix;
    }

    public String getAlternativeTermPrefixForOntologyPrefix(String ontologyPrefix) {
        String termPrefix = null;
        if (this.ontologyprefix2prefixes.containsKey(ontologyPrefix)) {
            termPrefix = this.ontologyprefix2prefixes.get(ontologyPrefix).getAlternativeTermPrefix();
        }
        return termPrefix;
    }

    /**
     * Returns the ontology prefix for an ontology URI.
     *
     * @param ontologyUri
     * @return String
     */
    public String getOntologyPrefix(String ontologyUri) {
        log.info(ontologyUri);
        return this.ontologyUri2prefixes.get(ontologyUri).getOntologyPrefix();
    }

    /**
     * Returns ontology URI based on ontology prefix.
     * @param ontologyPrefix
     * @return
     */
    public String getOntologyUri(String ontologyPrefix) {
        return this.ontologyprefix2prefixes.get(ontologyPrefix).getOntologyUri();
    }

    /**
     * Returns the ontology URI of a given term URI
     *
     * @param termUri
     * @return String
     */
    public String getOntologyUriOfTermUri(String termUri) {

        if (!this.isFullUri(termUri)) {
            termUri = this.getFullUri(termUri);
        }
        String ontologyUri = "";
        for (Map.Entry<String, Prefix> ontologyUri2prefix : this.ontologyUri2prefixes.entrySet()) {
            if ((termUri.startsWith(ontologyUri2prefix.getValue().getTermPrefix()) || (!ontologyUri2prefix.getValue().getAlternativeTermPrefix().isEmpty() && termUri.startsWith(ontologyUri2prefix.getValue().getAlternativeTermPrefix())))
                    && (ontologyUri.equals("") || ontologyUri2prefix.getKey().length()>ontologyUri.length())) {
                ontologyUri = ontologyUri2prefix.getKey();
            }
        }
        if (ontologyUri.isEmpty()) {
            log.debug(String.format("No LOV ontology uri found for term uri %s. Extracting ontology URI as external ontology", termUri));
            int posSlash = termUri.lastIndexOf('/');
            int posHash = termUri.lastIndexOf('#');
            ontologyUri = termUri.substring(0, Math.max(posSlash,posHash));
        }
        log.debug(String.format("Ontology uri for %s is: %s", termUri, ontologyUri));
        return ontologyUri;
    }

    public String getLocalName(String termUri) {
        int posSlash = termUri.lastIndexOf('/');
        int posHash = termUri.lastIndexOf('#');
        String localname = termUri.substring(Math.max(posSlash,posHash)+1,termUri.length());
        return localname;
    }

    public String getFullUri(Term term) {
        return this.getFullUri(term.getTermUri());
    }

//    public String getPrefixUri(String uri) {
//        if (!this.isFullUri(uri)) {
//            return uri;
//        }
//        char splitChar = '/';
//        if (uri.indexOf('#') >= 0) {
//            splitChar = '#';
//        }
//        System.out.println(splitChar);
//        int lastIndexOf = uri.lastIndexOf(splitChar);
//        String fullOntologyIRI = uri.substring(0, lastIndexOf);
//        String term = uri.substring(lastIndexOf+1, uri.length());
//        System.out.println(fullOntologyIRI);
//        String prefix = this.getOntologyPrefix(fullOntologyIRI);
//        return  prefix + ":" + term;
//    }

//    public Map<String, String> getPrefixes() {
////        for (Map.Entry<String,String> entry : this.prefixes.entrySet()) {
////            log.debug(entry.getKey() + " : " + entry.getValue());
////        }
//        return prefixes;
//    }

//    public String getOntologyPrefix(String ontologyUri) {
//        System.out.println(ontologyUri);
//        String ontologyPrefix = this.graphname2prefix.get(ontologyUri);
//        if (ontologyPrefix == null) {
//            ontologyPrefix = this.graphname2prefix.get(ontologyUri + "#");
//        }
//        if (ontologyPrefix == null) {
//            ontologyPrefix = this.graphname2prefix.get(ontologyUri + "/");
//        }
//        return ontologyPrefix;
//    }


}

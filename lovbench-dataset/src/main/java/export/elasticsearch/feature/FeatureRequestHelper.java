package export.elasticsearch.feature;

import experiment.feature.FeatureFactory;
import experiment.feature.extraction.AbstractFeature;
import experiment.feature.extraction.ontology.importance.AbstractOntologyImportanceFeature;
import experiment.feature.extraction.term.importance.AbstractTermImportanceFeature;
import experiment.feature.extraction.term.relevance.LOVTermMatch;
import experiment.model.Term;
import experiment.model.query.AbstractQuery;
import experiment.model.query.TermQuery;
import experiment.repository.file.FeatureSetScores;
import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Class that provides helper function to build JSON objects for elasticsearch queries.
 */
public class FeatureRequestHelper {

    private static final String relevanceFeatureFolder = "src/main/resources/ltr/relevance-features/";

    private static final Logger log = LoggerFactory.getLogger(FeatureRequestHelper.class);

    /**
     * Returns a mapping for all IMPORTANCE features in the feature list, meant to UPDATE a document mapping in an index.
     *
     * @param features
     * @return
     * @throws FileNotFoundException
     */
    public static JSONObject getFeatureMapping(List<AbstractFeature> features) {
        JSONObject featureMappingObject = new JSONObject();
        JSONObject properties = new JSONObject();
        for (AbstractFeature feature : features) {
            if (feature instanceof AbstractOntologyImportanceFeature || feature instanceof AbstractTermImportanceFeature) {
                JSONObject featureObject = new JSONObject();
                featureObject.put("type", "double");
                properties.put(feature.getFeatureName(), featureObject);
            }
        }
        featureMappingObject.put("properties", properties);

        return featureMappingObject;
    }

    /**
     * Reads the file with one feature name per line and creates a list with feature objects.
     *
     * @param featureSetFile
     * @return
     * @throws FileNotFoundException
     */
    public static List<AbstractFeature> readFeatureSetFile(String featureSetFile) throws FileNotFoundException {
        File file = new File(featureSetFile);
        if (!file.exists()) {
            throw new FileNotFoundException("Feature mapping file not found!");
        }

        List<AbstractFeature> featureList = new ArrayList<>();
        FeatureFactory featureFactory = new FeatureFactory();

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                AbstractFeature feature = featureFactory.getFeature(line);
                if (feature == null) {
                    log.error("Feature " + line + " from configuration file not found. Check feature name spelling.");
                } else {
                    featureList.add(feature);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return featureList;
    }



    /**
     * Based on a given feature list, creates the json object with the corresponding entries for the feature set
     * to be added to the feature store ind the LTR index.
     *
     * This shortcut assumes elastic search term index to be named "terms"
     * Prefer usage of @getFeatureSetSpecification(List<AbstractFeature> featureList, String validationQuery, String validationIndex)
     *
     * @param featureList
     * @return
     */
    public static JSONObject getFeatureSetSpecification(List<AbstractFeature> featureList) {
        return getFeatureSetSpecification(featureList, "test", "terms");
    }

    public static JSONObject getFeatureSetSpecification(List<AbstractFeature> featureList, String validationQuery, String validationIndex) {
        JSONArray featureArray = new JSONArray();
        for (AbstractFeature feature : featureList) {
            if (feature instanceof AbstractOntologyImportanceFeature || feature instanceof AbstractTermImportanceFeature) {
                featureArray.put(getImportanceFeatureSpecification(feature));
            } else if (feature instanceof LOVTermMatch) {
                featureArray.put(getRelevanceFeatureSpecification(feature));
            } else {
                log.error("Feature " + feature.getFeatureName() + " is a relevance feature and thus requires a respective elastic search query to be defined. See resources in " + relevanceFeatureFolder);
            }
        }
        JSONObject featureSet = new JSONObject().put("featureset", new JSONObject().put("features", featureArray));
        if (validationQuery != null && !validationQuery.isEmpty() && validationIndex != null && !validationIndex.isEmpty()) {
            featureSet.put("validation", new JSONObject()
                    .put("params", new JSONObject()
                        .put("keywords", validationQuery)
                    )
                    .put("index", validationIndex)
            );
        }

        return featureSet;
    }

    /**
     * Creates importance feature entries for the feature set specification.
     *
     * @param feature
     * @return
     */
    public static JSONObject getImportanceFeatureSpecification(AbstractFeature feature) {
        JSONObject featureObject = new JSONObject();
        featureObject.put("name", feature.getFeatureName());
        featureObject.put("params", new JSONArray());
        featureObject.put("template_language", "mustache");
        featureObject.put("template",
                new JSONObject()
                        .put("function_score", new JSONObject()
                                .put("functions", new JSONArray()
                                        .put(new JSONObject()
                                                .put("field_value_factor", new JSONObject()
                                                    .put("field", feature.getFeatureName())
                                                    .put("missing", 0)
                                                )
                                        )
                                )
                                .put("query", new JSONObject()
                                        .put("match_all", new JSONObject())
                                )
                        )
        );
        return featureObject;
    }

    /**
     * Creates relevance feature entries for the feature set specification. These have to be defined in a file! (not generic)
     * @param feature
     * @return
     */
    public static JSONObject getRelevanceFeatureSpecification(AbstractFeature feature) {
        JSONObject featureObject = null;
        try {
            featureObject = new JSONObject(readFile(relevanceFeatureFolder + feature.getFeatureName() + ".json", Charset.forName("UTF-8")));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return featureObject;
    }

    /**
     * Creates the body for an elasticsearch update request (value) of terms (key) from a FeatureSetScores object.
     *
     * @param featureSetScores
     * @return
     */
    public static Map<String, JSONObject> createDocUpdatesFromImportanceScores(FeatureSetScores<TermQuery,Term> featureSetScores) {
        Map<String, JSONObject> docUpdates = new HashMap<>();

        Map<Pair<TermQuery, Term>, Map<AbstractFeature, Double>> allScores = featureSetScores.getFeatureScores();
        for (Map.Entry<Pair<TermQuery, Term>, Map<AbstractFeature, Double>> termScores : allScores.entrySet()) {
            Term term = termScores.getKey().getRight();
            docUpdates.put(term.getTermUri(), createDocUpdateObject(termScores.getValue()));
        }

        return docUpdates;
    }

    public static JSONObject createDocUpdateObject(Map<AbstractFeature, Double> termScores) {
        JSONObject updateObject = new JSONObject();
        for (Map.Entry<AbstractFeature, Double> termFeatureScores : termScores.entrySet()) {
            updateObject.put(termFeatureScores.getKey().getFeatureName(), termFeatureScores.getValue());
        }
        return updateObject;
    }


    public static JSONObject createFeatureLoggingSLTRQuery(AbstractQuery query, Set<Term> terms, String featureSetName) {
        JSONObject sltrQuery = new JSONObject();
        sltrQuery.put("query",
            new JSONObject()
                .put("bool",
                    new JSONObject()
                        .put("filter",
                            new JSONArray()
                                .put(new JSONObject()
                                    .put("terms",
                                        new JSONObject()
                                            .put("_id", terms.stream().map(t -> t.getTermUri().toString()).collect(Collectors.toList()))
                                    )
                                )
                                .put(new JSONObject()
                                    .put("sltr",
                                        new JSONObject()
                                            .put("_name", "Logged_"+featureSetName)
                                            .put("featureset", featureSetName)
                                            .put("params",
                                                new JSONObject()
                                                    .put("keywords", String.join(" ", query.getSearchWords()))
                                            )
                                    )
                                )
                        )
                )
        );
        sltrQuery.put("ext",
            new JSONObject()
                .put("ltr_log",
                    new JSONObject()
                        .put("log_specs",
                            new JSONObject()
                                .put("name", "LogEntry_"+featureSetName)
                                .put("named_query", "Logged_"+featureSetName)
                                .put("missing_as_zero", true)
                        )
                )
        );
        return sltrQuery;
    }

    public static JSONObject createAddModelRequest(String modelName, String modelType, String modelDefinition) {
        JSONObject addModelRequest = new JSONObject();

        addModelRequest.put("model", new JSONObject()
            .put("name",modelName)
            .put("model", new JSONObject()
                .put("type", "model/"+modelType)
                .put("definition", modelDefinition)
            )
        );

        return addModelRequest;
    }


    static String readFile(String path, Charset encoding) throws IOException {
        log.info(Paths.get(path).toAbsolutePath().toString());
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }
}

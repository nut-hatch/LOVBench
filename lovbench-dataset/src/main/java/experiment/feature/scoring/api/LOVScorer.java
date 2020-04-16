package experiment.feature.scoring.api;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import experiment.feature.scoring.AbstractScorer;
import experiment.model.Ontology;
import experiment.model.Term;
import experiment.model.query.AbstractQuery;
import experiment.model.query.TermQuery;
import experiment.configuration.ExperimentConfiguration;
import experiment.repository.file.FileUtil;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Scorer that collects the LOV scores from the respective APIs.
 */
public class LOVScorer extends AbstractScorer {

    /**
     * Caches the scores of the lov term match.
     */
    Map<TermQuery,Map<Term,Double>> lovTermMatchCache;

    /**
     * Caches the scores of lov term popularity.
     */
    Map<TermQuery,Map<Term,Double>> lovTermPopularityCache;

    /**
     * Caches the scores of lov ontology match.
     */
    Map<AbstractQuery,Map<Ontology,Double>> lovOntologyMatchCache = new HashMap<>();

    private static final Logger log = LoggerFactory.getLogger( LOVScorer.class );

    /**
     * If scores have been previously queried from the API, they will be loaded from file.
     */
    public LOVScorer() {
        this.lovTermMatchCache = this.readTermScores(ExperimentConfiguration.getInstance().getLovAPITermMatchScoresFile());
        this.lovTermPopularityCache = this.readTermScores(ExperimentConfiguration.getInstance().getLovAPITermPopularityScoresFile());
        this.lovOntologyMatchCache = this.readOntologyScores(ExperimentConfiguration.getInstance().getLovAPIOntologyMatchScoresFile());
    }

    /**
     * Returns the term match score for a query and a term. If not in cache, they will be queried from the API.
     *
     * @param query
     * @param term
     * @return
     */
    public double getTermMatchScore(TermQuery query, Term term) {
        double score = 0.0;
        if (!this.lovTermMatchCache.containsKey(query)) {
            this.getScoresFromTermApi(query);
        }
        if (this.lovTermMatchCache.get(query).containsKey(term)) {
            score = this.lovTermMatchCache.get(query).get(term);
        }
        return score;
    }

    /**
     * Returns the term popularity score for a query and a term. If not in cache, they will be queried from the API.
     *
     * @param query
     * @param term
     * @return
     */
    public double getTermPopularityScore(TermQuery query, Term term) {
        double score = 0.0;
        if (!this.lovTermPopularityCache.containsKey(query)) {
            this.getScoresFromTermApi(query);
        }
        if (this.lovTermPopularityCache.get(query).containsKey(term)) {
            score = this.lovTermPopularityCache.get(query).get(term);
        }
        return score;
    }

    /**
     * Returns the ontology match score for a query and a term. If not in cache, they will be queried from the API.
     *
     * @param query
     * @param ontology
     * @return
     */
    public double getOntologyMatchScore(AbstractQuery query, Ontology ontology) {
        double score = 0.0;
        if (!this.lovOntologyMatchCache.containsKey(query)) {
            this.getScoresFromVocabularyApi(query);
        }
        if (this.lovOntologyMatchCache.containsKey(query) && this.lovOntologyMatchCache.get(query).containsKey(ontology)) {
            score = this.lovOntologyMatchCache.get(query).get(ontology);
        }
        return score;
    }

    /**
     * Gets the term scores from the LOV api for a given query. Scores will be added to cache and appended to file. Raw json response is also stored.
     *
     * @param query
     */
    private void getScoresFromTermApi(TermQuery query) {
        String filename = ExperimentConfiguration.getInstance().getLovAPIJSONResponsePath()+"term/"+query.toString().replace("/", "_")+".json";
        File file = new File(filename);
        if (!file.exists()) {
            try {
                // Create url string based on lov api base path from config + query
                String url = ExperimentConfiguration.getInstance().getLovAPITerms() + query.getLovAPIQueryString() + "&page_size=500";
                log.debug(url);
                try {
                    TimeUnit.MILLISECONDS.sleep(12000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                JSONObject json = new JSONObject(IOUtils.toString(new URL(url), Charset.forName("UTF-8")));
                this.saveJsonResponse(json, filename);

                this.lovTermMatchCache.put(query, new HashMap<>());
                this.lovTermPopularityCache.put(query, new HashMap<>());

                // Iterate through results and add to cache
                if (json != null && json.has("results")) {
                    JSONArray results = json.getJSONArray("results");
                    int i = 0;
                    while (i < results.length()) {
                        JSONObject result = results.getJSONObject(i);
                        String uri = result.getString("uri");
                        uri = uri.substring(2, uri.length() - 2);
                        double scoreFeatureHit = result.getDouble("scoreFeatureHit");
                        scoreFeatureHit = (double) Math.round(scoreFeatureHit * 1000000d) / 1000000d;
                        double scoreFeaturePop = result.getDouble("scoreFeaturePop");
                        scoreFeaturePop = (double) Math.round(scoreFeaturePop * 1000000d) / 1000000d;
                        this.lovTermMatchCache.get(query).put(new Term(uri), scoreFeatureHit);
                        this.lovTermPopularityCache.get(query).put(new Term(uri), scoreFeaturePop);
                        i++;
                    }
                    this.saveTermScores(ExperimentConfiguration.getInstance().getLovAPITermMatchScoresFile(), query, this.lovTermMatchCache.get(query));
                    this.saveTermScores(ExperimentConfiguration.getInstance().getLovAPITermPopularityScoresFile(), query, this.lovTermPopularityCache.get(query));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Gets the ontology scores from the LOV api for a given query. Scores will be added to cache and appended to file. Raw json response is also stored.
     *
     * @param query
     */
    private void getScoresFromVocabularyApi(AbstractQuery query) {
        String filename = ExperimentConfiguration.getInstance().getLovAPIJSONResponsePath()+"ontology/"+query.toString().replace("/", "_")+".json";
        File file = new File(filename);
        if (!file.exists()) {
            try {
                //https://lov.linkeddata.es/dataset/lov/api/v2/vocabulary/search?q=time&lang=English
                String url = ExperimentConfiguration.getInstance().getLovAPIVocabs() + query.getLovAPIQueryString() + "&page_size=500";
                log.debug(url);
                try {
                    TimeUnit.MILLISECONDS.sleep(6000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                JSONObject json = new JSONObject(IOUtils.toString(new URL(url), Charset.forName("UTF-8")));

                this.saveJsonResponse(json, filename);
                this.lovOntologyMatchCache.put(query, new HashMap<>());

                // Iterate through results and add to cache
                if (json != null && json.has("results")) {
                    JSONArray results = json.getJSONArray("results");
                    int i = 0;
                    while (i < results.length()) {
                        JSONObject result = results.getJSONObject(i);
                        String uri = result.getString("_id").replace("\"", "");
                        //                    uri = uri.substring(2,uri.length()-2);
                        double scoreFeatureHit = result.getDouble("_score");
                        scoreFeatureHit = (double) Math.round(scoreFeatureHit * 1000000d) / 1000000d;
                        this.lovOntologyMatchCache.get(query).put(new Ontology(uri), scoreFeatureHit);
                        i++;
                    }
                    this.saveOntologyScores(ExperimentConfiguration.getInstance().getLovAPIOntologyMatchScoresFile(), query, this.lovOntologyMatchCache.get(query));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Saves given term scores to file.
     *
     * @param filename
     * @param termQuery
     * @param scores
     */
    public void saveTermScores(String filename, TermQuery termQuery, Map<Term,Double> scores) {
        File file = new File(filename);
        FileUtil.createFolderIfNotExists(file);
        try (
                Writer writer = Files.newBufferedWriter(Paths.get(filename), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                CSVWriter csvWriter = new CSVWriter(writer,
                        CSVWriter.DEFAULT_SEPARATOR,
                        CSVWriter.DEFAULT_QUOTE_CHARACTER,
                        CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                        CSVWriter.DEFAULT_LINE_END)
        ) {
            for (Map.Entry<Term,Double> score : scores.entrySet()) {
                csvWriter.writeNext(new String[]{termQuery.toString(), score.getKey().toString(), score.getValue().toString()});
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Reads stored term scores from file.
     *
     * @param filename
     * @return
     */
    public Map<TermQuery,Map<Term,Double>> readTermScores(String filename) {
        Map<TermQuery,Map<Term,Double>> scores = new HashMap<>();
        if (new File(filename).exists()) {
            try (BufferedReader br = Files.newBufferedReader(Paths.get(filename), Charset.defaultCharset())) {
                CSVReader csvReader = new CSVReader(br);
                String[] arrLine;
                while ((arrLine = csvReader.readNext()) != null) {
                    TermQuery termQuery = new TermQuery(arrLine[0]);
                    Term term = new Term(arrLine[1]);
//                    log.info(String.format("LOV score read from file: %s - %s", termQuery.toString(), term.toString()));
                    double score = Double.parseDouble(arrLine[2]);
//                    log.info(String.format("LOV score read from file: %s - %s - %s", termQuery.toString(), term.toString(), score));
                    if (!scores.containsKey(termQuery)) {
                        scores.put(termQuery, new HashMap<>());
                    }
                    scores.get(termQuery).put(term, score);
                }
                br.close();
                csvReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return scores;
    }

    /**
     * Stores ontology scores to file.
     *
     * @param filename
     * @param ontologyQuery
     * @param scores
     */
    public void saveOntologyScores(String filename, AbstractQuery ontologyQuery, Map<Ontology,Double> scores) {
        File file = new File(filename);
        FileUtil.createFolderIfNotExists(file);
        try (
                Writer writer = Files.newBufferedWriter(Paths.get(filename), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                CSVWriter csvWriter = new CSVWriter(writer,
                        CSVWriter.DEFAULT_SEPARATOR,
                        CSVWriter.DEFAULT_QUOTE_CHARACTER,
                        CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                        CSVWriter.DEFAULT_LINE_END)
        ) {
            for (Map.Entry<Ontology,Double> score : scores.entrySet()) {
                csvWriter.writeNext(new String[]{ontologyQuery.toString(), score.getKey().toString(), score.getValue().toString()});
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Reads stored ontology scores from file.
     *
     * @param filename
     * @return
     */
    public Map<AbstractQuery,Map<Ontology,Double>> readOntologyScores(String filename) {
        Map<AbstractQuery,Map<Ontology,Double>> scores = new HashMap<>();
        if (new File(filename).exists()) {
            try (BufferedReader br = Files.newBufferedReader(Paths.get(filename), Charset.defaultCharset())) {
                CSVReader csvReader = new CSVReader(br);
                String[] arrLine;
                while ((arrLine = csvReader.readNext()) != null) {
                    AbstractQuery ontologyQuery = new TermQuery(arrLine[0]);
                    Ontology ontology = new Ontology(arrLine[1]);
                    double score = Double.parseDouble(arrLine[2]);
                    log.debug(String.format("LOV score read from file: %s - %s - %s", ontologyQuery.toString(), ontology.toString(), score));
                    if (!scores.containsKey(ontologyQuery)) {
                        scores.put(ontologyQuery, new HashMap<>());
                    }
                    scores.get(ontologyQuery).put(ontology, score);
                }
                br.close();
                csvReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return scores;
    }

    private void saveJsonResponse(JSONObject json, String filename) {
        File file = new File(filename);

        FileUtil.createFolderIfNotExists(file);
        try {
            Writer writer = new BufferedWriter(new FileWriter(filename));
            writer.write(json.toString());
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}

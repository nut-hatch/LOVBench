package export.elasticsearch.cli;

import arq.cmdline.CmdGeneral;
import experiment.feature.FeatureFactory;
import experiment.model.Relevance;
import experiment.model.Term;
import experiment.model.query.TermQuery;
import experiment.model.query.enums.ExtractionType;
import experiment.repository.file.FeatureSetScores;
import experiment.repository.file.GroundTruthTermRanking;
import export.elasticsearch.cli.config.ElasticsearchConfiguration;
import export.elasticsearch.feature.FeatureRequestHelper;
import export.elasticsearch.index.ElasticSearchIndex;
import org.apache.commons.io.FilenameUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Map;
import java.util.Set;

/**
 * This class reads feature scores stored in elasticsearch corresponding to a feature set configuration (bulk export).
 * This script is only relevant for local development in order to train a model which can then be deployed in ElasticSearch.
 *
 * The extraction is based on a ground truth file, and the features scores for the given query-term pairs are written to a file.
 *
 * The file is created in a way to work with the feature set configuration as given in elasticsearch,
 * meaning the features have the same order as in the feature set. This is absolutely necessary for working
 * with RankLib, as features have no names, just numbers! (0, 1, 2, 3).
 *
 * This script is only useful after the LTR plugin has been initialized (see {@link SetupLTRElasticSearch}) and the feature
 * importance scores have been written to the index (see {@link ExtractFeaturesElasticsearch})
 */
public class LogFeaturesElasticSearch extends CmdGeneral {
    private ElasticsearchConfiguration configuration;

    private static final Logger log = LoggerFactory.getLogger( LogFeaturesElasticSearch.class );

    public static void main(String... args) {
        new LogFeaturesElasticSearch(args).mainRun();
    }

    public LogFeaturesElasticSearch(String[] argv) {
        super(argv);
        getUsage().startCategory("Arguments");
        getUsage().addUsage("configFilePath", "Path to the config.properties file");

    }

    @Override
    protected String getSummary() {
        return getCommandName() + " configFilePath";
    }

    @Override
    protected void processModulesAndArgs() {
        if (getPositional().size() < 1) {
            doHelp();
        }
        this.configuration = new ElasticsearchConfiguration(getPositionalArg(0));
    }

    @Override
    protected String getCommandName() {
        return "log-features";
    }

    @Override
    protected void exec() {
        // Read GT file
        GroundTruthTermRanking groundTruthTermRanking = GroundTruthTermRanking.parse(this.configuration.getGroundTruthFilePath());

        // Elasticsearch index
        ElasticSearchIndex termIndex = new ElasticSearchIndex(this.configuration);

        FeatureSetScores<TermQuery,Term> scores = new FeatureSetScores<>(ExtractionType.TERM);

        FeatureFactory featureFactory = new FeatureFactory();

        for(Map.Entry<TermQuery, Map<Term, Relevance>> groundTruthRowMapEntry : groundTruthTermRanking.getGroundTruthTable().rowMap().entrySet()) {
            // build query
            TermQuery query = groundTruthRowMapEntry.getKey();
            Set<Term> terms = groundTruthRowMapEntry.getValue().keySet();
            JSONObject sltrQuery = FeatureRequestHelper.createFeatureLoggingSLTRQuery(query, terms, FilenameUtils.getBaseName(this.configuration.getFeatureSetDefinitionFilePath()));

            // make query
            System.out.println(sltrQuery.toString());
            JSONArray hits = termIndex.search(sltrQuery);
            System.out.println(hits.toString());

            // parse response
            for (int i = 0; i < hits.length(); i++) {
                JSONObject termHit = hits.getJSONObject(i);
                Term term = new Term(termHit.getString("_id"));

                JSONArray termScores = termHit.getJSONObject("fields").getJSONArray("_ltrlog").getJSONObject(0).getJSONArray("LogEntry_"+FilenameUtils.getBaseName(this.configuration.getFeatureSetDefinitionFilePath()));
                for (int j = 0; j < termScores.length(); j++) {
                    JSONObject termHitScore = termScores.getJSONObject(j);
                    scores.addScore(Pair.of(query,term), featureFactory.getFeature(termHitScore.getString("name")), termHitScore.getDouble("value"));
                }
            }
            break;
        }
        scores.writeCsv();
        // ltrFile.writeCsv();
    }

}

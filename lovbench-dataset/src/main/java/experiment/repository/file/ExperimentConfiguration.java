package experiment.repository.file;

import com.opencsv.CSVWriter;
import experiment.model.query.enums.QueryMatch;
import experiment.repository.triplestore.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.Timestamp;

/**
 * This singleton maintains the program-wide configurations for the experiment runs.
 *
 */
public class ExperimentConfiguration {

    /**
     * Singleton object.
     */
    private static ExperimentConfiguration experimentConfiguration = null;

    /**
     * The input folder for all files that need to be parsed for the experiment run.
     */
    String inputDir = "../resources/";

    /**
     * The output folder to which all results will be stored.
     */
    String resultDir = "src/main/resources/results/" + Long.toString(new Timestamp(System.currentTimeMillis()).getTime()) + "/";

    /**
     * The output folder to which all results will be stored.
     */
    String cacheDir = "src/main/resources/cache/";

    /**
     * File that contains the LOV prefixes to resolve query<->ontology matches.
     */
    String lovPrefixesFile = inputDir + "LOV_data/2019-08-06_lov_prefixes_edit.json";

    /**
     * File of the term ground truth.
     */
    String groundTruthTermsFilePath = inputDir + "LOVBench_GroundTruth.csv";

    /**
     * File of the ontology ground truth.
     */
    String groundTruthOntologiesFilePath = inputDir + "";

    /**
     * File that contains pre-computed maximum frequencies (as it is very expensive to compute).
     */
    String maximumFrequencyFile = cacheDir + "maximum_frequencies.csv";

    String tfFile = cacheDir + "tf.csv";

    String idfFile = cacheDir + "idf.csv";

    String vsmFile = cacheDir + "vsm.csv";

    /**
     * Output file for the term features.
     */
    String allScoresTermFile = resultDir + "TermRankingBenchmark.csv";

    /**
     * Output file for the ontology features.
     */
    String allScoresOntologyFile = resultDir + "OntologyRankingBenchmark.csv";

    /**
     * File to which this configuration will be written before the run.
     */
    String experimentConfigurationFile = resultDir + "ExperimentConfiguration.csv";

    /**
     * File name to store the lov term match score.
     */
    String lovAPITermMatchScoresFile = cacheDir + "lovScoresTermsMatch.csv";

    /**
     * File name to store the lov term popularity score.
     */
    String lovAPITermPopularityScoresFile = cacheDir + "lovScoresTermsPopularity.csv";

    /**
     * File name to store the lov ontology match score.
     */
    String lovAPIOntologyMatchScoresFile = cacheDir + "lovScoresVocabsMatch.csv";

    /**
     * File name to store the lov ontology match score.
     */
    String lovAPIJSONResponsePath = cacheDir + "lovApi/";

    /**
     * LOV API Url for terms with ending ?.
     */
    String lovAPITerms = "https://lov.linkeddata.es/dataset/lov/api/v2/term/searchScoreExplain?";

    /**
     * LOV API Url for vocabs with ending slash.
     */
    String lovAPIVocabs = "https://lov.linkeddata.es/dataset/lov/api/v2/vocabulary/search?";

    /**
     * Database name of the ontology collection.
     */
    String dbnameOntologies = "LOVnq_20190806";

    /**
     * Database name containing metadata of the ontology collection.
     */
    String dbnameMetadata = "LOVn3_20190806";

    /**
     * Database name containing metadata of the ontology collection.
     */
    String dbnameSearch = "LOVsearch_20190806";

    /**
     * Database server url.
     */
    String dbServer = "http://localhost:5820";

    /**
     * Database user name.
     */
    String dbUser = "admin";

    /**
     * Database password.
     */
    String dbPassword = "admin";

    /**
     * Configuration for the query match for query-match-dependent scoring algorithms.
     */
    QueryMatch queryMatch = QueryMatch.LOV;

    /**
     * The repository to access the ontology collection.
     */
    AbstractOntologyRepository repository;

    /**
     * The repository to access the ontology collection.
     */
    AbstractOntologyMetadataRepository repositoryMetadata;


    private static final Logger log = LoggerFactory.getLogger( ExperimentConfiguration.class );

    private ExperimentConfiguration() {
        // @TODO read config from file and/or parse cmd line configuration
        this.repository = LOVRepository.getInstance(this.dbnameOntologies);
        this.repositoryMetadata = LOVMetadataRepository.getInstance(this.dbnameMetadata);
    }

    /**
     * Get the configuration singleton object.
     *
     * @return ExperimentConfiguration
     */
    public static ExperimentConfiguration getInstance() {
        if (experimentConfiguration == null) {
            experimentConfiguration = new ExperimentConfiguration();
        }
        return experimentConfiguration;
    }

    /**
     * Writes configuration to csv file.
     */
    public void writeCsv() {
        String filename = this.getExperimentConfigurationFile();
        File runDetailsFile = new File(filename);
        FileUtil.createFolderIfNotExists(runDetailsFile);
        log.info(String.format("Experiment configuration written to file: %s", filename));
        try (
                Writer writer = Files.newBufferedWriter(Paths.get(filename), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                CSVWriter csvWriter = FileUtil.getCSVWriter(writer)
        ) {
            csvWriter.writeNext(new String[]{"GroundTruthFileTerms", "GroundTruthFileOntologies", "LOVPrefixFile", "DBNameVocabularies", "DBNameMetadata", "CacheOutput", "ResultOutput"});
            csvWriter.writeNext(new String[]{this.getGroundTruthTermsFilePath(), this.getGroundTruthOntologiesFilePath(), this.getLovPrefixesFile(), this.getDbnameOntologies(), this.getDbnameMetadata(), this.getCacheDir(), this.getResultDir()});
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static ExperimentConfiguration getExperimentConfiguration() {
        return experimentConfiguration;
    }

    public static void setExperimentConfiguration(ExperimentConfiguration experimentConfiguration) {
        ExperimentConfiguration.experimentConfiguration = experimentConfiguration;
    }

    public String getInputDir() {
        return inputDir;
    }

    public void setInputDir(String inputDir) {
        this.inputDir = inputDir;
    }

    public String getResultDir() {
        return resultDir;
    }

    public void setResultDir(String resultDir) {
        this.resultDir = resultDir;
    }

    public String getCacheDir() {
        return cacheDir;
    }

    public void setCacheDir(String cacheDir) {
        this.cacheDir = cacheDir;
    }

    public String getLovPrefixesFile() {
        return lovPrefixesFile;
    }

    public void setLovPrefixesFile(String lovPrefixesFile) {
        this.lovPrefixesFile = lovPrefixesFile;
    }

    public String getGroundTruthTermsFilePath() {
        return groundTruthTermsFilePath;
    }

    public void setGroundTruthTermsFilePath(String groundTruthTermsFilePath) {
        this.groundTruthTermsFilePath = groundTruthTermsFilePath;
    }

    public String getGroundTruthOntologiesFilePath() {
        return groundTruthOntologiesFilePath;
    }

    public void setGroundTruthOntologiesFilePath(String groundTruthOntologiesFilePath) {
        this.groundTruthOntologiesFilePath = groundTruthOntologiesFilePath;
    }

    public String getDbnameOntologies() {
        return dbnameOntologies;
    }

    public void setDbnameOntologies(String dbnameOntologies) {
        this.dbnameOntologies = dbnameOntologies;
    }

    public String getDbnameMetadata() {
        return dbnameMetadata;
    }

    public void setDbnameMetadata(String dbnameMetadata) {
        this.dbnameMetadata = dbnameMetadata;
    }

    public String getDbnameSearch() {
        return dbnameSearch;
    }

    public void setDbnameSearch(String dbnameSearch) {
        this.dbnameSearch = dbnameSearch;
    }

    public AbstractOntologyRepository getRepository() {
        return repository;
    }

    public void setRepository(AbstractOntologyRepository repository) {
        this.repository = repository;
    }

    public AbstractOntologyMetadataRepository getRepositoryMetadata() {
        return repositoryMetadata;
    }

    public void setRepositoryMetadata(AbstractOntologyMetadataRepository repositoryMetadata) {
        this.repositoryMetadata = repositoryMetadata;
    }

    public String getMaximumFrequencyFile() {
        return maximumFrequencyFile;
    }

    public void setMaximumFrequencyFile(String maximumFrequencyFile) {
        this.maximumFrequencyFile = maximumFrequencyFile;
    }

    public String getTfFile() {
        return tfFile;
    }

    public void setTfFile(String tfFile) {
        this.tfFile = tfFile;
    }

    public String getIdfFile() {
        return idfFile;
    }

    public void setIdfFile(String idfFile) {
        this.idfFile = idfFile;
    }

    public String getVsmFile() {
        return vsmFile;
    }

    public void setVsmFile(String vsmFile) {
        this.vsmFile = vsmFile;
    }

    public String getExperimentConfigurationFile() {
        return experimentConfigurationFile;
    }

    public void setExperimentConfigurationFile(String experimentConfigurationFile) {
        this.experimentConfigurationFile = experimentConfigurationFile;
    }

    public String getDbServer() {
        return dbServer;
    }

    public void setDbServer(String dbServer) {
        this.dbServer = dbServer;
    }

    public String getDbUser() {
        return dbUser;
    }

    public void setDbUser(String dbUser) {
        this.dbUser = dbUser;
    }

    public String getDbPassword() {
        return dbPassword;
    }

    public void setDbPassword(String dbPassword) {
        this.dbPassword = dbPassword;
    }

    public QueryMatch getQueryMatch() {
        return queryMatch;
    }

    public void setQueryMatch(QueryMatch queryMatch) {
        this.queryMatch = queryMatch;
    }

    public String getLovAPITerms() {
        return lovAPITerms;
    }

    public void setLovAPITerms(String lovAPITerms) {
        this.lovAPITerms = lovAPITerms;
    }

    public String getLovAPIVocabs() {
        return lovAPIVocabs;
    }

    public void setLovAPIVocabs(String lovAPIVocabs) {
        this.lovAPIVocabs = lovAPIVocabs;
    }

    public String getLovAPITermMatchScoresFile() {
        return lovAPITermMatchScoresFile;
    }

    public void setLovAPITermMatchScoresFile(String lovAPITermMatchScoresFile) {
        this.lovAPITermMatchScoresFile = lovAPITermMatchScoresFile;
    }

    public String getLovAPITermPopularityScoresFile() {
        return lovAPITermPopularityScoresFile;
    }

    public void setLovAPITermPopularityScoresFile(String lovAPITermPopularityScoresFile) {
        this.lovAPITermPopularityScoresFile = lovAPITermPopularityScoresFile;
    }

    public String getLovAPIOntologyMatchScoresFile() {
        return lovAPIOntologyMatchScoresFile;
    }

    public void setLovAPIOntologyMatchScoresFile(String lovAPIOntologyMatchScoresFile) {
        this.lovAPIOntologyMatchScoresFile = lovAPIOntologyMatchScoresFile;
    }

    public String getLovAPIJSONResponsePath() {
        return lovAPIJSONResponsePath;
    }

    public void setLovAPIJSONResponsePath(String lovAPIJSONResponsePath) {
        this.lovAPIJSONResponsePath = lovAPIJSONResponsePath;
    }

    public String getAllScoresTermFile() {
        return allScoresTermFile;
    }

    public void setAllScoresTermFile(String allScoresTermFile) {
        this.allScoresTermFile = allScoresTermFile;
    }

    public String getAllScoresOntologyFile() {
        return allScoresOntologyFile;
    }

    public void setAllScoresOntologyFile(String allScoresOntologyFile) {
        this.allScoresOntologyFile = allScoresOntologyFile;
    }
}

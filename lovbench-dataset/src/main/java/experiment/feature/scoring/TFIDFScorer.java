package experiment.feature.scoring;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import experiment.model.Ontology;
import experiment.model.Term;
import experiment.repository.file.ExperimentConfiguration;
import experiment.repository.file.FileUtil;
import experiment.repository.triplestore.AbstractOntologyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This scorer computes tf, idf, and tf-idf scores.
 * Since these scores are used for various features, it caches various values in order to reduce database queries.
 *
 */
public class TFIDFScorer extends AbstractScorer {

    /**
     * The repository for the ontology collection.
     */
    AbstractOntologyRepository repository;

    /**
     * Caches maximum frequencies of term occurences per ontology as they are computationally very expensive.
     */
    Map<Ontology, Integer> maximumFrequencyCache = new HashMap<>();

    Table<Term, Ontology, Double> tfCache = HashBasedTable.create();

    Map<Term, Double> idfCache =new HashMap<>();

    Map<Ontology, Double> ontologyNormCache =new HashMap<>();

    private static final Logger log = LoggerFactory.getLogger( TFIDFScorer.class );

    public TFIDFScorer(AbstractOntologyRepository repository) {
        super();
        this.repository = repository;
        this.readMaximumFrequenciesFromCsv();
        this.readTfFromCsv();
        this.readIdfFromCsv();
    }

    /**
     * Computes the Term Frequency for term in ontology
     *
     * @param term
     * @param ontology
     * @return double
     */
    public double tf(Term term, Ontology ontology) {
        if (!this.tfCache.contains(term,ontology)) {
            double tf = 0.5 + ( (0.5 * this.repository.termFrequency(term, ontology)) / this.getMaximumFrequency(ontology) );
            this.tfCache.put(term,ontology,tf);
            this.writeTfCsv(term,ontology,tf);
        }
        return this.tfCache.get(term,ontology);
    }

    /**
     * Computes the Inverse Document Frequency score of a term.
     *
     * @param term
     * @return double
     */
    public double idf(Term term) {
        if (!this.idfCache.containsKey(term)) {
//            log.info(term.getTermUri());
            int ontologiesContainingTerms = this.repository.countOntologiesContainingTerm(term);
            double idf = 0.0;
            if (ontologiesContainingTerms == 0) {
                log.error("no ontology containing term: " + term.getTermUri());
            } else {
                idf = Math.log(this.repository.countOntologies() / ontologiesContainingTerms);
            }
            this.idfCache.put(term, idf);
            this.writeIdfCsv(term, idf);
        }
        return this.idfCache.get(term);
    }

    /**
     * Reads the maximum term frequency of an ontology from cache, or if it does not exists computes it and stores it to cache and a file.
     *
     * @param ontology
     * @return int
     */
    private int getMaximumFrequency(Ontology ontology) {
        if (!this.maximumFrequencyCache.containsKey(ontology)) {
            int maxFrequency = this.repository.maximumFrequency(ontology);
            this.maximumFrequencyCache.put(ontology, maxFrequency);
            this.writeMaximumFrequencyCsv(ontology, maxFrequency);
        }
        return this.maximumFrequencyCache.get(ontology);
    }

    public double getOntologyNorm(Ontology ontology) {
        double ontologyNorm = 0.0;
        if (!this.ontologyNormCache.containsKey(ontology)) {
            Set<Term> termsInOntology = this.repository.getAllTerms(ontology);
            double tfidfSquaredSum = 0.0;
            for (Term term : termsInOntology) {
                tfidfSquaredSum += Math.pow(this.tf(term,ontology) * this.idf(term),2);
            }
            ontologyNorm = Math.sqrt(tfidfSquaredSum);
            this.ontologyNormCache.put(ontology, ontologyNorm);
        }

        return this.ontologyNormCache.get(ontology);
    }

    /**
     * Appends a computed maximum frequency for an ontology to a file.
     *
     * @param ontology
     * @param maximumFrequency
     */
    public void writeMaximumFrequencyCsv(Ontology ontology, int maximumFrequency) {
        String filename = ExperimentConfiguration.getInstance().getMaximumFrequencyFile();
        File file = new File(filename);
        FileUtil.createFolderIfNotExists(file);
        try (
                Writer writer = Files.newBufferedWriter(Paths.get(filename), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                CSVWriter csvWriter = FileUtil.getCSVWriter(writer);
        ) {
                csvWriter.writeNext(new String[]{ontology.getOntologyUri(),maximumFrequency+""});
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeTfCsv(Term term, Ontology ontology, double tf) {
        String filename = ExperimentConfiguration.getInstance().getTfFile();
        File file = new File(filename);
        FileUtil.createFolderIfNotExists(file);
        try (
                Writer writer = Files.newBufferedWriter(Paths.get(filename), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                CSVWriter csvWriter = FileUtil.getCSVWriter(writer);
        ) {
            csvWriter.writeNext(new String[]{term.getTermUri(),ontology.getOntologyUri(),tf+""});
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeIdfCsv(Term term, double idf) {
        String filename = ExperimentConfiguration.getInstance().getIdfFile();
        File file = new File(filename);
        FileUtil.createFolderIfNotExists(file);
        try (
                Writer writer = Files.newBufferedWriter(Paths.get(filename), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                CSVWriter csvWriter = FileUtil.getCSVWriter(writer);
        ) {
            csvWriter.writeNext(new String[]{term.getTermUri(),idf+""});
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Reads pre-computed maximum frequencies from file.
     */
    public void readMaximumFrequenciesFromCsv() {
        try (BufferedReader br = new BufferedReader(new FileReader(ExperimentConfiguration.getInstance().getMaximumFrequencyFile()))) {
            CSVReader csvReader = new CSVReader(br);
            String[] arrLine;
            while ((arrLine = csvReader.readNext()) != null) {
                Ontology ontology = new Ontology(arrLine[0]);
                int maxFrequency = Integer.parseInt(arrLine[1]);
                log.info("Maximum frequency read from cache: " + ontology + ": " + maxFrequency);
                this.maximumFrequencyCache.put(ontology,maxFrequency);
            }
            br.close();
            csvReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void readTfFromCsv() {
        try (BufferedReader br = new BufferedReader(new FileReader(ExperimentConfiguration.getInstance().getTfFile()))) {
            CSVReader csvReader = new CSVReader(br);
            String[] arrLine;
            while ((arrLine = csvReader.readNext()) != null) {
                Term term = new Term(arrLine[0]);
                Ontology ontology = new Ontology(arrLine[1]);
                double tf = Double.parseDouble(arrLine[2]);
                this.tfCache.put(term,ontology,tf);
            }
            br.close();
            csvReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void readIdfFromCsv() {
        try (BufferedReader br = new BufferedReader(new FileReader(ExperimentConfiguration.getInstance().getIdfFile()))) {
            CSVReader csvReader = new CSVReader(br);
            String[] arrLine;
            while ((arrLine = csvReader.readNext()) != null) {
                Term term = new Term(arrLine[0]);
                double idf = Double.parseDouble(arrLine[1]);
                this.idfCache.put(term,idf);
            }
            br.close();
            csvReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

package experiment.feature.scoring;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import experiment.model.Ontology;
import experiment.model.Term;
import experiment.configuration.ExperimentConfiguration;
import experiment.repository.file.FileUtil;
import experiment.repository.triplestore.AbstractOntologyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * This scorer computes tf, idf, and tf-idf scores.
 * Since these scores are used for various features, it caches various values in order to reduce database queries.
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

    Map<Ontology, Map<Term, Integer>> frequencyCache = new HashMap<>();

    Table<Term, Ontology, Double> tfCache = HashBasedTable.create();

    Map<Term, Double> idfCache = new HashMap<>();

    Map<Ontology, Double> ontologyNormCache = new HashMap<>();

    boolean useFileCache;

    private static final Logger log = LoggerFactory.getLogger(TFIDFScorer.class);

    public TFIDFScorer(AbstractOntologyRepository repository) {
        this(repository, false);
    }


    public TFIDFScorer(AbstractOntologyRepository repository, boolean useFileCache) {
        super();
        this.repository = repository;
        this.useFileCache = useFileCache;
        if (useFileCache) {
            this.readMaximumFrequenciesFromCsv();
            this.readTfFromCsv();
            this.readIdfFromCsv();
        }
    }

    /**
     * Computes the Term Frequency for term in ontology
     *
     * @param term
     * @param ontology
     * @return double
     */
    public double tf(Term term, Ontology ontology) {
        if (!this.frequencyCache.containsKey(ontology)) {
            this.countAllFrequencies(ontology);
        }
        if (!this.tfCache.contains(term, ontology)) {
            double tf = 0.0;
            if (this.frequencyCache.get(ontology).containsKey(term)) {
                tf = 0.5 + ((0.5 * (double)this.frequencyCache.get(ontology).get(term)) / (double)this.getMaximumFrequency(ontology));
            } else {
                log.error("It seems that the term " + term.getTermUri() + " does not belong to ontology " + ontology.getOntologyUri() + ". TF will be set to zero but this should not happen.");
            }
            this.tfCache.put(term, ontology, tf);
            if (this.useFileCache) {
                this.writeTfCsv(term, ontology, tf);
            }
        }
        return this.tfCache.get(term, ontology);
    }

    /**
     * Computes the Inverse Document Frequency score of a term.
     *
     * @param term
     * @return double
     */
    public double idf(Term term) {
        if (!this.idfCache.containsKey(term)) {
            int ontologiesContainingTerms = this.repository.countOntologiesContainingTerm(term);
            double idf = 0.0;
            if (ontologiesContainingTerms == 0) {
                log.error("It was requested to compute the tf for a term that does not belong to any ontology in the repository: " + term.getTermUri());
            } else {
                idf = Math.log((double)this.repository.countOntologies() / (double)ontologiesContainingTerms);
            }
            this.idfCache.put(term, idf);
            if (this.useFileCache) {
                this.writeIdfCsv(term, idf);
            }
        }
        return this.idfCache.get(term);
    }

    private void countAllFrequencies(Ontology ontology) {
        Map<Term, Integer> frequencies = new HashMap<>();
        int maxFreq = 0;
        for (Term term : this.repository.getAllTerms(ontology)) {
            int freq = this.repository.termFrequency(term, ontology);
            if (freq > 0) {
                frequencies.put(term, freq);
                if (freq > maxFreq) {
                    maxFreq = freq;
                }
            }
        }
        this.maximumFrequencyCache.put(ontology, maxFreq);
        this.frequencyCache.put(ontology, frequencies);
    }

    /**
     * Reads the maximum term frequency of an ontology from cache, or if it does not exists computes it and stores it to cache and a file.
     *
     * @param ontology
     * @return int
     */
    private int getMaximumFrequency(Ontology ontology) {
        if (!this.maximumFrequencyCache.containsKey(ontology)) {
            // This approach does not scale well because the sparql query is too comples.
            // Instead, run countAllFrequencies() first and this case should never happen.
//            int maxFrequency = this.repository.maximumFrequency(ontology);
//            this.maximumFrequencyCache.put(ontology, maxFrequency);
//            this.writeMaximumFrequencyCsv(ontology, maxFrequency);
            this.countAllFrequencies(ontology);
        }
        return this.maximumFrequencyCache.get(ontology);
    }

    public double getOntologyNorm(Ontology ontology) {
        double ontologyNorm = 0.0;
        if (!this.ontologyNormCache.containsKey(ontology)) {
            Set<Term> termsInOntology = this.repository.getAllTerms(ontology);
            double tfidfSquaredSum = 0.0;
            for (Term term : termsInOntology) {
                tfidfSquaredSum += Math.pow(this.tf(term, ontology) * this.idf(term), 2);
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
            csvWriter.writeNext(new String[]{ontology.getOntologyUri(), maximumFrequency + ""});
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
            csvWriter.writeNext(new String[]{term.getTermUri(), ontology.getOntologyUri(), tf + ""});
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
            csvWriter.writeNext(new String[]{term.getTermUri(), idf + ""});
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Reads pre-computed maximum frequencies from file.
     */
    public void readMaximumFrequenciesFromCsv() {
        if (new File(ExperimentConfiguration.getInstance().getMaximumFrequencyFile()).isFile()) {
            try (BufferedReader br = Files.newBufferedReader(Paths.get(ExperimentConfiguration.getInstance().getMaximumFrequencyFile()), Charset.defaultCharset())) {
                CSVReader csvReader = new CSVReader(br);
                String[] arrLine;
                while ((arrLine = csvReader.readNext()) != null) {
                    Ontology ontology = new Ontology(arrLine[0]);
                    int maxFrequency = Integer.parseInt(arrLine[1]);
                    log.debug("Maximum frequency read from cache: " + ontology + ": " + maxFrequency);
                    this.maximumFrequencyCache.put(ontology, maxFrequency);
                }
                br.close();
                csvReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void readTfFromCsv() {
        if (new File(ExperimentConfiguration.getInstance().getTfFile()).isFile()) {
            try (BufferedReader br = Files.newBufferedReader(Paths.get(ExperimentConfiguration.getInstance().getTfFile()), Charset.defaultCharset())) {
                CSVReader csvReader = new CSVReader(br);
                String[] arrLine;
                while ((arrLine = csvReader.readNext()) != null) {
                    Term term = new Term(arrLine[0]);
                    Ontology ontology = new Ontology(arrLine[1]);
                    double tf = Double.parseDouble(arrLine[2]);
                    this.tfCache.put(term, ontology, tf);
                }
                br.close();
                csvReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void readIdfFromCsv() {
        if (new File(ExperimentConfiguration.getInstance().getIdfFile()).isFile()) {
            try (BufferedReader br = Files.newBufferedReader(Paths.get(ExperimentConfiguration.getInstance().getIdfFile()), Charset.defaultCharset())) {
                CSVReader csvReader = new CSVReader(br);
                String[] arrLine;
                while ((arrLine = csvReader.readNext()) != null) {
                    Term term = new Term(arrLine[0]);
                    double idf = Double.parseDouble(arrLine[1]);
                    this.idfCache.put(term, idf);
                }
                br.close();
                csvReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

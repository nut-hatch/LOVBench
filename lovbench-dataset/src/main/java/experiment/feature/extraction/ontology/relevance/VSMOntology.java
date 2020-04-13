package experiment.feature.extraction.ontology.relevance;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import experiment.feature.scoring.TFIDFScorer;
import experiment.model.Ontology;
import experiment.model.Term;
import experiment.model.query.AbstractQuery;
import experiment.model.query.TermQuery;
import experiment.configuration.ExperimentConfiguration;
import experiment.repository.file.FileUtil;
import experiment.repository.triplestore.AbstractOntologyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;

public class VSMOntology extends AbstractOntologyRelevanceFeature {

    /**
     * TFIDF scorer object.
     */
    TFIDFScorer tfidfScorer;

    Map<AbstractQuery, Map<Ontology, Double>> vsmScoreCache = new HashMap<>();

    public static final String FEATURE_NAME = "VSM_O";

    private static final Logger log = LoggerFactory.getLogger( VSMOntology.class );

    public VSMOntology(AbstractOntologyRepository repository, TFIDFScorer tfidfScorer) {
        super(repository);
        this.tfidfScorer = tfidfScorer;
        this.readVsmFromCsv();
    }

    @Override
    public double getScore(AbstractQuery query, Ontology ontology) {
        if (!this.vsmScoreCache.containsKey(query) || !this.vsmScoreCache.get(query).containsKey(ontology)) {
            double vsm = 0.0;

            // get maximum frequency of words in searchWords
            int maxSearchWordFrequency = 0;
            for (String searchWord : query.getSearchWords()) {
                int searchWordFrequency = Collections.frequency(query.getSearchWords(), searchWord);
                if (searchWordFrequency > maxSearchWordFrequency) {
                    maxSearchWordFrequency = searchWordFrequency;
                }
            }

            // compute sub scores per query term
            double queryNorm = 0.0;

            for (String searchWord : query.getSearchWords()) {
                AbstractQuery searchWordQuery = new TermQuery(searchWord);


                double tfidfOntology = 0.0;
                double tfidfQuery = 0.0;

                Map<Ontology, Set<Term>> matchedTermsPerOntology = this.repository.getQueryMatch(searchWordQuery);
                Set<Term> matchedTerms = matchedTermsPerOntology.get(ontology);

                if (matchedTerms != null && !matchedTerms.isEmpty()) {
                    for (Term matchedTerm : matchedTerms) {
                        double tfidfForTerm = this.tfidfScorer.tf(matchedTerm, ontology) * this.tfidfScorer.idf(matchedTerm);
                        tfidfOntology += tfidfForTerm;
                    }
                    tfidfQuery = (Collections.frequency(query.getSearchWords(), searchWord) / maxSearchWordFrequency) * Math.log(this.repository.countOntologies() / matchedTermsPerOntology.keySet().size());
                } else {
                    log.debug(String.format("No matches for query %s and ontology %s. Score = 0", query.toString(), ontology));
                }


                queryNorm += Math.pow(tfidfQuery, 2);
                vsm += (tfidfOntology * tfidfQuery);

            }

            if (vsm > 0.0) {
                queryNorm = Math.sqrt(queryNorm);
                double ontologyNorm = this.tfidfScorer.getOntologyNorm(ontology);

                if (ontologyNorm > 0.0 && queryNorm > 0.0) {
                    // compute final vsm similarity score
                    vsm /= (ontologyNorm * queryNorm);
                }

            }
            if (!this.vsmScoreCache.containsKey(query)) {
                this.vsmScoreCache.put(query,new HashMap<>());
            }
            this.vsmScoreCache.get(query).put(ontology,vsm);
            this.writeVsmCsv(query, ontology, vsm);
        }

        return this.vsmScoreCache.get(query).get(ontology);
    }

    @Override
    public String getFeatureName() {
        return VSMOntology.FEATURE_NAME;
    }


    public void writeVsmCsv(AbstractQuery query, Ontology ontology, double vsm) {
        String filename = ExperimentConfiguration.getInstance().getVsmFile();
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
            csvWriter.writeNext(new String[]{query.toString(),ontology.getOntologyUri(),vsm+""});
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readVsmFromCsv() {
        try (BufferedReader br = new BufferedReader(new FileReader(ExperimentConfiguration.getInstance().getVsmFile()))) {
            CSVReader csvReader = new CSVReader(br);
            String[] arrLine;
            while ((arrLine = csvReader.readNext()) != null) {
                AbstractQuery query = new TermQuery(arrLine[0]);
                Ontology ontology = new Ontology(arrLine[1]);
                double vsm = Double.parseDouble(arrLine[2]);
                if (!this.vsmScoreCache.containsKey(query)) {
                    this.vsmScoreCache.put(query,new HashMap<>());
                }
                this.vsmScoreCache.get(query).put(ontology,vsm);
            }
            br.close();
            csvReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

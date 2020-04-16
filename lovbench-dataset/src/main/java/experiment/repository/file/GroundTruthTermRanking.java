package experiment.repository.file;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.opencsv.CSVParser;
import com.opencsv.CSVReader;
import experiment.model.Relevance;
import experiment.model.query.TermQuery;
import experiment.model.Term;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Class that contains the ground truth for terms
 */
public class GroundTruthTermRanking {

    /**
     * The ground truth table.
     */
    Table<TermQuery, Term, Relevance> groundTruthTable = HashBasedTable.create();

    private static final Logger log = LoggerFactory.getLogger( GroundTruthTermRanking.class );

    /**
     * Parse a complete ground truth file.
     *
     * @param filename
     * @return GroundTruthTerms
     */
    public static GroundTruthTermRanking parse(String filename) {
        return GroundTruthTermRanking.parse(filename, 0);
    }

    /**
     * Parse X line of a ground truth file (0 for complete)
     *
     * @param filename
     * @param numberOfLines
     * @return GroundTruthTerms
     */
    public static GroundTruthTermRanking parse(String filename, int numberOfLines) {
        GroundTruthTermRanking groundTruthTable = new GroundTruthTermRanking();

        try (BufferedReader br = Files.newBufferedReader(Paths.get(filename), Charset.defaultCharset())) {
            CSVReader csvReader = new CSVReader(br, CSVParser.DEFAULT_SEPARATOR,
                    CSVParser.DEFAULT_QUOTE_CHARACTER, 1);
            String[] arrLine;
            int i = 1;
            while ((arrLine = csvReader.readNext()) != null) {
                // If searchWords is empty, there is really not so much use of the query -> let's skip
                if (!arrLine[0].isEmpty()) {
                    log.debug(String.format("%s - %s - %s", arrLine[0],arrLine[1],arrLine[2]));
                    TermQuery query = new TermQuery(arrLine[0]);
                    Term term = new Term(arrLine[1]);
                    Relevance relevance = new Relevance(arrLine[2]);
                    groundTruthTable.groundTruthTable.put(query, term, relevance);
                    i++;
                    if (numberOfLines > 0 && i > numberOfLines) {
                        break;
                    }
                }
            }
            br.close();
            csvReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return groundTruthTable;
    }

    public Table<TermQuery, Term, Relevance> getGroundTruthTable() {
        return groundTruthTable;
    }

}
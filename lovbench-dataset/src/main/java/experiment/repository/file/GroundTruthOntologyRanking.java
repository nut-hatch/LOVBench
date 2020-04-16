package experiment.repository.file;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.opencsv.CSVReader;
import experiment.model.Ontology;
import experiment.model.Relevance;
import experiment.model.Term;
import experiment.model.query.OntologyQuery;
import experiment.model.query.TermQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Class that contains the ground truth for ontologies.
 */
public class GroundTruthOntologyRanking {

    /**
     * The ground truth table.
     */
    Table<OntologyQuery, Ontology, Relevance> groundTruthTable = HashBasedTable.create();

    private static final Logger log = LoggerFactory.getLogger( GroundTruthOntologyRanking.class );

    /**
     * Parse a complete ground truth file.
     *
     * @param filename
     * @return GroundTruthOntologyRanking
     */
    public static GroundTruthOntologyRanking parse(String filename) {
        return GroundTruthOntologyRanking.parse(filename, 0);
    }

    /**
     * Parse X line of a ground truth file (0 for complete)
     *
     * @param filename
     * @param numberOfLines
     * @return GroundTruthOntologyRanking
     */
    public static GroundTruthOntologyRanking parse(String filename, int numberOfLines) {
        GroundTruthOntologyRanking groundTruthTable = new GroundTruthOntologyRanking();

        try (BufferedReader br = Files.newBufferedReader(Paths.get(filename), Charset.defaultCharset())) {
            CSVReader csvReader = new CSVReader(br);
            String[] arrLine;
            int i = 1;
            while ((arrLine = csvReader.readNext()) != null) {
                // If searchWords is empty, there is really not so much use of the query -> let's skip
                if (!arrLine[0].split("//", -1)[0].isEmpty()) {
                    OntologyQuery query = new OntologyQuery(arrLine[0]);
                    Ontology ontology = new Ontology(LOVPrefixes.getInstance().getOntologyUri(arrLine[1]));
                    Relevance relevance = new Relevance(arrLine[2]);
                    groundTruthTable.groundTruthTable.put(query, ontology, relevance);
                    i++;
                }
                if (numberOfLines > 0 && i > numberOfLines) {
                    break;
                }
            }
            br.close();
            csvReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return groundTruthTable;
    }

    public Table<OntologyQuery, Ontology, Relevance> getGroundTruthTable() {
        return groundTruthTable;
    }

}

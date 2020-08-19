package experiment.repository.file;

import java.util.List;

/**
 * This is an alternative to the class FeatureSetScores.
 * The difference is the output: while FeatureSetScores creates individual feature score files,
 * this class creates a LTR file that can be used for RankLib.
 *
 * Another difference is its approach in implementation
 */
public class LTRFile {

    List<LTRFileRow> rows;


}

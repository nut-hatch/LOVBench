package experiment.feature.scoring.normaliser;

import org.apache.commons.math.stat.descriptive.SummaryStatistics;

import java.util.HashMap;
import java.util.Map;

/**
 * Class to handle normalisation of scores.
 */
public class Normalise {

    /**
     * Computes the zscore normalisation of scores.
     *
     * @param scores
     * @param <T>
     * @return
     */
    public static <T>  Map<T,Double> zscore(Map<T,Double> scores) {
        Map<T,Double> normalizedScores = new HashMap<>();

        if (scores != null && !scores.isEmpty()) {
            if (scores.size() == 1) {
                normalizedScores = new HashMap<>(scores);
            } else {
                SummaryStatistics stats = new SummaryStatistics();
                for (Map.Entry<T, Double> score : scores.entrySet()) {
                    stats.addValue(score.getValue());
                }
                double average = stats.getMean();
                double stddev = stats.getStandardDeviation();
                for (Map.Entry<T, Double> score : scores.entrySet()) {
                    double normalizedScore = (score.getValue() - average) / stddev;
                    normalizedScores.put(score.getKey(), normalizedScore);
                }
            }
        }

        return normalizedScores;
    }
}

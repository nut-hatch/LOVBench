package experiment.feature.scoring.graph;

import edu.uci.ics.jung.algorithms.scoring.PageRank;
import edu.uci.ics.jung.graph.Graph;
import experiment.feature.scoring.AbstractScorer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Class that computes PageRank scores for vertices of type V.
 *
 * @param <V>
 * @param <E>
 */
public class PageRankScorer<V, E> extends AbstractScorer {

    /**
     * Damping factor
     */
    double dampingFactor = 0.85;

    /**
     * Maximum iterations.
     */
    int maxIterations = 100;

    /**
     * Tolerance.
     */
    double tolerance = 0.0001;

    private static final Logger log = LoggerFactory.getLogger( PageRankScorer.class );

    public PageRankScorer() {
    }

    public PageRankScorer(double dampingFactor, int maxIterations, double tolerance) {
        this.dampingFactor = dampingFactor;
        this.maxIterations = maxIterations;
        this.tolerance = tolerance;
    }

    /**
     * Runs PageRank.
     *
     * @param graph
     * @return
     */
    public Map<V,Double> run(Graph<V,E> graph) {
        log.debug(String.format("Run PR for a graph with %s vertices and %s edges", graph.getVertexCount(), graph.getEdgeCount()));
        Map<V,Double> pageRankScores = new HashMap<>();

        PageRank<V,E> pageRank = new PageRank<V,E>(graph, this.dampingFactor);
        pageRank.setMaxIterations(this.maxIterations);
        pageRank.setTolerance(this.tolerance);
        pageRank.evaluate();

        for (V vertex : graph.getVertices()) {
            pageRankScores.put(vertex, pageRank.getVertexScore(vertex)*100000);
        }

        return pageRankScores;
    }
}

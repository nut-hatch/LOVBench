package experiment.feature.extraction.ontology.relevance;

import edu.uci.ics.jung.algorithms.shortestpath.DijkstraDistance;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.EdgeType;
import experiment.feature.scoring.graph.util.JungGraphUtil;
import experiment.model.Ontology;
import experiment.model.Term;
import experiment.model.query.AbstractQuery;
import experiment.model.query.enums.TermType;
import experiment.repository.triplestore.AbstractOntologyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Computes the Semantic Similarity Measure as specified by AKTiveRank.
 *
 * The score is based on classes that match the query of an ontology.
 *
 * The shortest paths among all matched classes is computed, their lengths are summed and divided by the number of matches.
 */
public class SemanticSimilarityMeasure extends AbstractOntologyRelevanceFeature {

    private static final Logger log = LoggerFactory.getLogger( SemanticSimilarityMeasure.class );

    public SemanticSimilarityMeasure(AbstractOntologyRepository repository) {
        super(repository);
    }

    @Override
    public double getScore(AbstractQuery query, Ontology ontology) {
        double semanticSimilarityScore = 0.0;
        List<Term> classQueryMatches = new ArrayList<>(this.repository.getTermQueryMatch(query,ontology, TermType.CLASS));
        Graph<Term,String> ontologyGraph = JungGraphUtil.createOntologyGraph(this.repository.getOntologyGraphTriples(ontology), EdgeType.UNDIRECTED);
        DijkstraDistance<Term,String> distance = new DijkstraDistance<>(ontologyGraph);

        if (classQueryMatches != null && classQueryMatches.size() > 0) {
            if (classQueryMatches.size() ==  1) {
                semanticSimilarityScore = 1.0;
            } else {
                int shortestPathLengthSum = 0;
                int countPairs = 0;
                for (int i = 0; i < classQueryMatches.size() - 1; i++) {
                    Term classQueryMatchStart = classQueryMatches.get(i);
                    for (int j = i + 1; j < classQueryMatches.size(); j++) {
                        Term classQueryMatchEnd = classQueryMatches.get(j);
                        if (ontologyGraph.containsVertex(classQueryMatchStart) && ontologyGraph.containsVertex(classQueryMatchEnd)) {
                            if (!classQueryMatchStart.equals(classQueryMatchEnd)) {
//                            int shortestPathLength = this.repository.getShortestPathLength(classQueryMatchStart, classQueryMatchEnd);

                                Number shortestPathLengthNumber = distance.getDistance(classQueryMatchStart, classQueryMatchEnd);
                                int shortestPathLength = 0;
                                if (shortestPathLengthNumber != null) {
                                    shortestPathLength = shortestPathLengthNumber.intValue();
                                }

                                log.debug(String.format("Shortest path %s - %s: %s", classQueryMatchStart, classQueryMatchEnd, shortestPathLength));
                                shortestPathLengthSum += shortestPathLength;
                                countPairs++;
                            } else {
                                shortestPathLengthSum += 1;
                            }
                        } else {
                            log.debug(String.format("at least one query match is not contained in ontology graph: %s - %s", classQueryMatchStart, classQueryMatchStart));
                            countPairs++;
                        }
                    }
                }
                log.debug(String.format("Shortest path length sum: %s - count pairs: %s", shortestPathLengthSum, countPairs));
                semanticSimilarityScore = (double) shortestPathLengthSum / countPairs;
            }
        }
        log.info(String.format("Semantic similarity score for query %s and ontology %s: %s", query, ontology, semanticSimilarityScore));
        return semanticSimilarityScore;
    }

    @Override
    public String getFeatureName() {
        return "Semantic_Similarity_O";
    }
}

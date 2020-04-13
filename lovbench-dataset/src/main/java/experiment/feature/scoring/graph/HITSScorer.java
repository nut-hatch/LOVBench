package experiment.feature.scoring.graph;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import edu.uci.ics.jung.algorithms.scoring.HITS;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.EdgeType;
import experiment.feature.scoring.AbstractScorer;
import experiment.feature.scoring.graph.util.JungGraphUtil;
import experiment.model.Ontology;
import experiment.model.query.AbstractQuery;
import experiment.repository.triplestore.AbstractOntologyRepository;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

/**
 * Computes the HITS scores for a given graph.
 *
 */
public class HITSScorer extends AbstractScorer {

    private static final Logger log = LoggerFactory.getLogger( HITSScorer.class );

    /**
     * Caches the authority scores to avoid re-computations.
     */
    Table<AbstractQuery,Ontology,Double> authorityScores = HashBasedTable.create();

    /**
     * Caches the hub scores to avoid re-computations.
     */
    Table<AbstractQuery,Ontology,Double> hubScores = HashBasedTable.create();

    AbstractOntologyRepository repository;

    Set<Pair<Ontology, Ontology>> graphRelations;

    public HITSScorer(AbstractOntologyRepository repository, Set<Pair<Ontology, Ontology>> graphRelations) {
        this.repository = repository;
        this.graphRelations = graphRelations;
    }

    /**
     * Reads authority score from cache, if it is not contained, it computes both HITS scores and adds to cache.
     *
     * @param query
     * @param ontology
     * @return double
     */
    public double getAuthorityScore(AbstractQuery query, Ontology ontology) {
        if (!this.authorityScores.containsRow(query)) {
            this.runHits(query);
        }

        return this.getScoreFromTable(this.authorityScores,query,ontology);
    }

    /**
     * Reads hub score from cache, if it is not contained, it computes both HITS scores and adds to cache.
     *
     * @param query
     * @param ontology
     * @return double
     */
    public double getHubScore(AbstractQuery query, Ontology ontology) {
        if (!this.hubScores.containsRow(query)) {
            this.runHits(query);
        }

        return this.getScoreFromTable(this.hubScores,query,ontology);
    }


    /**
     * Runs the HITS algorithm.
     *
     * @param query
     */
    private void runHits(AbstractQuery query) {
        Set<Pair<Ontology, Ontology>> graphRelationsOfQueryMatch = new HashSet<>();
        Set<Ontology> queryMatch = this.repository.getOntologyQueryMatch(query);
        if (!queryMatch.isEmpty()) {
            for (Pair<Ontology, Ontology> graphRelation : this.graphRelations) {
                if (queryMatch.contains(graphRelation.getLeft()) && queryMatch.contains(graphRelation.getRight())) {
                    graphRelationsOfQueryMatch.add(graphRelation);
                }
            }
        } else {
            log.debug("Empty Query Match!!");
        }
        Graph<Ontology,String> graph = JungGraphUtil.createRepositoryGraph(graphRelationsOfQueryMatch, EdgeType.DIRECTED);

        if (graph.getEdgeCount() > 0) {
            HITS<Ontology, String> hits = new HITS<>(graph);
            hits.setMaxIterations(100);
            hits.evaluate();

            for (Ontology ontology : graph.getVertices()) {
                log.debug("adding score for: " + query.toString() + " - " + ontology.toString());
                this.authorityScores.put(query, ontology, hits.getVertexScore(ontology).authority);
                this.hubScores.put(query, ontology, hits.getVertexScore(ontology).hub);
            }
        } else {
            log.debug(String.format("Query lead to zero matches or to a graph with no import edges for query %s", query.toString()));
            this.authorityScores.put(query, new Ontology(""), 0.0);
            this.hubScores.put(query, new Ontology(""), 0.0);
        }
    }

    /**
     * Reads a score from cache. Return 0 if not found.
     *
     * @param table
     * @param query
     * @param ontology
     * @return double
     */
    private double getScoreFromTable(Table<AbstractQuery,Ontology,Double> table, AbstractQuery query, Ontology ontology) {
        if (table.contains(query, ontology)) {
            return table.get(query,ontology);
        } else {
            return 0.0;
        }
    }

    public Table<AbstractQuery, Ontology, Double> getAuthorityScores() {
        return authorityScores;
    }

    public void setAuthorityScores(Table<AbstractQuery, Ontology, Double> authorityScores) {
        this.authorityScores = authorityScores;
    }

    public Table<AbstractQuery, Ontology, Double> getHubScores() {
        return hubScores;
    }

    public void setHubScores(Table<AbstractQuery, Ontology, Double> hubScores) {
        this.hubScores = hubScores;
    }
}

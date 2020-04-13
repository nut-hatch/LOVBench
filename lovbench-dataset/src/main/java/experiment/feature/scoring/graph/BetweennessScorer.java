package experiment.feature.scoring.graph;

import edu.uci.ics.jung.algorithms.importance.BetweennessCentrality;
import edu.uci.ics.jung.algorithms.importance.Ranking;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.EdgeType;
import experiment.feature.scoring.AbstractScorer;
import experiment.feature.scoring.graph.util.JungGraphUtil;
import experiment.model.Ontology;
import experiment.model.Term;
import experiment.repository.file.LOVPrefixes;
import experiment.repository.triplestore.AbstractOntologyRepository;
import org.apache.commons.lang3.tuple.Triple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Scorer for the betweenness score of a graph.
 */
public class BetweennessScorer extends AbstractScorer {

    /**
     * Caches the betweeness scores.
     */
    private Map<Ontology, Map<Term,Double>> betweennessScoreCache = new HashMap<>();

    /**
     * Repository with ontology collection.
     */
    private AbstractOntologyRepository repository;

    private static final Logger log = LoggerFactory.getLogger( BetweennessScorer.class );

    public BetweennessScorer(AbstractOntologyRepository repository) {
        this.repository = repository;
    }

    /**
     * Returns betweenness score for a term in an ontology from cache, or runs betweenness scorer if no scores available.
     *
     * @param term
     * @param ontology
     * @return
     */
    public double betweenness(Term term, Ontology ontology) {
        double betweenness = 0.0;
        if (!this.betweennessScoreCache.containsKey(ontology)) {
            this.runBetweennessScorer(ontology);
        }
        if (this.betweennessScoreCache.get(ontology).containsKey(term)) {
            betweenness = this.betweennessScoreCache.get(ontology).get(term);
        }
        return betweenness;
    }

    public Map<Term,Double> allBetweennessScores(Ontology ontology) {
        if (!this.betweennessScoreCache.containsKey(ontology)) {
            this.runBetweennessScorer(ontology);
        }
        return this.betweennessScoreCache.get(ontology);
    }

    /**
     * Computes the betweenness of nodes in an ontology graph.
     *
     * @param ontology
     */
    public void runBetweennessScorer(Ontology ontology) {
        Graph<Term,String> ontologyGraph = JungGraphUtil.createOntologyGraph(this.repository.getOntologyGraphTriples(ontology), EdgeType.UNDIRECTED);
        BetweennessCentrality ranker = new BetweennessCentrality(ontologyGraph,true, true);
        ranker.evaluate();

        Iterator<Ranking<?>> iterator = ranker.getRankings().iterator();

        String termPrefixForOntology = LOVPrefixes.getInstance().getTermPrefixForOntologyPrefix(ontology.getOntologyPrefix());

        // Missing ontologies in the dump have to be skipped - which will not have a prefix specified.
        this.betweennessScoreCache.put(ontology, new HashMap<>());
        if (termPrefixForOntology != null && !termPrefixForOntology.isEmpty()) {

            log.debug(String.format("Count scores: %s", ranker.getRankings().size()));

            while (iterator.hasNext()) {
                Ranking<?> ranking = iterator.next();
                Term ranked = null;
                if (ranking.getRanked() instanceof Term) {
                    ranked = (Term) ranking.getRanked();

                } else if (ranking.getRanked() instanceof String) {
                    String termUri = ((String) ranking.getRanked()).split("::")[0];
                    ranked = new Term(termUri);
                }
                double score = ranking.rankScore;
                log.debug(String.format("Betweenness score for term %s in ontology %s: %s", ranked.toString(), ontology.getOntologyUri(), score));
                // We filter ontology terms so late because all graph nodes should be considered for the scoring.
                if (ranked.getTermUri().startsWith(termPrefixForOntology)) {
                    if (this.betweennessScoreCache.get(ontology).containsKey(ranked)) {
                        double tmp = this.betweennessScoreCache.get(ontology).get(ranked);
                        this.betweennessScoreCache.get(ontology).put(ranked, tmp + score);
                    } else {
                        this.betweennessScoreCache.get(ontology).put(ranked, score);
                    }
                } else {
                    log.debug(String.format("Score dismissed: %s", ranked));
                }
            }
        }
    }

    /**
     * Adds scores to the cache.
     *
     * @param ontology
     * @param term
     * @param score
     */
    private void addScoreToCache(Ontology ontology, Term term, Double score) {
        if (!this.betweennessScoreCache.containsKey(ontology)) {
        }
    }
}

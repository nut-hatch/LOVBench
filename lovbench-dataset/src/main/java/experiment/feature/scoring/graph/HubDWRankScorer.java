package experiment.feature.scoring.graph;

import edu.uci.ics.jung.graph.util.EdgeType;
import experiment.feature.scoring.AbstractScorer;
import experiment.feature.scoring.graph.util.JungGraphUtil;
import experiment.feature.scoring.normaliser.Normalise;
import experiment.model.Ontology;
import experiment.model.Term;
import experiment.repository.file.LOVPrefixes;
import experiment.repository.triplestore.AbstractOntologyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Class that computes HubScores as specified by DWRank.
 */
public class HubDWRankScorer extends AbstractScorer {

    /**
     * The PageRankScorer.
     */
    PageRankScorer<Term, String> pageRankScorer = new PageRankScorer<>();

    /**
     * Repository with ontology collection.
     */
    AbstractOntologyRepository repository;

    /**
     * Caches the max hub scores per ontology.
     */
    Map<Ontology, Double> maxHubScoreCache = new HashMap<>();

    /**
     * Caches the min hub score per ontology.
     */
    Map<Ontology, Double> minHubScoreCache = new HashMap<>();

    /**
     * Caches the hub score for all terms.
     */
    Map<Ontology, Map<Term, Double>> hubScoreCache = new HashMap<>();

    private static final Logger log = LoggerFactory.getLogger(HubDWRankScorer.class);

    public HubDWRankScorer(AbstractOntologyRepository repository) {
        this.repository = repository;
    }

    /**
     * Computes the hub scores, as reversed pagerank with zscore normalisation.
     *
     * @param ontology
     * @return
     */
    public Map<Term, Double> getHubScores(Ontology ontology) {
        if (this.hubScoreCache.containsKey(ontology)) {
            return this.hubScoreCache.get(ontology);
        }

        Map<Term, Double> filteredOntologyTermScores = new HashMap<>();

        Map<Term, Double> ontologyTermScores = this.pageRankScorer.run(JungGraphUtil.createOntologyGraph(this.repository.getOntologyGraphTriples(ontology, true), EdgeType.DIRECTED));

        double minHub = Double.MAX_VALUE;
        double maxHub = 0.0;
        for (Term term : this.repository.getAllTerms(ontology)) {
            if (ontologyTermScores.containsKey(term)) {
                double score = ontologyTermScores.get(term);
                if (Double.compare(score, minHub) == -1) {
                    minHub = score;
                }
                if (Double.compare(score, maxHub) == 1) {
                    maxHub = score;
                }
                filteredOntologyTermScores.put(term, score);
            } else {
                if (Double.compare(0.0, minHub) == -1) {
                    minHub = 0.0;
                }
                filteredOntologyTermScores.put(term, 0.0);
            }
        }
        this.maxHubScoreCache.put(ontology, maxHub);
        this.minHubScoreCache.put(ontology, minHub);

        return filteredOntologyTermScores;
    }

    /**
     * Return the max hab score for an ontology.
     *
     * @param ontology
     * @return
     */
    public double getMaxHubScore(Ontology ontology) {
        if (!this.maxHubScoreCache.containsKey(ontology)) {
            this.getHubScores(ontology);
        }
        return this.maxHubScoreCache.get(ontology);
    }

    /**
     * Returns the min hub score for an ontology.
     *
     * @param ontology
     * @return
     */
    public double getMinHubScore(Ontology ontology) {
        if (!this.minHubScoreCache.containsKey(ontology)) {
            this.getHubScores(ontology);
        }
        return this.minHubScoreCache.get(ontology);
    }
}

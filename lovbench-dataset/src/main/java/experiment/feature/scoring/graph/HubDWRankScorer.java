package experiment.feature.scoring.graph;

import edu.uci.ics.jung.graph.util.EdgeType;
import experiment.feature.scoring.AbstractScorer;
import experiment.feature.scoring.graph.util.JungGraphUtil;
import experiment.feature.scoring.normaliser.Normalise;
import experiment.model.Ontology;
import experiment.model.Term;
import experiment.repository.file.LOVPrefixes;
import experiment.repository.triplestore.AbstractOntologyRepository;

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
    PageRankScorer<Term,String> pageRankScorer = new PageRankScorer<>();

    /**
     * Repository with ontology collection.
     */
    AbstractOntologyRepository repository;

    /**
     * Caches the max hub scores per ontology.
     */
    Map<Ontology,Double> maxHubScoreCache = new HashMap<>();

    /**
     * Caches the min hub score per ontology.
     */
    Map<Ontology,Double> minHubScoreCache = new HashMap<>();

    /**
     * Caches the hub score for all terms.
     */
    Map<Ontology, Map<Term,Double>> hubScoreCache = new HashMap<>();

    public HubDWRankScorer(AbstractOntologyRepository repository) {
        this.repository = repository;
    }

    /**
     * Computes the hub scores, as reversed pagerank with zscore normalisation.
     *
     * @param ontology
     * @return
     */
    public Map<Term,Double> getHubScores(Ontology ontology) {
        if (this.hubScoreCache.containsKey(ontology)) {
            return this.hubScoreCache.get(ontology);
        }

        Map<Term,Double> filteredOntologyTermScores = new HashMap<>();

        Map<Term,Double> ontologyTermScores = this.pageRankScorer.run(JungGraphUtil.createOntologyGraph(this.repository.getOntologyGraphTriples(ontology, true), EdgeType.DIRECTED));

        for (Map.Entry<Term,Double> ontologyTermScore : ontologyTermScores.entrySet()) {
            Term term = ontologyTermScore.getKey();
            double score = ontologyTermScore.getValue();
            if (!LOVPrefixes.getInstance().isBlankNode(term.getTermUri()) && term.getOntologyUriOfTerm().equals(ontology.getOntologyUri())) {
                filteredOntologyTermScores.put(term, score);
            }
        }

//        Map<Term,Double> normalizedScores = Normalise.zscore(filteredOntologyTermScores);
        Map<Term,Double> normalizedScores = filteredOntologyTermScores;

        if (normalizedScores != null && !normalizedScores.isEmpty()) {
            this.maxHubScoreCache.put(ontology,Collections.max(normalizedScores.values()));
            this.minHubScoreCache.put(ontology,Collections.min(normalizedScores.values()));
        } else {
            this.maxHubScoreCache.put(ontology, 0.0);
            this.minHubScoreCache.put(ontology, 0.0);
        }

        return normalizedScores;
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

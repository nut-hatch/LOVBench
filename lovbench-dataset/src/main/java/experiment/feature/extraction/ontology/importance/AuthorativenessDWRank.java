package experiment.feature.extraction.ontology.importance;

import edu.uci.ics.jung.graph.util.EdgeType;
import experiment.feature.scoring.graph.PageRankScorer;
import experiment.feature.scoring.graph.util.JungGraphUtil;
import experiment.feature.scoring.normaliser.Normalise;
import experiment.model.Ontology;
import experiment.repository.triplestore.AbstractOntologyRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Computes the authorativeness score as specified by DWRank.
 *
 * It computes the PageRank of ontology imports, considering implicit imports, using zscore normalisation.
 */
public class AuthorativenessDWRank extends AbstractOntologyImportanceFeature {

    public AuthorativenessDWRank(AbstractOntologyRepository repository) {
        super(repository);
    }

    @Override
    public Map<Ontology, Double> computeScores(Set<Ontology> ontologySet) {
        PageRankScorer<Ontology,String> pageRank = new PageRankScorer<>();
        Map<Ontology,Double> scores = pageRank.run(JungGraphUtil.createRepositoryGraph(this.repository.getOwlImports(null, true), EdgeType.DIRECTED));
        Map<Ontology,Double> normalizedScores = Normalise.zscore(scores);
        this.setScores(normalizedScores);
        return normalizedScores;
    }

    @Override
    protected void computeAllScores() {
        // Essentially normal PR with implicit imports and normalization
        PageRankScorer<Ontology,String> pageRank = new PageRankScorer<>();
        Map<Ontology,Double> scores = pageRank.run(JungGraphUtil.createRepositoryGraph(this.repository.getOwlImports(null, true), EdgeType.DIRECTED));
        Map<Ontology,Double> normalizedScores = Normalise.zscore(scores);
        this.setScores(normalizedScores);
    }

    @Override
    public String getFeatureName() {
        return "Authorativeness_O";
    }
}

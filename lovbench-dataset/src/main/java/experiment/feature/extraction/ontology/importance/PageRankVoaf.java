package experiment.feature.extraction.ontology.importance;

import edu.uci.ics.jung.graph.util.EdgeType;
import experiment.feature.scoring.graph.PageRankScorer;
import experiment.feature.scoring.graph.util.JungGraphUtil;
import experiment.model.Ontology;
import experiment.repository.triplestore.AbstractOntologyMetadataRepository;
import experiment.repository.triplestore.AbstractOntologyRepository;

import java.util.Map;
import java.util.Set;

/**
 * Feature to compute the PageRank for ontologies based on voaf relations in the ontology collection.
 *
 */
public class PageRankVoaf extends AbstractOntologyImportanceFeature {

    AbstractOntologyMetadataRepository metadataRepository;

    public PageRankVoaf(AbstractOntologyRepository repository, AbstractOntologyMetadataRepository metadataRepository) {
        super(repository);
        this.metadataRepository = metadataRepository;
    }

    @Override
    public Map<Ontology, Double> computeScores(Set<Ontology> ontologySet) {
        PageRankScorer<Ontology,String> pageRank = new PageRankScorer<>();
        Map<Ontology, Double> scores = pageRank.run(JungGraphUtil.createRepositoryGraph(this.metadataRepository.getAllVoafRelations(), EdgeType.DIRECTED));
        this.setScores(scores);
        return scores;
    }

    @Override
    protected void computeAllScores() {
        PageRankScorer<Ontology,String> pageRank = new PageRankScorer<>();
        this.setScores(pageRank.run(JungGraphUtil.createRepositoryGraph(this.metadataRepository.getAllVoafRelations(), EdgeType.DIRECTED)));
    }

    @Override
    public String getFeatureName() {
        return "PageRank_Voaf_O";
    }

//    @Override
//    public void computeAllScores() {
//        Graph<Ontology, DefaultEdge> g = JGraphTUtil.createOntologyGraph(this.getRepository().getOwlImports());
////        GraphUtil.visualiseGraph(g);
//        PageRankScorer pageRank = new PageRankScorer<Ontology,DefaultEdge>();
//        this.setScores(pageRank.run(g, this.dampingFactor, this.maxIterations, this.tolerance));
//    }

}

package experiment.feature.extraction.ontology.relevance;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.EdgeType;
import experiment.feature.scoring.graph.HITSScorer;
import experiment.feature.scoring.graph.util.JungGraphUtil;
import experiment.model.Ontology;
import experiment.model.query.AbstractQuery;
import experiment.repository.triplestore.AbstractOntologyMetadataRepository;
import experiment.repository.triplestore.AbstractOntologyRepository;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

/**
 * Feature to compute the authority score as computed by HITS algorithm based on owl:imports statements of query matches.
 *
 */
public class HITSAuthorityVoaf extends AbstractOntologyRelevanceFeature {

    /**
     * HITS scorer object.
     */
    HITSScorer hits;


    private static final Logger log = LoggerFactory.getLogger( HITSAuthorityVoaf.class );

    public HITSAuthorityVoaf(AbstractOntologyRepository repository, HITSScorer hits) {
        super(repository);
        this.hits = hits;
    }

    @Override
    public double getScore(AbstractQuery query, Ontology ontology) {
//        Set<Pair<Ontology, Ontology>> voafRelationsOfQueryMatches = new HashSet<>();
//        Set<Ontology> queryMatch = this.repository.getOntologyQueryMatch(query);
//        for (Pair<Ontology, Ontology> voafRelation : this.voafRelations) {
//            if (queryMatch.contains(voafRelation.getLeft()) && queryMatch.contains(voafRelation.getRight())) {
//                voafRelationsOfQueryMatches.add(voafRelation);
//            }
//        }
//        double score = 0.0;
//        if (!voafRelationsOfQueryMatches.isEmpty()) {
//            Graph<Ontology,String> graph = JungGraphUtil.createRepositoryGraph(voafRelationsOfQueryMatches, EdgeType.DIRECTED);
//            score = this.hits.getAuthorityScore(query, ontology, graph);
//        }
        double score = this.hits.getAuthorityScore(query, ontology);
        log.debug(query.toString() + " - " + ontology.toString() + " - " + score);
        return score;
    }

    @Override
    public String getFeatureName() {
        return "HITS_Authority_Voaf_O";
    }

}

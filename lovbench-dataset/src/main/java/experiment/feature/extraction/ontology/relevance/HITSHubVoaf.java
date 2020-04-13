package experiment.feature.extraction.ontology.relevance;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.EdgeType;
import experiment.feature.scoring.graph.HITSScorer;
import experiment.feature.scoring.graph.util.JungGraphUtil;
import experiment.model.Ontology;
import experiment.model.query.AbstractQuery;
import experiment.repository.triplestore.AbstractOntologyMetadataRepository;
import experiment.repository.triplestore.AbstractOntologyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Feature to compute the hub score as computed by HITS algorithm based on owl:imports statements of query matches.
 *
 */
public class HITSHubVoaf extends AbstractOntologyRelevanceFeature {

    /**
     * HITS scorer object.
     */
    HITSScorer hits;

    public static final String FEATURE_NAME = "HITS_Hub_Voaf_O";

    private static final Logger log = LoggerFactory.getLogger( HITSHubVoaf.class );

    public HITSHubVoaf(AbstractOntologyRepository repository, HITSScorer hits) {
        super(repository);
        this.hits = hits;
    }

    @Override
    public double getScore(AbstractQuery query, Ontology ontology) {
        double score = this.hits.getHubScore(query, ontology);
        log.debug(query.toString() + " - " + ontology.toString() + " - " + score);
        return score;
    }

    @Override
    public String getFeatureName() {
        return HITSHubVoaf.FEATURE_NAME;
    }
}

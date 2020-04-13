package experiment.feature.extraction.ontology.relevance;

import experiment.feature.scoring.api.LOVScorer;
import experiment.model.Ontology;
import experiment.model.query.AbstractQuery;
import experiment.model.query.OntologyQuery;
import experiment.repository.triplestore.AbstractOntologyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LOVOntologyMatch extends AbstractOntologyRelevanceFeature {

    LOVScorer lovScorer;

    public static final String FEATURE_NAME = "LOV_Match_O";

    private static final Logger log = LoggerFactory.getLogger( LOVOntologyMatch.class );

    public LOVOntologyMatch(AbstractOntologyRepository repository, LOVScorer lovScorer) {
        super(repository);
        this.lovScorer = lovScorer;
    }

    @Override
    public double getScore(AbstractQuery query, Ontology ontology) {
//        if (!(query instanceof OntologyQuery)) {
//            log.error("ERROR - cannot compute LOV Ontology Match for a term query.");
//            return 0.0;
//        }
        return this.lovScorer.getOntologyMatchScore(query,ontology);
    }

    @Override
    public String getFeatureName() {
        return FEATURE_NAME;
    }
}

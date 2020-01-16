package experiment.repository.triplestore;

import experiment.model.Ontology;
import experiment.model.Term;
import experiment.model.query.AbstractQuery;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Map;
import java.util.Set;


public abstract class AbstractOntologySearchRepository extends AbstractRepository {

    public AbstractOntologySearchRepository(String dbName) {
        super(dbName);
    }

    public abstract Map<Term,Double> search(AbstractQuery query);

}

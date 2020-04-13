package experiment.repository.triplestore;

import experiment.model.Term;
import experiment.model.query.AbstractQuery;
import experiment.configuration.ExperimentConfiguration;
import experiment.repository.triplestore.connector.AbstractConnector;
import experiment.repository.triplestore.connector.StardogConnector;
import org.openrdf.query.BindingSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LOVSearchRepository extends AbstractOntologySearchRepository {

    private static final Logger log = LoggerFactory.getLogger( LOVSearchRepository.class );

    public LOVSearchRepository(String dbName) {
        super(dbName);
    }

    @Override
    public Map<Term, Double> search(AbstractQuery query) {
        Map<Term, Double> result = new HashMap<>();
        log.debug(query.toString());
        String querySearch = String.join("|", query.getSearchWords());
//        StringBuilder sb = new StringBuilder();
//        for (String queryWord : query.getSearchWords()) {
//            sb.append(QueryParser.escape(queryWord)).append("|");
//            sb.append(QueryParser.escape(queryWord)).append("|");
//        }
//        String querySearch  = sb.deleteCharAt(sb.length() - 1).toString();
        querySearch = querySearch.replace("+","\\\\+");
        querySearch = querySearch.replace("-","\\\\-");
        querySearch = querySearch.replace("!","\\\\!");
        querySearch = querySearch.replace("(","\\\\(");
        querySearch = querySearch.replace(")","\\\\)");
        querySearch = querySearch.replace("{","\\\\{");
        querySearch = querySearch.replace("}","\\\\}");
        querySearch = querySearch.replace("[","\\\\[");
        querySearch = querySearch.replace("]","\\\\]");
        querySearch = querySearch.replace("^","\\\\^");
        querySearch = querySearch.replace("~","\\\\~");
        querySearch = querySearch.replace("*","\\\\*");
        querySearch = querySearch.replace("?","\\\\?");
        querySearch = querySearch.replace(":","\\\\:");
        querySearch = querySearch.replace("/","\\\\/");
        String sparql = "SELECT DISTINCT ?term ?score WHERE { ?term ?p ?l. (?l ?score) <tag:stardog:api:property:textMatch> \""+querySearch+"\". }";
        List<BindingSet> termScores = this.getConnector().selectQuery(sparql);
        for (BindingSet termScore : termScores) {
            Term term = new Term(termScore.getBinding("term").getValue().stringValue());
            Double score = Double.parseDouble(termScore.getBinding("score").getValue().stringValue());
            result.put(term, score);
        }
        return result;
    }

    @Override
    public AbstractConnector getConnector() {
        if (this.connector == null) {
            this.connector = new StardogConnector(this.dbName, ExperimentConfiguration.getInstance().getDbServer(), ExperimentConfiguration.getInstance().getDbUser(), ExperimentConfiguration.getInstance().getDbPassword());
        }
        return connector;
    }
}

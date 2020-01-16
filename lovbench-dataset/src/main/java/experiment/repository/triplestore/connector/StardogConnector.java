package experiment.repository.triplestore.connector;

import com.complexible.common.base.Option;
import com.complexible.stardog.api.ConnectionConfiguration;
import com.complexible.stardog.api.impl.SearchConnectionImpl;
import com.complexible.stardog.api.search.SearchConnection;
import com.complexible.stardog.search.SearchOptions;
import com.complexible.stardog.sesame.StardogRepository;
import org.openrdf.query.*;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Implements the connection to the Stardog triple store.
 *
 */
public class StardogConnector extends AbstractConnector {

    /**
     * Repository object to the database.
     */
    private Repository repository;

    private static final Logger log = LoggerFactory.getLogger( StardogConnector.class );


    public StardogConnector(String dbName, String server, String user, String password) {
        super(dbName, server, user, password);

        this.repository = new StardogRepository(ConnectionConfiguration
                .to(dbName).server(this.server)
                .credentials(this.user, this.password)
        );

        this.repository.initialize();
    }

    @Override
    public List<BindingSet> selectQuery(String sparql) {
        log.info(sparql);
        RepositoryConnection connection = this.repository.getConnection();

        TupleQuery tupleQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL, sparql);
        TupleQueryResult queryResult = tupleQuery.evaluate();

        List<BindingSet> result = null;
        try {
            result = QueryResults.asList(queryResult);
        } catch (Exception e) {
            e.printStackTrace();
        }

        queryResult.close();
        connection.close();
        return result;
    }

}

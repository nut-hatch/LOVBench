package experiment.repository.triplestore.connector;

import org.openrdf.query.BindingSet;

import java.util.List;

/**
 * Abstract class for connectors to different triple stores.
 *
 * So far for a fixed database but can be loosened.
 *
 */
public abstract class AbstractConnector {

    /**
     * Database name for the connection.
     */
    String dbName;

    /**
     * Server address for the connection.
     */
    String server;

    /**
     * Username for the connection.
     */
    String user;

    /**
     * Password for the connection
     */
    String password;

    public AbstractConnector(String dbName, String server, String user, String password) {
        this.dbName = dbName;
        this.server = server;
        this.user = user;
        this.password = password;
    }

    /**
     * Runs a select query on the database and returns the result as a list of BindingSet.
     *
     * @param sparql
     * @return List<BindingSet>
     */
    abstract public List<BindingSet> selectQuery(String sparql);

}

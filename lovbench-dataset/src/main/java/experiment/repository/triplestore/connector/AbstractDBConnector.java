package experiment.repository.triplestore.connector;

import org.openrdf.query.BindingSet;

import java.util.List;

/**
 * Abstract class for connectors to different triple stores.
 *
 * So far for a fixed database but can be loosened.
 *
 */
public abstract class AbstractDBConnector extends AbstractConnector {

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

    public AbstractDBConnector(String dbName, String server, String user, String password) {
        this.dbName = dbName;
        this.server = server;
        this.user = user;
        this.password = password;
    }

}

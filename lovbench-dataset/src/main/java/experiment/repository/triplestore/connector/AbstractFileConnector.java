package experiment.repository.triplestore.connector;

/**
 * Abstract class for connectors to different triple stores.
 *
 * So far for a fixed database but can be loosened.
 *
 */
public abstract class AbstractFileConnector extends AbstractConnector {

    /**
     * Database name for the connection.
     */
    String filename;

    public AbstractFileConnector(String filename) {
        this.filename = filename;
    }

}

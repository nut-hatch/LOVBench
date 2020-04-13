package experiment.repository.triplestore;

import experiment.repository.triplestore.connector.AbstractConnector;

/**
 * Abstract class for a triple store database.
 */
public abstract class AbstractRepository {

    /**
     * Database name that contains the ontology collection.
     */
    String dbName;

    /**
     * Connector to the database.
     */
    AbstractConnector connector;

    public AbstractRepository(String dbName) {
        this.dbName = dbName;
    }

    public AbstractRepository(AbstractConnector connector) {
        this.connector = connector;
    }

    /**
     * Function that creates and returns the Connector object.
     *
     * @return AbstractConnector
     */
    public abstract AbstractConnector getConnector();

    public void setConnector(AbstractConnector connector) {
        this.connector = connector;
    }
}

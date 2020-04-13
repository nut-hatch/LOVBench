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
     * Runs a select query on the database and returns the result as a list of BindingSet.
     *
     * @param sparql
     * @return List<BindingSet>
     */
    public List<BindingSet> selectQuery(String sparql) {
        return this.selectQuery(sparql, false);
    }


    abstract public List<BindingSet> selectQuery(String sparql, boolean appendPrefix);

}

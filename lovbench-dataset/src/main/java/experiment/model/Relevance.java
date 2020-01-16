package experiment.model;

/**
 * Class that represents a relevance judgment of the ground truth
 *
 */
public class Relevance {

    /**
     * The relevance score 0-4.
     */
    int relevanceLabel;

    public Relevance(String relevance) {
        this.relevanceLabel = Integer.parseInt(relevance);
    }
}

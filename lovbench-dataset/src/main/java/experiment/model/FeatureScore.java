package experiment.model;

/**
 * Class that keeps a query/term/score value.
 * @deprecated
 */
public class FeatureScore<Q, R> {

    /**
     * The query.
     */
    Q query;

    /**
     * The term or ontology.
     */
    R rankingelement;

    /**
     * The score for a feature.
     */
    double score;

    public FeatureScore(Q query, R rankingelement, double score) {
        this.query = query;
        this.rankingelement = rankingelement;
        this.score = score;
    }

    public Q getQuery() {
        return query;
    }

    public void setQuery(Q query) {
        this.query = query;
    }

    public R getRankingelement() {
        return rankingelement;
    }

    public void setRankingelement(R rankingelement) {
        this.rankingelement = rankingelement;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }
}

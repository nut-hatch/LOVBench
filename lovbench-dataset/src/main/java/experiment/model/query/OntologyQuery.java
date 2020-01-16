package experiment.model.query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Objects;

/**
 * Class that represents a query for LOV ontology search
 *
 */
public class OntologyQuery extends AbstractQuery {

    /**
     * Language filter of the query.
     * @TODO still needs to be handled
     */
    String filterLang;

    private static final Logger log = LoggerFactory.getLogger( OntologyQuery.class );

    public OntologyQuery(String queryString) {
        log.info(queryString);
        String[] query = queryString.split("//", -1);
        this.searchWords = Arrays.asList(query[0].split(" "));
        this.filterTags = query[1];
        this.filterLang = query[2];
    }

    @Override
    public String getLovAPIQueryString() {

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("q=").append(URLEncoder.encode(String.join(" ", this.searchWords)));

        if (this.getFilterTags() != null && !this.getFilterTags().isEmpty()) {
            stringBuilder.append("&tag=").append(this.getFilterTags());
        }
        if (this.getFilterLang() != null && !this.getFilterLang().isEmpty()) {
            stringBuilder.append("&lang=").append(this.getFilterLang());
        }

        return stringBuilder.toString();
    }

    @Override
    public String getQueryFilterString(String... bindingNames) {
        StringBuilder stringBuilder = new StringBuilder();
        if (!this.getSearchWords().isEmpty()) {
            stringBuilder.append(this.getSearchWordsFilterExpression(bindingNames));
        } else {
            log.error("Empty searchWords!");
            return "";
        }

        return stringBuilder.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OntologyQuery that = (OntologyQuery) o;
        return Objects.equals(searchWords, that.searchWords) &&
                Objects.equals(filterTags, that.filterTags) &&
                Objects.equals(filterLang, that.filterLang);
    }

    @Override
    public int hashCode() {
        return Objects.hash(searchWords, filterTags, filterLang);
    }

    @Override
    public String toString() {
        return String.join("//",String.join(" ", this.searchWords), this.filterTags, this.filterLang);
    }

    public String getFilterLang() {
        return filterLang;
    }

    public void setFilterLang(String filterLang) {
        this.filterLang = filterLang;
    }
}

package experiment.model.query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Abstract class for a query made for LOV term or ontology search.
 */
public abstract class AbstractQuery {

    /**
     * The words contains in the query string.
     */
    List<String> searchWords;

    /**
     * The filter for specific tags.
     * @TODO still needs to be handled!
     */
    String filterTags;

    private static final Logger log = LoggerFactory.getLogger( AbstractQuery.class );

    /**
     * Generates a sparql filter() string based on the filtered tag.
     * @todo
     *
     * @param bindingNames
     * @return String
     */
    public abstract String getQueryFilterString(String... bindingNames);

    /**
     * Returns a parameter string used for querying the LOV api.
     *
     * @return
     */
    public abstract String getLovAPIQueryString();

    /**
     * Generates a sparql filter() string for the given search words.
     *
     * @param bindingNames
     * @return String
     */
    public String getSearchWordsFilterExpression(String... bindingNames) {
        StringBuilder stringBuilder = new StringBuilder();
        String regex = "\"(" + String.join("|", this.getSearchWords()) + ")\"";
        // @TODO maybe check other dangerous chars for escaping.
        regex = regex.replace("+", "\\\\+");
        log.debug(regex);

        for (String binding : bindingNames) {
            if (stringBuilder.length() == 0) {
                stringBuilder.append(" FILTER( ");
            } else {
                stringBuilder.append(" || ");
            }
            stringBuilder.append("regex(str(").append(binding).append("), ").append(regex).append(", \"i\") ");
        }
        stringBuilder.append(" ) .");

        return stringBuilder.toString();
    }

    public List<String> getSearchWords() {
        return searchWords;
    }

    public void setSearchWords(List<String> searchWords) {
        this.searchWords = searchWords;
    }

    public String getFilterTags() {
        return filterTags;
    }

    public void setFilterTags(String filterTags) {
        this.filterTags = filterTags;
    }
}

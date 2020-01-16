package experiment.model.query;

import experiment.model.query.enums.TypeFilter;
import experiment.repository.triplestore.AbstractOntologyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Objects;

/**
 * Class for a query of a LOV term search.
 *
 */
public class TermQuery extends AbstractQuery {

    /**
     * Filter of the query for specific types.
     */
    TypeFilter filterTypes;

    /**
     * Filter of the query for specific ontologies.
     * @TODO still needs to be handled!
     */
    String filterVocs;

    private static final Logger log = LoggerFactory.getLogger( TermQuery.class );

    public TermQuery(String queryString) {
        this.searchWords = Arrays.asList(queryString.split(" "));
        // @TODO this is the deprecated query string format:
//        String[] query = queryString.split("//", -1);
//        this.searchWords = Arrays.asList(query[0].split(" "));
//        if (query[1].equals("class")) {
//            this.filterTypes = TypeFilter.CLASS;
//        } else if (query[1].equals("property")) {
//            this.filterTypes = TypeFilter.PROPERTY;
//        } else {
//            if (!query[1].isEmpty()) {
//                log.error("Unknown type filter!");
//            }
//            this.filterTypes = null;
//        }
//        this.filterTags = query[2];
//        this.filterVocs = query[3];
    }

    /**
     * Returns the sparql filter expression to filter the type specified in the query - property or class.
     *
     * @param bindingName
     * @return
     */
    public String getTypeFilterExpression(String bindingName) {
        StringBuilder stringBuilder = new StringBuilder();

        if (this.getFilterTypes() != null) {
            String typeValues = "";
            if (this.getFilterTypes().equals(TypeFilter.PROPERTY)) {
                typeValues = AbstractOntologyRepository.getTypePropertyValuesString();
            } else if (this.getFilterTypes().equals(TypeFilter.CLASS)) {
                typeValues = AbstractOntologyRepository.getTypeClassValuesString();
            } else {

                typeValues = AbstractOntologyRepository.getAllTypesValuesString();
            }

            stringBuilder.append(bindingName).append(" a ?typeFilterVar . ");
            stringBuilder.append("VALUES ?typeFilterVar ").append(typeValues).append(" . ");
        }

        return stringBuilder.toString();
    }

    @Override
    public String getLovAPIQueryString() {
        //q=Person&type=class&tag=People&vocab=foaf&page=1
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("q=").append(URLEncoder.encode(String.join(" ", this.searchWords)));
        if (this.getFilterTypes() != null) {
            stringBuilder.append("&type=");
            switch (this.getFilterTypes()) {
                case CLASS:
                    stringBuilder.append("class");
                    break;
                case PROPERTY:
                    stringBuilder.append("property");
                    break;
            }
        }
        if (this.getFilterTags() != null && !this.getFilterTags().isEmpty()) {
            stringBuilder.append("&tag=").append(this.getFilterTags());
        }
        if (this.getFilterVocs() != null && !this.getFilterVocs().isEmpty()) {
            stringBuilder.append("&vocab=").append(this.getFilterVocs());
        }
        return stringBuilder.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TermQuery queryTerm = (TermQuery) o;
        return Objects.equals(searchWords, queryTerm.searchWords) &&
                Objects.equals(filterTypes, queryTerm.filterTypes) &&
                Objects.equals(filterTags, queryTerm.filterTags) &&
                Objects.equals(filterVocs, queryTerm.filterVocs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(searchWords, filterTypes, filterTags, filterVocs);
    }

    @Override
    public String getQueryFilterString(String... bindingNames) {
        StringBuilder stringBuilder = new StringBuilder();
        if (!this.getSearchWords().isEmpty()) {
            stringBuilder.append(this.getSearchWordsFilterExpression(bindingNames));
            stringBuilder.append(this.getTypeFilterExpression(bindingNames[0]));
        } else {
            log.error("Empty searchWords!");
            return "";
        }

        return stringBuilder.toString();
    }

    @Override
    public String toString() {
        return String.join(" ", this.searchWords);
        // @TODO deprecated query format:
//        String filterType = "";
//        if (this.filterTypes == null) {
//            filterType = "";
//        } else if (this.filterTypes.equals(TypeFilter.CLASS)) {
//            filterType = "class";
//        } else if (this.filterTypes.equals(TypeFilter.PROPERTY)) {
//            filterType = "property";
//        }
//        return String.join("//",String.join(" ", this.searchWords), filterType, this.filterTags, this.filterVocs);
    }


    public TypeFilter getFilterTypes() {
        return filterTypes;
    }

    public void setFilterTypes(TypeFilter filterTypes) {
        this.filterTypes = filterTypes;
    }

    public String getFilterVocs() {
        return filterVocs;
    }

    public void setFilterVocs(String filterVocs) {
        this.filterVocs = filterVocs;
    }

}
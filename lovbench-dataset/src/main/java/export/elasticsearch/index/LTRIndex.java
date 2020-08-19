package export.elasticsearch.index;

import export.elasticsearch.cli.config.ElasticsearchConfiguration;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.json.JSONObject;

import java.io.IOException;

public class LTRIndex extends ElasticSearchIndex {

    public LTRIndex(String clusterName, String hostName, String port, String restPort, String indexName, String mappingType) {
        super(clusterName, hostName, port, restPort, indexName, mappingType);
    }

    public LTRIndex(ElasticsearchConfiguration config) {
        this(config.getClusterName(), config.getHostName(), config.getTransportPort(), config.getRestPort(), config.getLtrIndexName(), config.getLtrIndexMappingType());
    }

    /**
     * Checks whether the LTR Plugin has been initialized
     * @return
     */
    @Override
    public boolean exists() {
//        connect();
//        System.out.println(client.admin().indices().prepareExists(indexName).execute().actionGet().isExists());
//        return client.admin().indices().prepareExists(indexName).execute().actionGet().isExists();
        try {
            JSONObject exists = new JSONObject(Request.Get(this.getRestIndexUrl())
                    .setHeader("Content-Type", "application/json")
                    .connectTimeout(1000)
                    .socketTimeout(1000)
                    .execute().returnContent().asString());
            return !exists.getJSONObject("stores").isEmpty();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Initializes the LTR plugin, only has to be done once.
     *
     * @return
     */
    public boolean initialize() {
        try {
            JSONObject exists = new JSONObject(Request.Put(this.getRestIndexUrl())
                    .setHeader("Content-Type", "application/json")
                    .connectTimeout(1000)
                    .socketTimeout(1000)
                    .execute().returnContent().asString());
            return exists.getBoolean("acknowledged");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Adds a feature set to the feature store.
     *
     * @param featureSetName
     * @param featureSetJson
     * @return
     */
    public String addFeatureSet(String featureSetName, String featureSetJson) {
//        return client
//                .prepareIndex(indexName, mappingType, featureSetName)
//                .setSource(JSONHelper.readFile(featureSetFile), XContentType.JSON)
//                .execute().actionGet().getId();
        try {
            JSONObject add = new JSONObject(Request.Put(this.getFeatureSetUrl(featureSetName))
                    .bodyString(featureSetJson, ContentType.APPLICATION_JSON)
                    .setHeader("Content-Type", "application/json")
                    .connectTimeout(1000)
                    .socketTimeout(1000)
                    .execute().returnContent().asString());
            return add.getString("result");
        } catch (IOException e) {
            e.printStackTrace();
            return "not newly created";
        }
    }

    /**
     * Checks whether a feature set exists in the feature store (of the LTR index)
     * @param featureSetName
     * @return
     */
    public boolean featureSetExists(String featureSetName) {
//        connect();
//        System.out.println(client.prepareGet(indexName, mappingType, featureSetName).execute().actionGet().isExists());
//        return admin().indices().exists(Requests.indicesExistsRequest(indexName)).actionGet().isExists();
        try {
            JSONObject exists = new JSONObject(Request.Get(this.getFeatureSetUrl(featureSetName))
                    .setHeader("Content-Type", "application/json")
                    .connectTimeout(1000)
                    .socketTimeout(1000)
                    .execute().returnContent().asString());
            return exists.getBoolean("found");
        } catch (IOException e) {
            // 404 not found means it doesn't exist
            return false;
        }
    }


    /**
     * Deletes a feature store.
     *
     * @param featureSetName
     * @return
     */
    public String featureSetDelete(String featureSetName) {
        try {
            JSONObject delete = new JSONObject(Request.Delete(this.getFeatureSetUrl(featureSetName))
                    .connectTimeout(1000)
                    .socketTimeout(1000)
                    .execute().returnContent().asString());
            return delete.getString("result");
        } catch (IOException e) {
            return "nothing was deleted";
        }
    }

    public boolean addModel(String featureSetName, JSONObject modelSpecification) {
        try {
            JSONObject post = new JSONObject(Request.Post(this.getFeatureSetUrl(featureSetName))
                    .bodyString(modelSpecification.toString(), ContentType.APPLICATION_JSON)
                    .connectTimeout(1000)
                    .socketTimeout(1000)
                    .execute().returnContent().asString());
            return !post.getString("result").isEmpty();
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Returns the base REST url to query a feature set in the LTR/feature store.
     *
     * @param featureSetName
     * @return
     */
    private String getFeatureSetUrl(String featureSetName){
        String url = "http://" + hostName + ":"+this.port+"/"+indexName+"/"+mappingType+"/"+featureSetName;
        return url;
    }

}

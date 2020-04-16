package export.elasticsearch.index;

import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.*;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.Map;

public class ElasticSearchIndex implements Closeable {

    protected final String clusterName;
    protected final String hostName;
    protected final String port;
    protected final String indexName;
    protected final String mappingType;
    protected Client client = null;
    private static final Logger log = LoggerFactory.getLogger( ElasticSearchIndex.class );

    public ElasticSearchIndex(String clusterName, String hostName, String port, String indexName, String mappingType) {
        this.clusterName = clusterName;
        this.hostName = hostName;
        this.indexName = indexName;
        this.mappingType = mappingType;
        this.port = port;
    }

    public void connect() {
        if (client != null) return;
        Settings settings = Settings.builder().put("cluster.name", clusterName).build();
        try {

            client = new PreBuiltTransportClient(settings).addTransportAddress(new TransportAddress(InetAddress.getByName(hostName), Integer.parseInt(this.port)));
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        if (client == null) return;
        client.close();
        client = null;
    }

    /**
     * Checks whether the index exists.
     * @return
     */
    public boolean exists() {
        connect();
        return client.admin().indices().exists(Requests.indicesExistsRequest(indexName)).actionGet().isExists();
    }

    /**
     * Looks up existing documents by ID.
     * @param id
     * @return
     */
    public JSONObject get(String id) {
        connect();

        GetResponse getResponse = client.prepareGet(this.indexName, this.mappingType, id).get();

        if (getResponse.isExists()) {
            long version = getResponse.getVersion();
            String sourceAsString = getResponse.getSourceAsString();
            log.debug(version+"");
            log.debug(sourceAsString);
            return new JSONObject(sourceAsString);
        } else {
            log.error("Get request for " + this.indexName + "/"+ this.mappingType+"/"+ id + " failed! Maybe document id does not exist.");
        }

        return null;
     }

    /**
     * Updates document fields by id of existing documents.
     *
     * @param id
     * @param docUpdate
     * @return
     */
     public boolean update(String id, String docUpdate) {
        connect();

        UpdateResponse updateResponse = client.prepareUpdate(this.indexName, this.mappingType, id).setDoc(docUpdate, XContentType.JSON).get();
        if (updateResponse.getGetResult().isExists()) {
            return true;
        } else {
            log.error("Update request for " + this.indexName + "/"+ this.mappingType+"/"+ id + " failed! Maybe document id does not exist.");
        }
        return false;
     }

     public boolean bulkUpdate(Map<String,JSONObject> docUpdates) {
         connect();

         boolean allSuccessful = true;

         BulkRequestBuilder bulkRequest = client.prepareBulk();

         for (Map.Entry<String,JSONObject> docUpdate : docUpdates.entrySet()) {
             bulkRequest.add(client.prepareUpdate(this.indexName, this.mappingType, docUpdate.getKey()).setDoc(docUpdate.getValue().toString(), XContentType.JSON));
         }

         BulkResponse bulkResponse = bulkRequest.get();
         if (bulkResponse.hasFailures()) {
             Iterator<BulkItemResponse> it = bulkResponse.iterator();
             while (it.hasNext()) {
                 BulkItemResponse r = it.next();
                 if (r.isFailed()) {
                     log.warn("Request failed! " + r.getFailureMessage());
                 }
             }
             allSuccessful = false;
             // process failures by iterating through each bulk response item
         }
         return allSuccessful;
     }

//    public boolean create() {
//        return this.create(true);
//    }
//
//    public boolean create(boolean addMapping) {
////        connect();
////        if (!client.admin().indices().create(Requests.createIndexRequest(indexName).settings(JSONHelper.readFile("mappings/settings.json"), XContentType.JSON)).actionGet().isAcknowledged()) {
////            return false;
////        }
////
////        if (addMapping) {
////            String mappingFile = "mappings/" + mappingType + ".json";
////            if (!client.admin().indices().preparePutMapping().setIndices(indexName).setType(mappingType).setSource(JSONHelper.readFile(mappingFile), XContentType.JSON).execute().actionGet().isAcknowledged()) {
////                return false;
////            }
////        }
//
//        return true;
//    }

    /**
     * Extends the existing mapping of the index/type.
     *
     * @param mapping
     * @return
     */
    public boolean put(String mapping) {
        connect();

        if (!client.admin().indices().preparePutMapping().setIndices(indexName).setType(mappingType).setSource(mapping, XContentType.JSON).execute().actionGet().isAcknowledged()) {
            return false;
        }

        return true;
    }

    public String getClusterName() {
        return clusterName;
    }

    public String getHostName() {
        return hostName;
    }

    public String getPort() {
        return port;
    }

    public String getIndexName() {
        return indexName;
    }

    public String getMappingType() {
        return mappingType;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }
}

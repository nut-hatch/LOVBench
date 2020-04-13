package export.elasticsearch;

import org.elasticsearch.client.Client;
import org.elasticsearch.client.Requests;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

import java.io.Closeable;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class ElasticSearchIndex implements Closeable {

    protected final String clusterName;
    protected final String hostName;
    protected final String indexName;
    protected final String mappingType;
    protected Client client = null;

    public ElasticSearchIndex(String clusterName, String hostName, String indexName, String mappingType) {
        this.clusterName = clusterName;
        this.hostName = hostName;
        this.indexName = indexName;
        this.mappingType = mappingType;
    }

    public void connect() {
        if (client != null) return;
        Settings settings = Settings.builder().put("cluster.name", clusterName).build();
        try {
            client = new PreBuiltTransportClient(settings).addTransportAddress(new TransportAddress(InetAddress.getByName(hostName), 9300));
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        if (client == null) return;
        client.close();
        client = null;
    }

    public boolean exists() {
        connect();
        return client.admin().indices().exists(Requests.indicesExistsRequest(indexName)).actionGet().isExists();
    }





}

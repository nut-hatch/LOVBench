package export.elasticsearch.cli.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ElasticsearchConfiguration {

    private String clusterName;
    private String hostName;
    private String transportPort;
    private String restPort;

    private String termIndexName;
    private String termIndexMappingType;

    private String ltrIndexName;
    private String ltrIndexMappingType;

    private String lovNqFilePath;
    private String groundTruthFilePath;

    private String featureSetDefinitionFilePath;
    private String modelFilePath;

    private static final Logger log = LoggerFactory.getLogger( ElasticsearchConfiguration.class );

    public ElasticsearchConfiguration(String configFilePath) {
        Properties properties = new Properties();


        InputStream input = null;

        try {
            input = new FileInputStream(configFilePath);
            properties.load(input);

            for (String propertyName: properties.stringPropertyNames()) {
                switch (propertyName) {
                    case "clusterName":
                        this.setClusterName(properties.getProperty(propertyName));
                        break;
                    case "hostName":
                        this.setHostName(properties.getProperty(propertyName));
                        break;
                    case "transportPort":
                        this.setTransportPort(properties.getProperty(propertyName));
                        break;
                    case "restPort":
                        this.setRestPort(properties.getProperty(propertyName));
                        break;
                    case "termIndexName":
                        this.setTermIndexName(properties.getProperty(propertyName));
                        break;
                    case "termIndexMappingType":
                        this.setTermIndexMappingType(properties.getProperty(propertyName));
                        break;
                    case "ltrIndexName":
                        this.setLtrIndexName(properties.getProperty(propertyName));
                        break;
                    case "ltrIndexMappingType":
                        this.setLtrIndexMappingType(properties.getProperty(propertyName));
                        break;
                    case "lovNqFilePath":
                        this.setLovNqFilePath(properties.getProperty(propertyName));
                        break;
                    case "groundTruthFilePath":
                        this.setGroundTruthFilePath(properties.getProperty(propertyName));
                        break;
                    case "featureSetDefinitionFilePath":
                        this.setFeatureSetDefinitionFilePath(properties.getProperty(propertyName));
                        break;
                    case "modelFilePath":
                        this.setModelFilePath(properties.getProperty(propertyName));
                        break;
                    default: log.warn("Unknown configuration entry in configuration file: " + propertyName);
                }
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getTransportPort() {
        return transportPort;
    }

    public void setTransportPort(String transportPort) {
        this.transportPort = transportPort;
    }

    public String getRestPort() {
        return restPort;
    }

    public void setRestPort(String restPort) {
        this.restPort = restPort;
    }

    public String getTermIndexName() {
        return termIndexName;
    }

    public void setTermIndexName(String termIndexName) {
        this.termIndexName = termIndexName;
    }

    public String getTermIndexMappingType() {
        return termIndexMappingType;
    }

    public void setTermIndexMappingType(String termIndexMappingType) {
        this.termIndexMappingType = termIndexMappingType;
    }

    public String getLtrIndexName() {
        return ltrIndexName;
    }

    public void setLtrIndexName(String ltrIndexName) {
        this.ltrIndexName = ltrIndexName;
    }

    public String getLtrIndexMappingType() {
        return ltrIndexMappingType;
    }

    public void setLtrIndexMappingType(String ltrIndexMappingType) {
        this.ltrIndexMappingType = ltrIndexMappingType;
    }

    public String getLovNqFilePath() {
        return lovNqFilePath;
    }

    public void setLovNqFilePath(String lovNqFilePath) {
        this.lovNqFilePath = lovNqFilePath;
    }

    public String getGroundTruthFilePath() {
        return groundTruthFilePath;
    }

    public void setGroundTruthFilePath(String groundTruthFilePath) {
        this.groundTruthFilePath = groundTruthFilePath;
    }

    public String getFeatureSetDefinitionFilePath() {
        return featureSetDefinitionFilePath;
    }

    public void setFeatureSetDefinitionFilePath(String featureSetDefinitionFilePath) {
        this.featureSetDefinitionFilePath = featureSetDefinitionFilePath;
    }

    public String getModelFilePath() {
        return modelFilePath;
    }

    public void setModelFilePath(String modelFilePath) {
        this.modelFilePath = modelFilePath;
    }
}

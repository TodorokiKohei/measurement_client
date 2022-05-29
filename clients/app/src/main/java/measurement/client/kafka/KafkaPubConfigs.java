package measurement.client.kafka;

import java.util.Properties;

import measurement.client.base.CommonPubConfigs;

public class KafkaPubConfigs extends CommonPubConfigs {
    
    private String topicName;
    private Properties properties;

    public String getTopicName() {
        return topicName;
    }

    public Properties getProperties() {
        return properties;
    }

    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

}

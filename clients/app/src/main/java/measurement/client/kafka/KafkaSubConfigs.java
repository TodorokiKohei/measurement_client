package measurement.client.kafka;

import java.util.Properties;

import measurement.client.base.CommonSubConfigs;

public class KafkaSubConfigs extends CommonSubConfigs {

    private String topicName;
    private long maxWait;
    private Properties properties;

    public String getTopicName() {
        return topicName;
    }

    public long getMaxWait() {
        return maxWait;
    }

    public Properties getProperties() {
        return properties;
    }

    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }

    public void setMaxWait(long maxWait) {
        this.maxWait = maxWait;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }
}

package measurement.client.kafka;

import java.util.Properties;

public abstract class KafkaClientConfigs {
    private int number;
    private String topicName;
    private Properties properties;

    public int getNumber() {
        return number;
    }

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

    public void setNumber(int number) {
        this.number = number;
    }

}

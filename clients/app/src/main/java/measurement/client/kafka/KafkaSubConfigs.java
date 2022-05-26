package measurement.client.kafka;

public class KafkaSubConfigs extends KafkaClientConfigs {
    private long maxWait;

    public long getMaxWait() {
        return maxWait;
    }

    public void setMaxWait(long maxWait) {
        this.maxWait = maxWait;
    }
}

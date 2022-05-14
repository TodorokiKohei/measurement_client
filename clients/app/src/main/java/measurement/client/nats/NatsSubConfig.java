package measurement.client.nats;

public class NatsSubConfig extends NatsClientConfig {
    private String durable;
    private int batchSize;
    private long maxWait;    
    private NatsSubMode mode;
    private String queueGroup;

    public String getDurable() {
        return durable;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public long getMaxWait() {
        return maxWait;
    }

    public NatsSubMode getMode() {
        return mode;
    }

    public String getQueueGroup() {
        return queueGroup;
    }

    public void setDurable(String durable) {
        this.durable = durable;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    public void setMaxWait(long maxWait) {
        this.maxWait = maxWait;
    }

    public void setMode(NatsSubMode mode) {
        this.mode = mode;
    }

    public void setQueueGroup(String queueGroup) {
        this.queueGroup = queueGroup;
    }
}

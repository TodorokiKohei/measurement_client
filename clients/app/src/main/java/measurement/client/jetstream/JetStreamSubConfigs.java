package measurement.client.jetstream;

public class JetStreamSubConfigs extends JetStreamClientConfig {
    private String durable;
    private int batchSize;
    private long maxWait;    
    private JetStreamSubMode mode;
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

    public JetStreamSubMode getMode() {
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

    public void setMode(JetStreamSubMode mode) {
        this.mode = mode;
    }

    public void setQueueGroup(String queueGroup) {
        this.queueGroup = queueGroup;
    }
}

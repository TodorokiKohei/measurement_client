package measurement.client.jetstream;

import measurement.client.base.CommonSubConfigs;

public class JetStreamSubConfigs extends CommonSubConfigs {
    private String server;
    private String stream;
    private String subject;
    private String durable;
    private int batchSize;
    private long maxWait;
    private JetStreamSubMode mode;
    private String queueGroup;

    public String getServer() {
        return server;
    }

    public String getStream() {
        return stream;
    }

    public String getSubject() {
        return subject;
    }

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

    public void setServer(String server) {
        this.server = server;
    }

    public void setStream(String stream) {
        this.stream = stream;
    }

    public void setSubject(String subject) {
        this.subject = subject;
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

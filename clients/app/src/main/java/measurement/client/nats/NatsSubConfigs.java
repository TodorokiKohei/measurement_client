package measurement.client.nats;

import measurement.client.base.CommonSubConfigs;

public class NatsSubConfigs extends CommonSubConfigs {
    private String server;
    private String subject;
    private long maxWait;
    private String queueGroup;

    public String getServer() {
        return server;
    }

    public String getSubject() {
        return subject;
    }

    public long getMaxWait() {
        return maxWait;
    }

    public String getQueueGroup() {
        return queueGroup;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public void setMaxWait(long maxWait) {
        this.maxWait = maxWait;
    }

    public void setQueueGroup(String queueGroup) {
        this.queueGroup = queueGroup;
    }
}

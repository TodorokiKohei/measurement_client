package measurement.client.nats;

public class NatsSubConf extends NatsClientConfig {
    private double pollTimeout;
    private String mode;
    private String queueGroup;

    public double getPollTimeout() {
        return pollTimeout;
    }

    public String getMode() {
        return mode;
    }

    public String getQueueGroup() {
        return queueGroup;
    }

    public void setPollTimeout(double pollTimeout) {
        this.pollTimeout = pollTimeout;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public void setQueueGroup(String queueGroup) {
        this.queueGroup = queueGroup;
    }
}

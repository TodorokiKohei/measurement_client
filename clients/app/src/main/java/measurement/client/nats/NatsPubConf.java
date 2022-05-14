package measurement.client.nats;

public class NatsPubConf extends NatsClientConfig {
    private String messageRate;
    private long messageSize;

    public String getMessageRate() {
        return messageRate;
    }

    public long getMessageSize() {
        return messageSize;
    }

    public void setMessageRate(String messageRate) {
        this.messageRate = messageRate;
    }

    public void setMessageSize(long messageSize) {
        this.messageSize = messageSize;
    }

}

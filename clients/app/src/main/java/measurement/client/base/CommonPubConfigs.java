package measurement.client.base;

public abstract class CommonPubConfigs extends CommonClientConfigs{
    private String messageRate = "0";
    private String messageSize;

    public String getMessageRate() {
        return messageRate;
    }

    public String getMessageSize() {
        return messageSize;
    }

    public void setMessageRate(String messageRate) {
        this.messageRate = messageRate;
    }

    public void setMessageSize(String messageSize) {
        this.messageSize = messageSize;
    }
}

package measurement.client.base;

public abstract class CommonPubConfigs extends CommonClientConfigs {
    private String messageRate = "0";
    private String messageSize;
    private Boolean pubAsync = false;

    public String getMessageRate() {
        return messageRate;
    }

    public String getMessageSize() {
        return messageSize;
    }

    public Boolean getPubAsync() {
        return pubAsync;
    }

    public void setMessageRate(String messageRate) {
        this.messageRate = messageRate;
    }

    public void setMessageSize(String messageSize) {
        this.messageSize = messageSize;
    }

    public void setPubAsync(Boolean pubAsync) {
        this.pubAsync = pubAsync;
    }
}

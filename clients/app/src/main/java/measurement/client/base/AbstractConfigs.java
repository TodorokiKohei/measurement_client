package measurement.client.base;

public abstract class AbstractConfigs {
    protected long execTime;
    protected Boolean recordMessage = false;
    protected long publisherRiseTime = 0;
    protected long subscriberFallTime = 0;

    public long getExecTime() {
        return execTime;
    }

    public Boolean getRecordMessage() {
        return recordMessage;
    }

    public long getPublisherRiseTime() {
        return publisherRiseTime;
    }

    public long getSubscriberFallTime() {
        return subscriberFallTime;
    }

    public void setExecTime(long execTime) {
        this.execTime = execTime;
    }

    public void setRecordMessage(Boolean recordMessage) {
        this.recordMessage = recordMessage;
    }

    public void setPublisherRiseTime(long publisherRiseTime) {
        this.publisherRiseTime = publisherRiseTime;
    }

    public void setSubscriberFallTime(long subscriberFallTime) {
        this.subscriberFallTime = subscriberFallTime;
    }
}

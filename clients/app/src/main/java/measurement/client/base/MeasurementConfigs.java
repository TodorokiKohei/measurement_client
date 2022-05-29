package measurement.client.base;

public abstract class MeasurementConfigs<T , U> {
    protected long execTime;
    protected Boolean recordMessage = false;
    protected long publisherRiseTime = 0;
    protected long subscriberFallTime = 0;

    protected T pubConf;
    protected U subConf;

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

    public T getPubConf(){
        return pubConf;
    }

    public U getSubConf(){
        return subConf;
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

    public void setPubConf(T pubConf){
        this.pubConf = pubConf;
    }

    public void setSubConf(U subConf){
        this.subConf = subConf;
    }
}

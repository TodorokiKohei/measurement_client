package measurement.client.nats;

public class NatsConfigs {
    private long execTime;
    private Boolean recordMessage = false;
    private long publisherRiseTime = 0;
    private long subscriberFallTime = 0;
    private NatsPubConfig pubConf;
    private NatsSubConfig subConf;

    public long getExecTime() {
        return execTime;
    }

    public Boolean getRecordMessage() {
        return recordMessage;
    }

    public NatsPubConfig getPubConf() {
        return pubConf;
    }

    public NatsSubConfig getSubConf() {
        return subConf;
    }

    public void setExecTime(long execTime) {
        this.execTime = execTime;
    }

    public void setRecordMessage(Boolean recordMessage) {
        this.recordMessage = recordMessage;
    }

    public void setPubConf(NatsPubConfig pubConf) {
        this.pubConf = pubConf;
    }

    public void setSubConf(NatsSubConfig subConf) {
        this.subConf = subConf;
    }
}

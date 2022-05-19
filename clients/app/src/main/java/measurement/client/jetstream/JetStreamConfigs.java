package measurement.client.jetstream;

public class JetStreamConfigs {
    private long execTime;
    private Boolean recordMessage = false;
    private long publisherRiseTime = 0;
    private long subscriberFallTime = 0;
    private JetStreamPubConfig pubConf;
    private JetStreamSubConfig subConf;

    public long getExecTime() {
        return execTime;
    }

    public Boolean getRecordMessage() {
        return recordMessage;
    }

    public JetStreamPubConfig getPubConf() {
        return pubConf;
    }

    public JetStreamSubConfig getSubConf() {
        return subConf;
    }

    public void setExecTime(long execTime) {
        this.execTime = execTime;
    }

    public void setRecordMessage(Boolean recordMessage) {
        this.recordMessage = recordMessage;
    }

    public void setPubConf(JetStreamPubConfig pubConf) {
        this.pubConf = pubConf;
    }

    public void setSubConf(JetStreamSubConfig subConf) {
        this.subConf = subConf;
    }
}

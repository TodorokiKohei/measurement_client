package measurement.client.nats;

public class NatsConfigs {
    private long execTime;
    private NatsPubConfig pubConf;
    private NatsSubConfig subConf;

    public long getExecTime() {
        return execTime;
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

    public void setPubConf(NatsPubConfig pubConf) {
        this.pubConf = pubConf;
    }

    public void setSubConf(NatsSubConfig subConf) {
        this.subConf = subConf;
    }
}

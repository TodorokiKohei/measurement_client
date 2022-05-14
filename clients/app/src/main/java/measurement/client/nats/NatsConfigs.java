package measurement.client.nats;

public class NatsConfigs {
    private long execTime;
    private NatsPubConf pubConf;
    private NatsSubConf subConf;

    public long getExecTime() {
        return execTime;
    }

    public NatsPubConf getPubConf() {
        return pubConf;
    }

    public NatsSubConf getSubConf() {
        return subConf;
    }

    public void setExecTime(long execTime) {
        this.execTime = execTime;
    }

    public void setPubConf(NatsPubConf pubConf) {
        this.pubConf = pubConf;
    }

    public void setSubConf(NatsSubConf subConf) {
        this.subConf = subConf;
    }
}

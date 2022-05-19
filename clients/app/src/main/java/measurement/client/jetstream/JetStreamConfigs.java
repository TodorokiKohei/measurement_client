package measurement.client.jetstream;

import measurement.client.base.AbstractConfigs;

public class JetStreamConfigs extends AbstractConfigs {
    private JetStreamPubConfigs pubConf;
    private JetStreamSubConfigs subConf;

    public JetStreamPubConfigs getPubConf() {
        return pubConf;
    }

    public JetStreamSubConfigs getSubConf() {
        return subConf;
    }

    public void setPubConf(JetStreamPubConfigs pubConf) {
        this.pubConf = pubConf;
    }

    public void setSubConf(JetStreamSubConfigs subConf) {
        this.subConf = subConf;
    }
}

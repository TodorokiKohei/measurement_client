package measurement.client.kafka;

import measurement.client.base.AbstractConfigs;

public class KafkaConfigs extends AbstractConfigs {
    private KafkaPubConfigs pubConf;
    private KafkaSubConfigs subConf;

    public KafkaPubConfigs getPubConf() {
        return pubConf;
    }

    public KafkaSubConfigs getSubConf() {
        return subConf;
    }

    public void setPubConf(KafkaPubConfigs pubConf) {
        this.pubConf = pubConf;
    }

    public void setSubConf(KafkaSubConfigs subConf) {
        this.subConf = subConf;
    }

}

package measurement.client.nats;

import java.io.FileNotFoundException;

import measurement.client.Measurement;
import measurement.client.base.AbstractDriver;
import measurement.client.base.AbstractPublisher;
import measurement.client.base.AbstractSubscriber;
import measurement.client.base.CommonPubConfigs;
import measurement.client.base.CommonSubConfigs;
import measurement.client.base.MeasurementConfigs;
import measurement.client.base.Utils;

public class NatsDriver extends AbstractDriver {

    private static final String resourceName = "/natsconf.yaml";
    private NatsConfigs natsConfigs;

    @Override
    public MeasurementConfigs<? extends CommonPubConfigs, ? extends CommonSubConfigs> loadConfigs(String fileName) {
        try {
            natsConfigs = Utils.loadConfigsFromYaml(resourceName, fileName, NatsConfigs.class);
        } catch (FileNotFoundException e) {
            Measurement.logger.warning(e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
        return natsConfigs;
    }

    @Override
    public AbstractPublisher createPublisher(int clientNumber, long interval) {
        NatsPubConfigs nPubConfigs = natsConfigs.getPubConf();
        NatsPublisher natsPublisher = new NatsPublisher(
            "nats-publisher-" + clientNumber,
            interval,
            (int) Utils.byteStringToDouble(nPubConfigs.getMessageSize()),
            nPubConfigs.getServer(),
            nPubConfigs.getSubject());
        return natsPublisher;
    }

    @Override
    public AbstractSubscriber createSubscriber(int clientNumber) {
        NatsSubConfigs nSubConfigs = natsConfigs.getSubConf();
        NatsSubscriber natsSubscriber = new NatsSubscriber(
            "nats-subscriber-" + clientNumber,
            nSubConfigs.getServer(),
            nSubConfigs.getSubject(), 
            nSubConfigs.getMaxWait(), 
            nSubConfigs.getQueueGroup());
        return natsSubscriber;
    }
}

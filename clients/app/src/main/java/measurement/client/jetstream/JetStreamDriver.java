package measurement.client.jetstream;

import java.io.FileNotFoundException;

import measurement.client.Measurement;
import measurement.client.base.MeasurementConfigs;
import measurement.client.base.CommonPubConfigs;
import measurement.client.base.CommonSubConfigs;
import measurement.client.base.AbstractDriver;
import measurement.client.base.AbstractPublisher;
import measurement.client.base.AbstractSubscriber;
import measurement.client.base.Utils;

public class JetStreamDriver extends AbstractDriver {

    private static final String resourceName = "/jetstreamconf.yaml";
    private JetStreamConfigs jetStreamConfigs;

    public MeasurementConfigs<? extends CommonPubConfigs, ? extends CommonSubConfigs> loadConfigs(String fileName) {
        try {
            jetStreamConfigs = Utils.loadConfigsFromYaml(resourceName, fileName, JetStreamConfigs.class);
        } catch (FileNotFoundException e) {
            Measurement.logger.warning(e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
        return jetStreamConfigs;
    }

    @Override
    public AbstractPublisher createPublisher(int clientNumber, long interval) {
        JetStreamPubConfigs jPubConfigs = jetStreamConfigs.getPubConf();
        JetStreamPublisher jsPub = new JetStreamPublisher(
                "jetstream-publisher-" + clientNumber,
                interval,
                (int) Utils.byteStringToDouble(jPubConfigs.getMessageSize()),
                jPubConfigs.getPubAsync(),
                jPubConfigs.getServer(),
                jPubConfigs.getStream(),
                jPubConfigs.getSubject());
        return jsPub;
    }

    @Override
    public AbstractSubscriber createSubscriber(int clientNumber) {
        JetStreamSubConfigs jSubConfigs = jetStreamConfigs.getSubConf();
        JetStreamSubscriber jsSub = null;
        if (jSubConfigs.getMode() == JetStreamSubMode.pull) {
            jsSub = new JetStreamPullSubscriber(
                    "jetstream-subscriber-pull-" + clientNumber,
                    jSubConfigs.getServer(),
                    jSubConfigs.getStream(),
                    jSubConfigs.getSubject(),
                    jSubConfigs.getDurable(),
                    jSubConfigs.getBatchSize(),
                    jSubConfigs.getMaxWait());
        } else if (jSubConfigs.getMode() == JetStreamSubMode.push) {
            jsSub = new JetStreamPushSubscriber(
                    "jetstream-subscriber-push-" + clientNumber,
                    jSubConfigs.getServer(),
                    jSubConfigs.getStream(),
                    jSubConfigs.getSubject(),
                    jSubConfigs.getDurable(),
                    jSubConfigs.getMaxWait(),
                    jSubConfigs.getQueueGroup());
        }
        return jsSub;
    }
}

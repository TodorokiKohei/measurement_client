package measurement.client.kafka;

import java.io.FileNotFoundException;

import measurement.client.Measurement;
import measurement.client.base.MeasurementConfigs;
import measurement.client.base.AbstractDriver;
import measurement.client.base.AbstractPublisher;
import measurement.client.base.AbstractSubscriber;
import measurement.client.base.CommonPubConfigs;
import measurement.client.base.CommonSubConfigs;
import measurement.client.base.Utils;

public class KafkaDriver extends AbstractDriver {
    private static final String resourceName = "/kafkaconf.yaml";
    private MeasurementConfigs<KafkaPubConfigs, KafkaSubConfigs> kafkaConfigs;

    @Override
    public MeasurementConfigs<? extends CommonPubConfigs, ? extends CommonSubConfigs> loadConfigs(String fileName) {
        try {
            kafkaConfigs = Utils.loadConfigsFromYaml(resourceName, fileName, KafkaConfigs.class);
        } catch (FileNotFoundException e) {
            Measurement.logger.warning(e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
        return kafkaConfigs;
    }

    @Override
    public AbstractPublisher createPublisher(int clientNumber, long interval) {
        KafkaPubConfigs kPubConfigs = kafkaConfigs.getPubConf();
        String acks = kPubConfigs.getProperties().getProperty("acks", "0");
        KafkaPublisher kafkaPublisher = new KafkaPublisher(
                "kafka-publisher-" + clientNumber + "-" + acks,
                interval,
                (int) Utils.byteStringToDouble(kPubConfigs.getMessageSize()),
                kPubConfigs.getPubAsync(),
                kPubConfigs.getTopicName(),
                kPubConfigs.getProperties());
        return kafkaPublisher;
    }

    @Override
    public AbstractSubscriber createSubscriber(int clientNumber) {
        KafkaSubConfigs kSubConfigs = kafkaConfigs.getSubConf();
        KafkaSubscriber kafkaSubscriber = new KafkaSubscriber(
                "kafka-subscriber-" + clientNumber,
                kSubConfigs.getTopicName(),
                kSubConfigs.getMaxWait(),
                kSubConfigs.getProperties());
        return kafkaSubscriber;
    }
}

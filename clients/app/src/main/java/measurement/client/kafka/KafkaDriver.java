package measurement.client.kafka;

import java.io.FileNotFoundException;

import measurement.client.Measurement;
import measurement.client.base.CommonConfigs;
import measurement.client.base.AbstractDriver;
import measurement.client.base.Utils;

public class KafkaDriver extends AbstractDriver {
    private static final String resourceName = "/kafkaconf.yaml";
    private KafkaConfigs kafkaConfigs;

    @Override
    public CommonConfigs loadConfigs(String fileName) {
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
    public Boolean setupClients() {
        Boolean isCompleted = true;
        KafkaPubConfigs pubConf = kafkaConfigs.getPubConf();
        if (pubConf != null){
            isCompleted &= createPublisher(pubConf);
        }

        KafkaSubConfigs subConf = kafkaConfigs.getSubConf();
        if (subConf != null){
            isCompleted &= createSubscriber(subConf);
        }
        return isCompleted;
    }

    private Boolean createPublisher(KafkaPubConfigs pubConf){
        Boolean allOk = true;
        long interval = Utils.calcMicroSecInterval(pubConf.getMessageRate(), pubConf.getMessageSize());
        for(int i = 0; i < pubConf.getNumber(); i++){
            KafkaPublisher kafkaPublisher = new KafkaPublisher(
                "kafka-publisher-"+i,
                interval,
                (int) Utils.byteStringToDouble( pubConf.getMessageSize()), 
                pubConf.getTopicName(), 
                pubConf.getProperties());
            publisher.add(kafkaPublisher);
            allOk &= kafkaPublisher.isConnected();
        }
        return allOk;
    }

    private Boolean createSubscriber(KafkaSubConfigs subConf){
        Boolean allOk = true;
        for(int i = 0; i < subConf.getNumber(); i++){
            KafkaSubscriber kafkaSubscriber = new KafkaSubscriber(
                "kafka-subscriber-"+i,
                subConf.getTopicName(), 
                subConf.getMaxWait(),
                subConf.getProperties());
            subscriber.add(kafkaSubscriber);
            allOk &= kafkaSubscriber.isConnected();
        }
        return allOk;
    }
}

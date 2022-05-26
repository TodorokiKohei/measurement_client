package measurement.client.kafka;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.yaml.snakeyaml.Yaml;

import measurement.client.Measurement;
import measurement.client.base.AbstractConfigs;
import measurement.client.base.AbstractDriver;
import measurement.client.base.Utils;

public class KafkaDriver extends AbstractDriver {
    private static final String resourceName = "/kafkaconf.yaml";
    private KafkaConfigs kafkaConfigs;

    @Override
    public AbstractConfigs loadConfigs(String fileName) {
        // yaml形式の設定ファイルを読み込み
        InputStream is = null;
        try {
            if (fileName == null) {
                // 指定がなければクラスパス内のデフォルトファイルを読み込み
                is = AbstractDriver.class.getResourceAsStream(resourceName);
                Measurement.logger.info("Load resource file.(" + resourceName + ")");
                if (is == null)
                    throw new FileNotFoundException();
            } else {
                // 引数のファイルを読み込み
                is = new FileInputStream(fileName);
                Measurement.logger.info("Load argument file.(" + fileName + ")");
            }
        } catch (FileNotFoundException e) {
            if (fileName == null) {
                Measurement.logger.warning(resourceName + " not found.");
            } else {
                Measurement.logger.warning(fileName + " not found.");
            }
            System.exit(1);
        }
        Yaml yaml = new Yaml();
        kafkaConfigs = yaml.loadAs(is, KafkaConfigs.class);
        try {
            if (is != null) {
                is.close();
            }
        } catch (Exception e) {
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

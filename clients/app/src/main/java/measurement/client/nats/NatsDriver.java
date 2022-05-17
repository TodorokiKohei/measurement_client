package measurement.client.nats;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.yaml.snakeyaml.Yaml;

import measurement.client.Driver;
import measurement.client.Measurement;
import measurement.client.Recorder;
import measurement.client.Utils;
import measurement.client.AbstractPublisher;
import measurement.client.AbstractSubscriber;

public class NatsDriver extends Driver {

    private static final String resourceName = "/natsconf.yaml";

    private Recorder recorder;
    private NatsConfigs natsConfigs;
    private List<AbstractPublisher> publisher = new ArrayList<>();
    private List<AbstractSubscriber> subscriber = new ArrayList<>();

    public NatsDriver(String fileName) {
        // yaml形式の設定ファイルを読み込み
        InputStream is = null;
        try {
            if (fileName == null) {
                // 指定がなければクラスパス内のデフォルトファイルを読み込み
                is = Driver.class.getResourceAsStream(resourceName);
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
        natsConfigs = yaml.loadAs(is, NatsConfigs.class);
        try {
            if (is != null) {
                is.close();
            }
        } catch (Exception e) {}

        recorder = new Recorder();
        
    }

    @Override
    public void setupClients() {
        NatsPubConfig npc = natsConfigs.getPubConf();
        if (npc != null) {
            // Publishのメッセージ間隔をμsec単位で計算
            long interval = Utils.calcMicroSecInterval(npc.getMessageRate(), npc.getMessageSize());

            // Publisherの作成
            for (int i = 0; i < natsConfigs.getPubConf().getNumber(); i++) {
                publisher.add(new NatsPublisher(
                        "publisher-" + i,
                        interval,
                        (int)Utils.byteStringToDouble(npc.getMessageSize()),
                        npc.getServer(),
                        npc.getStream(),
                        npc.getSubject()));
            }
        }

        NatsSubConfig nsc = natsConfigs.getSubConf();
        if (nsc != null) {

            // Subscriberの作成
            for (int i = 0; i < natsConfigs.getSubConf().getNumber(); i++) {
                subscriber.add(new NatsSubscriber(
                        "subscriber-" + i,
                        nsc.getServer(),
                        nsc.getStream(),
                        nsc.getSubject(),
                        nsc.getDurable(),
                        nsc.getBatchSize(),
                        nsc.getMaxWait(),
                        nsc.getMode(),
                        nsc.getQueueGroup()));
            }
        }
    }

    @Override
    public void startMeasurement() {
        recorder.start();
        for (AbstractSubscriber sub : subscriber) {
            sub.start();
        }
        for (AbstractPublisher pub : publisher) {
            pub.start();
        }
    }

    @Override
    public void waitForMeasurement() {
        try {
            TimeUnit.SECONDS.sleep(natsConfigs.getExecTime());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stopMeasurement() {
        for (AbstractPublisher pub : publisher) {
            pub.terminate();
        }
        for (AbstractSubscriber sub : subscriber) {
            sub.terminate();
        }
    }

    @Override
    public void treadownClients() {
        for (AbstractPublisher pub : publisher) {
            pub.close();
        }
        for (AbstractSubscriber sub : subscriber) {
            sub.close();
        }
        recorder.terminate();
    }

    @Override
    public void printResult(){
        for (AbstractPublisher pub : publisher) {
            pub.printThrouput();
        }
        for (AbstractSubscriber sub : subscriber) {
            sub.printThrouput();
        }
    }
}

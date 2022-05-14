package measurement.client.nats;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.yaml.snakeyaml.Yaml;

import measurement.client.Driver;
import measurement.client.Measurement;
import measurement.client.Utils;
import measurement.client.AbstractPublisher;
import measurement.client.AbstractSubscriber;

public class NatsDriver extends Driver {

    private NatsConfigs natsConfigs;
    private List<AbstractPublisher> publisher = new ArrayList<>();
    private List<AbstractSubscriber> subscriber = new ArrayList<>();

    public NatsDriver(String fileName) {
        InputStream is = null;
        try {
            if (fileName == null) {
                is = Driver.class.getResourceAsStream("/natsconf.yaml");
                Measurement.logger.info("Use resource");
            } else {
                is = new FileInputStream(fileName);
                Measurement.logger.info("Use argument");
            }
        } catch (FileNotFoundException e) {
            Measurement.logger.warning(fileName + "not found.");
            System.exit(1);
        }
        Yaml yaml = new Yaml();
        natsConfigs = yaml.loadAs(is, NatsConfigs.class);
    }

    @Override
    public void setupClients() {
        NatsPubConfig npc = natsConfigs.getPubConf();
        if (npc != null) {
            // Publishのメッセージ間隔を計算
            long interval = Utils.calcMsInterval(npc.getMessageRate(), npc.getMessageSize());
            for (int i = 0; i < natsConfigs.getPubConf().getNumber(); i++) {
                publisher.add(new NatsPublisher(
                        "publisher-" + i,
                        interval,
                        npc.getServer(),
                        npc.getStream(),
                        npc.getSubject(),
                        Utils.byteStringToDouble(npc.getMessageSize())));
            }
        }

        NatsSubConfig nsc = natsConfigs.getSubConf();
        if (nsc != null) {
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
        for (AbstractPublisher pub : publisher) {
            pub.start();
        }
        for (AbstractSubscriber sub : subscriber) {
            sub.start();
        }
    }

    @Override
    public void waitForMeasurement() {
        try {
            Thread.sleep(5000);
        } catch (Exception e) {
            // TODO: handle exception
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
    }
}

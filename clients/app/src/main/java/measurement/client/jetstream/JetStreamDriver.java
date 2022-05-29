package measurement.client.jetstream;

import java.io.FileNotFoundException;

import measurement.client.Measurement;
import measurement.client.base.CommonConfigs;
import measurement.client.base.AbstractDriver;
import measurement.client.base.Utils;

public class JetStreamDriver extends AbstractDriver {

    private static final String resourceName = "/jetstreamconf.yaml";

    private JetStreamConfigs jetStreamConfigs;

    public CommonConfigs loadConfigs(String fileName) {
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
    public Boolean setupClients() {
        Boolean isCompleted = true;

        JetStreamPubConfigs jspc = jetStreamConfigs.getPubConf();
        if (jspc != null) {
            isCompleted &= createPublisher(jspc);
        }

        JetStreamSubConfigs jssc = jetStreamConfigs.getSubConf();
        if (jssc != null) {
            isCompleted &= createSubscriber(jssc);
        }
        return isCompleted;
    }

    private boolean createPublisher(JetStreamPubConfigs jspc) {
        // Publishのメッセージ間隔をμsec単位で計算
        long interval = Utils.calcMicroSecInterval(jspc.getMessageRate(), jspc.getMessageSize());

        // Publisherの作成
        Boolean allOk = true;
        for (int i = 0; i < jetStreamConfigs.getPubConf().getNumber(); i++) {
            JetStreamPublisher jsPub = new JetStreamPublisher(
                    "jetstream-publisher-" + i,
                    interval,
                    (int) Utils.byteStringToDouble(jspc.getMessageSize()),
                    jspc.getServer(),
                    jspc.getStream(),
                    jspc.getSubject());

            publisher.add(jsPub);
            allOk &= jsPub.isConnected();
        }
        return allOk;
    }

    private boolean createSubscriber(JetStreamSubConfigs jssc) {

        if (jssc.getMode() == JetStreamSubMode.pull)
            Measurement.logger.info("Create subscriber in mode of pull");
        else if (jssc.getMode() == JetStreamSubMode.push)
            Measurement.logger.info("Create subscriber in mode of push");

        // Subscriberの作成
        Boolean allOk = true;
        for (int i = 0; i < jetStreamConfigs.getSubConf().getNumber(); i++) {
            if (jssc.getMode() == JetStreamSubMode.pull) {
                JetStreamPullSubscriber jsPullSub = new JetStreamPullSubscriber(
                    "subscriber-pull-" + i,
                    jssc.getServer(),
                    jssc.getStream(),
                    jssc.getSubject(),
                    jssc.getDurable(),
                    jssc.getBatchSize(),
                    jssc.getMaxWait());
                subscriber.add(jsPullSub);
                allOk &= jsPullSub.isConnected();
            } else if (jssc.getMode() == JetStreamSubMode.push) {
                JetStreamPushSubscriber jsPushSub = new JetStreamPushSubscriber(
                    "subscriber-push-" + i,
                    jssc.getServer(),
                    jssc.getStream(),
                    jssc.getSubject(),
                    jssc.getDurable(),
                    jssc.getMaxWait(),
                    jssc.getQueueGroup());
                subscriber.add(jsPushSub);
                allOk &= jsPushSub.isConnected();
            }
        }
        return allOk;
    }
}

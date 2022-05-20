package measurement.client.jetstream;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.yaml.snakeyaml.Yaml;

import measurement.client.Measurement;
import measurement.client.base.AbstractConfigs;
import measurement.client.base.AbstractDriver;
import measurement.client.base.Utils;

public class JetStreamDriver extends AbstractDriver {

    private static final String resourceName = "/jetstreamconf.yaml";

    private JetStreamConfigs jetStreamConfigs;

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
        jetStreamConfigs = yaml.loadAs(is, JetStreamConfigs.class);
        try {
            if (is != null) {
                is.close();
            }
        } catch (Exception e) {
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
                    "publisher-" + i,
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

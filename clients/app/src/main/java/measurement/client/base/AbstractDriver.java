package measurement.client.base;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import measurement.client.Measurement;

public abstract class AbstractDriver {
    private MeasurementConfigs<? extends CommonPubConfigs, ? extends CommonSubConfigs> commonConfigs;
    private Recorder recorder;
    protected List<AbstractPublisher> publisher = new ArrayList<>();
    protected List<AbstractSubscriber> subscriber = new ArrayList<>();

    public void setCommonConfigs(MeasurementConfigs<? extends CommonPubConfigs, ? extends CommonSubConfigs> configs) {
        this.commonConfigs = configs;
    }

    public abstract MeasurementConfigs<? extends CommonPubConfigs, ? extends CommonSubConfigs> loadConfigs(
            String fileName);

    public abstract AbstractPublisher createPublisher(int clientNumber, long interval);

    public abstract AbstractSubscriber createSubscriber(int clientNumber);

    // public abstract Boolean setupClients();
    public Boolean setupClients() {
        Boolean isCompleted = true;

        CommonPubConfigs cPubConfigs = commonConfigs.getPubConf();
        if (cPubConfigs != null) {
            long interval = Utils.calcMicroSecInterval(cPubConfigs.getMessageRate(), cPubConfigs.getMessageSize());
            for (int i = 0; i < cPubConfigs.getNumber(); i++) {
                AbstractPublisher pub = createPublisher(i, interval);
                isCompleted &= pub.isConnected();
                publisher.add(pub);
            }
        }

        CommonSubConfigs cSubConfigs = commonConfigs.getSubConf();
        if (cSubConfigs != null) {
            for (int i = 0; i < cSubConfigs.getNumber(); i++) {
                AbstractSubscriber sub = createSubscriber(i);
                isCompleted &= sub.isConnected();
                subscriber.add(sub);
            }
        }
        return isCompleted;
    }

    public void setupRecoder(String outputDir) {
        if (commonConfigs.getSubConf() == null || !commonConfigs.getSubConf().getRecordMessage())
            return;
        this.recorder = new Recorder(outputDir);
        for (AbstractClient client : subscriber) {
            client.setRecorder(recorder);
            recorder.createOutputFile(client.getClientId());
        }
    }

    public void startMeasurement() {
        if (recorder != null)
            recorder.start();
        for (AbstractSubscriber sub : subscriber) {
            sub.start();
        }
        try {
            Measurement.logger
                    .info("Wait " + commonConfigs.getSubscriberFallTime() + " seconds before publisher start.");
            TimeUnit.SECONDS.sleep(commonConfigs.getPublisherRiseTime());
        } catch (Exception e) {
        }
        for (AbstractPublisher pub : publisher) {
            pub.start();
        }
    }

    public void waitForMeasurement() {
        try {
            long sleepTime = commonConfigs.getExecTime() - commonConfigs.getPublisherRiseTime()
                    - commonConfigs.getSubscriberFallTime();
            Measurement.logger.info("Wait " + sleepTime + " seconds for measurement.");
            TimeUnit.SECONDS.sleep(sleepTime);
        } catch (Exception e) {
        }
    }

    public void stopMeasurement() {
        for (AbstractPublisher pub : publisher) {
            pub.terminate();
        }
        try {
            Measurement.logger
                    .info("Wait " + commonConfigs.getSubscriberFallTime() + " seconds before subscrber terminate.");
            TimeUnit.SECONDS.sleep(commonConfigs.getSubscriberFallTime());
        } catch (Exception e) {
        }
        for (AbstractSubscriber sub : subscriber) {
            sub.terminate();
        }
        if (recorder != null)
            recorder.terminate();
    }

    public void treadownClients() {
        for (AbstractPublisher pub : publisher) {
            pub.close();
        }
        for (AbstractSubscriber sub : subscriber) {
            sub.close();
        }
        if (recorder != null)
            recorder.close();
    }

    public void recordResults(String outputDir) {
        for (AbstractPublisher pub : publisher) {
            pub.recordThrouput(outputDir);
        }
        for (AbstractSubscriber sub : subscriber) {
            sub.recordThrouput(outputDir);
        }
    }
}

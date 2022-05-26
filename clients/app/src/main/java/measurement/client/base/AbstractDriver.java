package measurement.client.base;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import measurement.client.Measurement;

public abstract class AbstractDriver {
    private AbstractConfigs configs;
    private Recorder recorder;
    protected List<AbstractPublisher> publisher = new ArrayList<>();
    protected List<AbstractSubscriber> subscriber = new ArrayList<>();

    public void setCommonConfigs(AbstractConfigs configs) {
        this.configs = configs;
    }

    public abstract AbstractConfigs loadConfigs(String fileName);

    public abstract Boolean setupClients();

    public void setupRecoder(String outputDir) {
        if (!configs.getRecordMessage())
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
            Measurement.logger.info("Wait " + configs.getSubscriberFallTime() + " seconds before publisher start.");
            TimeUnit.SECONDS.sleep(configs.getPublisherRiseTime());
        } catch (Exception e) {
        }
        for (AbstractPublisher pub : publisher) {
            pub.start();
        }
    }

    public void waitForMeasurement() {
        try {
            long sleepTime = configs.getExecTime() - configs.getPublisherRiseTime() - configs.getSubscriberFallTime();
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
            Measurement.logger.info("Wait " + configs.getSubscriberFallTime() + " seconds before subscrber terminate.");
            TimeUnit.SECONDS.sleep(configs.getSubscriberFallTime());
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

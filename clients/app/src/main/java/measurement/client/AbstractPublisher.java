package measurement.client;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public abstract class AbstractPublisher extends AbstractClient implements Runnable {
    protected long interval;
    protected volatile boolean isTerminated;

    protected ScheduledExecutorService service;
    protected ScheduledFuture<?> future;

    public AbstractPublisher(String clientId, long interval) {
        super(clientId);
        this.interval = interval;
        this.isTerminated = false;
    }

    // スレッド起動処理
    public void start() {
        service = Executors.newSingleThreadScheduledExecutor();
        if (interval == 0){
            future = service.schedule(this, 0, TimeUnit.MILLISECONDS);
        }else{
            future = service.scheduleAtFixedRate(this, 0, interval, TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public void run() {
        if (interval == 0) {
            Measurement.logger.info("Start continuing publish.");
            continuingPublish();
        } else {
            Measurement.logger.info("Start interval publish.");
            intervalPublish();
        }
    }

    // スレッドはループしてPublishを続ける
    public void continuingPublish() {
        int count = 0;
        while (!isTerminated) {
            publish();
            count++;
            if (count == 10)
                break;
            try {
                Thread.sleep(100);
            } catch (Exception e) {

            }
        }
    }

    // スレッドは1回Publishして終了する
    public void intervalPublish() {
        Measurement.logger.info("publish");
        if (!isTerminated) {
            publish();
        }
    }

    public void terminate() {
        if (future.isDone()) {
            isTerminated = true;
            future.cancel(false);
        }
        service.shutdown();
        try {
            if (service.awaitTermination(5, TimeUnit.SECONDS)) {
                Measurement.logger.finer("Thread closed successfully.");
            }
        } catch (InterruptedException e) {
            Measurement.logger.warning("Thread could not be closed.");
            e.printStackTrace();
        }
    }

    public abstract void publish();
}

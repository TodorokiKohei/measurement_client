package measurement.client;

import measurement.client.Measurement;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public abstract class AbstractPublisher extends AbstractClient implements Runnable{
    private double interval;
    private volatile boolean isTerminated;

    protected ScheduledExecutorService service;
    protected ScheduledFuture<?> future;

    public AbstractPublisher(String clientId, double interval){
        super(clientId);
        this.interval = interval;
    }

    // スレッド起動処理
    public void start(){
        service = Executors.newSingleThreadScheduledExecutor();
        future = service.schedule(this, 0, TimeUnit.SECONDS);
    }

    @Override
    public void run(){
        if (interval == 0){
            continuingPublish();
        }else{
            intervalPublish();
        }
    }

    // スレッドはループしてPublishを続ける
    public void continuingPublish(){
        int count = 0;
        while(!isTerminated){
            publish();
            count++;
            if (count == 10) break;
            Measurement.logger.info("Publish " + count);
            try {
                Thread.sleep(100);
            } catch (Exception e) {
                
            }
        }
        Measurement.logger.info("Contiuing publish comlited");
    }

    // スレッドは1回Publishして終了する
    public void intervalPublish(){
        publish();
        Measurement.logger.info("Interval publish comlited");
    }

    public void terminate(){
        if (future.isDone()){
            isTerminated = true;
            future.cancel(false);
        }
        service.shutdown();
        try {
            if (service.awaitTermination(5, TimeUnit.SECONDS)){
                Measurement.logger.info("Thread closed successfully.");
            }
        } catch (InterruptedException e) {
            Measurement.logger.warning("Thread could not be closed.");
            e.printStackTrace();
        }
    }

    public abstract void publish();
}

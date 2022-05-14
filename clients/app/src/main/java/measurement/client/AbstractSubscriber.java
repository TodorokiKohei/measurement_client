package measurement.client;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public abstract class AbstractSubscriber extends AbstractClient implements Runnable{
    protected volatile boolean isTerminated;

    protected ScheduledExecutorService service;
    protected ScheduledFuture<?> future;

    public AbstractSubscriber(String clientId) {
        super(clientId);
        this.isTerminated = false;
    }

    public void start() {
        // 別スレッドでSubscribe処理を開始
        service = Executors.newSingleThreadScheduledExecutor();
        future = service.schedule(this, 0, TimeUnit.MILLISECONDS);
    }

    @Override
    public void run(){
        // terminateが呼ばれるまでSubscribe処理
        Measurement.logger.info("Start subscribe.");
        while(!isTerminated){
            subscribe();
        }
    }

    public void terminate() {
        // 別スレッドのタスクを終了させる
        if (future.isDone()) {
            isTerminated = true;
            future.cancel(false);
        }
        service.shutdown();

        // スレッドが終了するまで一定時間待機する
        try {
            if (service.awaitTermination(5, TimeUnit.SECONDS)) {
                Measurement.logger.finer("Thread closed successfully.");
            }
        } catch (InterruptedException e) {
            Measurement.logger.warning("Thread could not be closed.");
            e.printStackTrace();
        }
    }
    public abstract void subscribe();
}

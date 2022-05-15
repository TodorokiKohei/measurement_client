package measurement.client;

import java.util.List;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public abstract class AbstractSubscriber extends AbstractClient implements Runnable{
    protected Map<Long, Long> throuputMap;
    protected volatile boolean isTerminated;

    protected ScheduledExecutorService service;
    protected ScheduledFuture<?> future;

    public AbstractSubscriber(String clientId) {
        super(clientId);
        this.throuputMap = new TreeMap<Long, Long>();
        this.isTerminated = false;
    }

    public void start() {
        // 別スレッドでSubscribe処理を開始
        service = Executors.newSingleThreadScheduledExecutor();
        future = service.schedule(this, 0, TimeUnit.MILLISECONDS);
    }

    private void recordThrouput(List<Payload> payloads){
        for(Payload payload: payloads){
            throuputMap.merge(payload.receivedTime, 1L, Long::sum);
        }
    }

    @Override
    public void run(){
        // terminateが呼ばれるまでSubscribe処理
        Measurement.logger.info("Start subscribe.");
        while(!isTerminated){
            List<Payload> payloads= subscribe();
            recordThrouput(payloads);
        }
    }

    public void terminate() {
        // 別スレッドのタスクを終了させる
        if (!future.isDone()) {
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

    public void printThrouput(){
        Measurement.logger.info(clientId + "'s throuput results.");
        Iterator<Map.Entry<Long, Long>> itr = throuputMap.entrySet().iterator();
        while(itr.hasNext()){
            Map.Entry<Long, Long> entry = itr.next();
            Measurement.logger.info("(" + clientId + ") " + entry.getKey() + ":" + entry.getValue());
        }
    }

    public abstract List<Payload> subscribe();
}
